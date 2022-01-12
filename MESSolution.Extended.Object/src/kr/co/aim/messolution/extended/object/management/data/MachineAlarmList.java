package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class MachineAlarmList extends UdfAccessor {

	@CTORMTemplate(seq = "1", name = "MachineName", type = "Key", dataType = "String", initial = "", history = "")
	private String MachineName;

	@CTORMTemplate(seq = "2", name = "UnitName", type = "Key", dataType = "String", initial = "", history = "")
	private String UnitName;

	@CTORMTemplate(seq = "3", name = "SubUnitName", type = "Key", dataType = "String", initial = "", history = "")
	private String SubUnitName;

	@CTORMTemplate(seq = "4", name = "AlarmCode", type = "Key", dataType = "String", initial = "", history = "")
	private String AlarmCode;

	@CTORMTemplate(seq = "5", name = "AlarmState", type = "Column", dataType = "String", initial = "", history = "")
	private String AlarmState;

	@CTORMTemplate(seq = "6", name = "AlarmSeverity", type = "Column", dataType = "String", initial = "", history = "")
	private String AlarmSeverity;

	@CTORMTemplate(seq = "7", name = "AlarmText", type = "Column", dataType = "String", initial = "", history = "")
	private String AlarmText;

	@CTORMTemplate(seq = "8", name = "EventTimeKey", type = "Column", dataType = "String", initial = "", history = "N")
	private String EventTimeKey;

	@CTORMTemplate(seq = "9", name = "EventName", type = "Column", dataType = "String", initial = "", history = "N")
	private String EventName;

	@CTORMTemplate(seq = "10", name = "EventUser", type = "Column", dataType = "String", initial = "", history = "N")
	private String EventUser;

	@CTORMTemplate(seq = "11", name = "EventTime", type = "Column", dataType = "Timestamp", initial = "", history = "N")
	private Timestamp EventTime;

	@CTORMTemplate(seq = "12", name = "EventComment", type = "Column", dataType = "String", initial = "", history = "N")
	private String EventComment;

	@CTORMTemplate(seq = "13", name = "AlarmTimeKey", type = "Column", dataType = "String", initial = "", history = "")
	private String AlarmTimeKey;

	// instantiation
	public MachineAlarmList()
	{

	}

	// instantiation
	public MachineAlarmList(String machineName, String unitName, String subUnitName, String alarmCode)
	{
		setMachineName(machineName);
		setUnitName(unitName);
		setSubUnitName(subUnitName);
		setAlarmCode(alarmCode);
	}

	public String getMachineName()
	{
		return MachineName;
	}

	public void setMachineName(String machineName)
	{
		MachineName = machineName;
	}

	public String getUnitName()
	{
		return UnitName;
	}

	public void setUnitName(String unitName)
	{
		UnitName = unitName;
	}

	public String getSubUnitName()
	{
		return SubUnitName;
	}

	public void setSubUnitName(String subUnitName)
	{
		SubUnitName = subUnitName;
	}

	public String getAlarmCode()
	{
		return AlarmCode;
	}

	public void setAlarmCode(String alarmCode)
	{
		AlarmCode = alarmCode;
	}

	public String getAlarmState()
	{
		return AlarmState;
	}

	public void setAlarmState(String alarmState)
	{
		AlarmState = alarmState;
	}

	public String getAlarmSeverity()
	{
		return AlarmSeverity;
	}

	public void setAlarmSeverity(String alarmSeverity)
	{
		AlarmSeverity = alarmSeverity;
	}

	public String getAlarmText()
	{
		return AlarmText;
	}

	public void setAlarmText(String alarmText)
	{
		AlarmText = alarmText;
	}

	public String getEventTimeKey()
	{
		return EventTimeKey;
	}

	public void setEventTimeKey(String eventTimeKey)
	{
		EventTimeKey = eventTimeKey;
	}

	public String getEventName()
	{
		return EventName;
	}

	public void setEventName(String eventName)
	{
		EventName = eventName;
	}

	public String getEventUser()
	{
		return EventUser;
	}

	public void setEventUser(String eventUser)
	{
		EventUser = eventUser;
	}

	public Timestamp getEventTime()
	{
		return EventTime;
	}

	public void setEventTime(Timestamp eventTime)
	{
		EventTime = eventTime;
	}

	public String getEventComment()
	{
		return EventComment;
	}

	public void setEventComment(String eventComment)
	{
		EventComment = eventComment;
	}

	public String getAlarmTimeKey()
	{
		return AlarmTimeKey;
	}

	public void setAlarmTimeKey(String alarmTimeKey)
	{
		AlarmTimeKey = alarmTimeKey;
	}
}
