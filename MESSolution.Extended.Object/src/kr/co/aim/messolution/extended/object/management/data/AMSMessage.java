package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class AMSMessage extends UdfAccessor
{
	@CTORMTemplate(seq = "1", name="alarmID", type="Key", dataType="String", initial="", history="")
	private String alarmID;
	
	@CTORMTemplate(seq = "2", name="alarmTypeName", type="Column", dataType="String", initial="", history="")
	private String alarmTypeName;
	
	@CTORMTemplate(seq = "3", name="keyValue", type="Column", dataType="String", initial="", history="")
	private String keyValue;
	
	@CTORMTemplate(seq = "4", name="emailSendLog", type="Column", dataType="String", initial="", history="")
	private String emailSendLog;

	@CTORMTemplate(seq = "5", name="emsSendLog", type="Column", dataType="String", initial="", history="")
	private String emsSendLog;
	
	@CTORMTemplate(seq = "6", name="weChatSendLog", type="Column", dataType="String", initial="", history="")
	private String weChatSendLog;
	
	@CTORMTemplate(seq = "7", name="fmbSendLog", type="Column", dataType="String", initial="", history="")
	private String fmbSendLog;
	
	@CTORMTemplate(seq = "8", name="smsSendLog", type="Column", dataType="String", initial="", history="")
	private String smsSendLog;
	
	@CTORMTemplate(seq = "9", name="messageLog", type="Column", dataType="String", initial="", history="")
	private String messageLog;
	
	@CTORMTemplate(seq = "10", name="sendUserList", type="Column", dataType="String", initial="", history="")
	private String sendUserList;
	
	@CTORMTemplate(seq = "11", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="")
	private String lastEventTimeKey;
	
	@CTORMTemplate(seq = "12", name="lastEventName", type="Column", dataType="String", initial="", history="")
	private String lastEventName;
	
	@CTORMTemplate(seq = "13", name="lastEventUser", type="Column", dataType="String", initial="", history="")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "14", name="lastEventComment", type="Column", dataType="String", initial="", history="")
	private String lastEventComment;
	
	@CTORMTemplate(seq = "15", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp lastEventTime;
	
	public String getEmsSendLog() {
		return emsSendLog;
	}

	public void setEmsSendLog(String emsSendLog) {
		this.emsSendLog = emsSendLog;
	}
	
	public String getAlarmID() {
		return alarmID;
	}

	public void setAlarmID(String alarmID) {
		this.alarmID = alarmID;
	}

	public String getAlarmTypeName() {
		return alarmTypeName;
	}

	public void setAlarmTypeName(String alarmTypeName) {
		this.alarmTypeName = alarmTypeName;
	}

	public String getKeyValue() {
		return keyValue;
	}

	public void setKeyValue(String keyValue) {
		this.keyValue = keyValue;
	}

	public String getEmailSendLog() {
		return emailSendLog;
	}

	public void setEmailSendLog(String emailSendLog) {
		this.emailSendLog = emailSendLog;
	}

	public String getWeChatSendLog() {
		return weChatSendLog;
	}

	public void setWeChatSendLog(String weChatSendLog) {
		this.weChatSendLog = weChatSendLog;
	}

	public String getFmbSendLog() {
		return fmbSendLog;
	}

	public void setFmbSendLog(String fmbSendLog) {
		this.fmbSendLog = fmbSendLog;
	}

	public String getSmsSendLog() {
		return smsSendLog;
	}

	public void setSmsSendLog(String smsSendLog) {
		this.smsSendLog = smsSendLog;
	}

	public String getMessageLog() {
		return messageLog;
	}

	public void setMessageLog(String messageLog) {
		this.messageLog = messageLog;
	}

	public String getSendUserList() {
		return sendUserList;
	}

	public void setSendUserList(String sendUserList) {
		this.sendUserList = sendUserList;
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
