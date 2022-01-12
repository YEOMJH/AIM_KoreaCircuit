package kr.co.aim.messolution.machine.event;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.info.SetEventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

public class SoftwareVersionChangeReport extends AsyncHandler {

	Log log = LogFactory.getLog(this.getClass());
	
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String softwareVersion = SMessageUtil.getBodyItemValue(doc, "SOFTWAREVERSION", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeVersion", getEventUser(), getEventComment(), "", "");
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(StringUtil.isEmpty(unitName)?machineName:unitName);

		//change machine Software version
		if (machineData.getUdfs().get("SOFTWAREVERSION").equals(softwareVersion))
		{
			log.info("Software version is same so do not changed");
		}
		else
		{
			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.getUdfs().put("SOFTWAREVERSION", softwareVersion);
			
			MESMachineServiceProxy.getMachineServiceImpl().setEvent(machineData, setEventInfo, eventInfo);
		}

		//To-do send to AMS: need cim confirm
	}
}
