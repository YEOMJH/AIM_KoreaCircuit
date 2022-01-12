package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class ProductFutureAction extends UdfAccessor {
	@CTORMTemplate(seq = "1", name = "productName", type = "Key", dataType = "String", initial = "", history = "")
	private String productName;
	@CTORMTemplate(seq = "2", name = "factoryName", type = "Key", dataType = "String", initial = "", history = "")
	private String factoryName;
	@CTORMTemplate(seq = "3", name = "processFlowName", type = "Key", dataType = "String", initial = "", history = "")
	private String processFlowName;
	@CTORMTemplate(seq = "4", name = "processFlowVersion", type = "Key", dataType = "String", initial = "", history = "")
	private String processFlowVersion;
	@CTORMTemplate(seq = "5", name = "processOperationName", type = "Key", dataType = "String", initial = "", history = "")
	private String processOperationName;
	@CTORMTemplate(seq = "6", name = "processOperationVersion", type = "Key", dataType = "String", initial = "", history = "")
	private String processOperationVersion;
	@CTORMTemplate(seq = "7", name = "unitName", type = "Key", dataType = "Key", initial = "", history = "")
	private String unitName;
	@CTORMTemplate(seq = "8", name = "reasonCode", type = "Key", dataType = "Key", initial = "", history = "")
	private String reasonCode;
	@CTORMTemplate(seq = "9", name = "reasonCodeType", type = "Column", dataType = "String", initial = "", history = "")
	private String reasonCodeType;
	@CTORMTemplate(seq = "10", name = "actionName", type = "Column", dataType = "String", initial = "", history = "")
	private String actionName;
	@CTORMTemplate(seq = "11", name = "actionType", type = "Column", dataType = "String", initial = "", history = "")
	private String actionType;
	@CTORMTemplate(seq = "12", name = "lotName", type = "Column", dataType = "String", initial = "", history = "")
	private String lotName;
	@CTORMTemplate(seq = "13", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventUser;
	@CTORMTemplate(seq = "14", name = "lastEventName", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventName;
	@CTORMTemplate(seq = "15", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventComment;
	@CTORMTemplate(seq = "16", name = "lastEventTimekey", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventTimekey;
	@CTORMTemplate(seq = "17", name = "lastEventTime", type = "Column", dataType = "Timestamp", initial = "", history = "N")
	private Timestamp lastEventTime;
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	public String getFactoryName() {
		return factoryName;
	}
	public void setFactoryName(String factoryName) {
		this.factoryName = factoryName;
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
	public String getUnitName() {
		return unitName;
	}
	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}
	public String getReasonCode() {
		return reasonCode;
	}
	public void setReasonCode(String reasonCode) {
		this.reasonCode = reasonCode;
	}
	public String getReasonCodeType() {
		return reasonCodeType;
	}
	public void setReasonCodeType(String reasonCodeType) {
		this.reasonCodeType = reasonCodeType;
	}
	public String getActionName() {
		return actionName;
	}
	public void setActionName(String actionName) {
		this.actionName = actionName;
	}
	public String getActionType() {
		return actionType;
	}
	public void setActionType(String actionType) {
		this.actionType = actionType;
	}
	public String getLotName() {
		return lotName;
	}
	public void setLotName(String lotName) {
		this.lotName = lotName;
	}
	public String getLastEventUser() {
		return lastEventUser;
	}
	public void setLastEventUser(String lastEventUser) {
		this.lastEventUser = lastEventUser;
	}
	public String getLastEventName() {
		return lastEventName;
	}
	public void setLastEventName(String lastEventName) {
		this.lastEventName = lastEventName;
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
	public Timestamp getLastEventTime() {
		return lastEventTime;
	}
	public void setLastEventTime(Timestamp lastEventTime) {
		this.lastEventTime = lastEventTime;
	}	
}
