package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class DSPProductRequestPlan extends UdfAccessor{
	
	@CTORMTemplate(seq = "1", name="planName", type="Key", dataType="String", initial="", history="")
	private String planName;
	
	@CTORMTemplate(seq = "2", name="productRequestName", type="Key", dataType="String", initial="", history="")
	private String productRequestName;
	
	@CTORMTemplate(seq = "3", name="factoryName", type="Key", dataType="String", initial="", history="")
	private String factoryName;

	@CTORMTemplate(seq = "4", name="productSpecName", type="Key", dataType="String", initial="", history="")
	private String productSpecName;

	@CTORMTemplate(seq = "5", name="productSpecVersion", type="Key", dataType="String", initial="", history="")
	private String productSpecVersion;
	
	@CTORMTemplate(seq = "6", name="processFlowName", type="Key", dataType="String", initial="", history="")
	private String processFlowName;

	@CTORMTemplate(seq = "7", name="processFlowVersion", type="Key", dataType="String", initial="", history="")
	private String processFlowVersion;
	
	@CTORMTemplate(seq = "8", name="processOperationName", type="Key", dataType="String", initial="", history="")
	private String processOperationName;

	@CTORMTemplate(seq = "9", name="processOperationVersion", type="Key", dataType="String", initial="", history="")
	private String processOperationVersion;
	
	@CTORMTemplate(seq = "10", name="machineName", type="Column", dataType="String", initial="", history="")
	private String machineName;
	
	@CTORMTemplate(seq = "11", name="planDate", type="Column", dataType="String", initial="", history="")
	private String planDate;
	
	@CTORMTemplate(seq = "12", name="priority", type="Column", dataType="Number", initial="", history="")
	private long priority;

	@CTORMTemplate(seq = "13", name="planLotQuantity", type="Column", dataType="Number", initial="", history="")
	private long planLotQuantity;

	@CTORMTemplate(seq = "14", name="createLotQuantity", type="Column", dataType="Number", initial="", history="")
	private long createLotQuantity;
	
	@CTORMTemplate(seq = "15", name="planSheetQuantity", type="Column", dataType="Number", initial="", history="")
	private long planSheetQuantity;

	@CTORMTemplate(seq = "16", name="createSheetQuantity", type="Column", dataType="Number", initial="", history="")
	private long createSheetQuantity;

	@CTORMTemplate(seq = "17", name="planState", type="Column", dataType="String", initial="", history="")
	private String planState;
	
	@CTORMTemplate(seq = "18", name="position", type="Column", dataType="Number", initial="", history="")
	private long position;
	
	@CTORMTemplate(seq = "19", name="createUser", type="Column", dataType="String", initial="", history="")
	private String createUser;

	@CTORMTemplate(seq = "20", name="createTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp createTime;

	@CTORMTemplate(seq = "21", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;

	@CTORMTemplate(seq = "22", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;

	@CTORMTemplate(seq = "23", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;

	@CTORMTemplate(seq = "24", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimeKey;
	
	@CTORMTemplate(seq = "25", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;
	
	@CTORMTemplate(seq = "26", name="productRequestHoldState", type="Column", dataType="String", initial="", history="")
	private String productRequestHoldState;

	public String getProductRequestName() {
		return productRequestName;
	}

	public void setProductRequestName(String productRequestName) {
		this.productRequestName = productRequestName;
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

	public String getMachineName() {
		return machineName;
	}

	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}

	public String getPlanName() {
		return planName;
	}

	public void setPlanName(String planName) {
		this.planName = planName;
	}

	public String getPlanDate() {
		return planDate;
	}

	public void setPlanDate(String planDate) {
		this.planDate = planDate;
	}
	
	public long getPriority() {
		return priority;
	}

	public void setPriority(long priority) {
		this.priority = priority;
	}

	public long getPlanLotQuantity() {
		return planLotQuantity;
	}

	public void setPlanLotQuantity(long planLotQuantity) {
		this.planLotQuantity = planLotQuantity;
	}

	public long getCreateLotQuantity() {
		return createLotQuantity;
	}

	public void setCreateLotQuantity(long createLotQuantity) {
		this.createLotQuantity = createLotQuantity;
	}

	public long getPlanSheetQuantity() {
		return planSheetQuantity;
	}

	public void setPlanSheetQuantity(long planSheetQuantity) {
		this.planSheetQuantity = planSheetQuantity;
	}

	public long getCreateSheetQuantity() {
		return createSheetQuantity;
	}

	public void setCreateSheetQuantity(long createSheetQuantity) {
		this.createSheetQuantity = createSheetQuantity;
	}

	public String getPlanState() {
		return planState;
	}

	public void setPlanState(String planState) {
		this.planState = planState;
	}

	public long getPosition() {
		return position;
	}

	public void setPosition(long position) {
		this.position = position;
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

	public String getLastEventTimeKey() {
		return lastEventTimeKey;
	}

	public void setLastEventTimeKey(String lastEventTimeKey) {
		this.lastEventTimeKey = lastEventTimeKey;
	}

	public String getLastEventComment() {
		return lastEventComment;
	}

	public void setLastEventComment(String lastEventComment) {
		this.lastEventComment = lastEventComment;
	}

	public String getProductRequestHoldState() {
		return productRequestHoldState;
	}

	public void setProductRequestHoldState(String productRequestHoldState) {
		this.productRequestHoldState = productRequestHoldState;
	}

	
}
