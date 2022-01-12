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
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.lot.management.info.MakeUnScrappedInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.info.ChangeGradeInfo;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;

public class ComponentUnscrap extends AsyncHandler {

	private static Log log = LogFactory.getLog(ComponentUnscrap.class);
	
	@Override
	public void doWorks(Document doc) throws CustomException {
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", false);
		String productName = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", true);
		String productGrade = SMessageUtil.getBodyItemValue(doc, "PRODUCTGRADE", false);
		String productJudge = SMessageUtil.getBodyItemValue(doc, "PRODUCTJUDGE", false);
		String fromSlotId = SMessageUtil.getBodyItemValue(doc, "FROMSLOTID", false);
		String toSlotId = SMessageUtil.getBodyItemValue(doc, "TOSLOTID", false);
		String fromSlotPosition = SMessageUtil.getBodyItemValue(doc, "FROMSLOTPOSITION", false);
		String toSlotPosition = SMessageUtil.getBodyItemValue(doc, "TOSLOTPOSITION", false);
		String scrapCode = SMessageUtil.getBodyItemValue(doc, "SCRAPCODE", false);
		String scrapText = SMessageUtil.getBodyItemValue(doc, "SCRAPTEXT", false);


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
		String productType = "";
		String eventName = "UnScrapProduct";

		// 1. Check Machine
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(unitName);
		Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotDataForUpdate(lotName);

		// 2. Select product or lot data
		if (StringUtils.equals(lotData.getProductType(), GenericServiceProxy.getConstantMap().ProductType_Panel) || "POSTCELL".equals(machineData.getFactoryName()))
		{
			productType = GenericServiceProxy.getConstantMap().ProductType_Panel;
			eventName = "UnScrapLot";

			lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(productName);
			factoryName = lotData.getFactoryName();
			productSpecName = lotData.getProductSpecName();
			productSpecVersion = lotData.getProductSpecVersion();
			processFlowName = lotData.getProcessFlowName();
			processFlowVersion = lotData.getProcessFlowVersion();
			processOperationName = lotData.getProcessOperationName();
			processOperationVersion = lotData.getProcessOperationVersion();
			productionType = lotData.getProductionType();
			productRequestName = lotData.getProductRequestName();
		}
		else
		{
			productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
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
		EventInfo eventInfo = EventInfoUtil.makeEventInfo(eventName, getEventUser(), scrapText, null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		if (productData != null && !productData.getProductGrade().equals("S"))
		{
			eventInfo.setEventName("ChangeProductGradeForUnScrap");
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
		dataInfo.setMaterialLocationName(unitName);
		dataInfo.setProductRequestName(productRequestName);
		dataInfo.setReasonCode(scrapCode);
		dataInfo.setProductJudge(productJudge);
		dataInfo.setProductGrade(productGrade);
		
		if(StringUtil.isNotEmpty(fromSlotId))
		{
			if(StringUtil.isNumeric(fromSlotId)) dataInfo.setFromSlotId(Integer.parseInt(fromSlotId));
			dataInfo.setFromSlotPosition(fromSlotPosition);
		}
		
		if(StringUtil.isNotEmpty(toSlotId))
		{
			if(StringUtil.isNumeric(toSlotId)) dataInfo.setToSlotId(Integer.parseInt(toSlotId));
			dataInfo.setToSlotPosition(toSlotPosition);
		}
		
	    ExtendedObjectProxy.getComponentHistoryService().create(eventInfo, dataInfo);

		// 4. UnScrap
		if (productType.equals(GenericServiceProxy.getConstantMap().ProductType_Panel))
		{
			CommonValidation.checkLotStateScrapped(lotData);

			// 4.1 UnScrap lot
			unScrapLot(eventInfo, lotData);
		}
		else
		{
			MachineSpec unitSpecData = GenericServiceProxy.getSpecUtil().getMachineSpec(unitName);

			if (StringUtils.equals(unitSpecData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_EVA) && StringUtils.equals(unitSpecData.getDescription(), "EVA"))
			{
				scrapCode = unitSpecData.getDescription();
			}
			
			// 4. Update ReasonCode
			productData.setReasonCode(scrapCode);
			ProductServiceProxy.getProductService().update(productData);
			productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
			lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(productData.getLotName());
			
			// 5. UnScrap product
			List<ProductU> productList = new ArrayList<ProductU>();
			ProductU productU = new ProductU();
			productU.setProductName(productData.getKey().getProductName());
			productU.setUdfs(productData.getUdfs());
			productList.add(productU);

			if (productData.getProductGrade().equals("S") && StringUtils.equals(unitSpecData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Sorter))
			{
				unScrapProduct(lotData, productData, eventInfo, "");
			}
			else
			{
				eventInfo.setEventName("ChangeProductGradeForUnScrap");
				changeProductGrade(productData, eventInfo);
			}
			
		}
	}

	private void changeProductGrade(Product productData, EventInfo eventInfo) throws CustomException 
	{
		String beforeProductGrade = "G";

		List<ListOrderedMap> productUList = new ArrayList<>();
		try
		{
			productUList = this.getRecoveryProduct(productData.getKey().getProductName());

			if (productUList != null)
			{
				beforeProductGrade = CommonUtil.getValue(productUList.get(0), "PRODUCTGRADE");
			}
		}
		catch (Exception e)
		{
		}
		
		ChangeGradeInfo changeGradeInfo = MESProductServiceProxy.getProductInfoUtil().changeGradeInfo(productData, productData.getPosition(), beforeProductGrade, productData.getProductProcessState(),
				productData.getSubProductGrades1(), productData.getSubProductGrades2(), productData.getSubProductQuantity1(), productData.getSubProductQuantity2());

		productData = MESProductServiceProxy.getProductServiceImpl().changeGrade(productData, changeGradeInfo, eventInfo);
		
	}

	private void unScrapLot(EventInfo eventInfo, Lot lotData) throws CustomException
	{
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		List<Object[]> updateLotArgList = new ArrayList<Object[]>();
		List<LotHistory> updateLotHistoryList = new ArrayList<LotHistory>();

		Lot oldLot = (Lot) ObjectUtil.copyTo(lotData);

		Node nextNode = ProcessFlowServiceProxy.getNodeService().getNode(lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), constantMap.Node_ProcessOperation , lotData.getUdfs().get("BEFOREOPERATIONNAME").toString(), lotData.getUdfs().get("BEFOREOPERATIONVER").toString()); 
		
		List<Object> lotBindList = new ArrayList<Object>();

		lotBindList.add(constantMap.Lot_Released);
		lotBindList.add(constantMap.Lot_Run);
		lotBindList.add(eventInfo.getEventName());
		lotBindList.add(eventInfo.getEventTimeKey());
		lotBindList.add(eventInfo.getEventTime());
		lotBindList.add(eventInfo.getEventUser());
		lotBindList.add(eventInfo.getEventComment());
		lotBindList.add("G");
		lotBindList.add("");
		lotBindList.add("");
		lotBindList.add(nextNode.getNodeAttribute1());
		lotBindList.add(nextNode.getNodeAttribute2());
		lotBindList.add(oldLot.getProcessOperationName());
		lotBindList.add(oldLot.getProcessOperationVersion());
		lotBindList.add(nextNode.getKey().getNodeId());
		lotBindList.add(lotData.getKey().getLotName());

		updateLotArgList.add(lotBindList.toArray());

		// History
		lotData.setLotState(constantMap.Lot_Released);
		lotData.setLotProcessState(constantMap.Lot_Run);
		lotData.setLotGrade("G");
		lotData.setReasonCode("");
		lotData.setReasonCodeType("");
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
		lotData.setUdfs(lotUdf);
		
		LotHistory lotHistory = new LotHistory();
		lotHistory = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lotData, lotHistory);
		
		updateLotHistoryList.add(lotHistory);

		// change scrap qty
		
		ProductRequest newProductRequestData = MESWorkOrderServiceProxy.getProductRequestServiceImpl().ChangeScrapQty(eventInfo, lotData.getProductRequestName(), 0, 1);
		//Modify by wangys 2020/11/26 Cancel auto Released
		/*if ( newProductRequestData.getProductRequestState().equals("Completed") &&
			(newProductRequestData.getPlanQuantity() > newProductRequestData.getFinishedQuantity() + newProductRequestData.getScrappedQuantity()) )
		{
			EventInfo newEventInfo = eventInfo;
			newEventInfo.setEventName("Release");
			newEventInfo.setEventTimeKey(ConvertUtil.getCurrTimeKey());
			MESWorkOrderServiceProxy.getProductRequestServiceImpl().makeReleased(newEventInfo, lotData.getProductRequestName());
		}*/

		if (updateLotArgList.size() > 0)
		{
			String queryStringLot = "UPDATE LOT SET LOTSTATE = ?, LOTPROCESSSTATE = ?, LASTEVENTNAME = ?, LASTEVENTTIMEKEY = ?, LASTEVENTTIME = ?, "
								  + "LASTEVENTUSER = ?, LASTEVENTCOMMENT = ?, LOTGRADE = ?, REASONCODE = ?, REASONCODETYPE = ?, PROCESSOPERATIONNAME = ?, "
								  + "PROCESSOPERATIONVERSION = ?, BEFOREOPERATIONNAME = ?, BEFOREOPERATIONVER = ?, NODESTACK = ? WHERE LOTNAME = ? ";
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
	}

	public void unScrapProduct(Lot lotData, Product productData, EventInfo eventInfo, String reasonCode) throws CustomException
	{
		String beforeLotState = lotData.getLotState();
		String beforeProductGrade = productData.getProductGrade();

		List<ListOrderedMap> productUList = new ArrayList<>();
		try
		{
			productUList = this.getRecoveryProduct(productData.getKey().getProductName());

			if (productUList != null)
			{
				beforeProductGrade = CommonUtil.getValue(productUList.get(0), "PRODUCTGRADE");
			}
		}
		catch (Exception e)
		{
		}

		List<ProductU> productUSequence = new ArrayList<ProductU>();

		ProductU productU = new ProductU();
		productU.setProductName(productData.getKey().getProductName());
		productU.setUdfs(productData.getUdfs());

		productUSequence.add(productU);

		MakeUnScrappedInfo makeUnScrappedInfo = new MakeUnScrappedInfo(lotData.getLotProcessState(), 1, productUSequence);

		eventInfo.setReasonCode(reasonCode);
		Lot newLotData = LotServiceProxy.getLotService().makeUnScrapped(lotData.getKey(), eventInfo, makeUnScrappedInfo);

		String newLotState = newLotData.getLotState();

		if (StringUtils.equals(beforeLotState, GenericServiceProxy.getConstantMap().Lot_Scrapped) && StringUtils.equals(newLotState, GenericServiceProxy.getConstantMap().Lot_Released))
		{
			// Set LotProcessState as RUN
			try
			{
				String sql = "UPDATE LOTHISTORY SET LOTPROCESSSTATE = :LOTPROCESSSTATE WHERE LOTNAME = :LOTNAME AND TIMEKEY = :TIMEKEY ";

				Map<String, String> args = new HashMap<String, String>();
				args.put("LOTPROCESSSTATE", GenericServiceProxy.getConstantMap().Lot_LoggedIn);
				args.put("LOTNAME", lotData.getKey().getLotName());
				args.put("TIMEKEY", eventInfo.getEventTimeKey());

				GenericServiceProxy.getSqlMesTemplate().update(sql, args);

				sql = "UPDATE LOT SET LOTPROCESSSTATE = :LOTPROCESSSTATE WHERE LOTNAME = :LOTNAME ";
				GenericServiceProxy.getSqlMesTemplate().update(sql, args);
			}
			catch (Exception e)
			{
			}
		}
		
		// Recover ProductGrade and productProcessState 
		try
		{
			String sql = "UPDATE PRODUCTHISTORY SET PRODUCTGRADE = :PRODUCTGRADE, PRODUCTPROCESSSTATE = :PRODUCTPROCESSSTATE, CARRIERNAME = :CARRIERNAME WHERE PRODUCTNAME = :PRODUCTNAME AND TIMEKEY = :TIMEKEY ";

			Map<String, String> args = new HashMap<String, String>();
			args.put("PRODUCTGRADE", beforeProductGrade);
			args.put("PRODUCTPROCESSSTATE", "Processing");
			args.put("CARRIERNAME", lotData.getCarrierName());
			args.put("PRODUCTNAME", productData.getKey().getProductName());
			args.put("TIMEKEY", eventInfo.getEventTimeKey());

			GenericServiceProxy.getSqlMesTemplate().update(sql, args);

			sql = "UPDATE PRODUCT SET PRODUCTGRADE = :PRODUCTGRADE, PRODUCTPROCESSSTATE = :PRODUCTPROCESSSTATE, CARRIERNAME = :CARRIERNAME WHERE PRODUCTNAME = :PRODUCTNAME ";
			GenericServiceProxy.getSqlMesTemplate().update(sql, args);
		}
		catch (Exception e)
		{
		}

		// Change Scrap Quantity	
		if(lotData.getFactoryName().equals("POSTCELL")&&lotData.getProductType().equals("Glass"))
		{
			Product productDataForSpec = MESProductServiceProxy.getProductServiceUtil().getProductData(productData.getKey().getProductName());
			ProductSpec productSpec = MESProductServiceProxy.getProductServiceUtil().getProductSpecByProductName(productDataForSpec);
			
			int xProductCount = Integer.parseInt(productSpec.getUdfs().get("PRODUCTCOUNTTOXAXIS"));
			int yProductCount = Integer.parseInt(productSpec.getUdfs().get("PRODUCTCOUNTTOYAXIS"));
			int incrementQty = (int) (xProductCount * yProductCount);
			
			MESWorkOrderServiceProxy.getProductRequestServiceImpl().ChangeScrapQty(eventInfo, lotData.getProductRequestName(), 0, incrementQty);
		}
		else
		{
			if(lotData.getFactoryName().equals("OLED")&&lotData.getProductType().equals("Sheet"))
			{
				MESWorkOrderServiceProxy.getProductRequestServiceImpl().ChangeScrapQty(eventInfo, lotData.getProductRequestName(), 0, 2);
			}
			else
			{						
				MESWorkOrderServiceProxy.getProductRequestServiceImpl().ChangeScrapQty(eventInfo, lotData.getProductRequestName(), 0, 1);		
			}			
		}
		
		//modify by wangys 2020/11/26 Cancel Auto CompleteWO
		
		ProductRequest newProductRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(lotData.getProductRequestName());
         
		if ( newProductRequestData.getProductRequestState().equals("Completed"))
		{
			throw new CustomException("PRODUCTREQUEST-0004", newProductRequestData.getProductRequestState());
		}
		/*if (newProductRequestData.getPlanQuantity() == newProductRequestData.getFinishedQuantity() + newProductRequestData.getScrappedQuantity())
		{
			EventInfo newEventInfo = eventInfo;
			newEventInfo.setEventName("Complete");
			MESWorkOrderServiceProxy.getProductRequestServiceImpl().makeCompleted(newEventInfo, lotData.getProductRequestName());
		}*/

		List<Map<String, Object>> sqlForSelectGlassResult = null;
		String sqlForSelectGlass = " SELECT SCRAPFLAG,GLASSNAME,GLASSJUDGE,SHEETNAME,PROCESSFLOWNAME,PROCESSFLOWVERSION,PROCESSOPERATIONNAME FROM CT_GLASSJUDGE WHERE SHEETNAME = :SHEETNAME ";

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("SHEETNAME", productData.getKey().getProductName());

		sqlForSelectGlassResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlForSelectGlass, bindMap);

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
	}

	public List<ListOrderedMap> getRecoveryProduct(String productName) throws CustomException
	{
		List<ListOrderedMap> productList;
		try
		{
			String sql = "" +
			 " SELECT * FROM  (  " +
			 " SELECT A.PRODUCTSTATE, A.PRODUCTPROCESSSTATE, A.PRODUCTHOLDSTATE ,A.TIMEKEY ,A.PRODUCTGRADE, A.NODESTACK,A.MACHINENAME FROM PRODUCTHISTORY A, " +
			 "  (SELECT TIMEKEY FROM PRODUCTHISTORY WHERE PRODUCTNAME =:PRODUCTNAME AND EVENTNAME IN ('ScrapProduct' , 'Scrap Product' , 'Scrap', 'ChangeProductGradeForScrap') AND ROWNUM = '1' ORDER BY TIMEKEY DESC) " +
			 "  B WHERE 1=1 " +
			 "  AND A.PRODUCTNAME = :PRODUCTNAME " +
			 "  AND A.TIMEKEY < B.TIMEKEY " +
			 "  ORDER BY A.TIMEKEY DESC) "+
			 "  WHERE 1=1 "+
			 "  AND ROWNUM  <= 5";
			
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("PRODUCTNAME", productName);

			productList = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		}
		catch (greenFrameDBErrorSignal de)
		{
			productList = null;
		}

		return productList;
	}
}
