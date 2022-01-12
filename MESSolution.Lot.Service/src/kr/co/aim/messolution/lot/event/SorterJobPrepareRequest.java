package kr.co.aim.messolution.lot.event;

import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;

public class SorterJobPrepareRequest extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		// MACHINENAME
		// PORTLIST
		// PORT
		// PORTNAME
		// CARRIERNAME

		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String jobName = SMessageUtil.getBodyItemValue(doc, "JOBNAME", true);

		// get line machine
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);

		String targetSubjectName = CommonUtil.getValue(machineData.getUdfs(), "MCSUBJECTNAME");

		GenericServiceProxy.getESBServive().sendBySender(targetSubjectName, doc, "EISSender");
	}

}
