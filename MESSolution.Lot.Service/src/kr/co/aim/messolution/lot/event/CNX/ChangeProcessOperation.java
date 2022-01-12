package kr.co.aim.messolution.lot.event.CNX;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.SampleLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.NodeStack;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.lot.management.info.MakeShippedInfo;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.info.IncrementFinishedQuantityByInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

public class ChangeProcessOperation extends SyncHandler {
	private static Log log = LogFactory.getLog(ChangeProcessOperation.class);
	@Override

	public Object doWorks(Document doc) throws CustomException
	{
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String newProcOperName = SMessageUtil.getBodyItemValue(doc, "NEWPROCESSOPERATIONNAME", true);
		String newProcOperVersion = SMessageUtil.getBodyItemValue(doc, "NEWPROCESSOPERATIONVERSION", true);
		String nodeStack = SMessageUtil.getBodyItemValue(doc, "NODESTACK", true);
		String beforeProcOperName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);

		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

		CommonValidation.checkJobDownFlag(lotData);
		CommonValidation.checkDummyProductReserve(lotData);
		CommonValidation.checkBaseLineLot(lotData);

		List<Product> producList = LotServiceProxy.getLotService().allProducts(lotName);

		// check ProcessInfo
		List<String> productNameList = new ArrayList<>();

		for (Product productA : producList)
		{
			String productName = productA.getKey().getProductName();
			productNameList.add(productName);
		}

		CommonValidation.checkProductProcessInfobyString(productNameList);

		String areaName = lotData.getAreaName();
		String factoryName = lotData.getFactoryName();
		String lotHoldState = lotData.getLotHoldState();
		String lotProcessState = lotData.getLotProcessState();
		String lotState = lotData.getLotState();
		String processFlowName = lotData.getProcessFlowName();
		String processFlowVersion = lotData.getProcessFlowVersion();
		String processOperationVersion = lotData.getProcessOperationVersion();
		String productionType = lotData.getProductionType();
		String productRequestName = lotData.getProductRequestName();
		String productSpec2Name = lotData.getProductSpec2Name();
		String productSpec2Version = lotData.getProductSpec2Version();
		String productSpecName = lotData.getProductSpecName();
		String productSpecVersion = lotData.getProductSpecVersion();
		long priority = lotData.getPriority();
		Timestamp dueDate = lotData.getDueDate();
		double subProductUnitQuantity1 = lotData.getSubProductUnitQuantity1();
		double subProductUnitQuantity2 = lotData.getSubProductUnitQuantity2();

		//if (StringUtil.equals(lotHoldState, "Y"))
		//	throw new CustomException("LOT-0033", lotName);

		List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUSequence(lotName);

