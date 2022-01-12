package kr.co.aim.messolution.machine.event;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.info.SetEventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

public class DspModeChangeReport extends AsyncHandler {

	Log log = LogFactory.getLog(this.getClass());
	
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String dspFlag = SMessageUtil.getBodyItemValue(doc, "DSPFLAG", true);
		
		Machine machineData    = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("DspModeChangeReport", getEventUser(), "", "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		String sDspMode = machineData.getUdfs().get("OPERATIONMODE");

		if (sDspMode.equals(dspFlag))
		{
			log.info("dspFlag is same so do not changed");
		}
		else
		{
			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.getUdfs().put("OPERATIONMODE", dspFlag);
			
			MESMachineServiceProxy.getMachineServiceImpl().setEvent(machineData, setEventInfo, eventInfo);
		}
	}
}
