package kr.co.aim.messolution.product.event;

import org.jdom.Document;

import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ComponentHistory;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.EnumInfoUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableHistory;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.machine.management.data.Machine;

public class ComponentPanelOutTray extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		ConstantMap constMap = GenericServiceProxy.getConstantMap();
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String panelName = SMessageUtil.getBodyItemValue(doc, "PANELNAME", true);
		String fromPosition = SMessageUtil.getBodyItemValue(doc, "FROMPOSITION", false);
		//String toPosition = SMessageUtil.getBodyItemValue(doc, "TOPOSITION", false);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", false);
		String trayName = SMessageUtil.getBodyItemValue(doc, "TRAYNAME", true);
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);	

		Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotDataForUpdate(panelName);
		String factoryName = lotData.getFactoryName();
		String productSpecName = lotData.getProductSpecName();
		String productSpecVersion = lotData.getProductSpecVersion();
		String processFlowName = lotData.getProcessFlowName();
		String processFlowVersion = lotData.getProcessFlowVersion();
		String processOperationName = lotData.getProcessOperationName();
		String processOperationVersion = lotData.getProcessOperationVersion();
		String productionType = lotData.getProductionType();
		String productRequestName = lotData.getProductRequestName();
        String machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(lotData.getFactoryName(),
				lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), 
				lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), machineName, true);
		//Insert to Component History
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ComponentPanelOutTray", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		ComponentHistory dataInfo = new ComponentHistory();
		dataInfo.setTimeKey(eventInfo.getEventTimeKey());
		dataInfo.setProductName(lotData.getKey().getLotName());
		dataInfo.setLotName("");
		dataInfo.setEventName(eventInfo.getEventName());
		dataInfo.setToSlotId(0);
		dataInfo.setFromSlotId(0);
		//dataInfo.setToSlotPosition(toPosition);
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
		
		// Deassign Panel
		EventInfo eventInfoDeassign = EventInfoUtil.makeEventInfo("DeassignPanel", this.getEventUser(), this.getEventComment());
		eventInfoDeassign.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		Lot oldLotData = (Lot) ObjectUtil.copyTo(lotData);
		
		//String trayName = lotData.getCarrierName();
		
		//TRAY-0023:BC report message error: reported panel [{0}] does not belong to tray [{1}].
		if (!lotData.getCarrierName().equals(trayName) && !EnumInfoUtil.SorterOperationCondition.BRMA.getOperationMode().equals(machineData.getUdfs().get("OPERATIONMODE")))
			throw new CustomException("TRAY-0023", panelName, trayName);

		lotData.setCarrierName("");
		lotData.setLastEventName(eventInfoDeassign.getEventName());
		lotData.setLastEventUser(eventInfoDeassign.getEventUser());
		lotData.setLastEventTime(eventInfoDeassign.getEventTime());
		lotData.setLastEventTimeKey(eventInfoDeassign.getEventTimeKey());
		lotData.setLastEventComment(eventInfoDeassign.getEventComment());
		lotData.getUdfs().put("POSITION", "");

		LotHistory lotHist = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLotData, lotData, new LotHistory());

		LotServiceProxy.getLotService().update(lotData);
		LotServiceProxy.getLotHistoryService().insert(lotHist);

		// Deassign panel from tray
		Durable trayData = MESDurableServiceProxy.getDurableServiceUtil().getDurableDataForUpdate(trayName);
		Durable oldDataInfo = (Durable) ObjectUtil.copyTo(trayData);

		long lotQty = trayData.getLotQuantity() - 1;

		if (lotQty >= 0)
		{
			trayData.setLotQuantity(lotQty);
		}
		else
		{
			// TRAY-0024:The tray LotQty does not match with the actual quantity of panels.
			throw new CustomException("TRAY-0024");
		}

		if (lotQty == 0)
			trayData.setDurableState(constMap.Dur_Available);

		trayData.setLastEventName(eventInfoDeassign.getEventName());
		trayData.setLastEventTimeKey(eventInfoDeassign.getEventTimeKey());
		trayData.setLastEventTime(eventInfoDeassign.getEventTime());
		trayData.setLastEventUser(eventInfoDeassign.getEventUser());
		trayData.setLastEventComment(eventInfoDeassign.getEventComment());

		DurableHistory durHistory = new DurableHistory();
		durHistory = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(oldDataInfo, trayData, durHistory);

		DurableServiceProxy.getDurableService().update(trayData);
		DurableServiceProxy.getDurableHistoryService().insert(durHistory);

	}
}