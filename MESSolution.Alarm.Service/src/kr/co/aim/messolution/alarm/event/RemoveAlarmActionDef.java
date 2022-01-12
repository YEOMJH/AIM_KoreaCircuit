package kr.co.aim.messolution.alarm.event;

import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.AlarmActionDef;
import kr.co.aim.messolution.extended.object.management.data.AlarmHoldEQP;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class RemoveAlarmActionDef extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
		String alarmCode = SMessageUtil.getBodyItemValue(doc, "ALARMCODE", true);
		String alarmType = SMessageUtil.getBodyItemValue(doc, "ALARMTYPE", true);
		String actionName = SMessageUtil.getBodyItemValue(doc, "ACTIONNAME", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Remove", getEventUser(), getEventComment(), null, null);
		AlarmActionDef actionData = ExtendedObjectProxy.getAlarmActionDefService().selectByKey(false, new Object[] {alarmCode, alarmType,machineName,unitName,actionName});
		
		actionData.setLastEventName(eventInfo.getEventName());
		actionData.setLastEventTime(TimeStampUtil.getTimestamp(TimeUtils.getCurrentTime(TimeUtils.FORMAT_DEFAULT)));
		actionData.setLastEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		actionData.setLastEventUser(eventInfo.getEventUser());
		actionData.setLastEventComment(eventInfo.getEventComment());
		
		ExtendedObjectProxy.getAlarmActionDefService().remove(eventInfo, actionData);
		
//		if(actionName.equals("EQPHold"))
//		{
//			removeHoldEQP(alarmCode);
//		}
		
		return doc;
	}
	private void removeHoldEQP(String alarmCode)
	{
		try
		{
			List<AlarmHoldEQP> HoldEQPList = ExtendedObjectProxy.getAlarmHoldEQPService().select(" alarmCode = ?", new Object[] {alarmCode});
			
			
			for (AlarmHoldEQP machine : HoldEQPList)
			{
				try
				{
					ExtendedObjectProxy.getAlarmHoldEQPService().delete(machine);
				}
				catch (Exception ce)
				{
					eventLog.error(String.format("AlarmCode[%s] Holdmachine has not removed", alarmCode));
				}
			}
		}
		catch (Exception ce)
		{
			eventLog.error(String.format("AlarmCode[%s] Holdmachine set have not removed", alarmCode));
		}
	}
}
