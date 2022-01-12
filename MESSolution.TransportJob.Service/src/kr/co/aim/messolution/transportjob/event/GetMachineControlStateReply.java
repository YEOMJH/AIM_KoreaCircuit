package kr.co.aim.messolution.transportjob.event;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.info.MakeCommunicationStateInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class GetMachineControlStateReply extends AsyncHandler {

	/**
	 * MessageSpec [MCS -> TEX -> OIC]
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

		MakeCommunicationStateInfo makeCommunicationStateInfo = MESMachineServiceProxy.getMachineInfoUtil().makeCommunicationStateInfo(machineData, machineControlState);
		MESMachineServiceProxy.getMachineServiceImpl().makeCommunicationState(machineData, makeCommunicationStateInfo, eventInfo);

		String originalSourceSubjectName = getOriginalSourceSubjectName();

		if (StringUtils.isNotEmpty(originalSourceSubjectName))
		{
			// Send Reply to OIC
			GenericServiceProxy.getESBServive().sendBySenderWithoutChangeReturnElement(originalSourceSubjectName, doc, "OICSender");
		}
	}
}
