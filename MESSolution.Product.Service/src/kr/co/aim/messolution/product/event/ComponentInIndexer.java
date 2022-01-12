package kr.co.aim.messolution.product.event;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ComponentHistory;
import kr.co.aim.messolution.extended.object.management.data.MQCPlan;
import kr.co.aim.messolution.extended.object.management.data.MQCPlanDetail;
import kr.co.aim.messolution.extended.object.management.data.MQCPlanDetail_Extended;
import kr.co.aim.messolution.extended.object.management.data.VirtualProductHistory;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.SetEventInfo;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class ComponentInIndexer extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{

		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String productName = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", true);
		String productJudge = SMessageUtil.getBodyItemValue(doc, "PRODUCTJUDGE", false);
		String productGrade = SMessageUtil.getBodyItemValue(doc, "PRODUCTGRADE", false);
		String fromSlotId = SMessageUtil.getBodyItemValue(doc, "FROMSLOTID", false);
		String toSlotId = SMessageUtil.getBodyItemValue(doc, "TOSLOTID", false);
		String fromSlotPosition = SMessageUtil.getBodyItemValue(doc, "FROMSLOTPOSITION", false);
		String toSlotPosition = SMessageUtil.getBodyItemValue(doc, "TOSLOTPOSITION", false);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", false);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);
		String productType = SMessageUtil.getBodyItemValue(doc, "PRODUCTTYPE", false);

		Product productData = null;
		Lot lotData = null;
		String factoryName = "";
		String productSpecName = "";
		String productSpecVersion = "";
		String processFlowName = "";
		String processFlowVersion = "";
		String processOperationName = "";
		String processOperationVersion = "";
		String productionType = "";
		String productRequestName = "";

		// 1. Check Machine
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(unitName);
		
		// 2. Select product or lot data
		if (StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Unpacker))
		{
			factoryName = "ARRAY";
			productType = "Sheet";
		}
		else if (productType.equals(GenericServiceProxy.getConstantMap().ProductType_Panel))
		{
			lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
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
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ComponentInIndexer", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		ComponentHistory dataInfo = new ComponentHistory();
		dataInfo.setTimeKey(eventInfo.getEventTimeKey());
		dataInfo.setProductName(productName);
		dataInfo.setLotName(lotName);
		dataInfo.setEventName(eventInfo.getEventName());
		dataInfo.setToSlotId(StringUtils.trimToEmpty(toSlotId) == "" ? 0 : Integer.valueOf(toSlotId));
		dataInfo.setFromSlotId(StringUtils.trimToEmpty(fromSlotId) == "" ? 0 : Integer.valueOf(fromSlotId));
		dataInfo.setToSlotPosition(toSlotPosition);
		dataInfo.setFromSlotPosition(fromSlotPosition);
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
		dataInfo.setProductGrade(productGrade);
		dataInfo.setProductJudge(productJudge);
		dataInfo.setProductRequestName(productRequestName);

		ComponentHistory componentHistoryData = ExtendedObjectProxy.getComponentHistoryService().create(eventInfo, dataInfo);

		// insert Virtual Glass ID for Unpacker
		/*
		VirtualProductHistory Virtualproduct = new VirtualProductHistory();
		Virtualproduct.setEVENTNAME(eventInfo.getEventName());
		Virtualproduct.setTIMEKEY(TimeStampUtil.getCurrentEventTimeKey());
		Virtualproduct.setMACHINENAME(unitName);
		Virtualproduct.setPRODUCTNAME(productName);
		Virtualproduct.setEVENTUSER(eventInfo.getEventUser());
		VirtualProductHistory VirtualProductHistory = ExtendedObjectProxy.VirtualProductHistoryService().create(eventInfo, Virtualproduct);
		*/

		if (productData != null)
		{
			ProcessFlow processFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(productData);

			// Increase DummyUsedCount
			if (CommonUtil.equalsIn(processFlowData.getProcessFlowType(), "MQCPrepare", "MQC", "MQCRecycle"))
			{
				this.increamentDummyUseCount(eventInfo, productData);
			}
		}
	}
	
	private void increamentDummyUseCount( EventInfo eventInfo , Product productData) throws CustomException
	{
		
		String sql = " SELECT E.*  FROM CT_MQCPLANDETAIL_EXTENDED E , "
									+ "	(SELECT  MP.*  FROM CT_MQCPLANDETAIL  D , "
														+ " (SELECT  M.JOBNAME  ,P.* FROM CT_MQCPLAN M , "
																				     + " (SELECT L.FACTORYNAME , L.MACHINENAME ,  L.LOTNAME , P.PRODUCTNAME , L.PRODUCTSPECNAME , L.PRODUCTSPECVERSION , "
																				     + "         L.PROCESSFLOWNAME , L.PROCESSFLOWVERSION , L.PROCESSOPERATIONNAME , L.PROCESSOPERATIONVERSION "
																				     + "  FROM PRODUCT P , LOT L "
																				     + "  WHERE P.LOTNAME = L.LOTNAME "
																				     + "  AND P.PRODUCTNAME =:PRODUCTNAME ) P "
													    + " WHERE M.FACTORYNAME = P.FACTORYNAME "
										                + " AND M.PRODUCTSPECNAME = P.PRODUCTSPECNAME "
										                + " AND M.PRODUCTSPECVERSION = P.PRODUCTSPECVERSION"
										                + " AND M.PROCESSFLOWNAME = P.PROCESSFLOWNAME "
										                + " AND M.PROCESSFLOWVERSION = P.PROCESSFLOWVERSION "
										                + " AND M.LOTNAME = P.LOTNAME "
										                + " AND M.MQCSTATE = 'Released') MP"
						           + " WHERE D.PROCESSFLOWNAME = MP.PROCESSFLOWNAME "
						           + " AND D.PROCESSFLOWVERSION = MP.PROCESSFLOWVERSION "
						           + " AND D.PROCESSOPERATIONNAME = MP.PROCESSOPERATIONNAME "
						           + " AND D.PROCESSOPERATIONVERSION = MP.PROCESSOPERATIONVERSION "
						           + " AND D.LOTNAME = MP.LOTNAME "
						           + " AND D.MQCRELEASEFLAG = 'Y' AND D.COUNTINGFLAG = 'Y'"
						           + " AND D.JOBNAME = MP.JOBNAME  )   DMP "
	           + " WHERE  E.PROCESSFLOWNAME = DMP.PROCESSFLOWNAME "
	           + " AND E.PROCESSFLOWVERSION = DMP.PROCESSFLOWVERSION "
	           + " AND E.PROCESSOPERATIONNAME = DMP.PROCESSOPERATIONNAME "
	           + " AND E.PROCESSOPERATIONVERSION = DMP.PROCESSOPERATIONVERSION "
	           + " AND E.LOTNAME = DMP.LOTNAME "
	           + " AND E.PRODUCTNAME = DMP.PRODUCTNAME "
	           + " AND E.JOBNAME = DMP.JOBNAME ";
		
		List<Map<String,Object>> resultList = null;
		
		try{
			
			resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, new Object[]{productData.getKey().getProductName()});
		}
		catch(Exception ex)
		{
			if(!(ex instanceof NotFoundSignal))
			{
				throw new CustomException(ex.getCause());
			}
		}
		
		if(resultList==null || resultList.size() ==0) return ;
		MQCPlanDetail_Extended transDataInfo = (MQCPlanDetail_Extended) ExtendedObjectProxy.getMQCPlanDetail_ExtendedService().tranform(resultList).get(0);
		
		// MQCPlanDetail_Extended DummyUsedCount
		transDataInfo.setDummyUsedCount(transDataInfo.getDummyUsedCount().intValue() + 1);
		ExtendedObjectProxy.getMQCPlanDetail_ExtendedService().update(transDataInfo);
		
		// Product DummyUsedCount
		String dummyUsedCount = productData.getUdfs().get("DUMMYUSEDCOUNT");
		
		if(StringUtils.isEmpty(dummyUsedCount))
			dummyUsedCount = "0";
		
		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.getUdfs().put("DUMMYUSEDCOUNT", String.valueOf(Long.parseLong(dummyUsedCount) + 1));
		
		MESProductServiceProxy.getProductServiceImpl().setEvent(productData, setEventInfo, eventInfo);
	}
}
