package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class TryRunControl extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name = "machineName", type = "Key", dataType = "String", initial = "", history = "")
	private String machineName;
	@CTORMTemplate(seq = "2", name = "processOperationName", type = "Key", dataType = "String", initial = "", history = "")
	private String processOperationName;
	@CTORMTemplate(seq = "3", name = "processOperationVersion", type = "Key", dataType = "String", initial = "", history = "")
	private String processOperationVersion;
	@CTORMTemplate(seq = "4", name = "factoryName", type = "Column", dataType = "String", initial = "", history = "")
	private String factoryName;
	@CTORMTemplate(seq = "5", name = "productSpecName", type = "Column", dataType = "String", initial = "", history = "")
	private String productSpecName;
	@CTORMTemplate(seq = "6", name = "productSpecVersion", type = "Column", dataType = "String", initial = "", history = "")
	private String productSpecVersion;
	@CTORMTemplate(seq = "7", name = "processFlowName", type = "Column", dataType = "String", initial = "", history = "")
	private String processFlowName;
	@CTORMTemplate(seq = "8", name = "processFlowVersion", type = "Column", dataType = "String", initial = "", history = "")
	private String processFlowVersion;
	@CTORMTemplate(seq = "9", name = "actionType", type = "Column", dataType = "String", initial = "", history = "")
	private String actionType;
	@CTORMTemplate(seq = "10", name = "useFlag", type = "Column", dataType = "String", initial = "", history = "")
	private String useFlag;
	@CTORMTemplate(seq = "11", name = "useCount", type = "Column", dataType = "Number", initial = "", history = "")
	private Number useCount;
	@CTORMTemplate(seq = "12", name = "useCountLimit", type = "Column", dataType = "Number", initial = "", history = "")
	private Number useCountLimit;
	@CTORMTemplate(seq = "13", name = "lastEventName", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventName;
	@CTORMTemplate(seq = "14", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventUser;
	@CTORMTemplate(seq = "15", name = "lastEventTime", type = "Column", dataType = "Timestamp", initial = "", history = "N")
	private Timestamp lastEventTime;
	@CTORMTemplate(seq = "16", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventComment;
	@CTORMTemplate(seq = "17", name = "lastEventTimekey", type = "Column", dataType = "String", initial = "", history = "N")
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
	public String getActionType() {
		return actionType;
	}
	public void setActionType(String actionType) {
		this.actionType = actionType;
	}
	public String getUseFlag() {
		return useFlag;
	}
	public void setUseFlag(String useFlag) {
		this.useFlag = useFlag;
	}
	public Number getUseCount() {
		return useCount;
	}
	public void setUseCount(Number useCount) {
		this.useCount = useCount;
	}
	public Number getUseCountLimit() {
		return useCountLimit;
	}
	public void setUseCountLimit(Number useCountLimit) {
		this.useCountLimit = useCountLimit;
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

