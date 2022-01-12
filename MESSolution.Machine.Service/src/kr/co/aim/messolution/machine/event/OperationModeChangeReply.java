package kr.co.aim.messolution.machine.event;

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
import kr.co.aim.greentrack.machine.management.info.MakeCommunicationStateInfo;
import kr.co.aim.greentrack.machine.management.info.MakeMachineStateByStateInfo;

public class OperationModeChangeReply extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		// MACHINENAME
		// OPERATIONMODENAME
		// RESULT
		// RESULTDESCRIPTION

		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String machineStateName = SMessageUtil.getBodyItemValue(doc, "OPERATIONMODENAME", true);
		String result = SMessageUtil.getBodyItemValue(doc, "RESULT", true);
		String resultDesc = SMessageUtil.getBodyItemValue(doc, "RESULTDESCRIPTION", false);

		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);

		// For send to OIC
		String originalSourceSubjectName = getOriginalSourceSubjectName();

		// Send Reply to OIC
		GenericServiceProxy.getESBServive().sendBySenderWithoutChangeReturnElement(originalSourceSubjectName, doc, "OICSender");
	}

}
