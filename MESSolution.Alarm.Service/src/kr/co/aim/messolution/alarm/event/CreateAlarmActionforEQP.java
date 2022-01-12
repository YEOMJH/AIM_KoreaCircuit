package kr.co.aim.messolution.alarm.event;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.AlarmHoldEQP;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;

import org.jdom.Document;

public class CreateAlarmActionforEQP extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
//		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
//		String alarmCode = SMessageUtil.getBodyItemValue(doc, "ALARMCODE", true);
//		String sequence = SMessageUtil.getBodyItemValue(doc, "SEQ", false);
//		String actionName = SMessageUtil.getBodyItemValue(doc, "ACTIONNAME", true);
//		String reasonCodeType = SMessageUtil.getBodyItemValue(doc, "REASONCODETYPE", true);
//		String reasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODE", true);
//		
//		List<Element> MachineList = SMessageUtil.getBodySequenceItemList(doc, "MACHINELIST", true);
//		
//		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", getEventUser(), getEventComment(), null, null);
//		
//		AlarmDefinition alarmDef = ExtendedObjectProxy.getAlarmDefinitionService().selectByKey(false, new Object[] {alarmCode, factoryName});
//		
//		AlarmActionDef actionData = new AlarmActionDef(alarmDef.getAlarmCode(), actionName);
//		
//		actionData.setReasonCodeType(reasonCodeType);
//		actionData.setReasonCode(reasonCode);
//
//		actionData.setLastEventName(eventInfo.getEventName());
//		actionData.setLastEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
//		actionData.setLastEventUser(eventInfo.getEventUser());
//		actionData.setLastEventComment(eventInfo.getEventComment());
//
//		try
//		{
//			actionData.setSeq(Long.parseLong(sequence));
//		}
//		catch (Exception ex)
//		{
//			try
//			{
//				List<AlarmActionDef> actionList = ExtendedObjectProxy.getAlarmActionDefService().select(" alarmCode = ? ORDER BY seq DESC", new Object[] { alarmCode });
//
//				for (AlarmActionDef action : actionList)
//				{
//					long lastIndex = action.getSeq();
//
//					actionData.setSeq(lastIndex++);
//
//					break;
//				}
//			}
//			catch (Exception ce)
//			{
//				actionData.setSeq(1);
//			}
//
//		}
//
//		try
//		{
//			actionData = ExtendedObjectProxy.getAlarmActionDefService().create(eventInfo, actionData);
//		}
//		catch (DuplicateNameSignal de)
//		{
//			actionData = ExtendedObjectProxy.getAlarmActionDefService().modify(eventInfo, actionData);
//		}
//
//		
//		for (Element machine : MachineList)
//		{
//			String machineName = SMessageUtil.getChildText(machine, "MACHINENAME", true);
//			AlarmHoldEQP HoldEQPData = new AlarmHoldEQP(alarmDef.getAlarmCode(), machineName);
//			
//			HoldEQPData.setActionName(actionName);
//			try
//			{
//				HoldEQPData = ExtendedObjectProxy.getAlarmHoldEQPService().create(eventInfo, HoldEQPData);
//			}
//			catch (DuplicateNameSignal de)
//			{
//				removeHoldEQP(alarmCode);
//				
//				List<Element> newMachineList = SMessageUtil.getBodySequenceItemList(doc, "MACHINELIST", true);
//				
//				for (Element newMachine : newMachineList)
//				{
//					String newMachineName = SMessageUtil.getChildText(newMachine, "MACHINENAME", true);
//					
//					AlarmHoldEQP NewHoldEQPData = new AlarmHoldEQP(alarmDef.getAlarmCode(), newMachineName);
//					NewHoldEQPData.setActionName(actionName);
//					
//					HoldEQPData = ExtendedObjectProxy.getAlarmHoldEQPService().create(eventInfo, NewHoldEQPData);					
//				}
//				
//				return doc;
//			}
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
