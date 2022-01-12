package kr.co.aim.messolution.alarm.event;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.Alarm;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;

public class RemoveAlarm extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String alarmCode = SMessageUtil.getBodyItemValue(doc, "ALARMCODE", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Clear", getEventUser(), getEventComment(), null, null);
		
		Alarm alarmData = ExtendedObjectProxy.getAlarmService().selectByKey(false, new Object[] {alarmCode, factoryName, machineName, unitName});
		
		alarmData.setAlarmState("CLEAR");
		alarmData.setResolveUser(eventInfo.getEventUser());
		alarmData.setResolveTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		alarmData.setLastEventName(eventInfo.getEventName());
		alarmData.setLastEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		alarmData.setLastEventUser(eventInfo.getEventUser());
		alarmData.setLastEventComment(eventInfo.getEventComment());
		
		ExtendedObjectProxy.getAlarmService().modify(eventInfo, alarmData);
		
		return doc;
	}
}
