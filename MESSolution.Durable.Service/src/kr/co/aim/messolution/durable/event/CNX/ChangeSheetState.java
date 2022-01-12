package kr.co.aim.messolution.durable.event.CNX;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskStick;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class ChangeSheetState extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		Element eleBody = SMessageUtil.getBodyElement(doc);

		if (eleBody != null)
		{
			for (Element eledur : SMessageUtil.getBodySequenceItemList(doc, "STICKLIST", false))
			{
				String StickName = SMessageUtil.getChildText(eledur, "STICKNAME", true);
				String StickState = SMessageUtil.getChildText(eledur, "STICKSTATE", true);

				EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeStickState", this.getEventUser(), this.getEventComment(), "", "");
				eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
				MaskStick dataInfo = ExtendedObjectProxy.getMaskStickService().selectByKey(false, new Object[] { StickName });
				dataInfo.setStickName(StickName);
				dataInfo.setStickState(StickState);
				dataInfo.setLastEventComment(eventInfo.getEventComment());
				dataInfo.setLastEventName(eventInfo.getEventName());
				dataInfo.setLastEventTime(eventInfo.getEventTime());
				dataInfo.setLastEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
				dataInfo.setLastEventUser(eventInfo.getEventUser());

				ExtendedObjectProxy.getMaskStickService().modify(eventInfo, dataInfo);
			}
		}
		return doc;
	}

}
