package kr.co.aim.messolution.product.event;

import org.jdom.Document;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ComponentHistory;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.product.management.data.Product;

public class ComponentTurn extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String productName = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", true);
		String productJudge = SMessageUtil.getBodyItemValue(doc, "PRODUCTJUDGE", false);
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
		
		// 1. Select product or lot data
		if(productType.equals(GenericServiceProxy.getConstantMap().ProductType_Panel))
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

		// 2. Check Machine
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(unitName);

		// 3. Insert to Component History
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ComponentTurn", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

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

		ComponentHistory componentHistoryData = ExtendedObjectProxy.getComponentHistoryService().create(eventInfo, dataInfo);
		
	}
}
