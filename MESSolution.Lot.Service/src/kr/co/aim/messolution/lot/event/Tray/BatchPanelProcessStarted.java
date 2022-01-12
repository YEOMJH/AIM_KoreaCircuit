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
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.management.data.Port;

public class BatchPanelProcessStarted extends AsyncHandler 
{
	private static Log log = LogFactory.getLog(BatchPanelProcessStarted.class);

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String machineRecipeName = SMessageUtil.getBodyItemValue(doc, "MACHINERECIPENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", false);
		String trayName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		String trayPosition = SMessageUtil.getBodyItemValue(doc, "POSITION", true);
		List<Element> panelElementList = SMessageUtil.getBodySequenceItemList(doc, "PANELLIST", true);

		ConstantMap constMap = GenericServiceProxy.getConstantMap();
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
		Durable trayData = MESDurableServiceProxy.getDurableServiceUtil().getDurableDataForUpdate(trayName);
		
		CommonValidation.checkMachineHold(machineData);
		CommonValidation.CheckDurableState(trayData);
		CommonValidation.CheckDurableHoldState(trayData);
		//CommonValidation.CheckDurableCleanState(trayData);
		
		Map<String, Element> panelElementMap = makePanelElementMap(panelElementList);
		List<Lot> panelList = MESLotServiceProxy.getLotServiceUtil().getLotDataListByPanelNameList(new ArrayList<String>(panelElementMap.keySet()), true);
		
		List<Lot> updatePanelList = new ArrayList<Lot>();
		List<LotHistory> updatePanelHistoryList = new ArrayList<LotHistory>();
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TrackIn", this.getEventUser(), this.getEventComment());
		
		for(Lot panelData : panelList)
		{
			Lot oldPanelData = (Lot) ObjectUtil.copyTo(panelData);
			
			CommonValidation.checkLotState(panelData);
			CommonValidation.checkLotProcessState(panelData);
			CommonValidation.checkLotHoldState(panelData);
			
			panelData.setLotState(constMap.Lot_Released);
			panelData.setLotProcessState(constMap.Lot_LoggedIn);
			panelData.setMachineName(machineName);
			panelData.setMachineRecipeName(machineRecipeName);
			panelData.setLastLoggedInTime(eventInfo.getEventTime());
			panelData.setLastLoggedInUser(eventInfo.getEventUser());
			panelData.setLastEventName(eventInfo.getEventName());
			panelData.setLastEventUser(eventInfo.getEventUser());
			panelData.setLastEventFlag(constMap.FLAG_N);
			panelData.setLastEventTime(eventInfo.getEventTime());
			panelData.setLastEventTimeKey(eventInfo.getEventTimeKey());
			panelData.setLastEventComment(eventInfo.getEventComment());
	        
			panelData.getUdfs().put("PORTNAME", portData.getKey().getPortName());
			panelData.getUdfs().put("PORTTYPE", portData.getUdfs().get("PORTTYPE"));
			panelData.getUdfs().put("PORTUSETYPE", portData.getUdfs().get("PORTUSETYPE"));
			
			LotHistory lotHistory = new LotHistory();
			lotHistory = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldPanelData, panelData, lotHistory);
			
			updatePanelList.add(panelData);
			updatePanelHistoryList.add(lotHistory);
		}
		
		if(updatePanelList.size() > 0)
		{
			log.debug("Insert Lot, LotHistory");
			try
			{
				CommonUtil.executeBatch("update", updatePanelList);
				CommonUtil.executeBatch("insert", updatePanelHistoryList);
			}
			catch (Exception e)
			{
				log.error(e.getMessage());
				throw new CustomException(e.getCause());
			}
		}
	}
	
	public Map<String, Element> makePanelElementMap(List<Element> panelElementList)
	{
		Map<String, Element> panelElementMap = new HashMap<>();

		for (Element panelEleemnt : panelElementList)
			panelElementMap.put(panelEleemnt.getChildText("PANELNAME"), panelEleemnt);

		return panelElementMap;
	}
}
