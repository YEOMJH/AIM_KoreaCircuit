package kr.co.aim.messolution.alarm.event;

import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.AlarmDefinition;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class CreateAlarmDef extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
		String alarmCode = SMessageUtil.getBodyItemValue(doc, "ALARMCODE", true);
		String alarmType = SMessageUtil.getBodyItemValue(doc, "ALARMTYPE", true);
		String alarmSeverity = SMessageUtil.getBodyItemValue(doc, "ALARMSEVERITY", true);
		String description = SMessageUtil.getBodyItemValue(doc, "DESCRIPTION", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", getEventUser(), getEventComment(), null, null);
		
		AlarmDefinition alarmDef = new AlarmDefinition( alarmCode, alarmType,machineName,unitName );
		
		alarmDef.setAlarmSeverity(alarmSeverity);
		alarmDef.setDescription(description);
		
		alarmDef.setLastEventName(eventInfo.getEventName());
		alarmDef.setLastEventTime(TimeStampUtil.getTimestamp(TimeUtils.getCurrentTime(TimeUtils.FORMAT_DEFAULT)));
		alarmDef.setLastEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		alarmDef.setLastEventUser(eventInfo.getEventUser());
		alarmDef.setLastEventComment(eventInfo.getEventComment());
		
		List<AlarmDefinition> resultList = null;
		try
		{
		   resultList =	ExtendedObjectProxy.getAlarmDefinitionService().select(" alarmCode = ? and alarmType =? and machinename =? and unitName =? ", new Object[] {alarmCode,alarmType,machineName,unitName});
		}
		catch (greenFrameDBErrorSignal ex)
		{	
			
		}
		catch(Exception ex)
		{
			throw new CustomException(ex.getCause());
		}
		
		if (resultList != null && resultList.size() > 0)
		{
			// ALARM-0003:Registered Alarm Information.
			throw new CustomException("ALARM-0003");
		}
			
		
		
		alarmDef = ExtendedObjectProxy.getAlarmDefinitionService().create(eventInfo, alarmDef);
		
		return doc;
	}
}
