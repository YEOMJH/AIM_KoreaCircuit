package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class AlarmUserGroup extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="alarmGroupName", type="Key", dataType="String", initial="", history="")
	private String alarmGroupName    ;
	@CTORMTemplate(seq = "2", name="userId", type="Key", dataType="String", initial="", history="")
	private String userId            ;
	@CTORMTemplate(seq = "3", name="userLevel", type="Cloumn", dataType="String", initial="", history="")
	private String userLevel;
	@CTORMTemplate(seq = "4", name="range", type="Cloumn", dataType="String", initial="", history="")
	private String range             ;
	@CTORMTemplate(seq = "5", name="lastEventName", type="Cloumn", dataType="String", initial="", history="")
	private String lastEventName     ;
	@CTORMTemplate(seq = "6", name="lastEventTime", type="Cloumn", dataType="TimeStamp", initial="", history="")
	private Timestamp lastEventTime     ;
	@CTORMTemplate(seq = "7", name="lastEventTimeKey", type="Cloumn", dataType="String", initial="", history="")
	private String lastEventTimeKey   ;
	@CTORMTemplate(seq = "8", name="lastEventUser", type="Cloumn", dataType="String", initial="", history="")
	private String lastEventUser     ;
	@CTORMTemplate(seq = "9", name="lastEventComment", type="Cloumn", dataType="String", initial="", history="")
	private String lastEventComment  ;
	@CTORMTemplate(seq = "10", name="department", type="Cloumn", dataType="String", initial="", history="")
	private String department  ;
	@CTORMTemplate(seq = "11", name="email", type="Cloumn", dataType="String", initial="", history="")
	private String email  ;
	@CTORMTemplate(seq = "12", name="userName", type="Cloumn", dataType="String", initial="", history="")
	private String userName  ;
	@CTORMTemplate(seq = "13", name="phone", type="Cloumn", dataType="String", initial="", history="")
	private String phone  ;


	public AlarmUserGroup(){};
	public AlarmUserGroup(String alarmGroupName, String userId)
	{
		setAlarmGroupName(alarmGroupName);
		setUserId(userId);
	}
	public String getDepartment() {
		return department;
	}
	public void setDepartment(String department) {
		this.department = department;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getAlarmGroupName() {
		return alarmGroupName;
	}
	public void setAlarmGroupName(String alarmGroupName) {
		this.alarmGroupName = alarmGroupName;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getUserLevel() {
		return userLevel;
	}
	public void setUserLevel(String userLevel) {
		this.userLevel = userLevel;
	}
	public String getRange() {
		return range;
	}
	public void setRange(String range) {
		this.range = range;
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
