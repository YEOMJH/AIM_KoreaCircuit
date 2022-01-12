package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class SuperProductRequest extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="productRequestName", type="Key", dataType="String", initial="", history="")
	private String productRequestName;
	
	@CTORMTemplate(seq = "2", name="productRequestType", type="Column", dataType="String", initial="", history="")
	private String productRequestType;

	@CTORMTemplate(seq = "3", name="factoryName", type="Column", dataType="String", initial="", history="")
	private String factoryName;

	@CTORMTemplate(seq = "4", name="productSpecName", type="Column", dataType="String", initial="", history="")
	private String productSpecName;

	@CTORMTemplate(seq = "5", name="productSpecVersion", type="Column", dataType="String", initial="", history="")
	private String productSpecVersion;

	@CTORMTemplate(seq = "6", name="planReleasedTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp planReleasedTime;

	@CTORMTemplate(seq = "7", name="planFinishedTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp planFinishedTime;

	@CTORMTemplate(seq = "8", name="planQuantity", type="Column", dataType="Number", initial="", history="")
	private long planQuantity;

	@CTORMTemplate(seq = "9", name="createdQuantity", type="Column", dataType="Number", initial="", history="")
	private long createdQuantity;

	@CTORMTemplate(seq = "10", name="releasedQuantity", type="Column", dataType="Number", initial="", history="")
	private long releasedQuantity;

	@CTORMTemplate(seq = "11", name="finishedQuantity", type="Column", dataType="Number", initial="", history="")
	private long finishedQuantity;

	@CTORMTemplate(seq = "12", name="scrappedQuantity", type="Column", dataType="Number", initial="", history="")
	private long scrappedQuantity;

	@CTORMTemplate(seq = "13", name="productRequestState", type="Column", dataType="String", initial="", history="")
	private String productRequestState;

	@CTORMTemplate(seq = "14", name="productRequestHoldState", type="Column", dataType="String", initial="", history="")
	private String productRequestHoldState;

	@CTORMTemplate(seq = "15", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;

	@CTORMTemplate(seq = "16", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimeKey;

	@CTORMTemplate(seq = "17", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;

	@CTORMTemplate(seq = "18", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;

	@CTORMTemplate(seq = "19", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;

	@CTORMTemplate(seq = "20", name="lastEventFlag", type="Column", dataType="String", initial="", history="N")
	private String lastEventFlag;

	@CTORMTemplate(seq = "21", name="createTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp createTime;

	@CTORMTemplate(seq = "22", name="createUser", type="Column", dataType="String", initial="", history="")
	private String createUser;
	
	@CTORMTemplate(seq = "23", name="releaseTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp releaseTime;

	@CTORMTemplate(seq = "24", name="releaseUser", type="Column", dataType="String", initial="", history="")
	private String releaseUser;
	
	@CTORMTemplate(seq = "25", name="completeTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp completeTime;

	@CTORMTemplate(seq = "26", name="completeUser", type="Column", dataType="String", initial="", history="")
	private String completeUser;

	@CTORMTemplate(seq = "27", name="planSequence", type="Column", dataType="String", initial="", history="")
	private String planSequence;	
	
	@CTORMTemplate(seq = "28", name="processFlowName", type="Column", dataType="String", initial="", history="")
	private String processFlowName;

	@CTORMTemplate(seq = "29", name="processFlowVersion", type="Column", dataType="String", initial="", history="")
	private String processFlowVersion;

	@CTORMTemplate(seq = "30", name="autoShippingFlag", type="Column", dataType="String", initial="", history="")
	private String autoShippingFlag;

	@CTORMTemplate(seq = "31", name="description", type="Column", dataType="String", initial="", history="")
	private String description;

	@CTORMTemplate(seq = "32", name="machineName", type="Column", dataType="String", initial="", history="")
	private String machineName;

	@CTORMTemplate(seq = "33", name="subProductionType", type="Column", dataType="String", initial="", history="")
	private String subProductionType;
	
	@CTORMTemplate(seq = "34", name="productType", type="Column", dataType="String", initial="", history="")
	private String productType;
	
	@CTORMTemplate(seq = "35", name="projectProductRequestName", type="Column", dataType="String", initial="", history="")
	private String projectProductRequestName;
	
	@CTORMTemplate(seq = "36", name="costDepartment", type="Column", dataType="String", initial="", history="")
	private String costDepartment;
	
	@CTORMTemplate(seq = "37", name="riskFlag", type="Column", dataType="String", initial="", history="")
	private String riskFlag;

	public SuperProductRequest() {
		super();
	}

	public String getProductRequestName() {
		return productRequestName;
	}

	public void setProductRequestName(String productRequestName) {
		this.productRequestName = productRequestName;
	}

	public String getProductRequestType() {
		return productRequestType;
	}

	public void setProductRequestType(String productRequestType) {
		this.productRequestType = productRequestType;
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

	public Timestamp getPlanReleasedTime() {
		return planReleasedTime;
	}

	public void setPlanReleasedTime(Timestamp planReleasedTime) {
		this.planReleasedTime = planReleasedTime;
	}

	public Timestamp getPlanFinishedTime() {
		return planFinishedTime;
	}

	public void setPlanFinishedTime(Timestamp planFinishedTime) {
		this.planFinishedTime = planFinishedTime;
	}

	public long getPlanQuantity() {
		return planQuantity;
	}

	public void setPlanQuantity(long planQuantity) {
		this.planQuantity = planQuantity;
	}

	public long getCreatedQuantity() {
		return createdQuantity;
	}

	public void setCreatedQuantity(long createdQuantity) {
		this.createdQuantity = createdQuantity;
	}

	public long getReleasedQuantity() {
		return releasedQuantity;
	}

	public void setReleasedQuantity(long releasedQuantity) {
		this.releasedQuantity = releasedQuantity;
	}

	public long getFinishedQuantity() {
		return finishedQuantity;
	}

	public void setFinishedQuantity(long finishedQuantity) {
		this.finishedQuantity = finishedQuantity;
	}

	public long getScrappedQuantity() {
		return scrappedQuantity;
	}

	public void setScrappedQuantity(long scrappedQuantity) {
		this.scrappedQuantity = scrappedQuantity;
	}

	public String getProductRequestState() {
		return productRequestState;
	}

	public void setProductRequestState(String productRequestState) {
		this.productRequestState = productRequestState;
	}

	public String getProductRequestHoldState() {
		return productRequestHoldState;
	}

	public void setProductRequestHoldState(String productRequestHoldState) {
		this.productRequestHoldState = productRequestHoldState;
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

	public String getLastEventFlag() {
		return lastEventFlag;
	}

	public void setLastEventFlag(String lastEventFlag) {
		this.lastEventFlag = lastEventFlag;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public Timestamp getReleaseTime() {
		return releaseTime;
	}

	public void setReleaseTime(Timestamp releaseTime) {
		this.releaseTime = releaseTime;
	}

	public String getReleaseUser() {
		return releaseUser;
	}

	public void setReleaseUser(String releaseUser) {
		this.releaseUser = releaseUser;
	}

	public Timestamp getCompleteTime() {
		return completeTime;
	}

	public void setCompleteTime(Timestamp completeTime) {
		this.completeTime = completeTime;
	}

	public String getCompleteUser() {
		return completeUser;
	}

	public void setCompleteUser(String completeUser) {
		this.completeUser = completeUser;
	}

	public String getPlanSequence() {
		return planSequence;
	}

	public void setPlanSequence(String planSequence) {
		this.planSequence = planSequence;
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

	public String getAutoShippingFlag() {
		return autoShippingFlag;
	}

	public void setAutoShippingFlag(String autoShippingFlag) {
		this.autoShippingFlag = autoShippingFlag;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getMachineName() {
		return machineName;
	}

	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}

	public String getSubProductionType() {
		return subProductionType;
	}

	public void setSubProductionType(String subProductionType) {
		this.subProductionType = subProductionType;
	}

	public String getProductType()
	{
		return productType;
	}

	public void setProductType(String productType)
	{
		this.productType = productType;
	}

	public String getRiskFlag() {
		return riskFlag;
	}

	public void setRiskFlag(String riskFlag) {
		this.riskFlag = riskFlag;
	}
	

	public String getProjectProductRequestName()
	{
		return projectProductRequestName;
	}

	public void setProjectProductRequestName(String projectProductRequestName)
	{
		this.projectProductRequestName = projectProductRequestName;
	}

	public String getCostDepartment()
	{
		return costDepartment;
	}

	public void setCostDepartment(String costDepartment)
	{
		this.costDepartment = costDepartment;
	}
}