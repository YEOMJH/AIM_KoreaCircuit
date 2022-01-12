package kr.co.aim.messolution.durable.event.CNX;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.info.SetEventInfo;

public class ChangeEVAStageFlag extends SyncHandler {
	private static Log log = LogFactory.getLog(ChangeSheetState.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		Element eleBody = SMessageUtil.getBodyElement(doc);
		if (eleBody != null)
		{
			for (Element eledur : SMessageUtil.getBodySequenceItemList(doc, "EVALIST", false))
			{
				String machineName = SMessageUtil.getChildText(eledur, "MACHINENAME", true);
				String stageFlag = SMessageUtil.getChildText(eledur, "STAGEUSEFLAG", false);
				Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
				EventInfo eventInfo = new EventInfo();
				eventInfo = EventInfoUtil.makeEventInfo("ChangeEVAStageFlag", getEventUser(), getEventComment(), "", "");
				SetEventInfo setEventInfo = new SetEventInfo();
				setEventInfo.getUdfs().put("STAGEUSEFLAG", stageFlag);
				MachineServiceProxy.getMachineService().setEvent(machineData.getKey(), eventInfo, setEventInfo);
			}

		}
		return doc;
	}

}
