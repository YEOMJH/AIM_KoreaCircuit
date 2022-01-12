package kr.co.aim.messolution.alarm.event;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.Alarm;
import kr.co.aim.messolution.extended.object.management.data.CustomAlarm;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class ClearAlarm extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> alarmList = SMessageUtil.getBodySequenceItemList(doc, "ALARMLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ClearAlarm", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		for (Element alrm : alarmList)
		{
			String alarmTimeKey = SMessageUtil.getChildText(alrm, "ALARMTIMEKEY", true);
			String alarmCode = SMessageUtil.getChildText(alrm, "ALARMCODE", true);
			String category = SMessageUtil.getChildText(alrm, "CATEGORY", true);

			if (category.equals("MESAlarm"))
			{
				Alarm alarmData = new Alarm(alarmCode,alarmTimeKey);
				
				alarmData.setAlarmState("CLEAR");
				alarmData.setLastEventName("ClearAlarm");
				alarmData.setLastEventTimeKey(eventInfo.getEventTimeKey());
				alarmData.setLastEventUser(eventInfo.getEventUser());
				alarmData.setLastEventComment(eventInfo.getEventComment());
				
				ExtendedObjectProxy.getAlarmService().remove(eventInfo, alarmData);
				
			}
			else if (category.equals("CustomAlarm"))
			{
                CustomAlarm alarmData = new CustomAlarm(alarmCode,alarmTimeKey);
				
				alarmData.setAlarmState("CLEAR");
				alarmData.setLastEventName("ClearAlarm");
				alarmData.setLastEventTimeKey(eventInfo.getEventTimeKey());
				alarmData.setLastEventUser(eventInfo.getEventUser());
				alarmData.setLastEventComment(eventInfo.getEventComment());
				alarmData.setResolveTimeKey(eventInfo.getEventTimeKey());
				alarmData.setResolveUser(eventInfo.getEventUser());
				
				ExtendedObjectProxy.getCustomAlarmService().remove(eventInfo, alarmData);
				
			}
			else 
			{
				throw new CustomException("ALARM-0001", category);
			}
		}

		return doc;
	}

}
