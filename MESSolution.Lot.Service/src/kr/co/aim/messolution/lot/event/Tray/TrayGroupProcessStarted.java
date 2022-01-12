package kr.co.aim.messolution.lot.event.Tray;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableHistory;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.management.data.Port;

public class TrayGroupProcessStarted extends AsyncHandler 
{
	private static Log log = LogFactory.getLog(TrayGroupProcessStarted.class);

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", false);
		String portType = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", false);
		String portUseType = SMessageUtil.getBodyItemValue(doc, "PORTUSETYPE", false);
		String trayGroupName = SMessageUtil.getBodyItemValue(doc, "TRAYGROUPNAME", true);
		
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);

		Durable trayGroupData = MESDurableServiceProxy.getDurableServiceUtil().getDurableDataForUpdate(trayGroupName);
		List<Durable> trayList = MESDurableServiceProxy.getDurableServiceUtil().getSubTrayListByCoverTray(trayGroupName,true);
		
		CommonValidation.checkMachineHold(machineData);
		CommonValidation.CheckDurableState(trayGroupData);
		CommonValidation.CheckDurableHoldState(trayGroupData);
		
		// message from PL, BL ?? PU Port 
		if(!trayGroupData.getDurableType().equals("CoverTray"))
			throw new CustomException("DURABLE-0009", trayGroupName);
		
		// Deassign Tray from TrayGroup
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeassignTrayGroup", getEventUser(), getEventComment());
		
		List<Durable> updateDeassignTrayGroupList = new ArrayList<>();
		List<DurableHistory> updateDeassignTrayGroupHistList = new ArrayList<>();
		
		for (Durable durableData : trayList) 
		{
			CommonValidation.CheckDurableState(durableData);
			CommonValidation.CheckDurableHoldState(durableData);
			//CommonValidation.CheckDurableCleanState(durableData);
			
			Durable oldDurableData = (Durable) ObjectUtil.copyTo(durableData);
					
			// Deassign Tray group
			durableData.getUdfs().put("COVERNAME", "");
			durableData.getUdfs().put("POSITION", "");
			durableData.getUdfs().put("BCRFLAG", "N");
			durableData.getUdfs().put("DURABLETYPE1", "Tray");
			durableData.setLastEventName(eventInfo.getEventName());
			durableData.setLastEventTimeKey(eventInfo.getEventTimeKey());
			durableData.setLastEventTime(eventInfo.getEventTime());
			durableData.setLastEventUser(eventInfo.getEventUser());
			durableData.setLastEventComment(eventInfo.getEventComment());
			
			DurableHistory durableHist = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(oldDurableData, durableData, new DurableHistory());
			
			updateDeassignTrayGroupList.add(durableData);
			updateDeassignTrayGroupHistList.add(durableHist);
		}
		
		if (updateDeassignTrayGroupList.size() > 0)
		{
			try
			{
				CommonUtil.executeBatch("update", updateDeassignTrayGroupList, true);
				CommonUtil.executeBatch("insert", updateDeassignTrayGroupHistList, true);

				log.info(String.format("▶Successfully update %s pieces of trays data。(From Port: %s )", updateDeassignTrayGroupList.size(), portType));
			}
			catch (Exception e)
			{
				log.error(e.getMessage());
				throw new CustomException(e.getCause());
			}
		}
		// TrayGroup initialize
		trayGroupData.setDurableType("Tray");
		trayGroupData.setLotQuantity(0);
		trayGroupData.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);
		DurableServiceProxy.getDurableService().update(trayGroupData);
		
		SetEventInfo setEventInfo = new SetEventInfo();
		
		setEventInfo.getUdfs().put("COVERNAME", "");
		setEventInfo.getUdfs().put("POSITION", "");
		setEventInfo.getUdfs().put("FIRSTRUNQTY", "0");
		setEventInfo.getUdfs().put("BCRFLAG", "N");
		setEventInfo.getUdfs().put("DURABLETYPE1", "Tray");
		
		DurableServiceProxy.getDurableService().setEvent(trayGroupData.getKey(), eventInfo, setEventInfo);
	}

}
