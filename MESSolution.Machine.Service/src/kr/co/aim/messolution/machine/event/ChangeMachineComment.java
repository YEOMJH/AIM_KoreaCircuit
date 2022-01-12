package kr.co.aim.messolution.machine.event;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.info.SetEventInfo;

import org.jdom.Document;

public class ChangeMachineComment extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String sMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeEventComment", this.getEventUser(), this.getEventComment(), "", "");

		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(sMachineName);

		SetEventInfo setEventInfo = new SetEventInfo();
		
        //Start houxk20210318
		eventInfo.setReasonCode(machineData.getReasonCode());
		eventInfo.setReasonCodeType(machineData.getReasonCodeType());
		//End
		
		MESMachineServiceProxy.getMachineServiceImpl().setEvent(machineData, setEventInfo, eventInfo);

		return doc;
	}

}
