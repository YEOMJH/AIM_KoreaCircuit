package kr.co.aim.messolution.durable.event.CNX;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class ChangeMaskComment extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> MaskList = SMessageUtil.getBodySequenceItemList(doc, "MASKLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeMaskComment", this.getEventUser(), this.getEventComment(), "", "");

		for (Element Mask : MaskList)
		{
			String maskLotName = SMessageUtil.getChildText(Mask, "MASKLOTNAME", true);
			eventInfo.setLastEventTimekey(TimeStampUtil.getCurrentEventTimeKey());
			MaskLot maskLotInfo = ExtendedObjectProxy.getMaskLotService().selectByKey(true, new Object[] { maskLotName });
			maskLotInfo.setLastEventName(eventInfo.getEventName());
			maskLotInfo.setLastEventTime(eventInfo.getEventTime());
			maskLotInfo.setLastEventTimeKey(eventInfo.getLastEventTimekey());
			maskLotInfo.setLastEventUser(eventInfo.getEventUser());
			maskLotInfo.setLastEventComment(eventInfo.getEventComment());
			maskLotInfo.setMaskLotNote(eventInfo.getEventComment());

			ExtendedObjectProxy.getMaskLotService().modify(eventInfo, maskLotInfo);
		}
		
		return doc;
	}

}
