package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class ComponentMonitor extends UdfAccessor {
	public String getTimeKey() {
		return timeKey;
	}

	public void setTimeKey(String timeKey) {
		this.timeKey = timeKey;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public String getLotName() {
		return lotName;
	}

	public void setLotName(String lotName) {
		this.lotName = lotName;
	}

	public Timestamp getEventTime() {
		return eventTime;
	}

	public void setEventTime(Timestamp eventTime) {
		this.eventTime = eventTime;
	}

	public String getEventUser() {
		return eventUser;
	}

	public void setEventUser(String eventUser) {
		this.eventUser = eventUser;
	}

	public String getFactoryName() {
		return factoryName;
	}

	public void setFactoryName(String factoryName) {
		this.factoryName = factoryName;
	}

	public String getProductSpecName() {
		return productSpecName;
	}

	public void setProductSpecName(String productSpecName) {
		this.productSpecName = productSpecName;
	}

	public String getProductSpecVersion() {
		return productSpecVersion;
	}

	public void setProductSpecVersion(String productSpecVersion) {
		this.productSpecVersion = productSpecVersion;
	}

	public String getProcessOperationName() {
		return processOperationName;
	}

	public void setProcessOperationName(String processOperationName) {
		this.processOperationName = processOperationName;
	}

	public String getProcessOperationVersion() {
		return processOperationVersion;
	}

	public void setProcessOperationVersion(String processOperationVersion) {
		this.processOperationVersion = processOperationVersion;
	}

	public String getMachineName() {
		return machineName;
	}

	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}

	public String getMaterialLocationName() {
		return materialLocationName;
	}

	public void setMaterialLocationName(String materialLocationName) {
		this.materialLocationName = materialLocationName;
	}

	public String getRuleTime() {
		return ruleTime;
	}

	public void setRuleTime(String ruleTime) {
		this.ruleTime = ruleTime;
	}

	public String getEmailType() {
		return emailType;
	}

	public void setEmailType(String emailType) {
		this.emailType = emailType;
	}

	public String getSendFlag() {
		return sendFlag;
	}

	public void setSendFlag(String sendFlag) {
		this.sendFlag = sendFlag;
	}

	@CTORMTemplate(seq = "1", name="timeKey", type="Key", dataType="String", initial="", history="")
	private String timeKey;
	
	@CTORMTemplate(seq = "2", name="productName", type="Key", dataType="String", initial="", history="")
	private String productName;
	
	@CTORMTemplate(seq = "3", name="eventName", type="Key", dataType="String", initial="", history="")
	private String eventName;
	
	@CTORMTemplate(seq = "4", name="lotName", type="Column", dataType="String", initial="", history="")
	private String lotName;
	
	@CTORMTemplate(seq = "5", name="eventTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp eventTime;
	
	@CTORMTemplate(seq = "6", name="eventUser", type="Column", dataType="String", initial="", history="")
	private String eventUser;
	
	@CTORMTemplate(seq = "7", name="factoryName", type="Column", dataType="String", initial="", history="")
	private String factoryName;
	
	@CTORMTemplate(seq = "8", name="productSpecName", type="Column", dataType="String", initial="", history="")
	private String productSpecName;
	
	@CTORMTemplate(seq = "9", name="productSpecVersion", type="Column", dataType="String", initial="", history="")
	private String productSpecVersion;
	
	@CTORMTemplate(seq = "10", name="processOperationName", type="Column", dataType="String", initial="", history="")
	private String processOperationName;
	
	@CTORMTemplate(seq = "11", name="processOperationVersion", type="Column", dataType="String", initial="", history="")
	private String processOperationVersion;
	
	@CTORMTemplate(seq = "12", name="machineName", type="Column", dataType="String", initial="", history="")
	private String machineName;
	
	@CTORMTemplate(seq = "13", name="materialLocationName", type="Column", dataType="String", initial="", history="")
	private String materialLocationName;
	
	@CTORMTemplate(seq = "14", name="ruleTime", type="Column", dataType="String", initial="", history="")
	private String ruleTime;
	
	@CTORMTemplate(seq = "15", name="emailType", type="Column", dataType="String", initial="", history="")
	private String emailType;
	
	@CTORMTemplate(seq = "16", name="sendFlag", type="Column", dataType="String", initial="", history="")
	private String sendFlag;
}
