package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class MQCRunControl extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name = "machineName", type = "Key", dataType = "String", initial = "", history = "")
	private String machineName;
	@CTORMTemplate(seq = "2", name = "processOperationName", type = "Key", dataType = "String", initial = "", history = "")
	private String processOperationName;
	@CTORMTemplate(seq = "3", name = "processOperationVersion", type = "Key", dataType = "String", initial = "", history = "")
	private String processOperationVersion;
	@CTORMTemplate(seq = "4", name = "actualProductQty", type = "Column", dataType = "Number", initial = "", history = "")
	private Number actualProductQty;
	@CTORMTemplate(seq = "5", name = "factoryName", type = "Column", dataType = "String", initial = "", history = "")
	private String factoryName;
	@CTORMTemplate(seq = "6", name = "maxProductQtyByChamber", type = "Column", dataType = "Number", initial = "", history = "")
	private Number maxProductQtyByChamber;
	@CTORMTemplate(seq = "7", name = "maxMQCQtyByChamber", type = "Column", dataType = "Number", initial = "", history = "")
	private Number maxMQCQtyByChamber;
	@CTORMTemplate(seq = "8", name = "chamberQty", type = "Column", dataType = "Number", initial = "", history = "")
	private Number chamberQty;
	@CTORMTemplate(seq = "9", name = "mqcProcessFlowName", type = "Column", dataType = "String", initial = "", history = "")
	private String mqcProcessFlowName;
	@CTORMTemplate(seq = "10", name = "mqcProcessFlowVersion", type = "Column", dataType = "String", initial = "", history = "")
	private String mqcProcessFlowVersion;
	@CTORMTemplate(seq = "11", name = "mqcProcessQty", type = "Column", dataType = "Number", initial = "", history = "")
	private Number mqcProcessQty;
	@CTORMTemplate(seq = "12", name = "lastEventName", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventName;
	@CTORMTemplate(seq = "13", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventUser;
	@CTORMTemplate(seq = "14", name = "lastEventTime", type = "Column", dataType = "Timestamp", initial = "", history = "N")
	private Timestamp lastEventTime;
	@CTORMTemplate(seq = "15", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventComment;
	@CTORMTemplate(seq = "16", name = "lastEventTimekey", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventTimekey;
	
	
	public String getMachineName() {
		return machineName;
	}
	public void setMachineName(String machineName) {
		this.machineName = machineName;
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
	public Number getActualProductQty() {
		return actualProductQty;
	}
	public void setActualProductQty(Number actualProductQty) {
		this.actualProductQty = actualProductQty;
	}
	public String getFactoryName() {
		return factoryName;
	}
	public void setFactoryName(String factoryName) {
		this.factoryName = factoryName;
	}
	public Number getMaxProductQtyByChamber() {
		return maxProductQtyByChamber;
	}
	public void setMaxProductQtyByChamber(Number maxProductQtyByChamber) {
		this.maxProductQtyByChamber = maxProductQtyByChamber;
	}
	public Number getMaxMQCQtyByChamber() {
		return maxMQCQtyByChamber;
	}
	public void setMaxMQCQtyByChamber(Number maxMQCQtyByChamber) {
		this.maxMQCQtyByChamber = maxMQCQtyByChamber;
	}
	public Number getChamberQty() {
		return chamberQty;
	}
	public void setChamberQty(Number chamberQty) {
		this.chamberQty = chamberQty;
	}
	public String getMqcProcessFlowName() {
		return mqcProcessFlowName;
	}
	public void setMqcProcessFlowName(String mqcProcessFlowName) {
		this.mqcProcessFlowName = mqcProcessFlowName;
	}
	public String getMqcProcessFlowVersion() {
		return mqcProcessFlowVersion;
	}
	public void setMqcProcessFlowVersion(String mqcProcessFlowVersion) {
		this.mqcProcessFlowVersion = mqcProcessFlowVersion;
	}
	public Number getMqcProcessQty() {
		return mqcProcessQty;
	}
	public void setMqcProcessQty(Number mqcProcessQty) {
		this.mqcProcessQty = mqcProcessQty;
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
	public Timestamp getLastEventTime() {
		return lastEventTime;
	}
	public void setLastEventTime(Timestamp lastEventTime) {
		this.lastEventTime = lastEventTime;
	}
	public String getLastEventComment() {
		return lastEventComment;
	}
	public void setLastEventComment(String lastEventComment) {
		this.lastEventComment = lastEventComment;
	}
	public String getLastEventTimekey() {
		return lastEventTimekey;
	}
	public void setLastEventTimekey(String lastEventTimekey) {
		this.lastEventTimekey = lastEventTimekey;
	}
}
