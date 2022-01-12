package kr.co.aim.messolution.machine.event;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.info.SetEventInfo;

import org.jdom.Document;

public class ChangeMachineIdleTime extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String lastIdleTime = SMessageUtil.getBodyItemValue(doc, "LASTIDLETIME", true);

		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeMachineIdleTimeInfo", getEventUser(), getEventComment(), "", "");
		
		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.getUdfs().put("LASTIDLETIME", lastIdleTime);
		
		MESMachineServiceProxy.getMachineServiceImpl().setEvent(machineData, setEventInfo , eventInfo);
		
		return doc;
	}
}
