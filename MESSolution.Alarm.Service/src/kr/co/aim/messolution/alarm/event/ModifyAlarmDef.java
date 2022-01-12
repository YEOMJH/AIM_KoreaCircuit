package kr.co.aim.messolution.alarm.event;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.AlarmDefinition;
import kr.co.aim.messolution.extended.object.management.data.ReserveLot;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class ModifyAlarmDef extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
		String alarmCode = SMessageUtil.getBodyItemValue(doc, "ALARMCODE", true);
		String alarmType = SMessageUtil.getBodyItemValue(doc, "ALARMTYPE", true);
		String alarmSeverity = SMessageUtil.getBodyItemValue(doc, "ALARMSEVERITY", false);
		String description = SMessageUtil.getBodyItemValue(doc, "DESCRIPTION", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Modify", getEventUser(), getEventComment(), null, null);
		
		AlarmDefinition oldAlarmDef = ExtendedObjectProxy.getAlarmDefinitionService().selectByKey(false, new Object[] {alarmCode, alarmType,machineName,unitName});
		AlarmDefinition newAlarmDef = (AlarmDefinition) ObjectUtil.copyTo(oldAlarmDef);
			
		newAlarmDef.setAlarmSeverity(alarmSeverity);
		newAlarmDef.setDescription(description);
		
		newAlarmDef.setLastEventName(eventInfo.getEventName());
		newAlarmDef.setLastEventTime(TimeStampUtil.getTimestamp(TimeUtils.getCurrentTime(TimeUtils.FORMAT_DEFAULT)));
		newAlarmDef.setLastEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		newAlarmDef.setLastEventUser(eventInfo.getEventUser());
		newAlarmDef.setLastEventComment(eventInfo.getEventComment());
		
		ExtendedObjectProxy.getAlarmDefinitionService().updateToNew(oldAlarmDef, newAlarmDef);
		
		return doc;
	}
}
