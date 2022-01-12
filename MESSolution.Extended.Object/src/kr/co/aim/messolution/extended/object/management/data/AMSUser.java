package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class AMSUser extends UdfAccessor
{
	@CTORMTemplate(seq = "1", name="userID", type="Key", dataType="String", initial="", history="")
	private String userID;
	
	@CTORMTemplate(seq = "2", name="userName", type="Column", dataType="String", initial="", history="")
	private String userName;
	
	@CTORMTemplate(seq = "3", name="email", type="Column", dataType="String", initial="", history="")
	private String email;
	
	@CTORMTemplate(seq = "4", name="phone", type="Column", dataType="String", initial="", history="")
	private String phone;
	
	@CTORMTemplate(seq = "5", name="groupName", type="Column", dataType="String", initial="", history="")
	private String groupName;
	
	@CTORMTemplate(seq = "6", name="department", type="Column", dataType="String", initial="", history="")
	private String department;
	
	@CTORMTemplate(seq = "7", name="center", type="Column", dataType="String", initial="", history="")
	private String center;
	
	@CTORMTemplate(seq = "8", name="userLevel", type="Column", dataType="String", initial="", history="")
	private String userLevel;
	
	@CTORMTemplate(seq = "9", name="durationTime", type="Column", dataType="String", initial="", history="")
	private String durationTime;
	
	@CTORMTemplate(seq = "10", name="lasteventTimeKey", type="Column", dataType="String", initial="", history="")
	private String lasteventTimeKey;
	
	@CTORMTemplate(seq = "11", name="lastEventName", type="Column", dataType="String", initial="", history="")
	private String lastEventName;
	
	@CTORMTemplate(seq = "12", name="lasteventuser", type="Column", dataType="String", initial="", history="")
	private String lasteventuser;
	
	@CTORMTemplate(seq = "13", name="lastEventComment", type="Column", dataType="String", initial="", history="")
	private String lastEventComment;
	
	@CTORMTemplate(seq = "14", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp lastEventTime;
	
	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public String getCenter() {
		return center;
	}

	public void setCenter(String center) {
		this.center = center;
	}

	public String getUserLevel() {
		return userLevel;
	}

	public void setUserLevel(String userLevel) {
		this.userLevel = userLevel;
	}

	public String getDurationTime() {
		return durationTime;
	}

	public void setDurationTime(String durationTime) {
		this.durationTime = durationTime;
	}

	public String getLasteventTimeKey() {
		return lasteventTimeKey;
	}

	public void setLasteventTimeKey(String lasteventTimeKey) {
		this.lasteventTimeKey = lasteventTimeKey;
	}

	public String getLastEventName() {
		return lastEventName;
	}

	public void setLastEventName(String lastEventName) {
		this.lastEventName = lastEventName;
	}

	public String getLasteventuser() {
		return lasteventuser;
	}

	public void setLasteventuser(String lasteventuser) {
		this.lasteventuser = lasteventuser;
	}

	public String getLastEventComment() {
		return lastEventComment;
	}

	public void setLastEventComment(String lastEventComment) {
		this.lastEventComment = lastEventComment;
	}

	public Timestamp getLastEventTime() {
		return lastEventTime;
	}

	public void setLastEventTime(Timestamp lastEventTime) {
		this.lastEventTime = lastEventTime;
	}
}
