package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class AlarmActionDef extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="alarmCode", type="Key", dataType="String", initial="", history="")
	private String  alarmCode            ;
	@CTORMTemplate(seq = "2", name="alarmType", type="Key", dataType="String", initial="", history="")
	private String  alarmType            ;
	@CTORMTemplate(seq = "3", name="machineName", type="Key", dataType="String", initial="", history="")
	private String  machineName          ;
	@CTORMTemplate(seq = "4", name="unitName", type="Key", dataType="String", initial="", history="")
	private String  unitName             ;
	@CTORMTemplate(seq = "5", name="actionName", type="Key", dataType="String", initial="", history="")
	private String  actionName           ;
	@CTORMTemplate(seq = "6", name="seq", type="Column", dataType="String", initial="", history="")
	private String  seq                  ;
	@CTORMTemplate(seq = "7", name="releaseType", type="Column", dataType="String", initial="", history="")
	private String  releaseType             ;
	@CTORMTemplate(seq = "8", name="mailFlag", type="Column", dataType="String", initial="", history="")
	private String  mailFlag             ;
	@CTORMTemplate(seq = "9", name="holdFlag", type="Column", dataType="String", initial="", history="")
	private String  holdFlag             ;
	@CTORMTemplate(seq = "10", name="machineLockFlag", type="Column", dataType="String", initial="", history="")
	private String  machineLockFlag      ;
	@CTORMTemplate(seq = "11", name="reasonCodeType", type="Column", dataType="String", initial="", history="")
	private String  reasonCodeType       ;
	@CTORMTemplate(seq = "12", name="reasonCode", type="Column", dataType="String", initial="", history="")
	private String  reasonCode           ;
	@CTORMTemplate(seq = "13", name="lastEventName", type="Column", dataType="String", initial="", history="")
	private String  lastEventName        ;
	@CTORMTemplate(seq = "14", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp  lastEventTime     ;
	@CTORMTemplate(seq = "15", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="")
	private String  lastEventTimeKey      ;
	@CTORMTemplate(seq = "16", name="lastEventUser", type="Column", dataType="String", initial="", history="")
	private String  lastEventUser        ;
	@CTORMTemplate(seq = "17", name="lastEventComment", type="Column", dataType="String", initial="", history="")
	private String  lastEventComment     ;
	

	//instantiation
	public AlarmActionDef()
	{
		
	}

	public AlarmActionDef(String alarmCode, String alarmType, String machineName, String unitName,String actionName)
	{
		setAlarmCode(alarmCode);
	    setAlarmType(alarmType);
		setActionName(actionName);
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

	public String getActionName() {
		return actionName;
	}

	public void setActionName(String actionName) {
		this.actionName = actionName;
	}

	public String getSeq() {
		return seq;
	}

	public void setSeq(String seq) {
		this.seq = seq;
	}

	public String getReleaseType() {
		return releaseType;
	}

	public void setReleaseType(String releaseType) {
		this.releaseType = releaseType;
	}

	public String getMailFlag() {
		return mailFlag;
	}

	public void setMailFlag(String mailFlag) {
		this.mailFlag = mailFlag;
	}

	public String getHoldFlag() {
		return holdFlag;
	}

	public void setHoldFlag(String holdFlag) {
		this.holdFlag = holdFlag;
	}

	public String getMachineLockFlag() {
		return machineLockFlag;
	}

	public void setMachineLockFlag(String machineLockFlag) {
		this.machineLockFlag = machineLockFlag;
	}

	public String getReasonCodeType() {
		return reasonCodeType;
	}

	public void setReasonCodeType(String reasonCodeType) {
		this.reasonCodeType = reasonCodeType;
	}

	public String getReasonCode() {
		return reasonCode;
	}

	public void setReasonCode(String reasonCode) {
		this.reasonCode = reasonCode;
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
