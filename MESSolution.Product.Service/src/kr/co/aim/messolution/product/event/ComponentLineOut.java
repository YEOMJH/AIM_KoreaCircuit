package kr.co.aim.messolution.product.event;

import org.apache.commons.collections.functors.StringValueTransformer;
import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ComponentHistory;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.product.management.data.Product;

public class ComponentLineOut extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
		String subUnitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", false);
		String productName = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", true);
		
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
		String productGrade ="";
		//Packing
		Lot lotData = null;
		
		// 1. Check Machine
	    Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(StringUtil.isEmpty(subUnitName)?unitName:subUnitName);

		// 2. Select product or lot data
		if (StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Unpacker))
		{
			factoryName = "ARRAY";
			productType = "Sheet";
		}
		else if(StringUtil.equals(machineData.getFactoryName(), "POSTCELL"))
		{
			lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(productName);
			factoryName = lotData.getFactoryName();
			productSpecName = lotData.getProductSpecName();
			productSpecVersion = lotData.getProductSpecVersion();
			processFlowName = lotData.getProcessFlowName();
			processFlowVersion = lotData.getProcessFlowVersion();
			processOperationName = lotData.getProcessOperationName();
			processOperationVersion = lotData.getProcessOperationVersion();
			productionType = lotData.getProductionType();
			productRequestName = lotData.getProductRequestName();
			productType = lotData.getProductType();
			productGrade = lotData.getLotGrade();
			productName = lotData.getKey().getLotName();
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
			productGrade = productData.getProductGrade();
		}
		
		// 3. Insert to Component History
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ComponentLineOut", getEventUser(), getEventComment(), null, null);
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
		dataInfo.setMaterialLocationName(machineData.getKey().getMachineName());
		dataInfo.setProductGrade(productGrade);
		dataInfo.setProductRequestName(productRequestName);

		ComponentHistory componentHistoryData = ExtendedObjectProxy.getComponentHistoryService().create(eventInfo, dataInfo);
		
		if(StringUtil.equals(factoryName, "POSTCELL"))
		{
			//String reasonCode = "";
			//String reasonCodeType = "";
			
			eventInfo.setEventName("PanelHold");

			lotData.setLotHoldState("Y");
			//eventInfo.setReasonCode(reasonCode);
			//eventInfo.setReasonCodeType(reasonCodeType);
			lotData.setLastEventComment(eventInfo.getEventComment());
			lotData.setLastEventName(eventInfo.getEventName());
			lotData.setLastEventTime(eventInfo.getEventTime());
			lotData.setLastEventUser(eventInfo.getEventUser());
			lotData.setLastEventTimeKey(eventInfo.getEventTimeKey());
			SetEventInfo setEventInfo = new SetEventInfo();
			
			LotServiceProxy.getLotService().update(lotData);
			LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
		}
	}
}
