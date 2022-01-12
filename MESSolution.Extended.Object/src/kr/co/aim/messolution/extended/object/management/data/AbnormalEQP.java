package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class AbnormalEQP extends UdfAccessor{
	@CTORMTemplate(seq = "1", name="abnormalName", type="Key", dataType="String", initial="", history="")
	private String abnormalName;	
	
	@CTORMTemplate(seq = "2", name="abnormalEQPName", type="Column", dataType="String", initial="", history="")
	private String abnormalEQPName;
	
	@CTORMTemplate(seq = "3", name="abnormalState", type="Column", dataType="String", initial="", history="")
	private String abnormalState;
	
	@CTORMTemplate(seq = "4", name="lotName", type="Column", dataType="String", initial="", history="")
	private String lotName;
	
	@CTORMTemplate(seq = "5", name="createUser", type="Column", dataType="String", initial="", history="")
	private String createUser;
	
	@CTORMTemplate(seq = "6", name="createTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp createTime;
	
	@CTORMTemplate(seq = "7", name="reasonCode", type="Column", dataType="String", initial="", history="")
	private String reasonCode;
	
	@CTORMTemplate(seq = "8", name="department", type="Column", dataType="String", initial="", history="")
	private String department;
	
	@CTORMTemplate(seq = "9", name="abnormalType", type="Column", dataType="String", initial="", history="")
	private String abnormalType;
	
	@CTORMTemplate(seq = "10", name="abnormalLevel", type="Column", dataType="String", initial="", history="")
	private String abnormalLevel;
	
	@CTORMTemplate(seq = "11", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;
	
	@CTORMTemplate(seq = "12", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;
	
	@CTORMTemplate(seq = "13", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimeKey;
	
	@CTORMTemplate(seq = "14", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "15", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "16", name="handler", type="Column", dataType="String", initial="", history="")
	private String handler;
	
	@CTORMTemplate(seq = "17", name="phone", type="Column", dataType="String", initial="", history="")
	private String phone;
	
	@CTORMTemplate(seq = "18", name="startTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp startTime;
	
	@CTORMTemplate(seq = "19", name="unitName", type="Column", dataType="String", initial="", history="")
	private String unitName;
	
	@CTORMTemplate(seq = "20", name="subUnitName", type="Column", dataType="String", initial="", history="")
	private String subUnitName;
	
	@CTORMTemplate(seq = "21", name="reason", type="Column", dataType="String", initial="", history="")
	private String reason;
	
	@CTORMTemplate(seq = "22", name="causeEQPName", type="Column", dataType="String", initial="", history="")
	private String causeEQPName;
	
	@CTORMTemplate(seq = "23", name="productNameList", type="Column", dataType="String", initial="", history="")
	private String productNameList;
	
	@CTORMTemplate(seq = "24", name="sendFlag", type="Column", dataType="String", initial="", history="")
	private String sendFlag;
	
	@CTORMTemplate(seq = "25", name="planFinishTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp planFinishTime;
	
	@CTORMTemplate(seq = "26", name="realFinishTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp realFinishTime;
	
	@CTORMTemplate(seq = "27", name="description", type="Column", dataType="String", initial="", history="")
	private String description;
	
	@CTORMTemplate(seq = "28", name="process", type="Column", dataType="String", initial="", history="")
	private String process;
	
	@CTORMTemplate(seq = "29", name="productionType", type="Column", dataType="String", initial="", history="")
	private String productionType;
	
	@CTORMTemplate(seq = "30", name="confirmFlag", type="Column", dataType="String", initial="", history="")
	private String confirmFlag;
	
	@CTORMTemplate(seq = "31", name="abnormalOperationName", type="Column", dataType="String", initial="", history="")
	private String abnormalOperationName;
	
	@CTORMTemplate(seq = "32", name="factoryName", type="Column", dataType="String", initial="", history="")
	private String factoryName;
	
	@CTORMTemplate(seq = "33", name="canReleaseHoldLotFlag", type="Column", dataType="String", initial="", history="")
	private String canReleaseHoldLotFlag;
	
	@CTORMTemplate(seq = "34", name="qaJudgeResult", type="Column", dataType="String", initial="", history="")
	private String qaJudgeResult;
	
	@CTORMTemplate(seq = "35", name="productSpecName", type="Column", dataType="String", initial="", history="")
	private String productSpecName;

	public String getQAJudgeResult() {
		return qaJudgeResult;
	}

	public void setQAJudgeResult(String qaJudgeResult) {
		this.qaJudgeResult = qaJudgeResult;
	}
	
	public String getCanReleaseHoldLotFlag() {
		return canReleaseHoldLotFlag;
	}

	public void setCanReleaseHoldLotFlag(String canReleaseHoldLotFlag) {
		this.canReleaseHoldLotFlag = canReleaseHoldLotFlag;
	}

	public String getAbnormalOperationName() {
		return abnormalOperationName;
	}

	public void setAbnormalOperationName(String abnormalOperationName) {
		this.abnormalOperationName = abnormalOperationName;
	}

	public String getProductionType() {
		return productionType;
	}

	public void setProductionType(String productionType) {
		this.productionType = productionType;
	}

	public String getProcess() {
		return process;
	}

	public void setProcess(String process) {
		this.process = process;
	}

	//instantiation
	public AbnormalEQP()
	{
		
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAbnormalName() {
		return abnormalName;
	}

	public void setAbnormalName(String abnormalName) {
		this.abnormalName = abnormalName;
	}

	public String getAbnormalEQPName() {
		return abnormalEQPName;
	}

	public void setAbnormalEQPName(String abnormalEQPName) {
		this.abnormalEQPName = abnormalEQPName;
	}

	public String getAbnormalState() {
		return abnormalState;
	}

	public void setAbnormalState(String abnormalState) {
		this.abnormalState = abnormalState;
	}

	public String getLotName() {
		return lotName;
	}

	public void setLotName(String lotName) {
		this.lotName = lotName;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public String getReasonCode() {
		return reasonCode;
	}

	public void setReasonCode(String reasonCode) {
		this.reasonCode = reasonCode;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public String getAbnormalType() {
		return abnormalType;
	}

	public void setAbnormalType(String abnormalType) {
		this.abnormalType = abnormalType;
	}

	public String getAbnormalLevel() {
		return abnormalLevel;
	}

	public void setAbnormalLevel(String abnormalLevel) {
		this.abnormalLevel = abnormalLevel;
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

	public Timestamp getLastEventTime() {
		return lastEventTime;
	}

	public void setLastEventTime(Timestamp lastEventTime) {
		this.lastEventTime = lastEventTime;
	}

	public String getHandler() {
		return handler;
	}

	public void setHandler(String handler) {
		this.handler = handler;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public Timestamp getStartTime() {
		return startTime;
	}

	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}

	public String getUnitName() {
		return unitName;
	}

	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}

	public String getSubUnitName() {
		return subUnitName;
	}

	public void setSubUnitName(String subUnitName) {
		this.subUnitName = subUnitName;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getCauseEQPName() {
		return causeEQPName;
	}

	public void setCauseEQPName(String causeEQPName) {
		this.causeEQPName = causeEQPName;
	}

	public String getProductNameList() {
		return productNameList;
	}

	public void setProductNameList(String productNameList) {
		this.productNameList = productNameList;
	}
	
	public String getSendFlag() {
		return sendFlag;
	}

	public void setSendFlag(String sendFlag) {
		this.sendFlag = sendFlag;
	}
	
	public String getFactoryName() {
		return factoryName;
	}

	public void setFactoryName(String factoryName) {
		this.factoryName = factoryName;
	}
	
	public String getConfirmFlag() {
		return confirmFlag;
	}

	public void setConfirmFlag(String ConfirmFlag) {
		this.confirmFlag = ConfirmFlag;
	}
	
	public Timestamp getPlanFinishTime() {
		return planFinishTime;
	}

	public void setPlanFinishTime(Timestamp planFinishTime) {
		this.planFinishTime = planFinishTime;
	}

	public Timestamp getRealFinishTime() {
		return realFinishTime;
	}

	public void setRealFinishTime(Timestamp realFinishTime) {
		this.realFinishTime = realFinishTime;
	}

	public String getProductSpecName() {
		return productSpecName;
	}

	public void setProductSpecName(String productSpecName) {
		this.productSpecName = productSpecName;
	}
}
