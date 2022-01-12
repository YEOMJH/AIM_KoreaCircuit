package kr.co.aim.messolution.machine.event;

import java.util.HashMap;
import java.util.Map;

import org.jdom.Document;

import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.info.SetEventInfo;

public class SubjectReport extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String sMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String sSubjectName = SMessageUtil.getBodyItemValue(doc, "SUBJECTNAME", true);

		SetEventInfo setInfo = new SetEventInfo();
		setInfo.getUdfs().put("MCSUBJECTNAME", sSubjectName);
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(sMachineName);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("AssignSubject", getEventUser(), getEventComment(), "", "");

		MESMachineServiceProxy.getMachineServiceImpl().setEvent(machineData, setInfo, eventInfo);
	}
}
