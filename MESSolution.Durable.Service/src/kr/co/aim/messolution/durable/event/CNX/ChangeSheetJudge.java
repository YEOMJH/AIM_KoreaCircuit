package kr.co.aim.messolution.durable.event.CNX;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskStick;
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

public class ChangeSheetJudge extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		Element eleBody = SMessageUtil.getBodyElement(doc);

		if (eleBody != null)
		{
			for (Element eledur : SMessageUtil.getBodySequenceItemList(doc, "STICKLIST", false))
			{
				String stickName = SMessageUtil.getChildText(eledur, "STICKNAME", true);
				String stickGrade = SMessageUtil.getChildText(eledur, "STICKGRADE", false);

				EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeStickJudge", this.getEventUser(), this.getEventComment(), "", "");
				eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

				MaskStick dataInfo = ExtendedObjectProxy.getMaskStickService().selectByKey(false, new Object[] { stickName });
				if(StringUtil.equals(dataInfo.getStickState(), "InUse")||StringUtil.equals(dataInfo.getStickState(), "Scrapped"))
				{					
					throw new CustomException("STICK-0001", stickName);
				}
				if(stickGrade.equals("A")||stickGrade.equals("B")||stickGrade.equals("C"))
				{
					dataInfo.setStickJudge("G");
					dataInfo.setStickState("Available");
				}
				else if(stickGrade.equals("NG"))
				{
					dataInfo.setStickJudge("N");
					dataInfo.setStickState("NotAvailable");
				}
				dataInfo.setStickName(stickName);
				dataInfo.setStickGrade(stickGrade);
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
