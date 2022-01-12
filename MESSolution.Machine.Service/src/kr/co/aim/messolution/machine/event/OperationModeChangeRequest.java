package kr.co.aim.messolution.machine.event;

import org.jdom.Document;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;

public class OperationModeChangeRequest extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{

		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);

		// from PEX to EQP
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);

		if (!machineData.getMachineStateName().equals(GenericServiceProxy.getConstantMap().Mac_Idle))
		{
			throw new CustomException("MACHINE-0007", machineData.getKey().getMachineName());
		}

		// MES-EAP protocol
		SMessageUtil.setHeaderItemValue(doc, "EVENTUSER", machineData.getKey().getMachineName());

		String targetSubjectName = CommonUtil.getValue(machineData.getUdfs(), "MCSUBJECTNAME");

		GenericServiceProxy.getESBServive().sendBySender(targetSubjectName, doc, "EISSender");
	}

}
