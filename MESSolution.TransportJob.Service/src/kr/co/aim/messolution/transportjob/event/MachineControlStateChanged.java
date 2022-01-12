package kr.co.aim.messolution.transportjob.event;

import org.jdom.Document;

import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.management.info.MakeCommunicationStateInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;

public class MachineControlStateChanged extends AsyncHandler {

	/**
	 * MessageSpec [MCS -> TEX -> FMC]
	 * 
	 * <Body>
	 *    <MACHINENAME />
	 *    <MACHINECONTROLSTATE />
	 * </Body>
	 */
	
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeCommState", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		// Validation : Exist Carrier
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String machineControlState = SMessageUtil.getBodyItemValue(doc, "MACHINECONTROLSTATE", true);

		if (machineControlState.equals("REMOTE"))
		{
			machineControlState = "OnLineRemote";
		}
		else if (machineControlState.equals("LOCAL"))
		{
			machineControlState = "OnLineLocal";
		}
		else if (machineControlState.equals("OFFLINE"))
		{
			machineControlState = "OffLine";
		}

		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);

		MakeCommunicationStateInfo makeMachineControlStateInfo = MESMachineServiceProxy.getMachineInfoUtil().makeCommunicationStateInfo(machineData, machineControlState);
		MESMachineServiceProxy.getMachineServiceImpl().makeCommunicationState(machineData, makeMachineControlStateInfo, eventInfo);

		try
		{
			// success then report to FMB
			GenericServiceProxy.getESBServive().sendBySenderToFMB(doc);
		}
		catch (Exception ex)
		{
			eventLog.warn("FMC Report Failed!");
		}
	}
}