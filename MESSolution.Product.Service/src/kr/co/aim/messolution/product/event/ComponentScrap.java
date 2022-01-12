package kr.co.aim.messolution.product.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ComponentHistory;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.PolicyUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableHistory;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.DeassignCarrierInfo;
import kr.co.aim.greentrack.lot.management.info.MakeScrappedInfo;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.info.ChangeGradeInfo;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;

public class ComponentScrap extends AsyncHandler {

	private static Log log = LogFactory.getLog(ComponentScrap.class);
	
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String productName = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", true);
		String vcrProductName = SMessageUtil.getBodyItemValue(doc, "VCRPRODUCTNAME", false);
		String productGrade = SMessageUtil.getBodyItemValue(doc, "PRODUCTGRADE", false);
		String productJudge = SMessageUtil.getBodyItemValue(doc, "PRODUCTJUDGE", false);
		String scrapCode = SMessageUtil.getBodyItemValue(doc, "SCRAPCODE", false);

		Product productData = null;
		String factoryName = "";
		String productSpecName = "";
		String productSpecVersion = "";
		String processFlowName = "";
		String processFlowVersion = "";
		String processOperationName = "";
		String processOperationVersion = "";
		String productionType = "";
		String productRequestName = "";
		String productType ="";
		String eventName = "ScrapProduct";

		// 1. Check Machine
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(unitName);
		Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotDataForUpdate(lotName);

		// 2. Select product or lot data
		// 2018.08.15. PostCell EQP Cannot report ProductType//2021/1/6caixu CutComponentScrap
		if (StringUtils.equals(lotData.getProductType(), GenericServiceProxy.getConstantMap().ProductType_Panel)&& machineData.getFactoryName().equals("POSTCELL") )
		{
			    
				productType = GenericServiceProxy.getConstantMap().ProductType_Panel;
				eventName = "ScrapLot";
				
				CommonValidation.checkLotState(lotData);
				//CommonValidation.checkLotProcessStateRun(lotData);
				factoryName = lotData.getFactoryName();
				productSpecName = lotData.getProductSpecName();
				productSpecVersion = lotData.getProductSpecVersion();
				processFlowName = lotData.getProcessFlowName();
				processFlowVersion = lotData.getProcessFlowVersion();
				processOperationName = lotData.getProcessOperationName();
				processOperationVersion = lotData.getProcessOperationVersion();
				productionType = lotData.getProductionType();
				productRequestName = lotData.getProductRequestName();
				String machineRecipeName = getMachineRecipe(factoryName,productSpecName,productSpecVersion,processFlowName,processFlowVersion,processOperationName,processOperationVersion, machineName);
			
			    
		}
		else
		{
			productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
			
			if (productData.getProductState().equals("Scrapped"))
			{
				throw new CustomException("PRODUCT-0001"); 
			}
			
			factoryName = productData.getFactoryName();
			productSpecName = productData.getProductSpecName();
			productSpecVersion = productData.getProductSpecVersion();
			processFlowName = productData.getProcessFlowName();
			processFlowVersion = productData.getProcessFlowVersion();
			processOperationName = productData.getProcessOperationName();
			processOperationVersion = productData.getProcessOperationVersion();
			productionType = productData.getProductionType();
			productRequestName = productData.getProductRequestName();
			productType = productData.getProductType();
		}

