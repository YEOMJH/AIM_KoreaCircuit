package kr.co.aim.messolution.machine.event;

import java.util.HashMap;
import java.util.Map;

import org.jdom.Document;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.info.MakeMachineStateByStateInfo;

public class SubSubUnitStateChanged extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "SUBSUBUNITNAME", true);
		String RmachineName = SMessageUtil.getBodyItemValue(doc, "SUBSUBUNITNAME", true);
		String machineStateName = SMessageUtil.getBodyItemValue(doc, "SUBSUBUNITSTATENAME", false);
		String reasonCode = "";

		// check exist Machine
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		String oldMachineState = machineData.getMachineStateName();// old

		if (StringUtil.equals(machineStateName, "MANUAL"))
		{
			throw new CustomException("MACHINE-0035", machineStateName);
		}

		if (StringUtil.equals(machineData.getUdfs().get("CHANGESTATELOCKBYOIC"), "Y"))
		{
			throw new CustomException("MACHINE-0036", machineStateName, oldMachineState);
		}

		// if MACHINESTATAE is same, Pass
		if (StringUtil.equals(machineStateName, machineData.getMachineStateName()))
		{
			throw new CustomException("MACHINE-0001", machineName, machineData.getMachineStateName(), machineStateName);
		}

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeState", getEventUser(), getEventComment(), "", "");

		Map<String, String> machineUdfs = new HashMap<String, String>();
		if (machineStateName.equalsIgnoreCase("IDLE"))
		{
			machineUdfs.put("LASTIDLETIME", TimeStampUtil.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
			//machineUdfs.put("MACHINESUBSTATE", "R/D");
			//reasonCode = "I101-Run Down";
		}
		else
		{
			machineUdfs.put("LASTIDLETIME", "");
		}

		if (machineStateName.equalsIgnoreCase("RUN"))
		{
			machineUdfs.put("MACHINESUBSTATE", "RUN");
			//reasonCode = "RuN";
		}
		else if (machineStateName.equalsIgnoreCase("DOWN"))
		{
			machineUdfs.put("MACHINESUBSTATE", "Machine Down");
			//reasonCode = RmachineName;
		}

		eventInfo.setReasonCode(reasonCode);

		MakeMachineStateByStateInfo transitionInfo = MESMachineServiceProxy.getMachineInfoUtil().makeMachineStateByStateInfo(machineData, machineStateName);
		transitionInfo.setUdfs(machineUdfs);
		MESMachineServiceProxy.getMachineServiceImpl().makeMachineStateByState(machineData, transitionInfo, eventInfo);
      try {	// success then report to FMB
		GenericServiceProxy.getESBServive().sendBySenderToFMB(doc);

		// send to PMS
		GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("PMS"), doc, "PMSSender");
	
} catch (Exception e) {
	// TODO: handle exception
}
	

	}

}