		Map<String, String> autoLotudfs = lotData.getUdfs();
		ProcessOperationSpec processOperSpec = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), newProcOperName, newProcOperVersion);
		String detailProcessOperationType = processOperSpec.getDetailProcessOperationType();

		if (StringUtil.equals(lotProcessState, "") && StringUtil.equals(lotState, "Shipped"))
			throw new CustomException("LOT-0034");

		ProcessFlow processFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);

		if (StringUtil.equals(processFlowData.getProcessFlowType(), "Rework")
				&& !isThereSamplingData(factoryName, lotName, productSpecName, productSpecVersion, processFlowName, processFlowVersion, newProcOperName, newProcOperVersion))
		{
			throw new CustomException("LOT-0141", newProcOperName);
		}

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);

		if ((detailProcessOperationType.equals("SHIP") || detailProcessOperationType.equals("TSPSHIP")) && autoLotudfs.get("AUTOSHIPPINGFLAG").equals("Y"))
		{
			String destFactoryname = getDestFactoryName(lotData);

			if (StringUtils.isNotEmpty(destFactoryname))
			{
				executeAutoShipNew(eventInfo, lotData, destFactoryname);
				return doc;
			}
		}

		// Normal Change Operation
		ChangeSpecInfo changeSpecInfo = MESLotServiceProxy.getLotInfoUtil().changeSpecInfo(lotData, areaName, dueDate, factoryName, lotHoldState, lotProcessState, lotState, nodeStack, priority,
				processFlowName, processFlowVersion, newProcOperName, newProcOperVersion, beforeProcOperName, productionType, productRequestName, productSpec2Name, productSpec2Version,
				productSpecName, productSpecVersion, productUdfs, subProductUnitQuantity1, subProductUnitQuantity2);

		eventInfo = EventInfoUtil.makeEventInfo_IgnoreHold("ChangeOper", getEventUser(), getEventComment(), "", "");

		Lot newLotData = MESLotServiceProxy.getLotServiceImpl().changeProcessOperation(eventInfo, lotData, changeSpecInfo);

		List<Product> productDataList = MESProductServiceProxy.getProductServiceUtil().getProductListByLotName(lotName);
		ProcessOperationSpec beforeOperationSpecData = CommonUtil.getProcessOperationSpec(factoryName, beforeProcOperName, processOperationVersion);
		ProcessOperationSpec newOperationSpecData = CommonUtil.getProcessOperationSpec(factoryName, newProcOperName, newProcOperVersion);
		MESLotServiceProxy.getLotServiceUtil().deleteSamplingDataBetweenTwoOperations(eventInfo, newLotData, productDataList, beforeOperationSpecData, newOperationSpecData);

		// FirstGlass
		newLotData = MESLotServiceProxy.getLotServiceUtil().excuteFirstGlass(eventInfo, lotData, newLotData, "N");

		newLotData = MESLotServiceProxy.getLotServiceUtil().executeReserveAction(eventInfo, lotData, newLotData);
		
		ProcessFlow flowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);				
		
		if (!StringUtils.equals(newLotData.getLotHoldState(), "Y")&&(flowData.getProcessFlowType().equals("Main")))
		{
			// Set ReasonCode
			String holdEventComment = "Span Main Operation";
			log.info(holdEventComment);
			
			eventInfo.setReasonCodeType("HOLD");
			eventInfo.setReasonCode("ChanegOperation");
			eventInfo.setEventComment(holdEventComment);
			

			// LotMultiHold
			MESLotServiceProxy.getLotServiceImpl().lotMultiHold(eventInfo, newLotData, new HashMap<String, String>());
		}
		
		if (StringUtil.isNotEmpty(lotData.getProductionType().toString())
				&& (StringUtil.equals(lotData.getProductionType(), "P")
						|| StringUtil.equals(lotData.getProductionType(), "E"))) 
		{
			try 
			{
				MESLotServiceProxy.getLotServiceImpl().sendEmailToMFG(eventInfo, lotData, lotName);
			} 
			catch (Exception e) 
			{
				eventLog.info(" Failed to send mail. ");
			}
		}

		return doc;
	}

	private String getDestFactoryName(Lot afterTrackOutLot) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT DISTINCT PF.TOFACTORYNAME AS DESTINATIONFACTORYNAME ");
		sql.append("  FROM TPPOLICY T, POSFACTORYRELATION PF ");
		sql.append(" WHERE T.CONDITIONID = PF.CONDITIONID ");
		sql.append("   AND T.FACTORYNAME = :SOURCEFACTORYNAME ");
		sql.append("   AND T.PRODUCTSPECNAME = :SOURCEPRODUCTSPECNAME ");
		sql.append("   AND PF.SHIPUNIT = :SHIPUNIT ");

		Map<String, String> inquirybindMap = new HashMap<String, String>();
		inquirybindMap.put("SOURCEFACTORYNAME", afterTrackOutLot.getFactoryName());
		inquirybindMap.put("SOURCEPRODUCTSPECNAME", afterTrackOutLot.getProductSpecName());
		inquirybindMap.put("SHIPUNIT", "Lot");

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), inquirybindMap);

		if (sqlResult.size() == 0)
		{
			throw new CustomException("SYS-1151");
		}
		else if (sqlResult.size() > 1)
		{
			return "";
		}

		String destFactoryName = CommonUtil.getValue(sqlResult.get(0), "DESTINATIONFACTORYNAME");

		return destFactoryName;
	}

	private void executeAutoShipNew(EventInfo eventInfo, Lot lotData, String destFactoryName) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		String lotName = lotData.getKey().getLotName();
		List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUSequence(lotName);

		// Set OldProductRequestName for CancelReceive
		MakeShippedInfo makeShippedInfo = MESLotServiceProxy.getLotInfoUtil().makeShippedInfo(lotData, lotData.getAreaName(), "", destFactoryName, productUdfs);
		makeShippedInfo.getUdfs().put("OLDPRODUCTREQUESTNAME", lotData.getProductRequestName());
		
		// Change Operation
		lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

		if (lotData.getFactoryName().equals("ARRAY"))
		{
			ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo();
			changeSpecInfo.setAreaName(lotData.getAreaName());
			changeSpecInfo.setDueDate(lotData.getDueDate());
			changeSpecInfo.setFactoryName(lotData.getFactoryName());
			changeSpecInfo.setLotHoldState(lotData.getLotHoldState());
			changeSpecInfo.setLotProcessState(lotData.getLotProcessState());
			changeSpecInfo.setLotState(lotData.getLotState());
			changeSpecInfo.setPriority(lotData.getPriority());
			changeSpecInfo.setProcessFlowName(lotData.getProcessFlowName());
			changeSpecInfo.setProcessFlowVersion(lotData.getProcessFlowVersion());
			changeSpecInfo.setProductionType(lotData.getProductionType());
			changeSpecInfo.setProductRequestName(lotData.getProductRequestName());
			changeSpecInfo.setProductSpec2Name(lotData.getProductSpec2Name());
			changeSpecInfo.setProductSpec2Version(lotData.getProductSpec2Version());
			changeSpecInfo.setProductSpecName(lotData.getProductSpecName());
			changeSpecInfo.setProductSpecVersion(lotData.getProductSpecVersion());
			changeSpecInfo.setSubProductUnitQuantity1(lotData.getSubProductUnitQuantity1());
			changeSpecInfo.setSubProductUnitQuantity2(lotData.getSubProductUnitQuantity2());
			changeSpecInfo.setUdfs(lotData.getUdfs());

			changeSpecInfo.getUdfs().put("AUTOSHIPPINGFLAG", "");
			
			StringBuffer sql = new StringBuffer();
			sql.append("SELECT PROCESSOPERATIONNAME, PROCESSOPERATIONVERSION ");
			sql.append("  FROM PROCESSOPERATIONSPEC ");
			sql.append(" WHERE DETAILPROCESSOPERATIONTYPE = :DETAILPROCESSOPERATIONTYPE ");
			sql.append("   AND FACTORYNAME = :FACTORYNAME ");

			Map<String, String> inquirybindMap = new HashMap<String, String>();
			inquirybindMap.put("DETAILPROCESSOPERATIONTYPE", "SHIP");
			inquirybindMap.put("FACTORYNAME", lotData.getFactoryName());

			@SuppressWarnings("unchecked")
			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), inquirybindMap);

			String processOperationName = CommonUtil.getValue(sqlResult.get(0), "PROCESSOPERATIONNAME");
			String processOperationVer = CommonUtil.getValue(sqlResult.get(0), "PROCESSOPERATIONVERSION");
			String NodeId = NodeStack.getNodeID(lotData.getFactoryName(), lotData.getProcessFlowName(), processOperationName, processOperationVer);

			changeSpecInfo.setNodeStack(NodeId);
			changeSpecInfo.setProcessOperationName(processOperationName);
			changeSpecInfo.setProcessOperationVersion(processOperationVer);

			eventInfo.setEventName("ChangeOper");
			LotServiceProxy.getLotService().changeSpec(lotData.getKey(), eventInfo, changeSpecInfo);
		}

		eventInfo.setEventName("Ship");
		MESLotServiceProxy.getLotServiceImpl().shipLot(eventInfo, lotData, makeShippedInfo);

		String productRequestName = lotData.getProductRequestName();

		ProductRequest productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(productRequestName);

		IncrementFinishedQuantityByInfo incrementFinishedQuantityByInfo = new IncrementFinishedQuantityByInfo();
		incrementFinishedQuantityByInfo.setQuantity((long) lotData.getProductQuantity());

		// Increment Work Order Finished Quantity
		eventInfo.setEventName("IncreamentQuantity");
		productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceImpl().incrementFinishedQuantityBy(productRequestData, incrementFinishedQuantityByInfo, eventInfo);
	}

	private boolean isThereSamplingData(String factoryName, String lotName, String productSpecName, String productSpecVersion, String toProcessFlowName, String toProcessFlowVersion,
			String toProcessOperationName, String toProcessOperationVersion) throws CustomException
	{
		List<SampleLot> sampleLotList = ExtendedObjectProxy.getSampleLotService().getSampleLotDataListByToInfo(lotName, factoryName, productSpecName, productSpecVersion, toProcessFlowName,
				toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion);

		if (sampleLotList == null)
			return false;

		return true;
	}
}