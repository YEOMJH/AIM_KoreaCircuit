package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class AMSRule extends UdfAccessor
{
	@CTORMTemplate(seq = "1", name="alarmTypeName", type="Key", dataType="String", initial="", history="")
	private String alarmTypeName;
	
	@CTORMTemplate(seq = "2", name="emailFlag", type="Column", dataType="String", initial="", history="")
	private String emailFlag;
	
	@CTORMTemplate(seq = "3", name="wechatFlag", type="Column", dataType="String", initial="", history="")
	private String wechatFlag;
	
	@CTORMTemplate(seq = "4", name="emFlag", type="Column", dataType="String", initial="", history="")
	private String emFlag;

	@CTORMTemplate(seq = "5", name="fmbFlag", type="Column", dataType="String", initial="", history="")
	private String fmbFlag;
	
	@CTORMTemplate(seq = "6", name="smsFlag", type="Column", dataType="String", initial="", history="")
	private String smsFlag;
	
	@CTORMTemplate(seq = "7", name="description", type="Column", dataType="String", initial="", history="")
	private String description;
	
	@CTORMTemplate(seq = "8", name="range", type="Column", dataType="String", initial="", history="")
	private String range;
	
	@CTORMTemplate(seq = "9", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="")
	private String lastEventTimeKey;
	
	@CTORMTemplate(seq = "10", name="lastEventName", type="Column", dataType="String", initial="", history="")
	private String lastEventName;
	
	@CTORMTemplate(seq = "11", name="lastEventUser", type="Column", dataType="String", initial="", history="")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "12", name="lastEventComment", type="Column", dataType="String", initial="", history="")
	private String lastEventComment;
	
	@CTORMTemplate(seq = "13", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp lastEventTime;
	
	public String getEmFlag() {
		return emFlag;
	}

	public void setEmFlag(String emFlag) {
		this.emFlag = emFlag;
	}

	public String getFmbFlag() {
		return fmbFlag;
	}

	public void setFmbFlag(String fmbFlag) {
		this.fmbFlag = fmbFlag;
	}

	public String getSmsFlag() {
		return smsFlag;
	}

	public void setSmsFlag(String smsFlag) {
		this.smsFlag = smsFlag;
	}
	
	public String getAlarmTypeName() {
		return alarmTypeName;
	}

	public void setAlarmTypeName(String alarmTypeName) {
		this.alarmTypeName = alarmTypeName;
	}

	public String getEmailFlag() {
		return emailFlag;
	}

	public void setEmailFlag(String emailFlag) {
		this.emailFlag = emailFlag;
	}

	public String getWechatFlag() {
		return wechatFlag;
	}

	public void setWechatFlag(String wechatFlag) {
		this.wechatFlag = wechatFlag;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getRange() {
		return range;
	}

	public void setRange(String range) {
		this.range = range;
	}

	public String getLastEventTimeKey() {
		return lastEventTimeKey;
	}

	public void setLastEventTimeKey(String lastEventTimeKey) {
		this.lastEventTimeKey = lastEventTimeKey;
	}

	public String getLastEventName() {
		return lastEventName;
	}

	public void setLastEventName(String lastEventName) {
		this.lastEventName = lastEventName;
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

	public Timestamp getLastEventTime() {
		return lastEventTime;
	}

	public void setLastEventTime(Timestamp lastEventTime) {
		this.lastEventTime = lastEventTime;
	}
}
