package kr.co.aim.messolution.transportjob.event;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MachineAlarmList;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class MachineAlarmStateChanged extends AsyncHandler {

	/**
	 * MessageSpec [MCS -> TEX -> FMC]
	 * 
	 * <Body>
	 *    <MACHINENAME />
	 *    <UNITNAME />
	 *    <ALARMCODE />
	 *    <ALARMSTATE />
	 *    <ALARMTEXT />
	 * </Body>
	 */
	
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String alarmCode = SMessageUtil.getBodyItemValue(doc, "ALARMCODE", true);
		String alarmState = SMessageUtil.getBodyItemValue(doc, "ALARMSTATE", true);
		String description = SMessageUtil.getBodyItemValue(doc, "ALARMTEXT", false);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String subUnitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false);

		if (StringUtils.isEmpty(subUnitName))
		{
			subUnitName = "-";
		}

		if (StringUtils.isEmpty(unitName))
		{
			unitName = "-";
		}

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Issue", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		MachineAlarmList eqpAlarmData = new MachineAlarmList(machineName, unitName, subUnitName, alarmCode);

		eqpAlarmData.setMachineName(machineName);
		eqpAlarmData.setAlarmCode(alarmCode);
		eqpAlarmData.setAlarmState(alarmState);
		eqpAlarmData.setAlarmText(description);
		eqpAlarmData.setEventTimeKey(eventInfo.getEventTimeKey());
		eqpAlarmData.setEventName(eventInfo.getEventName());
		eqpAlarmData.setEventUser(eventInfo.getEventUser());
		eqpAlarmData.setEventTime(eventInfo.getEventTime());
		eqpAlarmData.setEventComment(eventInfo.getEventComment());

		// MachineAlarmList Modify
		try
		{
			MachineAlarmList machineAlarmList = ExtendedObjectProxy.getMachineAlarmListService().selectByKey(false, new Object[] { machineName, unitName, subUnitName, alarmCode });

			if (StringUtils.equals(alarmState, "SET"))
			{
				eventInfo.setEventName("Issue");
				ExtendedObjectProxy.getMachineAlarmListService().modify(eventInfo, machineAlarmList);
			}
			else if (StringUtils.equals(alarmState, "CLEAR"))
			{
				eventInfo.setEventName("Clear");
				ExtendedObjectProxy.getMachineAlarmListService().remove(eventInfo, machineAlarmList);
			}
		}
		catch (Exception ex)
		{
			if (StringUtils.equals(alarmState, "SET"))
			{
				eventInfo.setEventName("Issue");
				
				eqpAlarmData.setAlarmTimeKey(eventInfo.getEventTimeKey());
				ExtendedObjectProxy.getMachineAlarmListService().create(eventInfo, eqpAlarmData);
			}
			else if (StringUtils.equals(alarmState, "CLEAR"))
			{
				eventInfo.setEventName("Clear");
				ExtendedObjectProxy.getMachineAlarmListService().addHistory(eventInfo, "MachineAlarmHistory", eqpAlarmData, LogFactory.getLog(MachineAlarmList.class));
			}
		}

		try
		{
			// success then report to FMB
			GenericServiceProxy.getESBServive().sendBySenderToFMB(doc);
		}
		catch (Exception ex)
		{
			eventLog.warn("FMC Report Failed!");
		}
	}
}
