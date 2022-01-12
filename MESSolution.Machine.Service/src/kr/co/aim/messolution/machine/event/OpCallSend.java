package kr.co.aim.messolution.machine.event;

import org.jdom.Document;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;

public class OpCallSend extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String opCallDescription = SMessageUtil.getBodyItemValue(doc, "OPCALLDESCRIPTION", true);

		String subjectName = "";

		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		subjectName = machineData.getUdfs().get("MCSUBJECTNAME");

		GenericServiceProxy.getESBServive().sendBySender(subjectName, doc, "EISSender");
	}
}
