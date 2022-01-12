package kr.co.aim.messolution.lot.event.Tray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableHistory;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.management.data.Port;

public class TrayProcessAbort extends AsyncHandler 
{
	private static Log log = LogFactory.getLog(TrayProcessAbort.class);

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String trayName = SMessageUtil.getBodyItemValue(doc, "TRAYNAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String portType = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", true);
		String portUseType = SMessageUtil.getBodyItemValue(doc, "PORTUSETYPE", false);
		String machineRecipeName = SMessageUtil.getBodyItemValue(doc, "MACHINERECIPENAME", false);
		List<Element> panelElementList = SMessageUtil.getBodySequenceItemList(doc, "PANELLIST", true);

		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
		Durable trayData = MESDurableServiceProxy.getDurableServiceUtil().getDurableDataForUpdate(trayName);
		
		if (panelElementList == null || panelElementList.size() == 0)
		{
			// TRAY-0028:No PanelList information received from BC.
			throw new CustomException("TRAY-0028");
		}

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("AssignTray", getEventUser(), getEventComment());

		List<Lot> updateLotList = new ArrayList<>(panelElementList.size());
		List<LotHistory> updateLotHistList = new ArrayList<>(panelElementList.size());

		Map<String, Element> panelElementMap = this.makePanelElementMap(panelElementList);
		List<Lot> lotDataList = MESLotServiceProxy.getLotServiceUtil().getLotDataListByPanelNameList(new ArrayList<String>(panelElementMap.keySet()), true);

		int pCount = 0 ;
		for (Lot lotData : lotDataList)
		{
			CommonValidation.checkLotProcessStateRun(lotData);

			Lot oldLotData = (Lot) ObjectUtil.copyTo(lotData);

			lotData.setCarrierName(trayName);
			lotData.setLastEventName(eventInfo.getEventName());
			lotData.setLastEventTimeKey(eventInfo.getEventTimeKey());
			lotData.setLastEventTime(eventInfo.getEventTime());
			lotData.setLastEventUser(eventInfo.getEventUser());
			lotData.setLastEventComment(eventInfo.getEventComment());

			// position need cim check
			lotData.getUdfs().put("POSITION", String.valueOf(++pCount));
			lotData.getUdfs().put("PORTNAME", portData.getKey().getPortName());
			lotData.getUdfs().put("PORTTYPE", portData.getUdfs().get("PORTTYPE"));
			lotData.getUdfs().put("PORTUSETYPE", portData.getUdfs().get("PORTUSETYPE"));

			LotHistory lotHistData = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLotData, lotData, new LotHistory());

			updateLotList.add(lotData);
			updateLotHistList.add(lotHistData);
		}

		try
		{
			CommonUtil.executeBatch("update", updateLotList, true);
			CommonUtil.executeBatch("insert", updateLotHistList, true);

			log.info(String.format("▶Successfully update %s pieces of Panels.", updateLotList.size()));
		}
		catch (Exception e)
		{
			log.error(e.getMessage());
			throw new CustomException(e.getCause());
		}

		Durable oldTrayData = (Durable) ObjectUtil.copyTo(trayData);

		trayData.setLotQuantity(panelElementMap.size());
		trayData.setDurableState(GenericServiceProxy.getConstantMap().Dur_InUse);
		trayData.setLastEventName(eventInfo.getEventName());
		trayData.setLastEventTimeKey(eventInfo.getEventTimeKey());
		trayData.setLastEventTime(eventInfo.getEventTime());
		trayData.setLastEventUser(eventInfo.getEventUser());
		trayData.setLastEventComment(eventInfo.getEventComment());

		trayData.getUdfs().put("MACHINENAME", machineName);
		trayData.getUdfs().put("PORTNAME", portName);

		DurableHistory durHistory = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(oldTrayData, trayData, new DurableHistory());

		DurableServiceProxy.getDurableService().update(trayData);
		DurableServiceProxy.getDurableHistoryService().insert(durHistory);

	}
	
	public Map<String, Element> makePanelElementMap(List<Element> panelElementList)
	{
		Map<String, Element> panelElementMap = new HashMap<>();

		for (Element panelEleemnt : panelElementList)
			panelElementMap.put(panelEleemnt.getChildText("PANELNAME"), panelEleemnt);

		return panelElementMap;
	}
}
