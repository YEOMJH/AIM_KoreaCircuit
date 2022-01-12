package kr.co.aim.messolution.machine.event;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.info.SetEventInfo;

import org.jdom.Document;

public class ChangeMachineLockFlag extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String machineLockFlag = SMessageUtil.getBodyItemValue(doc, "MACHINELOCKFLAG", true);
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeMachineLockFlag", getEventUser(), getEventComment(), "", "");

		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.getUdfs().put("MACHINELOCKFLAG", machineLockFlag);

		MachineServiceProxy.getMachineService().setEvent(machineData.getKey(), eventInfo, setEventInfo);

		return doc;
	}

}
