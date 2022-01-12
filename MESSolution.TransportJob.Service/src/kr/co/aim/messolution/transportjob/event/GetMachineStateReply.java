package kr.co.aim.messolution.transportjob.event;

import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.info.MakeMachineStateByStateInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class GetMachineStateReply extends AsyncHandler {

	/**
	 * MessageSpec [MCS -> TEX -> OIC]
	 * 
	 * <Body>
	 *    <MACHINENAME />
	 *    <MACHINESTATE />
	 *    <FULLSTATE />
	 * </Body>
	 */
	
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeState", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String machineState = SMessageUtil.getBodyItemValue(doc, "MACHINESTATE", true);
		String fullState = SMessageUtil.getBodyItemValue(doc, "FULLSTATE", false);

		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);

		// if MACHINESTATAE is same, Pass
		if (StringUtil.equals(machineState, machineData.getMachineStateName()))
		{
			throw new CustomException("MACHINE-0001", machineName, machineData.getMachineStateName(), machineState);
		}

		Map<String, String> machineUdfs = new HashMap<String, String>();

		if (!StringUtil.equals(machineData.getUdfs().get("FULLSTATE"), fullState))
		{
			machineUdfs.put("FULLSTATE", fullState);
		}

		MakeMachineStateByStateInfo makeMachineStateByStateInfo = MESMachineServiceProxy.getMachineInfoUtil().makeMachineStateByStateInfo(machineData, machineState);
		makeMachineStateByStateInfo.setUdfs(machineUdfs);
		MESMachineServiceProxy.getMachineServiceImpl().makeMachineStateByState(machineData, makeMachineStateByStateInfo, eventInfo);

		String originalSourceSubjectName = getOriginalSourceSubjectName();

		if (StringUtils.isNotEmpty(originalSourceSubjectName))
		{
			// Send Reply to OIC
			GenericServiceProxy.getESBServive().sendBySenderWithoutChangeReturnElement(originalSourceSubjectName, doc, "OICSender");
		}
	}
}
