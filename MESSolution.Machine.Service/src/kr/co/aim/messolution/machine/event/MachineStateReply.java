package kr.co.aim.messolution.machine.event;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
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
import kr.co.aim.greentrack.machine.management.info.MakeCommunicationStateInfo;
import kr.co.aim.greentrack.machine.management.info.MakeMachineStateByStateInfo;

public class MachineStateReply extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String machineStateName = SMessageUtil.getBodyItemValue(doc, "MACHINESTATENAME", true);
		String communicationState = SMessageUtil.getBodyItemValue(doc, "COMMUNICATIONSTATE", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeState", getEventUser(), getEventComment(), "", "");

		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);

		// if MACHINESTATAE and COMMUNICATIONSTATE is same, Pass
		if (StringUtil.equals(machineStateName, machineData.getMachineStateName()) && StringUtil.equals(communicationState, machineData.getCommunicationState()))
		{
			eventLog.info("Machine[" + machineName + "] already is State[" + machineStateName + ", " + communicationState + "] so cannot change");
		}

		// if Current MACHINESTATENAME = 'ENG' then cannot change state (Only can change at OIC)
		if (StringUtil.equals(machineData.getMachineStateName(), "ENG") && (!StringUtil.equals(machineStateName, "ENG") && !StringUtil.equals(machineStateName, "DOWN")))
		{
			eventLog.info("MACHINESTATENAME = 'ENG' then cannot change state (Only can change at OIC)");
			// throw new CustomException("MACHINE-9006");
		}

		// if Current MACHINESTATENAME = 'TEST' then cannot change state (Only can change at OIC)
		if (StringUtil.equals(machineData.getMachineStateName(), "TEST") && (!StringUtil.equals(machineStateName, "TEST") && !StringUtil.equals(machineStateName, "DOWN")))
		{
			eventLog.info("Current MACHINESTATENAME = 'TEST' then cannot change state (Only can change at OIC)");
			// throw new CustomException("MACHINE-9007");
		}

		Map<String, String> machineUdfs = new HashMap<String, String>();
		if (StringUtils.equals(machineStateName, "IDLE"))
		{
			machineUdfs.put("LASTIDLETIME", TimeStampUtil.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		}
		else
		{
			machineUdfs.put("LASTIDLETIME", "");
		}

		// Change machineState
		if (!StringUtil.equals(machineStateName, machineData.getMachineStateName()))
		{
			MakeMachineStateByStateInfo makeMachineStateByStateInfo = MESMachineServiceProxy.getMachineInfoUtil().makeMachineStateByStateInfo(machineData, machineStateName);
			makeMachineStateByStateInfo.setUdfs(machineUdfs);
			MESMachineServiceProxy.getMachineServiceImpl().makeMachineStateByState(machineData, makeMachineStateByStateInfo, eventInfo);
		}

		// Change communicationState
		if (!StringUtil.equals(communicationState, machineData.getCommunicationState()))
		{
			MakeCommunicationStateInfo makeCommunicationStateInfo = MESMachineServiceProxy.getMachineInfoUtil().makeCommunicationStateInfo(machineData, communicationState);
			makeCommunicationStateInfo.setUdfs(machineUdfs);
			MESMachineServiceProxy.getMachineServiceImpl().makeCommunicationState(machineData, makeCommunicationStateInfo, eventInfo);
		}

		// success then report to FMB
		GenericServiceProxy.getESBServive().sendBySenderToFMB(doc);

		// Send Reply to OIC
		String originalSourceSubjectName = getOriginalSourceSubjectName();
		GenericServiceProxy.getESBServive().sendBySenderWithoutChangeReturnElement(originalSourceSubjectName, doc, "OICSender");
	}

}
