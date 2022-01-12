package kr.co.aim.messolution.alarm.event;

import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.AlarmActionDef;
import kr.co.aim.messolution.extended.object.management.data.AlarmDefinition;
import kr.co.aim.messolution.extended.object.management.data.AlarmHoldEQP;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class RemoveAlarmDef extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
		String alarmCode = SMessageUtil.getBodyItemValue(doc, "ALARMCODE", true);
		String alarmType = SMessageUtil.getBodyItemValue(doc, "ALARMTYPE", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Remove", getEventUser(), getEventComment(), null, null);
		
		AlarmDefinition alarmDef = ExtendedObjectProxy.getAlarmDefinitionService().selectByKey(false, new Object[] {alarmCode, alarmType,machineName,unitName});
		
		ExtendedObjectProxy.getAlarmDefinitionService().remove(eventInfo, alarmDef);
		
		removeCascading(alarmCode);
		
		removeHoldEQP(alarmCode);
		
		return doc;
	}
	
	private void removeCascading(String alarmCode)
	{
		try
		{
			List<AlarmActionDef> actionList = ExtendedObjectProxy.getAlarmActionDefService().select(" alarmCode = ? ORDER BY seq DESC", new Object[] {alarmCode});
			
			for (AlarmActionDef action : actionList)
			{
				try
				{
					ExtendedObjectProxy.getAlarmActionDefService().delete(action);
				}
				catch (Exception ce)
				{
					eventLog.error(String.format("AlarmCode[%s] [%d]th action has not removed", action.getAlarmCode(), action.getSeq()));
				}
			}
		}
		catch (Exception ce)
		{
			eventLog.error(String.format("AlarmCode[%s] action set have not removed", alarmCode));
		}
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
