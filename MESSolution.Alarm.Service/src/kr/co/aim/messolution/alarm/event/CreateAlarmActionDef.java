package kr.co.aim.messolution.alarm.event;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.AlarmActionDef;
import kr.co.aim.messolution.extended.object.management.data.AlarmDefinition;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class CreateAlarmActionDef extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
		String alarmCode = SMessageUtil.getBodyItemValue(doc, "ALARMCODE", true);
		String alarmType = SMessageUtil.getBodyItemValue(doc, "ALARMTYPE", true);
		String actionName = SMessageUtil.getBodyItemValue(doc, "ACTIONNAME", true);
		String releaseType = SMessageUtil.getBodyItemValue(doc, "RELEASETYPE", true);
		String mailFlag = SMessageUtil.getBodyItemValue(doc, "MAILFLAG", true);
		String machineLockFlag = SMessageUtil.getBodyItemValue(doc, "MACHINELOCKFLAG", true);
		String holdFlag = SMessageUtil.getBodyItemValue(doc, "HOLDFLAG", true);
		String reasonCodeType = SMessageUtil.getBodyItemValue(doc, "REASONCODETYPE", true);
		String reasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODE", true);
		String sequence = SMessageUtil.getBodyItemValue(doc, "SEQ", false);
	
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", getEventUser(), getEventComment(), null, null);
		
		AlarmDefinition alarmDef = ExtendedObjectProxy.getAlarmDefinitionService().selectByKey(false, new Object[] { alarmCode, alarmType,machineName,unitName});
		AlarmActionDef actionData = new AlarmActionDef(alarmCode, alarmType,machineName,unitName,actionName);
		
		actionData.setMailFlag(mailFlag);
		actionData.setReleaseType(releaseType);
		actionData.setHoldFlag(holdFlag);
		actionData.setMachineLockFlag(machineLockFlag);
		actionData.setReasonCodeType(reasonCodeType);
		actionData.setReasonCode(reasonCode);
		
		actionData.setLastEventName(eventInfo.getEventName());
		actionData.setLastEventTime(TimeStampUtil.getTimestamp(TimeUtils.getCurrentTime(TimeUtils.FORMAT_DEFAULT)));
		actionData.setLastEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		actionData.setLastEventUser(eventInfo.getEventUser());
		actionData.setLastEventComment(eventInfo.getEventComment());
		
		List<AlarmActionDef> resultList = null;
		try
		{
		   resultList =	ExtendedObjectProxy.getAlarmActionDefService().select(" alarmCode = ? and alarmType =? and machinename =? and unitName =? and actionName =?  ORDER BY seq DESC", new Object[] {alarmCode,alarmType,machineName,unitName,actionName});
		}
		catch (greenFrameDBErrorSignal ex)
		{	
			
		}
		catch(Exception ex)
		{
			throw new CustomException(ex.getCause());
		}
		
		if(resultList !=null && resultList.size()>0) 
		{
			//ALARM-0002: Registered Alarm Action Information.
			throw new CustomException("ALARM-0002");
		}
		
		if(sequence !=null && StringUtils.isNotEmpty(sequence)&&StringUtils.isNumeric(sequence))
		{
			actionData.setSeq(sequence);
		}
		else 
		{
			actionData.setSeq("1");
		}
		
		actionData = ExtendedObjectProxy.getAlarmActionDefService().create(eventInfo, actionData);
		
		return doc;
	}
}