		// 3. Insert to Component History
		EventInfo eventInfo = EventInfoUtil.makeEventInfo(eventName, getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		ProcessFlow processFlowData = CommonUtil.getProcessFlowData(factoryName, processFlowName, processFlowVersion);
		
		if (!processFlowData.getProcessFlowType().equalsIgnoreCase("Sort") && !StringUtils.equals(productType, GenericServiceProxy.getConstantMap().ProductType_Panel))
		{
			eventInfo.setEventName("ChangeProductGradeForScrap");
		}
		
		ComponentHistory dataInfo = new ComponentHistory();
		dataInfo.setTimeKey(eventInfo.getEventTimeKey());
		dataInfo.setProductName(productName);
		dataInfo.setLotName(lotName);
		dataInfo.setEventName(eventInfo.getEventName());
		dataInfo.setEventTime(eventInfo.getEventTime());
		dataInfo.setEventUser(eventInfo.getEventUser());
		dataInfo.setFactoryName(factoryName);
		dataInfo.setProductSpecName(productSpecName);
		dataInfo.setProductSpecVersion(productSpecVersion);
		dataInfo.setProcessFlowName(processFlowName);
		dataInfo.setProcessFlowVersion(processFlowVersion);
		dataInfo.setProcessOperationName(processOperationName);
		dataInfo.setProcessOperationVersion(processOperationVersion);
		dataInfo.setProductionType(productionType);
		dataInfo.setProductType(productType);
		dataInfo.setMachineName(machineName);
		dataInfo.setProductJudge(productJudge);
		dataInfo.setProductGrade(productGrade);
		dataInfo.setMaterialLocationName(unitName);
		dataInfo.setProductRequestName(productRequestName);
		dataInfo.setReasonCode(scrapCode);
		
	    ExtendedObjectProxy.getComponentHistoryService().create(eventInfo, dataInfo);

		// 4. Scrap
		if (productType.equals(GenericServiceProxy.getConstantMap().ProductType_Panel))
		{
			// 4.1 Scrap lot
			Map<String, String> udfs = new HashMap<String, String>();
			udfs.put("VCRPRODUCTNAME", vcrProductName);
			lotData.setUdfs(udfs);
			eventInfo.setReasonCode(scrapCode);
			scrapLot(eventInfo, lotData);
		}
		else
		{
			// 4.2 Scrap product
			List<Element> productList = new ArrayList<Element>();

			if (productData != null)
			{
				LotKey lotKey = new LotKey();
				lotKey.setLotName(productData.getLotName());
				lotData = LotServiceProxy.getLotService().selectByKeyForUpdate(lotKey);
			}

			Element product = null;
			product = new Element("PRODUCTLIST");

			Element productElement = new Element("PRODUCTNAME");
			productElement.setText(productName);
			product.addContent(productElement);

			Element positionElement = new Element("POSITION");
			positionElement.setText(String.valueOf(productData.getPosition()));
			product.addContent(positionElement);

			productList.add(product);

			MachineSpec unitSpecData = GenericServiceProxy.getSpecUtil().getMachineSpec(unitName);

			if (StringUtils.equals(unitSpecData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_EVA) && StringUtils.equals(unitSpecData.getDescription(), "EVA"))
			{
				scrapCode = unitSpecData.getDescription();
			}
			
			scrapProduct(lotData, productData, eventInfo, scrapCode);
			
		}
	}
	public void scrapProduct(Lot lotData, Product productData, EventInfo eventInfo, String reasonCode) throws CustomException
	{
		List<ProductU> productUSequence = new ArrayList<ProductU>();
		
		ProductU productU = new ProductU();
		productU.setProductName(productData.getKey().getProductName());
		productU.setUdfs(productData.getUdfs());
		productUSequence.add(productU);

		eventInfo.setReasonCode(reasonCode);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		MakeScrappedInfo makeScrappedInfo = new MakeScrappedInfo(1, productUSequence);
		
		ProcessFlow processFlowData = CommonUtil.getProcessFlowData(lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion());
		ProcessOperationSpec operationData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion());

