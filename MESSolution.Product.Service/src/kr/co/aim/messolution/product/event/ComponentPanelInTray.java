package kr.co.aim.messolution.product.event;

import org.jdom.Document;

import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ComponentHistory;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;

public class ComponentPanelInTray extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String panelName = SMessageUtil.getBodyItemValue(doc, "PANELNAME", true);
		String fromPosition = SMessageUtil.getBodyItemValue(doc, "FROMPOSITION", false);
		String toPosition = SMessageUtil.getBodyItemValue(doc, "TOPOSITION", false);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", false);
		String trayName = SMessageUtil.getBodyItemValue(doc, "TRAYNAME", false);

		Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(panelName);
		String factoryName = lotData.getFactoryName();
		String productSpecName = lotData.getProductSpecName();
		String productSpecVersion = lotData.getProductSpecVersion();
		String processFlowName = lotData.getProcessFlowName();
		String processFlowVersion = lotData.getProcessFlowVersion();
		String processOperationName = lotData.getProcessOperationName();
		String processOperationVersion = lotData.getProcessOperationVersion();
		String productionType = lotData.getProductionType();
		String productRequestName = lotData.getProductRequestName();

		//Insert to Component History
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ComponentPanelInTray", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		ComponentHistory dataInfo = new ComponentHistory();
		dataInfo.setTimeKey(eventInfo.getEventTimeKey());
		dataInfo.setProductName(lotData.getKey().getLotName());
		dataInfo.setLotName("");
		dataInfo.setEventName(eventInfo.getEventName());
		dataInfo.setToSlotId(0);
		dataInfo.setFromSlotId(0);
		dataInfo.setToSlotPosition(toPosition);
		dataInfo.setFromSlotPosition(fromPosition);
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
		dataInfo.setProductType("Panel");
		dataInfo.setMachineName(machineName);
		dataInfo.setMaterialLocationName("");
		dataInfo.setProductGrade(lotData.getLotGrade());
		dataInfo.setProductRequestName(productRequestName);

		ComponentHistory componentHistoryData = ExtendedObjectProxy.getComponentHistoryService().create(eventInfo, dataInfo);
		
	}
}

