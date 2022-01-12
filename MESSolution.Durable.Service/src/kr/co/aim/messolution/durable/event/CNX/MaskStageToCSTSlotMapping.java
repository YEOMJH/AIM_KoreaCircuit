package kr.co.aim.messolution.durable.event.CNX;

import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.info.SetEventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class MaskStageToCSTSlotMapping extends SyncHandler {

	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> stageList = SMessageUtil.getBodySequenceItemList(doc, "STAGELIST", true);

		for (Element eleStage : stageList)
		{
			String stageName = SMessageUtil.getChildText(eleStage, "STAGENAME", true);
			String slotNo = SMessageUtil.getChildText(eleStage, "SLOTNO", false);

			EventInfo eventInfo = EventInfoUtil.makeEventInfo("MappingStageToCST", this.getEventUser(), this.getEventComment(), "", "");
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			Machine stageData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(stageName);
			Map<String, String> machineUdfs = stageData.getUdfs();
			String originalReservedCstSlotNo = machineUdfs.get("RESERVECSTSLOTNO");

			if (StringUtil.equals(originalReservedCstSlotNo, slotNo))
				continue;

			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.getUdfs().put("RESERVECSTSLOTNO", slotNo);

			MESMachineServiceProxy.getMachineServiceImpl().setEvent(stageData, setEventInfo, eventInfo);
		}

		return doc;
	}

}
