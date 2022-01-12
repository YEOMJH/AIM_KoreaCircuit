package kr.co.aim.messolution.product.event;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ComponentHistory;
import kr.co.aim.messolution.extended.object.management.data.MaterialProduct;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.info.DecrementQuantityInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.product.management.data.Product;

public class ComponentOutIndexer extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {

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
		
		// 1.Machine data check
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);

		// 2. Select product or lot data
		if (StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Unpacker))
		{
			factoryName = "ARRAY";
			productType = "Sheet";
		}
		else if (productType.equals(GenericServiceProxy.getConstantMap().ProductType_Panel))
		{
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
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ComponentOutIndexer", getEventUser(), getEventComment(), null, null);
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

		ExtendedObjectProxy.getComponentHistoryService().create(eventInfo,dataInfo);
		
		// decrement product quentity
		//Mark By yueke 20210624 ->Change To UnpackProcessEnd case by EQP Report NG
		/*
		if (StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Unpacker))
		{
			this.decreaseCrateQuantity(eventInfo, lotName, carrierName, Double.valueOf(1),productName,machineName);
		}*/

	}
	
	private void decreaseCrateQuantity(EventInfo eventInfo, String lotName, String consumableName, 
			double quantity,String productName,String machineName) throws CustomException
	{
		eventInfo.setEventName("Consume");
		Map<String, String> udfs = new HashMap<String, String>();
		Consumable consumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(consumableName);
		Lot lotData= MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
		
		DecrementQuantityInfo transitionInfo = MESConsumableServiceProxy.getConsumableInfoUtil().decrementQuantityInfo(lotName, "", "", "",
				TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()), quantity, udfs);

		MESConsumableServiceProxy.getConsumableServiceImpl().decrementQuantity(consumableData, transitionInfo, eventInfo);

		MaterialProduct dataInfo = new MaterialProduct();
		dataInfo.setTimeKey(eventInfo.getEventTimeKey());
		dataInfo.setProductName(productName);
		dataInfo.setLotName(lotName);
		dataInfo.setMaterialKind(GenericServiceProxy.getConstantMap().MaterialKind_Consumable);
		dataInfo.setMaterialType(consumableData.getConsumableType());
		dataInfo.setMaterialName(consumableData.getKey().getConsumableName());
		dataInfo.setQuantity(1);
		dataInfo.setEventName(eventInfo.getEventName());
		dataInfo.setEventTime(eventInfo.getEventTime());
		dataInfo.setFactoryName(lotData.getFactoryName());
		dataInfo.setProductSpecName(lotData.getProductSpecName());
		dataInfo.setProductSpecVersion(lotData.getProductSpecVersion());
		dataInfo.setProcessFlowName(lotData.getProcessFlowName());
		dataInfo.setProcessFlowVersion(lotData.getProcessFlowVersion());
		dataInfo.setProcessOperationName(lotData.getProcessOperationName());

		dataInfo.setProcessOperationVersion(lotData.getProcessOperationVersion());
		dataInfo.setMachineName(machineName);
		dataInfo.setMaterialLocationName("");

		ExtendedObjectProxy.getMaterialProductService().create(eventInfo, dataInfo);
	}
}
