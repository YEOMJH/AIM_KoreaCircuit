package kr.co.aim.messolution.lot.event.CNX;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.LOIPanelCodeList;
import kr.co.aim.messolution.extended.object.management.data.ReserveRepairPanelInfo;
import kr.co.aim.messolution.extended.object.management.data.ReviewComponentHistory;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.info.ChangeGradeInfo;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

import com.sun.org.apache.bcel.internal.generic.NEW;
import com.sun.org.apache.xpath.internal.operations.Bool;
import com.sun.xml.internal.bind.v2.model.core.BuiltinLeafInfo;

public class ReviewProductJudge extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String FactoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String ProductName = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", true);
		String ProcessOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String RSOperation = SMessageUtil.getBodyItemValue(doc, "RSPROCESSOPERATIONNAME", false);
		String MachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String UnitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String ProductJudge = SMessageUtil.getBodyItemValue(doc, "PRODUCTJUDGE", false);
		String GlassAJudge = SMessageUtil.getBodyItemValue(doc, "GLASSAJUDGE", false);
		String GlassBJudge = SMessageUtil.getBodyItemValue(doc, "GLASSBJUDGE", false);
		String SubProductGrade = SMessageUtil.getBodyItemValue(doc, "SUBPRODUCTGRADE", false);
		String SubProductGrades2 = SMessageUtil.getBodyItemValue(doc, "SUBPRODUCTGRADES2", false);
		String ActionType = SMessageUtil.getBodyItemValue(doc, "ACTIONTYPE", true);
		String detailGrade = SMessageUtil.getBodyItemValue(doc, "DETAILGRADE", false);
		String glassADetailGrade = SMessageUtil.getBodyItemValue(doc, "GLASSADETAILGRADE", false);
		String glassBDetailGrade = SMessageUtil.getBodyItemValue(doc, "GLASSBDETAILGRADE", false);
		String checkPEP0 = SMessageUtil.getBodyItemValue(doc, "CHECKPEP0", false);

		List<Element> PanelList = SMessageUtil.getBodySequenceItemList(doc, "PANELJUDGELIST", false);
		List<Element> LOIDefectList = SMessageUtil.getBodySequenceItemList(doc, "LOIDEFECTLIST", false);
		List<Element> reserveRepairList = SMessageUtil.getBodySequenceItemList(doc, "RESERVEREPAIRLIST", false);
		List<Element> adcDefectList = SMessageUtil.getBodySequenceItemList(doc, "ADCDEFECTLIST", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo(ActionType, getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		boolean onlineRS=false;
		Product productInfo = MESProductServiceProxy.getProductServiceUtil().getProductData(ProductName);
		String currentFlow=productInfo.getProcessFlowName();
		String productSpecName=productInfo.getProductSpecName();
		
		String aoiFlow="";
		String currectOper=productInfo.getProcessOperationName();
		ProcessOperationSpec operationSpecData = CommonUtil.getProcessOperationSpec(FactoryName,currectOper ,"00001");
		
		StringBuffer sql=new StringBuffer();
		sql.append(" SELECT A.PROCESSFLOWNAME  ");
		sql.append("   FROM TPFOPOLICY A, POSMACHINE B , PROCESSFLOW C ");
		sql.append("  WHERE     A.CONDITIONID = B.CONDITIONID ");
		sql.append("        AND A.FACTORYNAME = :FACTORYNAME ");
		sql.append("        AND A.PRODUCTSPECNAME = :PRODUCTSPECNAME ");
		sql.append("        AND A.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
		sql.append("        AND A.PROCESSFLOWNAME = C.PROCESSFLOWNAME ");
		sql.append("        AND A.FACTORYNAME = C.FACTORYNAME ");
		sql.append("        AND (C.PROCESSFLOWTYPE='Inspection' OR C.PROCESSFLOWTYPE='Sample') ");
		
		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("FACTORYNAME", FactoryName);
		bindMap.put("PRODUCTSPECNAME", productSpecName);
		bindMap.put("PROCESSOPERATIONNAME", ProcessOperationName);
		List<Map<String, Object>> aoiFlowSelect = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
		if(aoiFlowSelect!=null && aoiFlowSelect.size()>0)
		{
			aoiFlow=aoiFlowSelect.get(0).get("PROCESSFLOWNAME").toString();
		}
		if(StringUtils.equals(currectOper, "21200"))
		{
			onlineRS=true;
		}
		else if(StringUtils.equals(currentFlow, aoiFlow)&&
				(StringUtils.equals(operationSpecData.getDetailProcessOperationType(), "AOI")
				||StringUtils.equals(operationSpecData.getDetailProcessOperationType(), "LOI")
				||StringUtils.equals(operationSpecData.getDetailProcessOperationType(), "Auto Macro")
				||StringUtils.equals(operationSpecData.getDetailProcessOperationType(), "VIEW")))
		{
			onlineRS=true;
		}
		

		if (ActionType.equals("AssignUser"))
		{
			int qty = getReviewProductJudge(ProductName, ProcessOperationName, MachineName);

			String ActionKind = "";

			if (qty == 0)
				ActionKind = "Insert";
			else
				ActionKind = "Update";

			MESLotServiceProxy.getLotServiceImpl().insertCtReviewProductJudge(eventInfo, ProductName, ProcessOperationName, MachineName, ProductJudge, ActionKind,false);
			
			// Insert LotHis RSIn
			String sqlForRSIn= " SELECT P.PRODUCTNAME FROM PRODUCT P ,CT_REVIEWPRODUCTJUDGE R "
					+ " WHERE P.LOTNAME=:LOTNAME "
					+ " AND P.PRODUCTNAME = R.PRODUCTNAME  "
					+ " AND R.PROCESSOPERATIONNAME=:PROCESSOPERATIONNAME ";
				
			Map<String, Object> bindForRSIn = new HashMap<String, Object>();
			bindForRSIn.put("LOTNAME",productInfo.getLotName());
			bindForRSIn.put("PROCESSOPERATIONNAME",ProcessOperationName);
			
			List<Map<String, Object>> rsInData = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlForRSIn, bindForRSIn);
			if(rsInData!=null &&rsInData.size()==1)
			{
				Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(productInfo.getLotName());
				EventInfo eventInfoForLot = EventInfoUtil.makeEventInfo("ReviewStationIn", this.getEventUser(), this.getEventComment()+" ReviewStationIn "+RSOperation, "", "");
				LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfoForLot, new SetEventInfo());
			}

			try
			{
				List<ReviewComponentHistory> reviewComponentHisList=ExtendedObjectProxy.getReviewComponentHistoryService().select(
						" PRODUCTNAME=? AND PROCESSOPERATIONNAME=? AND MATERIALLOCATIONNAME=? ", new Object[]{ProductName,ProcessOperationName,UnitName  });
				if(reviewComponentHisList!=null &&reviewComponentHisList.size()>0)
				{
					for(ReviewComponentHistory reviewComponentHisInfo:reviewComponentHisList)
					{
						reviewComponentHisInfo.setAssignTime(eventInfo.getEventTime());
						reviewComponentHisInfo.setAssignUser(eventInfo.getEventUser());	
					}
					ExtendedObjectProxy.getReviewComponentHistoryService().update(reviewComponentHisList);
				}
			}
			catch(greenFrameDBErrorSignal n)
			{
				
			}			
		}
		else if(ActionType.equals("AssignDMUser"))
		{
			int qty = getReviewProductJudge(ProductName, ProcessOperationName, MachineName);

			String ActionKind = "";

			if (qty == 0)
				ActionKind = "Insert";
			else
				ActionKind = "Update";

			MESLotServiceProxy.getLotServiceImpl().insertCtReviewProductJudge(eventInfo, ProductName, ProcessOperationName, MachineName, ProductJudge, ActionKind,true);		
		}
		else
		{
			String subProductJudge = "";

            if(onlineRS)
            {            	
    			for (Element code : PanelList)
    			{
    				String PanelName = code.getChild("PANELNAME").getText();
    				String PanelJudge = code.getChild("PANELJUDGE").getText();
    				String PanelGrade = code.getChild("PANELGRADE").getText();
    				Map<String, String> scrapMapInfo = insertCt_PanelJudge(eventInfo, PanelName, PanelJudge, PanelGrade, ProcessOperationName, FactoryName);
    				insertCtReviewPanelJudgeNew(eventInfo, ProductName, ProcessOperationName, MachineName, PanelName, PanelJudge, PanelGrade, scrapMapInfo);
    			}
            }	   	

			if (ActionType.equals("SaveSheetJudge")&&onlineRS)
			{
				subProductJudge = SubProductGrade.substring(0, SubProductGrade.length() / 2);

				List<Map<String, Object>> result = checkGlassExist(FactoryName, ProductName);

				if (FactoryName.equals("ARRAY"))
				{
					if (result.size() > 0)
					{
						UpdateGlassJudge(ProductName + "1", GlassAJudge, ProductName, "ReviewStationProductJudge", eventInfo.getEventUser(), eventInfo.getEventComment(), ProcessOperationName,
								eventInfo.getEventTime(), subProductJudge, glassADetailGrade);
					}
					else
					{
						InsertGlassJudge(ProductName + "1", GlassAJudge, ProductName, "", "", "ReviewStationProductJudge", eventInfo.getEventUser(), eventInfo.getEventComment(), ProcessOperationName,
								eventInfo.getEventTime(), subProductJudge, glassADetailGrade);
					}
				}
				else
				{
					if (result.size() > 0)
					{
						UpdateGlassJudge(ProductName, GlassAJudge, ProductName, "ReviewStationProductJudge", eventInfo.getEventUser(), eventInfo.getEventComment(), ProcessOperationName,
								eventInfo.getEventTime(), subProductJudge, glassADetailGrade);
					}
					else
					{
						InsertGlassJudge(ProductName, GlassAJudge, ProductName, "", "", "ReviewStationProductJudge", eventInfo.getEventUser(), eventInfo.getEventComment(), ProcessOperationName,
								eventInfo.getEventTime(), subProductJudge, glassADetailGrade);
					}
				}

				if (FactoryName.equals("ARRAY"))
				{
					subProductJudge = SubProductGrade.substring(SubProductGrade.length() / 2, SubProductGrade.length());

					result = checkGlassExist(ProductName);

					if (result.size() > 0)
					{
						UpdateGlassJudge(ProductName + "2", GlassBJudge, ProductName, "ReviewStationProductJudge", eventInfo.getEventUser(), eventInfo.getEventComment(), ProcessOperationName,
								eventInfo.getEventTime(), subProductJudge, glassBDetailGrade);
					}
					else
					{
						InsertGlassJudge(ProductName + "2", GlassBJudge, ProductName, "", "", "ReviewStationProductJudge", eventInfo.getEventUser(), eventInfo.getEventComment(), ProcessOperationName,
								eventInfo.getEventTime(), subProductJudge, glassBDetailGrade);
					}

					MESProductServiceProxy.getProductServiceUtil().changeProductGrade(eventInfo, ProductName, ProductJudge,
							SubProductGrade, SubProductGrades2, detailGrade);
				}				

			}
			else if (ActionType.equals("SaveGlassJudge")&&onlineRS)
			{
				List<Map<String, Object>> result = checkGlassExist(FactoryName, ProductName);

				if (FactoryName.equals("ARRAY"))
				{
					if (result.size() > 0)
					{
						UpdateGlassJudge(ProductName + "1", GlassAJudge, ProductName, "ReviewStationProductJudge", eventInfo.getEventUser(), eventInfo.getEventComment(), ProcessOperationName,
								eventInfo.getEventTime(), SubProductGrade, glassADetailGrade);
					}
					else
					{
						InsertGlassJudge(ProductName + "1", GlassAJudge, ProductName, "", "", "ReviewStationProductJudge", eventInfo.getEventUser(), eventInfo.getEventComment(), ProcessOperationName,
								eventInfo.getEventTime(), SubProductGrade, glassADetailGrade);
					}
				}
				else
				{
					if (result.size() > 0)
					{
						UpdateGlassJudge(ProductName, GlassAJudge, ProductName.substring(0, ProductName.length() - 1), "ReviewStationProductJudge", eventInfo.getEventUser(),
								eventInfo.getEventComment(), ProcessOperationName, eventInfo.getEventTime(), SubProductGrade, glassADetailGrade);
					}
					else
					{
						InsertGlassJudge(ProductName, GlassAJudge, ProductName.substring(0, ProductName.length() - 1), "", "", "ReviewStationProductJudge", eventInfo.getEventUser(),
								eventInfo.getEventComment(), ProcessOperationName, eventInfo.getEventTime(), SubProductGrade, glassADetailGrade);
					}
				}

				MESProductServiceProxy.getProductServiceUtil().changeProductGrade(eventInfo, ProductName, ProductJudge,
						SubProductGrade, SubProductGrades2, detailGrade);
			}
			else if (ActionType.equals("ClearAssignUser")||ActionType.equals("ClearDMAssignUser"))
			{
				eventInfo.setEventUser("");
			}

			MESLotServiceProxy.getLotServiceImpl().insertCtReviewProductJudge(eventInfo, ProductName, ProcessOperationName, MachineName, ProductJudge, "Update",false);
			// Insert LotHis RSOut
			
			if(ActionType.equals("SaveGlassJudge")||ActionType.equals("SaveSheetJudge")||ActionType.equals("SaveSheetJudgeForAT"))
			{
				try
				{
					List<ReviewComponentHistory> reviewComponentHisList=ExtendedObjectProxy.getReviewComponentHistoryService().select(
							" PRODUCTNAME=? AND PROCESSOPERATIONNAME=? AND MATERIALLOCATIONNAME=? ", new Object[]{ProductName,ProcessOperationName,UnitName});
					if(reviewComponentHisList!=null &&reviewComponentHisList.size()>0)
					{
						ExtendedObjectProxy.getReviewComponentHistoryService().delete(reviewComponentHisList);
					}
					if(!StringUtils.contains(UnitName, "FAT"))
					{
						String sqlForRSOut= " SELECT P.PRODUCTNAME FROM CT_REVIEWCOMPONENTHISTORY R,PRODUCT P "
								+ " WHERE P.PRODUCTNAME = R.PRODUCTNAME "
								+ " AND P.LOTNAME=:LOTNAME  "
								+ " AND R.PROCESSOPERATIONNAME=:PROCESSOPERATIONNAME  ";
							
						Map<String, Object> bindForRSOut = new HashMap<String, Object>();
						bindForRSOut.put("LOTNAME",productInfo.getLotName());
						bindForRSOut.put("PROCESSOPERATIONNAME",ProcessOperationName);
						
						List<Map<String, Object>> rsOutData = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlForRSOut, bindForRSOut);
						if(rsOutData!=null&&rsOutData.size()==0&&!StringUtils.equals(productInfo.getProcessOperationName(), ProcessOperationName))
						{
							Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(productInfo.getLotName());
							EventInfo eventInfoForLot = EventInfoUtil.makeEventInfo("ReviewStationOut", this.getEventUser(), this.getEventComment()+" ReviewStationOut "+RSOperation, "", "");
							LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfoForLot, new SetEventInfo());
						}
					}				
				}
				catch(greenFrameDBErrorSignal n)
				{
					
				}
			}
		}
		
		if(LOIDefectList != null && LOIDefectList.size() > 0)
		{
			
			List<LOIPanelCodeList> dataList = new ArrayList<LOIPanelCodeList>();
			for (Element elementLOI : LOIDefectList) 
			{
				String panelName = elementLOI.getChild("PANELNAME").getText();
				String defectType = elementLOI.getChild("DEFECTTYPE").getText();
				String defectCode = elementLOI.getChild("DEFECTCODE").getText();
				String area = elementLOI.getChild("AREA").getText();
				int quantity = Integer.parseInt(elementLOI.getChild("QUANTITY").getText());
				String glassName = elementLOI.getChild("GLASSNAME").getText();
				String lotName = elementLOI.getChild("LOTNAME").getText();
				
				LOIPanelCodeList LOIData = new LOIPanelCodeList();
				LOIData.setPanelName(panelName);
				LOIData.setCodeLevel1(defectType);
				LOIData.setCodeLevel2(defectCode);
				LOIData.setArea(area);
				LOIData.setQuantity(quantity);
				LOIData.setGlassName(glassName);
				LOIData.setLotName(lotName);
				LOIData.setEventComment(eventInfo.getEventComment());
				LOIData.setEventUser(eventInfo.getEventUser());
				LOIData.setEventTime(eventInfo.getEventTime());
				LOIData.setEventName(eventInfo.getEventName());
				LOIData.setTimeKey(eventInfo.getEventTimeKey());
				
				dataList.add(LOIData);
			}
			ExtendedObjectProxy.getLOIPanelCodeListService().create(eventInfo, dataList);
		}
		
		if(reserveRepairList != null && reserveRepairList.size() > 0)
		{
			//delete reserveRepairInfo case of reJudge
			try
			{
				List<ReserveRepairPanelInfo> existPanelList=ExtendedObjectProxy.getReserveRepairPanelInfoService().select(" SHEET_ID=? AND STEP_ID=? " , new Object[]{ProductName,RSOperation});
				if(existPanelList.size()>0)
				{
					ExtendedObjectProxy.getReserveRepairPanelInfoService().delete(existPanelList);
				}
			}
			catch(greenFrameDBErrorSignal x)
			{
			}
			
			ProductSpec productSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(FactoryName, productSpecName, GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);
			int productCountToX=Integer.parseInt(productSpecData.getUdfs().get("PRODUCTCOUNTTOXAXIS"));
			int halfPanelCount = Integer.parseInt(productSpecData.getUdfs().get("PRODUCTCOUNTTOXAXIS")) * Integer.parseInt(productSpecData.getUdfs().get("PRODUCTCOUNTTOYAXIS"))/2; 

		    //Insert
			List<ReserveRepairPanelInfo> dataList = new ArrayList<ReserveRepairPanelInfo>();
			for (Element elementReserve : reserveRepairList) 
			{
				String image_Data = elementReserve.getChild("IMAGE_DATA").getText();
				String panel_ID = elementReserve.getChild("PANEL_ID").getText();
				String processFlowName = elementReserve.getChild("PROCESSFLOWNAME").getText();
				String step_ID = elementReserve.getChild("STEP_ID").getText();
				String sheet_ID = elementReserve.getChild("SHEET_ID").getText();
				String sheet_Start_Time = elementReserve.getChild("SHEET_START_TIME").getText();
				String glass_Start_Time = elementReserve.getChild("GLASS_START_TIME").getText();
				String panel_Start_Time = elementReserve.getChild("PANEL_START_TIME").getText();
				String glass_ID = elementReserve.getChild("GLASS_ID").getText();
				String panel_No = elementReserve.getChild("PANEL_NO").getText();
				String defect_No = elementReserve.getChild("DEFECT_NO").getText();
				String defect_Code = elementReserve.getChild("DEFECT_CODE").getText();
				String defect_Pattern = elementReserve.getChild("DEFECT_PATTERN").getText();
				String defect_Size_Type = elementReserve.getChild("DEFECT_SIZE_TYPE").getText();
				String defect_Judge = elementReserve.getChild("DEFECT_JUDGE").getText();
				String glass_X = elementReserve.getChild("GLASS_X").getText();
				String glass_Y = elementReserve.getChild("GLASS_Y").getText();
				String glass_X2 = elementReserve.getChild("GLASS_X2").getText();
				String glass_Y2 = elementReserve.getChild("GLASS_Y2").getText();
				String sheet_X = elementReserve.getChild("SHEET_X").getText();
				String sheet_Y = elementReserve.getChild("SHEET_Y").getText();
				String sheet_X2 = elementReserve.getChild("SHEET_X2").getText();
				String sheet_Y2 = elementReserve.getChild("SHEET_Y2").getText();
				String panel_X = elementReserve.getChild("PANEL_X").getText();
				String panel_Y = elementReserve.getChild("PANEL_Y").getText();
				String panel_X2 = elementReserve.getChild("PANEL_X2").getText();
				String panel_Y2 = elementReserve.getChild("PANEL_Y2").getText();
				String rs_Judge = elementReserve.getChild("RS_JUDGE").getText();
				String rs_Code = elementReserve.getChild("RS_CODE").getText();
				String rs_Defect_Image_Name = elementReserve.getChild("RS_DEFECT_IMAGE_NAME").getText();
				String repairOperation = elementReserve.getChild("REPAIROPERATIONNAME").getText();
				int panelNum=0;
				
				if(!StringUtils.equals(checkPEP0, "True"))
				{
					if(panel_ID.length()<15||panel_No.length()<3)
					{
						panelNum=-1;
					}
					else
					{
						if(StringUtils.equals(panel_ID.substring(11,12), "1"))
						{
							panelNum+=0;
						}
						else if(StringUtils.equals(panel_ID.substring(11,12), "2"))
						{
							panelNum+=halfPanelCount;
						}
						
						if(panel_No.substring(1,2).toCharArray()[0]<73&&panel_No.substring(1,2).toCharArray()[0]>64)
						{
							panelNum+=panel_No.substring(1,2).toCharArray()[0]-64;
						}
						else if (panel_No.substring(1,2).toCharArray()[0]>73&&panel_No.substring(1,2).toCharArray()[0]<79)
						{
							panelNum+=panel_No.substring(1,2).toCharArray()[0]-65;
						}
						else if(panel_No.substring(1,2).toCharArray()[0]>79&&panel_No.substring(1,2).toCharArray()[0]<=90)
						{
							panelNum+=panel_No.substring(1,2).toCharArray()[0]-66;					
						}	
						
						panelNum+=(panel_No.substring(0,1).toCharArray()[0]-64-1)*productCountToX;
						
						if(panelNum>halfPanelCount*2)
						{
							panelNum=-1;
						}

						
						if((panel_No.substring(1,2).toCharArray()[0]<65)||panel_ID.length()!=15)
						{
							panelNum=-1;
						}
					}
				}
				else 
				{
					//PEP0 Case
					panelNum=-5;
				}
								
				ReserveRepairPanelInfo reserveRepairPanelInfo = new ReserveRepairPanelInfo();
				reserveRepairPanelInfo.setImage_Data(image_Data);
				reserveRepairPanelInfo.setPanel_ID(panel_ID);
				reserveRepairPanelInfo.setTimekey(TimeStampUtil.getCurrentEventTimeKey());
				reserveRepairPanelInfo.setProcessFlowName(processFlowName);
				reserveRepairPanelInfo.setStep_ID(step_ID);
				reserveRepairPanelInfo.setSheet_ID(sheet_ID);
				reserveRepairPanelInfo.setSheet_Start_Time(sheet_Start_Time);
				reserveRepairPanelInfo.setGlass_Start_Time(glass_Start_Time);
				reserveRepairPanelInfo.setPanel_Start_Time(panel_Start_Time);
				reserveRepairPanelInfo.setGlass_ID(glass_ID);
				reserveRepairPanelInfo.setPanel_No(panel_No);
				reserveRepairPanelInfo.setDefect_No(defect_No);
				reserveRepairPanelInfo.setDefect_Code(defect_Code);
				reserveRepairPanelInfo.setDefect_Pattern(defect_Pattern);
				reserveRepairPanelInfo.setDefect_Size_Type(defect_Size_Type);
				reserveRepairPanelInfo.setDefect_Judge(defect_Judge);
				reserveRepairPanelInfo.setGlass_X(glass_X);
				reserveRepairPanelInfo.setGlass_Y(glass_Y);
				reserveRepairPanelInfo.setGlass_X2(glass_X2);
				reserveRepairPanelInfo.setGlass_Y2(glass_Y2);
				reserveRepairPanelInfo.setSheet_X(sheet_X);
				reserveRepairPanelInfo.setSheet_Y(sheet_Y);
				reserveRepairPanelInfo.setSheet_X2(sheet_X2);
				reserveRepairPanelInfo.setSheet_Y2(sheet_Y2);
				reserveRepairPanelInfo.setPanel_X(panel_X);
				reserveRepairPanelInfo.setPanel_Y(panel_Y);
				reserveRepairPanelInfo.setPanel_X2(panel_X2);
				reserveRepairPanelInfo.setPanel_Y2(panel_Y2);
				reserveRepairPanelInfo.setRs_Judge(rs_Judge);
				reserveRepairPanelInfo.setRs_Code(rs_Code);
				reserveRepairPanelInfo.setRs_Defect_Image_Name(rs_Defect_Image_Name);
				reserveRepairPanelInfo.setPanelNum(Integer.toString(panelNum));
				reserveRepairPanelInfo.setRepairOperation(repairOperation);
				
				dataList.add(reserveRepairPanelInfo);
			}
			ExtendedObjectProxy.getReserveRepairPanelInfoService().create(eventInfo, dataList);
		}
		
		if(adcDefectList != null && adcDefectList.size() > 0)
		{
			int i=0;
			for(Element adcDefectInfo:adcDefectList)
			{
				String imageName = adcDefectInfo.getChild("IMAGENAME").getText();
				String screenJudge = adcDefectInfo.getChild("SCREENJUDGE").getText();
				String defectCode = adcDefectInfo.getChild("DEFECTCODE").getText();
				String defectJudge = adcDefectInfo.getChild("DEFECTJUDGE").getText();
				String panelName = adcDefectInfo.getChild("PANELNAME").getText();
				String detax = adcDefectInfo.getChild("DETAX").getText();
				String detay = adcDefectInfo.getChild("DETAY").getText();
				String detax2 = adcDefectInfo.getChild("DETAX2").getText();
				String detay2 = adcDefectInfo.getChild("DETAY2").getText();
				
				if(i==0&& getReviewProductImageJudge(ProductName, ProcessOperationName, MachineName, imageName)>0)
				{
                    break;					
				}
				MESLotServiceProxy.getLotServiceImpl().insertCtReviewProductImageJudge(eventInfo, ProductName, ProcessOperationName, MachineName, imageName, screenJudge, defectCode, defectJudge, panelName,
						"Insert", detax, detay, detax2, detay2);
			}

		}

		return doc;
	}

	private void InsertGlassJudge(String glassName, String glassJudge, String sheetName, String processFlowName, String processFlowVersion, String eventName, String eventUser, String eventComment,
			String processOperationName, Timestamp eventTime, String panelGrades, String glassDetailGrade) throws CustomException
	{
		try
		{
			StringBuffer sql = new StringBuffer();
			sql.append("INSERT ");
			sql.append("  INTO CT_GLASSJUDGE  ");
			sql.append("  (GLASSNAME, GLASSJUDGE, SHEETNAME, LASTEVENTNAME, LASTEVENTUSER, ");
			sql.append("   LASTEVENTTIME, LASTEVENTCOMMENT, PROCESSOPERATIONNAME, PANELGRADES, GLASSDETAILGRADE) ");
			sql.append("VALUES  ");
			sql.append("  (:GLASSNAME, :GLASSJUDGE, :SHEETNAME, :LASTEVENTNAME, :LASTEVENTUSER, ");
			sql.append("   :LASTEVENTTIME, :LASTEVENTCOMMENT, :PROCESSOPERATIONNAME, :PANELGRADES, :GLASSDETAILGRADE) ");

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("GLASSNAME", glassName);
			bindMap.put("GLASSJUDGE", glassJudge);
			bindMap.put("SHEETNAME", sheetName);
			bindMap.put("LASTEVENTNAME", eventName);
			bindMap.put("LASTEVENTUSER", eventUser);
			bindMap.put("LASTEVENTTIME", eventTime);
			bindMap.put("LASTEVENTCOMMENT", eventComment);
			bindMap.put("PROCESSOPERATIONNAME", processOperationName);
			bindMap.put("PANELGRADES", panelGrades);
			bindMap.put("GLASSDETAILGRADE", glassDetailGrade);

			GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), bindMap);
		}
		catch (Exception e)
		{
			throw new CustomException("SYS-8001", "for insert " + glassName + " into CT_GLASSJUDGE");
		}
	}

	private void UpdateGlassJudge(String glassName, String glassJudge, String sheetName, String eventName, String eventUser, String eventComment, String processOperationName, Timestamp eventTime,
			String panelGrades, String glassDetailGrade) throws CustomException
	{
		try
		{
			StringBuffer sql = new StringBuffer();
			sql.append("UPDATE CT_GLASSJUDGE ");
			sql.append("   SET GLASSJUDGE = :GLASSJUDGE, ");
			sql.append("       SHEETNAME = :SHEETNAME, ");
			sql.append("       PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME, ");
			sql.append("       LASTEVENTNAME = :LASTEVENTNAME, ");
			sql.append("       LASTEVENTUSER = :LASTEVENTUSER, ");
			sql.append("       LASTEVENTTIME = :LASTEVENTTIME, ");
			sql.append("       LASTEVENTCOMMENT = :LASTEVENTCOMMENT, ");
			sql.append("       PANELGRADES = :PANELGRADES, ");
			sql.append("       GLASSDETAILGRADE = :GLASSDETAILGRADE ");
			sql.append(" WHERE GLASSNAME = :GLASSNAME ");

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("GLASSNAME", glassName);
			bindMap.put("GLASSJUDGE", glassJudge);
			bindMap.put("SHEETNAME", sheetName);
			bindMap.put("LASTEVENTNAME", eventName); 
			bindMap.put("LASTEVENTUSER", eventUser);
			bindMap.put("LASTEVENTTIME", eventTime);
			bindMap.put("LASTEVENTCOMMENT", eventComment);
			bindMap.put("PROCESSOPERATIONNAME", processOperationName);
			bindMap.put("PANELGRADES", panelGrades);
			bindMap.put("GLASSDETAILGRADE", glassDetailGrade);

			GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), bindMap);
		}
		catch (Exception e)
		{
			throw new CustomException("SYS-8001", "for update " + glassName + " to the CT_GLASSJUDGE");
		}
	}

	public int getReviewProductJudge(String ProductName, String ProcessOperationName, String MachineName)
	{
		int qty = 0;

		StringBuffer sql = new StringBuffer();
		sql.append("SELECT COUNT (PRODUCTNAME) AS QTY ");
		sql.append("  FROM CT_REVIEWPRODUCTJUDGE ");
		sql.append(" WHERE PRODUCTNAME = :PRODUCTNAME ");
		sql.append("   AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
		sql.append("   AND MACHINENAME = :MACHINENAME ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("PRODUCTNAME", ProductName);
		args.put("PROCESSOPERATIONNAME", ProcessOperationName);
		args.put("MACHINENAME", MachineName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (result.size() > 0)
		{
			String quantity = ConvertUtil.getMapValueByName(result.get(0), "QTY");
			qty = Integer.parseInt(quantity);
		}

		return qty;
	}

	private Map<String, String> insertCt_PanelJudge(EventInfo eventInfo, String panelName, String panelJudge, String panelGrade, String processOperationName, String factoryName)
	{
		Map<String, String> returnMap = new HashMap<String, String>();

		StringBuffer querySql = new StringBuffer();
		querySql.append("SELECT PANELNAME, FIRSTSCRAPOPERATIONNAME, FIRSTSCRAPFACTORYNAME, PANELJUDGE ");
		querySql.append("  FROM CT_PANELJUDGE ");
		querySql.append(" WHERE PANELNAME = :PANELNAME ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("PANELNAME", panelName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(querySql.toString(), args);

		if (result.size() > 0) // update
		{
			String oldFirstScrapOperationName = ConvertUtil.getMapValueByName(result.get(0), "FIRSTSCRAPOPERATIONNAME");
			String oldFirstScrapFactoryName = ConvertUtil.getMapValueByName(result.get(0), "FIRSTSCRAPFACTORYNAME");
			String oldPanelJudge = ConvertUtil.getMapValueByName(result.get(0), "PANELJUDGE");
			StringBuilder updateSql = new StringBuilder();
			updateSql.append("UPDATE CT_PANELJUDGE ");
			updateSql.append("   SET PANELJUDGE = :PANELJUDGE, ");
			updateSql.append("       OLDPANELJUDGE = :OLDPANELJUDGE, ");
			updateSql.append("       LASTEVENTNAME = :LASTEVENTNAME, ");
			updateSql.append("       LASTEVENTUSER = :LASTEVENTUSER, ");
			updateSql.append("       LASTEVENTTIME = :LASTEVENTTIME, ");
			updateSql.append("       LASTEVENTCOMMENT = :LASTEVENTCOMMENT, ");
			updateSql.append("       PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("PANELNAME", panelName);
			bindMap.put("PANELJUDGE", panelJudge);
			bindMap.put("OLDPANELJUDGE", oldPanelJudge);
			bindMap.put("LASTEVENTNAME", eventInfo.getEventName());
			bindMap.put("LASTEVENTUSER", eventInfo.getEventUser());
			bindMap.put("LASTEVENTTIME", eventInfo.getEventTime());
			bindMap.put("LASTEVENTCOMMENT", eventInfo.getEventComment());
			bindMap.put("PROCESSOPERATIONNAME", processOperationName);

			if (oldFirstScrapOperationName.equalsIgnoreCase("") || oldFirstScrapOperationName.isEmpty() || oldFirstScrapFactoryName.equalsIgnoreCase("") || oldFirstScrapFactoryName.isEmpty())
			{
				if (panelJudge.equalsIgnoreCase("S"))
				{
					updateSql.append(" , FIRSTSCRAPOPERATIONNAME = :FIRSTSCRAPOPERATIONNAME ");
					updateSql.append(" , FIRSTSCRAPFACTORYNAME = :FIRSTSCRAPFACTORYNAME ");

					bindMap.put("FIRSTSCRAPOPERATIONNAME", processOperationName);
					bindMap.put("FIRSTSCRAPFACTORYNAME", factoryName);

					returnMap.put("FIRSTSCRAPOPERATIONNAME", processOperationName);
					returnMap.put("FIRSTSCRAPFACTORYNAME", factoryName);
				}
				else
				{
					returnMap.put("FIRSTSCRAPOPERATIONNAME", null);
					returnMap.put("FIRSTSCRAPFACTORYNAME", null);
				}
			}
			else
			{
				returnMap.put("FIRSTSCRAPOPERATIONNAME", oldFirstScrapOperationName);
				returnMap.put("FIRSTSCRAPFACTORYNAME", oldFirstScrapFactoryName);
			}

			updateSql.append(" WHERE  PANELNAME = :PANELNAME ");
			GenericServiceProxy.getSqlMesTemplate().update(updateSql.toString(), bindMap);
		}
		else
		{// insert
			StringBuffer insertSql = new StringBuffer();
			insertSql.append("INSERT ");
			insertSql.append("  INTO CT_PANELJUDGE  ");
			insertSql.append("  (PANELNAME, PANELJUDGE, OLDPANELJUDGE, GLASSNAME, SHEETNAME, ");
			insertSql.append("   LASTEVENTNAME, LASTEVENTUSER, LASTEVENTTIME, LASTEVENTCOMMENT, PROCESSOPERATIONNAME, ");
			insertSql.append("   LOTNAME, FIRSTSCRAPOPERATIONNAME, FIRSTSCRAPFACTORYNAME) ");
			insertSql.append("VALUES  ");
			insertSql.append("  (:PANELNAME, :PANELJUDGE, :OLDPANELJUDGE, :GLASSNAME, :SHEETNAME, ");
			insertSql.append("   :LASTEVENTNAME, :LASTEVENTUSER, :LASTEVENTTIME, :LASTEVENTCOMMENT, :PROCESSOPERATIONNAME, ");
			insertSql.append("   :LOTNAME, :FIRSTSCRAPOPERATIONNAME, :FIRSTSCRAPFACTORYNAME) ");

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("PANELNAME", panelName);
			bindMap.put("PANELJUDGE", panelJudge);
			bindMap.put("OLDPANELJUDGE", "");
			bindMap.put("GLASSNAME", panelName.substring(0, panelName.length() - 3));
			bindMap.put("SHEETNAME", panelName.substring(0, panelName.length() - 4));
			bindMap.put("LASTEVENTNAME", eventInfo.getEventName());
			bindMap.put("LASTEVENTUSER", eventInfo.getEventUser());
			bindMap.put("LASTEVENTTIME", eventInfo.getEventTime());
			bindMap.put("LASTEVENTCOMMENT", eventInfo.getEventComment());
			bindMap.put("PROCESSOPERATIONNAME", processOperationName);
			bindMap.put("LOTNAME", "");

			if (panelJudge.equalsIgnoreCase("S"))
			{
				bindMap.put("FIRSTSCRAPOPERATIONNAME", processOperationName);
				bindMap.put("FIRSTSCRAPFACTORYNAME", factoryName);

				returnMap.put("FIRSTSCRAPOPERATIONNAME", processOperationName);
				returnMap.put("FIRSTSCRAPFACTORYNAME", factoryName);
			}
			else
			{
				bindMap.put("FIRSTSCRAPOPERATIONNAME", "");
				bindMap.put("FIRSTSCRAPFACTORYNAME", "");

				returnMap.put("FIRSTSCRAPOPERATIONNAME", "");
				returnMap.put("FIRSTSCRAPFACTORYNAME", "");
			}

			GenericServiceProxy.getSqlMesTemplate().update(insertSql.toString(), bindMap);
		}

		return returnMap;
	}

	private void insertCtReviewPanelJudgeNew(EventInfo eventInfo, String ProductName, String ProcessOperationName, String MachineName, String PanelName, String PanelJudge, String PanelGrade,
			Map<String, String> scrapMapInfo) throws CustomException
	{
		Map<String, Object> bindMap = new HashMap<String, Object>();

		StringBuffer sql = new StringBuffer();
		sql.append("DELETE FROM CT_REVIEWPANELJUDGE ");
		sql.append(" WHERE 1 = 1 ");
		sql.append("   AND PRODUCTNAME = :PRODUCTNAME ");
		sql.append("   AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
		sql.append("   AND MACHINENAME = :MACHINENAME ");
		sql.append("   AND PANELNAME = :PANELNAME ");

		bindMap.put("PRODUCTNAME", ProductName);
		bindMap.put("PROCESSOPERATIONNAME", ProcessOperationName);
		bindMap.put("MACHINENAME", MachineName);
		bindMap.put("PANELNAME", PanelName);

		GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), bindMap);

		bindMap = new HashMap<String, Object>();

		StringBuffer sql2 = new StringBuffer();
		sql2.append("INSERT ");
		sql2.append("  INTO CT_REVIEWPANELJUDGE  ");
		sql2.append("  (PRODUCTNAME, PROCESSOPERATIONNAME, MACHINENAME, PANELNAME, PANELJUDGE, ");
		sql2.append("   PANELGRADE, CREATETIME, CREATEUSER, SCRAPFACTORYNAME, SCRAPOPERATIONNAME, ");
		sql2.append("   GLASSNAME, SHEETNAME) ");
		sql2.append("VALUES  ");
		sql2.append("  (:PRODUCTNAME, :PROCESSOPERATIONNAME, :MACHINENAME, :PANELNAME, :PANELJUDGE, ");
		sql2.append("   :PANELGRADE, :CREATETIME, :CREATEUSER, :SCRAPFACTORYNAME, :SCRAPOPERATIONNAME, ");
		sql2.append("   :GLASSNAME, :SHEETNAME) ");

		bindMap.put("PRODUCTNAME", ProductName);
		bindMap.put("PROCESSOPERATIONNAME", ProcessOperationName);
		bindMap.put("MACHINENAME", MachineName);
		bindMap.put("PANELNAME", PanelName);
		bindMap.put("PANELJUDGE", PanelJudge);
		bindMap.put("PANELGRADE", PanelGrade);
		bindMap.put("CREATETIME", eventInfo.getEventTime());
		bindMap.put("CREATEUSER", eventInfo.getEventUser());
		bindMap.put("SCRAPFACTORYNAME", scrapMapInfo.get("FIRSTSCRAPFACTORYNAME"));
		bindMap.put("SCRAPOPERATIONNAME", scrapMapInfo.get("FIRSTSCRAPOPERATIONNAME"));
		bindMap.put("GLASSNAME", PanelName.substring(0, PanelName.length() - 2));
		bindMap.put("SHEETNAME", PanelName.substring(0, PanelName.length() - 3));

		GenericServiceProxy.getSqlMesTemplate().update(sql2.toString(), bindMap);
	}

	private List<Map<String, Object>> checkGlassExist(String factoryName, String productName)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT GLASSNAME ");
		sql.append("  FROM CT_GLASSJUDGE ");
		sql.append(" WHERE GLASSNAME = :GLASSNAME ");

		Map<String, Object> args = new HashMap<String, Object>();

		if (StringUtils.equals(factoryName, "ARRAY"))
			args.put("GLASSNAME", productName + "1");
		else
			args.put("GLASSNAME", productName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		return result;
	}

	private List<Map<String, Object>> checkGlassExist(String productName)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT GLASSNAME ");
		sql.append("  FROM CT_GLASSJUDGE ");
		sql.append(" WHERE GLASSNAME = :GLASSNAME ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("GLASSNAME", productName + "2");

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		return result;
	}
	
	public int getReviewProductImageJudge(String ProductName, String ProcessOperationName, String MachineName, String ImageName)
	{
		int qty = 0;

		StringBuffer sql = new StringBuffer();
		sql.append("SELECT COUNT (PRODUCTNAME) AS QTY ");
		sql.append("  FROM CT_REVIEWPRODUCTIMAGEJUDGE ");
		sql.append(" WHERE PRODUCTNAME = :PRODUCTNAME ");
		sql.append("   AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
		sql.append("   AND MACHINENAME = :MACHINENAME ");
		sql.append("   AND IMAGESEQ = :IMAGENAME ");
		
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("PRODUCTNAME", ProductName);
		args.put("PROCESSOPERATIONNAME", ProcessOperationName);
		args.put("MACHINENAME", MachineName);
		args.put("IMAGENAME", ImageName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (result.size() > 0)
		{
			String quantity = ConvertUtil.getMapValueByName(result.get(0), "QTY");
			qty = Integer.parseInt(quantity);
		}

		return qty;
	}
}
