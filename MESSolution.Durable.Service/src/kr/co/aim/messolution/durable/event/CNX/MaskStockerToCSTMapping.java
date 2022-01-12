package kr.co.aim.messolution.durable.event.CNX;

import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class MaskStockerToCSTMapping extends SyncHandler {

	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> durableList = SMessageUtil.getBodySequenceItemList(doc, "DURABLELIST", true);

		for (Element eleDurable : durableList)
		{
			String durableName = SMessageUtil.getChildText(eleDurable, "DURABLENAME", true);
			String reserveMaskStocker = SMessageUtil.getChildText(eleDurable, "RESERVEMASKSTOCKER", false);

			EventInfo eventInfo = EventInfoUtil.makeEventInfo("MappingStockerToCST", this.getEventUser(), this.getEventComment(), "", "");
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableName);
			Map<String, String> durableUdfs = durableData.getUdfs();
			String originalReservedMaskStocker = durableUdfs.get("RESERVEMASKSTOCKER");

			if (StringUtil.equals(originalReservedMaskStocker, reserveMaskStocker))
				continue;

			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.getUdfs().put("RESERVEMASKSTOCKER", reserveMaskStocker);

			MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
		}

		return doc;
	}

}
