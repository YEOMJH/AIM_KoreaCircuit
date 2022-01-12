package kr.co.aim.messolution.lot.event.Tray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EnumInfoUtil;
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
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.PortServiceProxy;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.info.MakePortStateByStateInfo;
import kr.co.aim.greentrack.port.management.info.MakeTransferStateInfo;

public class PackingTrayProcessEnd extends AsyncHandler 
{
	private static Log log = LogFactory.getLog(PackingTrayProcessEnd.class);

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String portType = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", true);
		String portUseType = SMessageUtil.getBodyItemValue(doc, "PORTUSETYPE", false);
		String trayName = SMessageUtil.getBodyItemValue(doc, "TRAYNAME", true);
		String machineRecipeName = SMessageUtil.getBodyItemValue(doc, "MACHINERECIPENAME", false);
		List<Element> panelList = SMessageUtil.getBodySequenceItemList(doc, "PANELLIST", true);
		
		ConstantMap constMap =  GenericServiceProxy.getConstantMap();

		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
		Durable trayData = MESDurableServiceProxy.getDurableServiceUtil().getDurableDataForUpdate(trayName);

		List<Lot> updateLotList = new ArrayList<Lot>();
		List<LotHistory> updateLotHistList = new ArrayList<LotHistory>();

		Map<String, Element> panelElementMap = this.makePanelElementMap(panelList);
		List<Lot> lotDataList = MESLotServiceProxy.getLotServiceUtil().getLotDataListByPanelNameList(new ArrayList<String>(panelElementMap.keySet()), true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("PackingNG", getEventUser(), getEventComment());
		
		for (Lot lotData : lotDataList)
		{
			Lot oldLotData = (Lot) ObjectUtil.copyTo(lotData);

			lotData.setLastEventName(eventInfo.getEventName());
			lotData.setLastEventTimeKey(eventInfo.getEventTimeKey());
			lotData.setLastEventTime(eventInfo.getEventTime());
			lotData.setLastEventUser(eventInfo.getEventUser());
			lotData.setLastEventComment(eventInfo.getEventComment());
			lotData.setLotHoldState("Y");
			lotData.setMachineRecipeName(machineRecipeName);
			lotData.setMachineName(machineName);
			lotData.getUdfs().put("PORTNAME", portData.getKey().getPortName());
			lotData.getUdfs().put("PORTTYPE", portType);
			lotData.getUdfs().put("PORTUSETYPE", portUseType);

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
		
		//Durable Hold
		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.getUdfs().put("DURABLEHOLDSTATE", constMap.DURABLE_HOLDSTATE_Y);
		DurableServiceProxy.getDurableService().setEvent(trayData.getKey(), eventInfo, setEventInfo);
		
		//Change PortInfo
		makeTransferState(eventInfo, portData);
	}
	
	public Map<String, Element> makePanelElementMap(List<Element> panelElementList)
	{
		Map<String, Element> panelElementMap = new HashMap<>();

		for (Element panelEleemnt : panelElementList)
			panelElementMap.put(panelEleemnt.getChildText("PANELNAME"), panelEleemnt);

		return panelElementMap;
	}
	
	private void makeTransferState(EventInfo eventInfo, Port portData)
	{
		MakeTransferStateInfo makeTransferStateInfo = new MakeTransferStateInfo();
		makeTransferStateInfo.setTransferState(GenericServiceProxy.getConstantMap().Port_Processing);
		makeTransferStateInfo.setValidateEventFlag("N");
		
		Map<String, String> udfs = new HashMap<String, String>();
		udfs.put("FULLSTATE", "FULL");
		
		makeTransferStateInfo.setUdfs(udfs);
		
		PortServiceProxy.getPortService().makeTransferState(portData.getKey(), eventInfo, makeTransferStateInfo);			
	}
}
