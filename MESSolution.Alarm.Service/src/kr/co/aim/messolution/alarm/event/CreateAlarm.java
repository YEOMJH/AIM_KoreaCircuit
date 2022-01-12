package kr.co.aim.messolution.alarm.event;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.AlarmActionDef;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;

import org.jdom.Document;

public class CreateAlarm extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{

//		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
//		String alarmCode = SMessageUtil.getBodyItemValue(doc, "ALARMCODE", true);
//		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
//		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
//		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", false);
//		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);
//
//		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Issue", getEventUser(), getEventComment(), null, null);
//		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
//
//		AlarmDefinition alarmDef = ExtendedObjectProxy.getAlarmDefinitionService().selectByKey(false, new Object[] { alarmCode, factoryName });
//
//		Alarm alarmData = new Alarm(alarmCode, "", machineName, unitName);
//
//		alarmData.setAlarmSeverity(alarmDef.getAlarmSeverity());
//		alarmData.setAlarmState("ISSUE");
//		alarmData.setAlarmType(alarmDef.getAlarmType());
//		alarmData.setDescription(alarmDef.getDescription());
//		alarmData.setFactoryName(alarmDef.getFactoryName());
//
//		alarmData.setCreateTimeKey(eventInfo.getEventTimeKey());
//		alarmData.setCreateUser(eventInfo.getEventUser());
//
//		alarmData.setLastEventName(eventInfo.getEventName());
//		alarmData.setLastEventTimeKey(eventInfo.getEventTimeKey());
//		alarmData.setLastEventUser(eventInfo.getEventUser());
//		alarmData.setLastEventComment(eventInfo.getEventComment());
//
//		alarmData = ExtendedObjectProxy.getAlarmService().create(eventInfo, alarmData);
//
//		try
//		{
//			doAlarmAction(alarmDef.getAlarmCode(), alarmDef.getFactoryName(), machineName, lotName, carrierName);
//		}
//		catch (Exception ex)
//		{
//			eventLog.error(ex);
//		}

		return doc;
	}

	private void doAlarmAction(String alarmCode, String factoryName, String machineName, String lotName, String carrierName) throws CustomException
	{
		List<AlarmActionDef> actionList = ExtendedObjectProxy.getAlarmActionDefService().select("alarmCode = ? ORDER BY seq ", new Object[] { alarmCode });

		for (AlarmActionDef actionData : actionList)
		{
			eventLog.info(String.format("Alarm[%s] AlarmAction[%s] would be executed soon", actionData.getAlarmCode(), actionData.getActionName()));
		}
	}
}
