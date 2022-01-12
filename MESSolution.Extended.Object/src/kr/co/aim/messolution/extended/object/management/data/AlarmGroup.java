package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class AlarmGroup extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="alarmGroupName", type="Key", dataType="String", initial="", history="")
	private String alarmGroupName;
	@CTORMTemplate(seq = "2", name="description", type="Key", dataType="String", initial="", history="")
	private String description;
	@CTORMTemplate(seq = "3", name="alarmType", type="Column", dataType="String", initial="", history="")
	private String alarmType;
	@CTORMTemplate(seq = "4", name="lastEventName", type="Column", dataType="String", initial="", history="")
	private String lastEventName;
	@CTORMTemplate(seq = "5", name="lastEventTime", type="Column", dataType="TimeStamp", initial="", history="")
	private String lastEventTime;
	@CTORMTemplate(seq = "6", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="")
	private String lastEventTimeKey;
	@CTORMTemplate(seq = "7", name="lastEventUser", type="Column", dataType="String", initial="", history="")
	private String lastEventUser;
	@CTORMTemplate(seq = "8", name="lastEventComment", type="Column", dataType="String", initial="", history="")
	private String lastEventComment;
	
	public AlarmGroup(){}
	public AlarmGroup(String alarmGroupName, String description)
	{
		setAlarmGroupName(alarmGroupName);
		setDescription(description);
	}
	public String getAlarmGroupName() {
		return alarmGroupName;
	}
	public void setAlarmGroupName(String alarmGroupName) {
		this.alarmGroupName = alarmGroupName;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getAlarmType() {
		return alarmType;
	}
	public void setAlarmType(String alarmType) {
		this.alarmType = alarmType;
	}
	public String getLastEventName() {
		return lastEventName;
	}
	public void setLastEventName(String lastEventName) {
		this.lastEventName = lastEventName;
	}
	public String getLastEventTime() {
		return lastEventTime;
	}
	public void setLastEventTime(String lastEventTime) {
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
