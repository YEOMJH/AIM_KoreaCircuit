package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MVIPanelJudge;
import kr.co.aim.messolution.extended.object.management.data.SVIPickInfo;
import kr.co.aim.messolution.extended.object.management.data.SorterPickPrintInfo;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greenframe.exception.ErrorSignal;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableHistory;
import kr.co.aim.greentrack.durable.management.data.DurableSpec;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.lot.management.policy.util.LotPFIValueSetter;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.iter.NodeStackUtil;
import kr.co.aim.greentrack.processflow.management.iter.PFIValueSetter;
import kr.co.aim.greentrack.processflow.management.iter.ProcessFlowIterator;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class MVIJudgeForSort extends SyncHandler {

	private static Log log = LogFactory.getLog(MVIJudgeForSort.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String opticalJudge = SMessageUtil.getBodyItemValue(doc, "OPTICALJUDGE", true);
		String electricalJudge = SMessageUtil.getBodyItemValue(doc, "ELECTRICALJUDGE", true);
		String tpJudge = SMessageUtil.getBodyItemValue(doc, "TPJUDGE", false);
		String judge = SMessageUtil.getBodyItemValue(doc, "JUDGE", true);
		String rx = SMessageUtil.getBodyItemValue(doc, "RX", true);
		String ry = SMessageUtil.getBodyItemValue(doc, "RY", true);
		String gx = SMessageUtil.getBodyItemValue(doc, "GX", true);
		String gy = SMessageUtil.getBodyItemValue(doc, "GY", true);
		String bx = SMessageUtil.getBodyItemValue(doc, "BX", true);
		String by = SMessageUtil.getBodyItemValue(doc, "BY", true);

		List<Element> opticalList = SMessageUtil.getBodySequenceItemList(doc, "OPTICALLIST", false);
		List<Element> electricalList = SMessageUtil.getBodySequenceItemList(doc, "ELECTRICALLIST", false);
		List<Element> tpInspectionList = SMessageUtil.getBodySequenceItemList(doc, "TPINSPECTIONLIST", false);

		// EventInfo MVIJudge
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("MVIJudge", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(ConvertUtil.getCurrTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());

		log.debug("lotName : " + lotName);

		Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
		ProcessOperationSpec processOperationData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion());
		String machineName = lotData.getMachineName();

		if (!processOperationData.getDetailProcessOperationType().equals("MVI") && !processOperationData.getDetailProcessOperationType().equals("SVI/MVI"))
			throw new CustomException("MVI-0003");

		CommonValidation.checkLotProcessStateRun(lotData);
		CommonValidation.checkLotHoldState(lotData);
		CommonValidation.checkLotState(lotData);

		// set lot grade/ detail grade
		String lotGrade = "";
		String lotDetailGrade = ""; // electricalJudge;

		if (judge.equals("S") || judge.equals("N"))
		{
			lotGrade = "S";
			lotDetailGrade = "";
		}
		else if(judge.equals("S1"))//caixu 2020/12/10 ADD S1
		{
			lotGrade = "S";
			lotDetailGrade =judge;
		}
		else
		{
			if (judge.equals("P"))
			{
				lotGrade = "P";
				lotDetailGrade = "";
			}
			else if (judge.equals("R"))
			{
				lotGrade = "R";
				lotDetailGrade = "";
			}
			else
			{
				lotGrade = "G";
				lotDetailGrade = judge;
			}
		}

		long seq = 1;

		// 1.check exist lot from MVIPanelJudge
		List<MVIPanelJudge> mviPanelJudgeDataList = ExtendedObjectProxy.getMVIPanelJudgeService().getMVIPanelJudgeDataList(lotName);

		if (mviPanelJudgeDataList != null)
			seq = mviPanelJudgeDataList.get(0).getSeq();

		// 2.MVIPanelJudge save
		ExtendedObjectProxy.getMVIPanelJudgeService().setPanelJudgeDataForSort(seq, lotName, opticalJudge, electricalJudge, judge, eventInfo, machineName, tpJudge);

		if (electricalJudge.equals("P"))
			lotDetailGrade = "";

		// 3.lot grade/ detail grade update
		setLotData(lotName, lotGrade, lotDetailGrade, eventInfo);

		// 4.save opticalList
		if (opticalList.size() > 0)
		{
			// 4.1 get color gamut
			BigDecimal Rx = new BigDecimal(rx);
			BigDecimal Ry = new BigDecimal(ry);
			BigDecimal Gx = new BigDecimal(gx);
			BigDecimal Gy = new BigDecimal(gy);
			BigDecimal Bx = new BigDecimal(bx);
			BigDecimal By = new BigDecimal(by);
			BigDecimal k = new BigDecimal("0.3164");

			BigDecimal Gy_By = Gy.subtract(By);
			BigDecimal By_Ry = By.subtract(Ry);
			BigDecimal Ry_Gy = Ry.subtract(Gy);

			// (Rx*(Gy-By) + Gx*(By-Ry) + Bx*(Ry-Gy))/2/0.1582
			BigDecimal gamut = Rx.multiply(Gy_By).add(Gx.multiply(By_Ry)).add(Bx.multiply(Ry_Gy)).divide(k, 20, BigDecimal.ROUND_HALF_UP);

			// 4.2 save opticalList
			setOpticalData(opticalList, seq, lotName, gamut.toString(), eventInfo);
		}

		// 5.save electricalList
		if (electricalList.size() > 0)
		{
			setElectricalData(electricalList, seq, lotName, eventInfo);
			setUserDefectHist(machineName, lotName, judge, electricalList, eventInfo);
		}

		// 5.1 save tpList
		if (tpInspectionList.size() > 0)
			setTPInspectionData(tpInspectionList, seq, lotName, eventInfo);

		// 6.ReworkCheck
		ExtendedObjectProxy.getReworkProductService().setReworkCountData(lotData, processOperationData, lotGrade);
		setSortPickInfo(lotName,judge,electricalList, eventInfo);

		return doc;
	}

	public void setLotData(String lotName, String lotGrade, String lotDetailGrade, EventInfo eventInfo) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);

		// lot grade/ detail grade update
		lotData.setLotGrade(lotGrade);
		LotServiceProxy.getLotService().update(lotData);

		// Set Event
		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.getUdfs().put("LOTDETAILGRADE", lotDetailGrade);

		LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
	}

	public void setPanelJudgeData(long seq, String lotName, String opticalJudge, String electricalJudge, String judge, EventInfo eventInfo, EventInfo trackOutEventInfo, String machineName,
			String tpJudge) throws CustomException
	{
		try
		{
			MVIPanelJudge dataInfo = ExtendedObjectProxy.getMVIPanelJudgeService().selectByKey(false, new Object[] { seq, lotName });
			dataInfo.setOpticalJudge(opticalJudge);
			dataInfo.setElectricalJudge(electricalJudge);
			dataInfo.setAfterGrade(judge);
			dataInfo.setEventTime(eventInfo.getEventTime());
			dataInfo.setEventUser(eventInfo.getEventUser());
			dataInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
			dataInfo.setLastLoggedOutTime(trackOutEventInfo.getEventTime());
			dataInfo.setMachineName(machineName);
			dataInfo.setTpJudge(tpJudge);

			ExtendedObjectProxy.getMVIPanelJudgeService().update(dataInfo);
		}
		catch (greenFrameDBErrorSignal ex)
		{
			throw new CustomException("LOT-0007", "MVIPanelJudge [" + lotName + "] does not exist!");
		}
	}

	public void setOpticalData(List<Element> opticalList, long seq, String lotName, String gamut, EventInfo eventInfo) throws CustomException
	{
		ExtendedObjectProxy.getMVIOpticalInspectionService().deleteMVIOpticalInspectionDataList(eventInfo, seq, lotName);

		for (Element defectData : opticalList)
		{
			try
			{
				ExtendedObjectProxy.getMVIOpticalInspectionService().createMVIOpticalInspectionData(eventInfo, seq, lotName, defectData, gamut);
			}
			catch (greenFrameDBErrorSignal ex)
			{
				throw new CustomException("LOT-0007", ex.getMessage());
			}
		}
	}

	public void setElectricalData(List<Element> electricalList, long seq, String lotName, EventInfo eventInfo) throws CustomException
	{
		ExtendedObjectProxy.getMVIElectricalInspectionService().deleteMVIElectricalInspectionDataList(eventInfo, seq, lotName);

		for (Element defectData : electricalList)
		{
			try
			{
				ExtendedObjectProxy.getMVIElectricalInspectionService().createMVIElectricalInspectionData(eventInfo, seq, lotName, defectData);
			}
			catch (greenFrameDBErrorSignal ex)
			{
				throw new CustomException("LOT-0313");
			}
		}
	}

	public void setTPInspectionData(List<Element> tpInspectionList, long seq, String lotName, EventInfo eventInfo) throws CustomException
	{
		ExtendedObjectProxy.getMVITPInspectionService().deleteMVITPInspectionDataList(eventInfo, seq, lotName);

		for (Element tpInspectionData : tpInspectionList)
		{
			try
			{
				ExtendedObjectProxy.getMVITPInspectionService().createMVITPInspectionData(eventInfo, seq, lotName, tpInspectionData);
			}
			catch (greenFrameDBErrorSignal ex)
			{
				throw new CustomException("LOT-0313");
			}
		}
	}

	public void setUserDefectHist(String machineName, String lotName, String judge, List<Element> electricalList, EventInfo eventInfo) throws CustomException
	{
		Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
		ProductRequest productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(lotData.getProductRequestName());

		List<String> defectList = new ArrayList<String>();

		for (Element electrical : electricalList)
		{
			String defectCode = electrical.getChildText("DEFECT_CODE");

			if (CommonUtil.equalsIn("DEFECT_ORDER", "S", "K1", "K"))
				defectList.add(defectCode);
		}

		String defectCode = defectList.toString().replace(",", "").trim();
		ExtendedObjectProxy.getMVIUserDefectService().insertMVIUserDefectHistData(eventInfo, lotData, machineName, productRequestData, judge, defectCode);
	}
	public void setSortPickInfo( String lotName, String judge, List<Element> electricalList, EventInfo eventInfo) throws CustomException
	{
	
		String sql1= "Select  TIMEKEY,ENDQUANTITY,QUANTITY FROM CT_SVIPICKINFO "
				+ "WHERE PROCESSOPERATIONNAME ='35052'"
				+ "AND JUDGE =:JUDGE "
				+ " AND CODE = :CODE "
				+ " AND PRODUCTSPECNAME = :PRODUCTSPECNAME "
				+ " AND WORKORDER = :WORKORDER "
		        + " AND POINT = :POINT "
		        + " AND QUANTITY != ENDQUANTITY "
		        + " ORDER BY TIMEKEY DESC";
		String sql2= "Select  TIMEKEY,ENDQUANTITY,QUANTITY FROM CT_SVIPICKINFO "
				+ "WHERE PROCESSOPERATIONNAME ='35052'"
				+ "AND JUDGE =:JUDGE "
				+ " AND CODE = :CODE "
				+ " AND PRODUCTSPECNAME = :PRODUCTSPECNAME "
				+ " AND WORKORDER = :WORKORDER "
				+ " AND QUANTITY != ENDQUANTITY ";
		String sql3= "Select  TIMEKEY,ENDQUANTITY,QUANTITY FROM CT_SVIPICKINFO "
				+ "WHERE PROCESSOPERATIONNAME ='35052'"
				+ "AND JUDGE =:JUDGE "
				+ " AND CODE = :CODE "
				+ " AND PRODUCTSPECNAME = :PRODUCTSPECNAME "
				+ " AND QUANTITY != ENDQUANTITY ";

		Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
		String productSpec=lotData.getProductSpecName();
		String workerOrder=lotData.getProductRequestName();
		String point=lotName.substring(12, 14);

		for (Element electrical : electricalList)
		{
			String defectCode = electrical.getChildText("DEFECT_CODE");
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("JUDGE",judge);
			bindMap.put("CODE",defectCode);
			bindMap.put("PRODUCTSPECNAME",productSpec);
			bindMap.put("WORKORDER",workerOrder);
			bindMap.put("POINT",point);
			List<Map<String, Object>> result1 = GenericServiceProxy.getSqlMesTemplate().queryForList(sql1, bindMap);
			if(result1.size()>0)
			{
			   pickinfoModify(result1,defectCode,lotName,eventInfo);
				
			}
			else
			{
				List<Map<String, Object>> result2 = GenericServiceProxy.getSqlMesTemplate().queryForList(sql2, bindMap);
				if(result2.size()>0)
				{
					pickinfoModify(result2,defectCode,lotName,eventInfo);
					
				}
				else
				{
					List<Map<String, Object>> result3 = GenericServiceProxy.getSqlMesTemplate().queryForList(sql3, bindMap);
					if(result3.size()>0)
					{
						pickinfoModify(result3,defectCode,lotName,eventInfo);
						
					}
					
					
				}
				
				
			}
		}
		
	}
	public void pickinfoModify(List<Map<String, Object>> result,String defectCode,String lotName, EventInfo eventInfo ) throws CustomException
	{

		SorterPickPrintInfo dataInfo = null;
		String timeKey= result.get(0).get("TIMEKEY").toString();
		int endQuantity= Integer.parseInt(result.get(0).get("ENDQUANTITY").toString());
		try
		{
			dataInfo = ExtendedObjectProxy.getSorterPickPrintInfoService().selectByKey(false, new Object[] { lotName });
		}
		catch (Exception ex)
		{
			if (ex instanceof greenFrameDBErrorSignal && ((greenFrameDBErrorSignal) ex).getErrorCode().equals(ErrorSignal.NotFoundSignal))
				log.info("Not found SorterPickPrintInfo by PanelName =  " + lotName);
			else
				throw new CustomException(ex.getCause());
		}

		EventInfo pickEventInfo = EventInfoUtil.makeEventInfo(this.getMessageName(), getEventUser(), getEventComment());

		if (dataInfo == null)
		{
			SorterPickPrintInfo sorterPickData = new SorterPickPrintInfo();
			sorterPickData.setLotName(lotName);
			sorterPickData.setPickPrintMode("YN");
			sorterPickData.setCode(defectCode);

			ExtendedObjectProxy.getSorterPickPrintInfoService().create(pickEventInfo, sorterPickData);
			SVIPickInfo pickData = ExtendedObjectProxy.getSVIPickInfoService().getDataInfoByKey(timeKey, true);
			eventInfo = EventInfoUtil.makeEventInfo(this.getMessageName(), this.getEventUser(), this.getEventComment());
			endQuantity=endQuantity+1;
			pickData.setEndQuantity(endQuantity);
			ExtendedObjectProxy.getSVIPickInfoService().modify(eventInfo, pickData);
	    }
		//}
	}
}