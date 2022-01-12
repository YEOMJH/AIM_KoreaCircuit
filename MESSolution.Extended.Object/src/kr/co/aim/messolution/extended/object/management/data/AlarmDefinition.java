package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class AlarmDefinition extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="alarmCode", type="Key", dataType="String", initial="", history="")
	private String alarmCode;
	@CTORMTemplate(seq = "2", name="alarmType", type="Key", dataType="String", initial="", history="")
	private String alarmType;
	@CTORMTemplate(seq = "3", name="machineName", type="Key", dataType="String", initial="", history="")
	private String machineName;
	@CTORMTemplate(seq = "4", name="unitName", type="Key", dataType="String", initial="", history="")
	private String unitName;
	@CTORMTemplate(seq = "5", name="alarmSeverity", type="Column", dataType="String", initial="", history="")
	private String alarmSeverity;
	@CTORMTemplate(seq = "6", name="description", type="Column", dataType="String", initial="", history="")
	private String description;
	@CTORMTemplate(seq = "7", name="lastEventName", type="Column", dataType="String", initial="", history="")
	private String lastEventName;
	@CTORMTemplate(seq = "8", name="lastEventTime", type="Column", dataType="TimeStamp", initial="", history="")
	private Timestamp lastEventTime;
	@CTORMTemplate(seq = "9", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="")
	private String lastEventTimeKey;
	@CTORMTemplate(seq = "10", name="lastEventUser", type="Column", dataType="String", initial="", history="")
	private String lastEventUser;
	@CTORMTemplate(seq = "11", name="lastEventComment", type="Column", dataType="String", initial="", history="")
	private String lastEventComment;
	
	public AlarmDefinition () {}
	public AlarmDefinition (String alarmCode, String alarmType, String machineName, String unitName)
	{
		setAlarmCode(alarmCode);
		setAlarmType(alarmType);
		setMachineName(machineName);
		setUnitName(unitName);
	}
	public String getAlarmCode() {
		return alarmCode;
	}
	public void setAlarmCode(String alarmCode) {
		this.alarmCode = alarmCode;
	}
	public String getAlarmType() {
		return alarmType;
	}
	public void setAlarmType(String alarmType) {
		this.alarmType = alarmType;
	}
	public String getMachineName() {
		return machineName;
	}
	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}
	public String getUnitName() {
		return unitName;
	}
	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}
	public String getAlarmSeverity() {
		return alarmSeverity;
	}
	public void setAlarmSeverity(String alarmSeverity) {
		this.alarmSeverity = alarmSeverity;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getLastEventName() {
		return lastEventName;
	}
	public void setLastEventName(String lastEventName) {
		this.lastEventName = lastEventName;
	}
	public Timestamp getLastEventTime() {
		return lastEventTime;
	}
	public void setLastEventTime(Timestamp lastEventTime) {
		this.lastEventTime = lastEventTime;
	}
	public String getLastEventTimeKey() {
		return lastEventTimeKey;
	}
	public void setLastEventTimeKey(String lastEventTimeKey) {
		this.lastEventTimeKey = lastEventTimeKey;
	}
	public String getLastEventUser() {
		return lastEventUser;
	}
	public void setLastEventUser(String lastEventUser) {
		this.lastEventUser = lastEventUser;
	}
	public String getLastEventComment() {
		return lastEventComment;
	}
	public void setLastEventComment(String lastEventComment) {
		this.lastEventComment = lastEventComment;
	}
	
}