		if (processFlowData.getProcessFlowType().equalsIgnoreCase("Sort")
				|| StringUtil.in(operationData.getDetailProcessOperationType(), GenericServiceProxy.getConstantMap().SORT_OLEDtoTP, GenericServiceProxy.getConstantMap().SORT_TPtoOLED))
		{
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT S.REASONTYPE,S.REASONCODE,S.LASTEVENTCOMMENT  ");
			sql.append("FROM CT_SORTJOB C,CT_SORTJOBPRODUCT S ");
			sql.append("WHERE C.JOBNAME=S.JOBNAME "); 
			sql.append("AND C.JOBTYPE in ('SourceOnly','Buf(SourceOnly)') "); 
			sql.append("AND C.JOBSTATE='STARTED' ");
			sql.append("AND S.PRODUCTNAME= :PRODUCTNAME ");
			

			Map<String, Object> bindMap = new HashMap<String, Object>();

			bindMap.put("PRODUCTNAME", productData.getKey().getProductName());
			
			List<ListOrderedMap> result= kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
			if(result.size()>0)
			{
				String reasonCode1 = CommonUtil.getValue(result.get(0), "REASONCODE");
				String reasonCodeType1 = CommonUtil.getValue(result.get(0), "REASONTYPE");
				String eventComment1 = CommonUtil.getValue(result.get(0), "LASTEVENTCOMMENT");
				eventInfo.setReasonCode(reasonCode1);
				eventInfo.setReasonCodeType(reasonCodeType1);
				
				if(StringUtil.isNotEmpty(eventComment1))
				{
					eventInfo.setEventComment(eventComment1);
				}
			}

			String subTimeKey = "";
			try
			{
				List<LotHistory> lotHistoryData = LotServiceProxy.getLotHistoryService().select("WHERE LOTNAME =?AND EVENTCOMMENT=? ORDER BY TIMEKEY DESC",
						new Object[] { lotData.getKey().getLotName(), "ComponentScrap" });
				subTimeKey = lotHistoryData.get(0).getKey().getTimeKey().substring(2, 20);
			}
			catch (Exception e)
			{
				subTimeKey = "0";
			}
			
			String subCurrentTimeKey = TimeUtils.getCurrentEventTimeKey().substring(2, 20);
			
			if(Long.parseLong(subTimeKey)>(Long.parseLong(subCurrentTimeKey)))
			{
				eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			}
			
			LotServiceProxy.getLotService().makeScrapped(lotData.getKey(), eventInfo, makeScrappedInfo);
			
			if(lotData.getProductQuantity() == 1 && !(lotData.getCarrierName().isEmpty()))
			{
				MESLotServiceProxy.getLotServiceImpl().updateLotData("LOTGRADE", "S", productData.getLotName());
				
				//Deassign Carrier
				Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(lotData.getCarrierName());
				
				productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);
				DeassignCarrierInfo createInfo =  MESLotServiceProxy.getLotInfoUtil().deassignCarrierInfo(lotData, durableData, productUSequence); 
				EventInfo eventInfoDeassign = EventInfoUtil.makeEventInfo("DeassignCarrier", getEventUser(), getEventComment(), "", "");
				eventInfoDeassign.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
				
				MESLotServiceProxy.getLotServiceImpl().deassignCarrier(lotData, createInfo, eventInfoDeassign);
			}
			
			// Update ProductGrade as S
			try
			{
				String sql1 = "UPDATE PRODUCTHISTORY SET PRODUCTGRADE = :PRODUCTGRADE, CARRIERNAME = :CARRIERNAME WHERE PRODUCTNAME = :PRODUCTNAME AND TIMEKEY = :TIMEKEY ";

				Map<String, String> args1 = new HashMap<String, String>();
				args1.put("PRODUCTGRADE", "S");
				args1.put("CARRIERNAME", "");
				args1.put("PRODUCTNAME", productData.getKey().getProductName());
				args1.put("TIMEKEY", eventInfo.getEventTimeKey());

				GenericServiceProxy.getSqlMesTemplate().update(sql1, args1);

				sql1 = "UPDATE PRODUCT SET PRODUCTGRADE = :PRODUCTGRADE, CARRIERNAME = :CARRIERNAME WHERE PRODUCTNAME = :PRODUCTNAME ";
				GenericServiceProxy.getSqlMesTemplate().update(sql1, args1);
			}
			catch (Exception en)
			{
			}
		 
			// Change Scrap Quantity
			//If the OLED product is a sheet, the number of scraps for the work order is reduced
			
			ProductRequest newProductRequestData = new ProductRequest();
			
