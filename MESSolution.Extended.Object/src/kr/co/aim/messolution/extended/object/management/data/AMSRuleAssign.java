package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class AMSRuleAssign extends UdfAccessor
{
	@CTORMTemplate(seq = "1", name="alarmTypeName", type="Key", dataType="String", initial="", history="")
	private String alarmTypeName;
	
	@CTORMTemplate(seq = "2", name="userID", type="Key", dataType="String", initial="", history="")
	private String userID;
	
	@CTORMTemplate(seq = "3", name="department", type="Column", dataType="String", initial="", history="")
	private String department;
	
	@CTORMTemplate(seq = "4", name="factory", type="Column", dataType="String", initial="", history="")
	private String factory;
	
	@CTORMTemplate(seq = "5", name="range", type="Column", dataType="String", initial="", history="")
	private String range;
	
	@CTORMTemplate(seq = "6", name="lasteventTimeKey", type="Column", dataType="String", initial="", history="")
	private String lasteventTimeKey;
	
	@CTORMTemplate(seq = "7", name="lastEventName", type="Column", dataType="String", initial="", history="")
	private String lastEventName;
	
	@CTORMTemplate(seq = "8", name="lasteventuser", type="Column", dataType="String", initial="", history="")
	private String lasteventuser;
	
	@CTORMTemplate(seq = "9", name="LASTEVENTCOMMENT", type="Column", dataType="String", initial="", history="")
	private String LASTEVENTCOMMENT;
	
	@CTORMTemplate(seq = "10", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp lastEventTime;
	
	public String getAlarmTypeName() {
		return alarmTypeName;
	}

	public void setAlarmTypeName(String alarmTypeName) {
		this.alarmTypeName = alarmTypeName;
	}

	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public String getFactory() {
		return factory;
	}

	public void setFactory(String factory) {
		this.factory = factory;
	}

	public String getRange() {
		return range;
	}

	public void setRange(String range) {
		this.range = range;
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

	public String getLASTEVENTCOMMENT() {
		return LASTEVENTCOMMENT;
	}

	public void setLASTEVENTCOMMENT(String lASTEVENTCOMMENT) {
		LASTEVENTCOMMENT = lASTEVENTCOMMENT;
	}

	public Timestamp getLastEventTime() {
		return lastEventTime;
	}

	public void setLastEventTime(Timestamp lastEventTime) {
		this.lastEventTime = lastEventTime;
	}
}
