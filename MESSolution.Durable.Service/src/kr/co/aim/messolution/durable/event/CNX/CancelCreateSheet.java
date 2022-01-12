package kr.co.aim.messolution.durable.event.CNX;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskStick;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class CancelCreateSheet extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> SheetList = SMessageUtil.getBodySequenceItemList(doc, "STICKLIST", true);

		for (Element Sheet : SheetList)
		{
			String sheetName = SMessageUtil.getChildText(Sheet, "STICKNAME", true);

			EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelCreateStick", this.getEventUser(), this.getEventComment(), "", "");
			eventInfo.setLastEventTimekey(TimeStampUtil.getCurrentEventTimeKey());

			MaskStick dataInfo = ExtendedObjectProxy.getMaskStickService().selectByKey(false, new Object[] { sheetName });
			dataInfo.setLastEventComment(eventInfo.getEventComment());
			dataInfo.setLastEventName(eventInfo.getEventName());
			dataInfo.setLastEventTime(eventInfo.getEventTime());
			dataInfo.setLastEventTimeKey(eventInfo.getLastEventTimekey());
			dataInfo.setLastEventUser(eventInfo.getEventUser());

			ExtendedObjectProxy.getMaskStickService().remove(eventInfo, dataInfo);
		}

		return doc;
	}

}
