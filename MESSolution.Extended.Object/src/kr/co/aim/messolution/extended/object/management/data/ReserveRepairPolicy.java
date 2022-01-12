package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class ReserveRepairPolicy extends UdfAccessor{
	@CTORMTemplate(seq = "1", name="factoryName", type="Key", dataType="String", initial="", history="")
	private String factoryName;
	@CTORMTemplate(seq = "2", name="productSpecName", type="Key", dataType="String", initial="", history="")
	private String productSpecName;
	@CTORMTemplate(seq = "3", name="productSpecVersion", type="Key", dataType="String", initial="", history="")
	private String productSpecVersion;
	@CTORMTemplate(seq = "4", name="processFlowName", type="Key", dataType="String", initial="", history="")
	private String processFlowName;
	@CTORMTemplate(seq = "5", name="processFlowVersion", type="Key", dataType="String", initial="", history="")
	private String processFlowVersion;
	@CTORMTemplate(seq = "6", name="processOperationName", type="Key", dataType="String", initial="", history="")
	private String processOperationName;
	@CTORMTemplate(seq = "7", name="processOperationVersion", type="Key", dataType="String", initial="", history="")
	private String processOperationVersion;
	@CTORMTemplate(seq = "8", name="repairFlowName", type="Column", dataType="String", initial="", history="")
	private String repairFlowName;
	@CTORMTemplate(seq = "9", name="repairFlowVersion", type="Column", dataType="String", initial="", history="")
	private String repairFlowVersion;
	@CTORMTemplate(seq = "10", name="repairOperationName", type="Column", dataType="String", initial="", history="")
	private String repairOperationName;
	@CTORMTemplate(seq = "11", name="repairOperationVersion", type="Column", dataType="String", initial="", history="")
	private String repairOperationVersion;
	@CTORMTemplate(seq = "12", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;
	@CTORMTemplate(seq = "13", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimeKey;
	@CTORMTemplate(seq = "14", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;
	@CTORMTemplate(seq = "15", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;
	@CTORMTemplate(seq = "16", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;
	@CTORMTemplate(seq = "17", name="actualRepairOperation", type="Column", dataType="String", initial="", history="N")
	private String actualRepairOperation;
	@CTORMTemplate(seq = "18", name="lastRepairOperation", type="Column", dataType="String", initial="", history="N")
	private String lastRepairOperation;
	@CTORMTemplate(seq = "19", name="lastRSOperForRP", type="Column", dataType="String", initial="", history="N")
	private String lastRSOperForRP;
	
	
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
	public String getRepairFlowName() {
		return repairFlowName;
	}
	public void setRepairFlowName(String repairFlowName) {
		this.repairFlowName = repairFlowName;
	}
	public String getRepairFlowVersion() {
		return repairFlowVersion;
	}
	public void setRepairFlowVersion(String repairFlowVersion) {
		this.repairFlowVersion = repairFlowVersion;
	}
	public String getRepairOperationName() {
		return repairOperationName;
	}
	public void setRepairOperationName(String repairOperationName) {
		this.repairOperationName = repairOperationName;
	}
	public String getRepairOperationVersion() {
		return repairOperationVersion;
	}
	public void setRepairOperationVersion(String repairOperationVersion) {
		this.repairOperationVersion = repairOperationVersion;
	}
	public String getLastEventName() {
		return lastEventName;
	}
	public void setLastEventName(String lastEventName) {
		this.lastEventName = lastEventName;
	}
	public String getLastEventTimeKey() {
		return lastEventTimeKey;
	}
	public void setLastEventTimeKey(String lastEventTimeKey) {
		this.lastEventTimeKey = lastEventTimeKey;
	}
	public Timestamp getLastEventTime() {
		return lastEventTime;
	}
	public void setLastEventTime(Timestamp lastEventTime) {
		this.lastEventTime = lastEventTime;
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
	public String getActualRepairOperation() {
		return actualRepairOperation;
	}
	public void setActualRepairOperation(String actualRepairOperation) {
		this.actualRepairOperation = actualRepairOperation;
	}
	public String getLastRepairOperation() {
		return lastRepairOperation;
	}
	public void setLastRepairOperation(String lastRepairOperation) {
		this.lastRepairOperation = lastRepairOperation;
	}
	public String getLastRSOperForRP() {
		return lastRSOperForRP;
	}
	public void setLastRSOperForRP(String lastRSOperForRP) {
		this.lastRSOperForRP = lastRSOperForRP;
	}
}
