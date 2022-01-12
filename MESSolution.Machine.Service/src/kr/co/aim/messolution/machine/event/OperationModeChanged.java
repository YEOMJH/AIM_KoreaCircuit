package kr.co.aim.messolution.machine.event;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.info.SetEventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

public class OperationModeChanged extends AsyncHandler {
	private static Log log = LogFactory.getLog(OperationModeChanged.class);

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String operationMode = SMessageUtil.getBodyItemValue(doc, "OPERATIONMODENAME", true);
		String operationModeDesc = SMessageUtil.getBodyItemValue(doc, "OPERATIONMODEDESCRIPTION", false);

		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeOperationMode", getEventUser(), operationModeDesc, "", "");

		String sOperationMode = machineData.getUdfs().get("OPERATIONMODE");

		if (sOperationMode.equals(operationMode))
		{
			log.info("OperationMode is same so do not changed");
		}
		else
		{
			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.getUdfs().put("OPERATIONMODE", operationMode);
			
			MESMachineServiceProxy.getMachineServiceImpl().setEvent(machineData, setEventInfo, eventInfo);
		}
	}
}
