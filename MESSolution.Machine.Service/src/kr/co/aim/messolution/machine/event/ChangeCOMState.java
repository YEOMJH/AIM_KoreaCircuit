package kr.co.aim.messolution.machine.event;

import org.jdom.Document;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.info.MakeCommunicationStateInfo;

public class ChangeCOMState extends SyncHandler {

	@Override
	public Document doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String communicationName = SMessageUtil.getBodyItemValue(doc, "COMMUNICATIONSTATE", true);
		String reasonCodeType = SMessageUtil.getBodyItemValue(doc, "REASONCODETYPE", false);
		String reasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODE", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeCommState", getEventUser(), getEventComment(), reasonCodeType, reasonCode);

		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);

		MakeCommunicationStateInfo transitionInfo = MESMachineServiceProxy.getMachineInfoUtil().makeCommunicationStateInfo(machineData, communicationName);

		MESMachineServiceProxy.getMachineServiceImpl().makeCommunicationState(machineData, transitionInfo, eventInfo);

		// success then report to FMB
		GenericServiceProxy.getESBServive().sendBySenderToFMB(doc);

		return doc;
	}
}
