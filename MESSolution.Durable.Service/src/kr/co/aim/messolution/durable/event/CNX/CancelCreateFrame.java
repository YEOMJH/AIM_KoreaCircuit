package kr.co.aim.messolution.durable.event.CNX;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskFrame;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class CancelCreateFrame extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> SheetList = SMessageUtil.getBodySequenceItemList(doc, "FRAMELIST", true);

		for (Element Sheet : SheetList)
		{
			String frameName = SMessageUtil.getChildText(Sheet, "FRAMENAME", true);

			EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelCreateFrame", this.getEventUser(), this.getEventComment(), "", "");
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			MaskFrame dataInfo = ExtendedObjectProxy.getMaskFrameService().selectByKey(false, new Object[] { frameName });
			if(!StringUtil.equals(dataInfo.getFrameState(), "Created"))
			{
				throw new CustomException("FRAME-0003", frameName);
			}
			dataInfo.setLastEventComment(eventInfo.getEventComment());
			dataInfo.setLastEventName(eventInfo.getEventName());
			dataInfo.setLastEventTime(eventInfo.getEventTime());
			dataInfo.setLastEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
			dataInfo.setLastEventUser(eventInfo.getEventUser());

			ExtendedObjectProxy.getMaskFrameService().remove(eventInfo, dataInfo);
		}

		return doc;
	}

}