			if(lotData.getFactoryName().equals("POSTCELL")&&lotData.getProductType().equals("Glass"))
			{
				Product productDataForSpec = MESProductServiceProxy.getProductServiceUtil().getProductData(productData.getKey().getProductName());
				ProductSpec productSpec = MESProductServiceProxy.getProductServiceUtil().getProductSpecByProductName(productDataForSpec);
				
				int xProductCount = Integer.parseInt(productSpec.getUdfs().get("PRODUCTCOUNTTOXAXIS"));
				int yProductCount = Integer.parseInt(productSpec.getUdfs().get("PRODUCTCOUNTTOYAXIS"))/2;
				int incrementQty = (int) ( xProductCount * yProductCount);
				
				newProductRequestData = MESWorkOrderServiceProxy.getProductRequestServiceImpl().ChangeScrapQty(eventInfo, lotData.getProductRequestName(), incrementQty, 0);
			}
			else
			{
				if(lotData.getFactoryName().equals("OLED")&&lotData.getProductType().equals("Sheet"))
				{
					newProductRequestData = MESWorkOrderServiceProxy.getProductRequestServiceImpl().ChangeScrapQty(eventInfo, lotData.getProductRequestName(), 2, 0);
				}
				else
				{
					newProductRequestData = MESWorkOrderServiceProxy.getProductRequestServiceImpl().ChangeScrapQty(eventInfo, lotData.getProductRequestName(), 1, 0);
				}
			}

			/*if (newProductRequestData.getPlanQuantity() == newProductRequestData.getFinishedQuantity() + newProductRequestData.getScrappedQuantity())
			{
				EventInfo newEventInfo = eventInfo;
				newEventInfo.setEventName("Complete");
				newEventInfo.setEventTimeKey(ConvertUtil.getCurrTimeKey());
				MESWorkOrderServiceProxy.getProductRequestServiceImpl().makeCompleted(newEventInfo, lotData.getProductRequestName());
			}*/

			List<Map<String, Object>> sqlForSelectGlassResult = null;
			String sqlForSelectGlass = " SELECT SCRAPFLAG,GLASSNAME,GLASSJUDGE,SHEETNAME,PROCESSFLOWNAME,PROCESSFLOWVERSION,PROCESSOPERATIONNAME FROM CT_GLASSJUDGE WHERE SHEETNAME = :SHEETNAME ";

			Map<String, Object> bindMap2 = new HashMap<String, Object>();
			bindMap2.put("SHEETNAME", productData.getKey().getProductName());

			sqlForSelectGlassResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlForSelectGlass, bindMap2);

			for (int j = 0; j < sqlForSelectGlassResult.size(); j++)
			{
				String sqlForUpdateScrapFlag = "UPDATE CT_GLASSJUDGE SET SCRAPFLAG = :SCRAPFLAG" + " WHERE GLASSNAME = :GLASSNAME";

				Map<String, Object> bindMap1 = new HashMap<String, Object>();
				bindMap1.put("SCRAPFLAG", "Y");
				bindMap1.put("GLASSNAME", sqlForSelectGlassResult.get(j).get("GLASSNAME"));
				GenericServiceProxy.getSqlMesTemplate().update(sqlForUpdateScrapFlag, bindMap1);

				String sqlForInsertGlassJudgeHist = " INSERT INTO CT_GLASSJUDGEHISTORY "
												  + "        ( GLASSNAME, TIMEKEY, GLASSJUDGE, SHEETNAME, PROCESSFLOWNAME, PROCESSFLOWVERSION, "
												  + "          EVENTNAME, EVENTUSER, EVENTTIME, EVENTCOMMENT, PROCESSOPERATIONNAME,SCRAPFLAG) "
												  + " VALUES ( :GLASSNAME, :TIMEKEY, :GLASSJUDGE, :SHEETNAME, :PROCESSFLOWNAME, :PROCESSFLOWVERSION, "
												  + "          :EVENTNAME, :EVENTUSER , :EVENTTIME, :EVENTCOMMENT, :PROCESSOPERATIONNAME,:SCRAPFLAG ) ";

				Map<String, Object> bindMap11 = new HashMap<String, Object>();
				bindMap11.put("GLASSNAME", sqlForSelectGlassResult.get(j).get("GLASSNAME"));
				bindMap11.put("GLASSJUDGE", sqlForSelectGlassResult.get(j).get("GLASSJUDGE"));
				bindMap11.put("SHEETNAME", sqlForSelectGlassResult.get(j).get("SHEETNAME"));
				bindMap11.put("PROCESSFLOWNAME", sqlForSelectGlassResult.get(j).get("PROCESSFLOWNAME"));
				bindMap11.put("PROCESSFLOWVERSION", sqlForSelectGlassResult.get(j).get("PROCESSFLOWVERSION"));
				bindMap11.put("EVENTNAME", eventInfo.getEventName());
				bindMap11.put("EVENTUSER", eventInfo.getEventUser());
				bindMap11.put("EVENTTIME", eventInfo.getEventTime());
				bindMap11.put("EVENTCOMMENT", eventInfo.getEventComment());
				bindMap11.put("PROCESSOPERATIONNAME", sqlForSelectGlassResult.get(j).get("PROCESSOPERATIONNAME"));
				bindMap11.put("TIMEKEY", TimeStampUtil.getCurrentEventTimeKey());
				bindMap11.put("SCRAPFLAG", "Y");

				GenericServiceProxy.getSqlMesTemplate().update(sqlForInsertGlassJudgeHist, bindMap11);
			}		
			deleteScrapProduct(productUSequence);
		}
		else
		{
			eventInfo.setEventName("ChangeProductGradeForScrap");
			ChangeGradeInfo changeGradeInfo = MESProductServiceProxy.getProductInfoUtil().changeGradeInfo(productData, productData.getPosition(), "S", productData.getProductProcessState(),
					productData.getSubProductGrades1(), productData.getSubProductGrades2(), productData.getSubProductQuantity1(), productData.getSubProductQuantity2());

			productData = MESProductServiceProxy.getProductServiceImpl().changeGrade(productData, changeGradeInfo, eventInfo);
		}

	}
	private void deleteScrapProduct(List<ProductU> productUSequence) throws CustomException {
		// TODO Auto-generated method stub
		String queryStrigScrapProduct="DELETE FROM CT_OASCRAPPRODUCTNAME WHERE PRODUCTNAME=:PRODUCTNAME";
		List<Object[]> deleteScrapListProduct= new ArrayList<Object[]>();
		for(ProductU productData : productUSequence)
		{
			List<Object> bindList = new ArrayList<Object>();
			bindList.add(productData.getProductName());
			deleteScrapListProduct.add(bindList.toArray());
		}
		try
		{
		MESLotServiceProxy.getLotServiceUtil().updateBatch(queryStrigScrapProduct, deleteScrapListProduct);
		}
		catch (Exception e)
		{
		  log.info("Delete CT_OASCRAPPRODUCTNAME fail ");
		}
		
	}

	private void scrapLot(EventInfo eventInfo, Lot lotData) throws CustomException
	{
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		String queryStringLot = "UPDATE LOT SET LOTSTATE = ?, LOTPROCESSSTATE = ?, LASTEVENTNAME = ?, LASTEVENTTIMEKEY = ?, LASTEVENTTIME = ?, "
				+ "LASTEVENTUSER = ?, LASTEVENTCOMMENT = ?, LOTGRADE = ?, REASONCODE = ?, REASONCODETYPE = ?, PROCESSOPERATIONNAME = ?, "
				+ "PROCESSOPERATIONVERSION = ?, BEFOREOPERATIONNAME = ?, BEFOREOPERATIONVER = ?, NODESTACK = ?, LOTDETAILGRADE = ? WHERE LOTNAME = ?";

		List<Object[]> updateLotArgList = new ArrayList<Object[]>();
		List<LotHistory> updateLotHistoryList = new ArrayList<LotHistory>();

		List<ProductU> productUSequence = new ArrayList<ProductU>();
		// productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllUnScrapProductUSequence(lotData);

		// 1.SourceLot Deassign With SourceCST
		if (StringUtil.isNotEmpty(lotData.getCarrierName()))
		{
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(lotData.getCarrierName());

			DeassignCarrierInfo createInfo = MESLotServiceProxy.getLotInfoUtil().deassignCarrierInfo(lotData, durableData, productUSequence);

			eventInfo.setEventName("Deassign");

			if (!durableData.getUdfs().get("COVERNAME").isEmpty())
			{
				Durable olddurableInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableData.getUdfs().get("COVERNAME"));
				Durable durableInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableData.getUdfs().get("COVERNAME"));
                if(durableData.getKey().getDurableName().equals(durableData.getUdfs().get("COVERNAME")))
                {
                	durableInfo.setLotQuantity(durableInfo.getLotQuantity());
                	
                }else
                {
                	durableInfo.setLotQuantity(durableInfo.getLotQuantity() - 1);	
                }
				durableInfo.setLastEventName(eventInfo.getEventName());
				durableInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
				durableInfo.setLastEventTime(eventInfo.getEventTime());
				durableInfo.setLastEventUser(eventInfo.getEventUser());
				durableInfo.setLastEventComment(eventInfo.getEventComment());

				DurableHistory durHistory = new DurableHistory();
				durHistory = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(olddurableInfo, durableInfo, durHistory);

				DurableServiceProxy.getDurableService().update(durableInfo);
				DurableServiceProxy.getDurableHistoryService().insert(durHistory);
			}

			// 2021-03-16	dhko	add exception
			try
			{
				MESLotServiceProxy.getLotServiceImpl().deassignCarrier(lotData, createInfo, eventInfo);
			}
			catch (Exception ex)
			{
				log.error(ex.getCause());
			}
			
			lotData.setCarrierName("");
		}

		List<Map<String, Object>> operationData = getOperationByDetailOperType(lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), "ScrapPack");
		Node nextNode = ProcessFlowServiceProxy.getNodeService().getNode(lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), constantMap.Node_ProcessOperation, operationData.get(0).get("PROCESSOPERATIONNAME").toString(), operationData.get(0).get("PROCESSOPERATIONVERSION").toString());
		
		// 2.Scrap
		eventInfo.setEventName("ScrapLot");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());

		Lot oldLot = (Lot)ObjectUtil.copyTo(lotData);

		List<Object> lotBindList = new ArrayList<Object>();

		lotBindList.add(constantMap.Lot_Scrapped);
		lotBindList.add(constantMap.Lot_Wait);
		lotBindList.add(eventInfo.getEventName());
		lotBindList.add(eventInfo.getEventTimeKey());
		lotBindList.add(eventInfo.getEventTime());
		lotBindList.add(eventInfo.getEventUser());
		lotBindList.add(eventInfo.getEventComment());
		lotBindList.add("S");
		lotBindList.add(eventInfo.getReasonCode());
		lotBindList.add(eventInfo.getReasonCodeType());
		lotBindList.add(nextNode.getNodeAttribute1());
		lotBindList.add(nextNode.getNodeAttribute2());
		lotBindList.add(oldLot.getProcessOperationName());
		lotBindList.add(oldLot.getProcessOperationVersion());
		lotBindList.add(nextNode.getKey().getNodeId());
		lotBindList.add("");
		lotBindList.add(lotData.getKey().getLotName());

		updateLotArgList.add(lotBindList.toArray());

		// History
		lotData.setLotState(constantMap.Lot_Scrapped);
		lotData.setLotProcessState(constantMap.Lot_Wait);
		lotData.setLotGrade("S");
		lotData.setReasonCode(eventInfo.getReasonCode());
		lotData.setReasonCodeType(eventInfo.getReasonCodeType());
		lotData.setProcessOperationName(nextNode.getNodeAttribute1());
		lotData.setProcessOperationVersion(nextNode.getNodeAttribute2());
		lotData.setNodeStack(nextNode.getKey().getNodeId());
		lotData.setLastEventName(eventInfo.getEventName());
		lotData.setLastEventTime(eventInfo.getEventTime());
		lotData.setLastEventTimeKey(eventInfo.getEventTimeKey());
		lotData.setLastEventComment(eventInfo.getEventComment());
		lotData.setLastEventUser(eventInfo.getEventUser());
		
		Map<String, String> lotUdf = new HashMap<>();
		lotUdf = oldLot.getUdfs();
		lotUdf.put("BEFOREOPERATIONNAME", oldLot.getProcessOperationName());
		lotUdf.put("BEFOREOPERATIONVER", oldLot.getProcessOperationVersion());
		lotUdf.put("LOTDETAILGRADE", "");
		lotData.setUdfs(lotUdf);
		
		LotHistory lotHistory = new LotHistory();
		lotHistory = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lotData, lotHistory);
		
		updateLotHistoryList.add(lotHistory);

		
		// Change WO
		//modify by wangys 2020/11/26 Cancel Auto CompleteWO
		ProductRequest newProductRequestData = MESWorkOrderServiceProxy.getProductRequestServiceImpl().ChangeScrapQty(eventInfo, lotData.getProductRequestName(), 1, 0);
		
		/*if (newProductRequestData.getPlanQuantity() == newProductRequestData.getFinishedQuantity() + newProductRequestData.getScrappedQuantity())
		{
			EventInfo newEventInfo = eventInfo;
			newEventInfo.setEventName("Complete");
			newEventInfo.setEventTimeKey(ConvertUtil.getCurrTimeKey());
			MESWorkOrderServiceProxy.getProductRequestServiceImpl().makeCompleted(newEventInfo, lotData.getProductRequestName());
		}*/

		MESLotServiceProxy.getLotServiceUtil().updateBatch(queryStringLot, updateLotArgList);
		try 
		{
			CommonUtil.executeBatch("insert", updateLotHistoryList);
		} 
		catch (Exception e) 
		{
			log.error(e.getMessage());
			throw new CustomException(e.getCause());
		}
	}

	private List<Map<String, Object>> getOperationByDetailOperType(String factoryname, String productSpecName, String productSpecVersion, String processFlowName, String processFlowVersion, String detailOperType)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT TP.PROCESSOPERATIONNAME, TP.PROCESSOPERATIONVERSION, PS.DETAILPROCESSOPERATIONTYPE ");
		sql.append("  FROM TPFOPOLICY TP, PROCESSOPERATIONSPEC PS ");
		sql.append(" WHERE TP.FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND TP.PRODUCTSPECNAME = :PRODUCTSPECNAME ");
		sql.append("   AND TP.PRODUCTSPECVERSION = :PRODUCTSPECVERSION ");
		sql.append("   AND TP.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("   AND TP.PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
		sql.append("   AND TP.PROCESSOPERATIONNAME = PS.PROCESSOPERATIONNAME ");
		sql.append("   AND TP.PROCESSOPERATIONVERSION = PS.PROCESSOPERATIONVERSION ");
		sql.append("   AND PS.DETAILPROCESSOPERATIONTYPE = :DETAILPROCESSOPERATIONTYPE ");

		Map<String, String> args = new HashMap<String, String>();
		args.put("FACTORYNAME", factoryname);
		args.put("PRODUCTSPECNAME", productSpecName);
		args.put("PRODUCTSPECVERSION", productSpecVersion);
		args.put("PROCESSFLOWNAME", processFlowName);
		args.put("PROCESSFLOWVERSION", processFlowVersion);
		args.put("DETAILPROCESSOPERATIONTYPE", detailOperType);
		
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);
		
		return result;
	}
	public String getMachineRecipe(String factoryName, String productSpecName, String productSpecVersion, String processFlowName, String processFlowVersion, 
			String processOperationName, String processOperationVersion, String machineName) throws CustomException
    {
	 ListOrderedMap instruction = PolicyUtil.getMachineRecipeName(factoryName, productSpecName, productSpecVersion, processFlowName, processFlowVersion, processOperationName, processOperationVersion, machineName);

	 String designatedRecipeName = CommonUtil.getValue(instruction, "MACHINERECIPENAME");
	
	 {
		if (StringUtil.isEmpty(designatedRecipeName))
			throw new CustomException("MACHINE-0102", designatedRecipeName);
	 }
	
	 return designatedRecipeName;
    }
}
