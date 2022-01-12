package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;


public class BankQueueTime extends UdfAccessor{
	
	@CTORMTemplate(seq = "1", name = "lotName", type = "Key", dataType = "String", initial = "", history = "")
	private String lotName;
	
	@CTORMTemplate(seq = "2", name = "bankType", type = "Column", dataType = "String", initial = "", history = "")
	private String bankType;
	
	@CTORMTemplate(seq = "3", name = "factoryName", type = "Column", dataType = "String", initial = "", history = "")
	private String factoryName;
	
	@CTORMTemplate(seq = "4", name = "productSpecName", type = "Column", dataType = "String", initial = "", history = "")
	private String productSpecName;
	
	@CTORMTemplate(seq = "5", name = "productSpecVersion", type = "Column", dataType = "String", initial = "", history = "")
	private String productSpecVersion;
	
	@CTORMTemplate(seq = "6", name = "processFlowName", type = "Column", dataType = "String", initial = "", history = "")
	private String processFlowName;
	
	@CTORMTemplate(seq = "7", name = "processFlowVersion", type = "Column", dataType = "String", initial = "", history = "")
	private String processFlowVersion;
	
	@CTORMTemplate(seq = "8", name = "processOperationName", type = "Column", dataType = "String", initial = "", history = "")
	private String processOperationName;
	
	@CTORMTemplate(seq = "9", name = "processOperationVersion", type = "Column", dataType = "String", initial = "", history = "")
	private String processOperationVersion;
	
	@CTORMTemplate(seq = "10", name = "toFactoryName", type = "Column", dataType = "String", initial = "", history = "")
	private String toFactoryName;
	
	@CTORMTemplate(seq = "11", name = "warningDurationLimit", type = "Column", dataType = "String", initial = "", history = "")
	private String warningDurationLimit;
	
	@CTORMTemplate(seq = "12", name = "interlockDurationLimit", type = "Column", dataType = "String", initial = "", history = "")
	private String interlockDurationLimit;
	
	@CTORMTemplate(seq = "13", name = "queueTimeState", type = "Column", dataType = "String", initial = "", history = "")
	private String queueTimeState;
	
	@CTORMTemplate(seq = "14", name = "enterTime", type = "Column", dataType = "Timestamp", initial = "", history = "")
	private Timestamp enterTime;
	
	@CTORMTemplate(seq = "15", name = "exitTime", type = "Column", dataType = "Timestamp", initial = "", history = "")
	private Timestamp exitTime;
	
	@CTORMTemplate(seq = "16", name = "warningTime", type = "Column", dataType = "Timestamp", initial = "", history = "")
	private Timestamp warningTime;
	
	@CTORMTemplate(seq = "17", name = "interlockTime", type = "Column", dataType = "Timestamp", initial = "", history = "")
	private Timestamp interlockTime;
	
	@CTORMTemplate(seq = "18", name = "resolveTime", type = "Column", dataType = "Timestamp", initial = "", history = "")
	private Timestamp resolveTime;
	
	@CTORMTemplate(seq = "19", name = "resolveUser", type = "Column", dataType = "String", initial = "", history = "")
	private String resolveUser;
	
	@CTORMTemplate(seq = "20", name = "alarmType", type = "Column", dataType = "String", initial = "", history = "")
	private String alarmType;
	
	@CTORMTemplate(seq = "21", name = "lastEventName", type = "Column", dataType = "String", initial = "", history = "")
	private String lastEventName;
	
	@CTORMTemplate(seq = "22", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "23", name = "lastEventTimekey", type = "Column", dataType = "String", initial = "", history = "")
	private String lastEventTimekey;

	public String getLotName() {
		return lotName;
	}

	public void setLotName(String lotName) {
		this.lotName = lotName;
	}

	public String getBankType() {
		return bankType;
	}

	public void setBankType(String bankType) {
		this.bankType = bankType;
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

	public String getProcessFlowName() {
		return processFlowName;
	}

	public void setProcessFlowName(String processFlowName) {
		this.processFlowName = processFlowName;
	}

	public String getProcessFlowVersion() {
		return processFlowVersion;
	}

	public void setProcessFlowVersion(String processFlowVersion) {
		this.processFlowVersion = processFlowVersion;
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

	public String getToFactoryName() {
		return toFactoryName;
	}

	public void setToFactoryName(String toFactoryName) {
		this.toFactoryName = toFactoryName;
	}

	public String getWarningDurationLimit() {
		return warningDurationLimit;
	}

	public void setWarningDurationLimit(String warningDurationLimit) {
		this.warningDurationLimit = warningDurationLimit;
	}

	public String getInterlockDurationLimit() {
		return interlockDurationLimit;
	}

	public void setInterlockDurationLimit(String interlockDurationLimit) {
		this.interlockDurationLimit = interlockDurationLimit;
	}

	public String getQueueTimeState() {
		return queueTimeState;
	}

	public void setQueueTimeState(String queueTimeState) {
		this.queueTimeState = queueTimeState;
	}

	public Timestamp getEnterTime() {
		return enterTime;
	}

	public void setEnterTime(Timestamp enterTime) {
		this.enterTime = enterTime;
	}

	public Timestamp getExitTime() {
		return exitTime;
	}

	public void setExitTime(Timestamp exitTime) {
		this.exitTime = exitTime;
	}

	public Timestamp getWarningTime() {
		return warningTime;
	}

	public void setWarningTime(Timestamp warningTime) {
		this.warningTime = warningTime;
	}

	public Timestamp getInterlockTime() {
		return interlockTime;
	}

	public void setInterlockTime(Timestamp interlockTime) {
		this.interlockTime = interlockTime;
	}

	public Timestamp getResolveTime() {
		return resolveTime;
	}

	public void setResolveTime(Timestamp resolveTime) {
		this.resolveTime = resolveTime;
	}

	public String getResolveUser() {
		return resolveUser;
	}

	public void setResolveUser(String resolveUser) {
		this.resolveUser = resolveUser;
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

	public String getLastEventUser() {
		return lastEventUser;
	}

	public void setLastEventUser(String lastEventUser) {
		this.lastEventUser = lastEventUser;
	}

	public String getLastEventTimekey() {
		return lastEventTimekey;
	}

	public void setLastEventTimekey(String lastEventTimekey) {
		this.lastEventTimekey = lastEventTimekey;
	}
	
	
	
}
