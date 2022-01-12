package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class AbnormalSheetDetail extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="abnormalSheetName", type="Key", dataType="String", initial="", history="")
	private String abnormalSheetName;	
	
	@CTORMTemplate(seq = "2", name="productName", type="Key", dataType="String", initial="", history="")
	private String productName;
	
	@CTORMTemplate(seq = "3", name="abnormalCode", type="Key", dataType="String", initial="", history="")
	private String abnormalCode;
	
	@CTORMTemplate(seq = "4", name="processState", type="Column", dataType="String", initial="", history="")
	private String processState;
	
	@CTORMTemplate(seq = "5", name="actionCode", type="Column", dataType="String", initial="", history="")
	private String actionCode;
	
	@CTORMTemplate(seq = "6", name="processComment", type="Column", dataType="String", initial="", history="")
	private String processComment;
	
	@CTORMTemplate(seq = "7", name="taUser", type="Column", dataType="String", initial="", history="")
	private String taUser;
	
	@CTORMTemplate(seq = "8", name="engDepartment", type="Column", dataType="String", initial="", history="")
	private String engDepartment;
	
	@CTORMTemplate(seq = "9", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;
	
	@CTORMTemplate(seq = "10", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "11", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "12", name="slotPosition", type="Column", dataType="Number", initial="", history="")
	private int slotPosition;
	
	@CTORMTemplate(seq = "13", name="rsCode", type="Column", dataType="String", initial="", history="")
	private String rsCode;
	
	@CTORMTemplate(seq = "14", name="changeDepartmentCount", type="Column", dataType="Number", initial="", history="")
	private int changeDepartmentCount;
	
	@CTORMTemplate(seq = "15", name="abnormalComment", type="Column", dataType="String", initial="", history="")
	private String abnormalComment;
	
	@CTORMTemplate(seq = "16", name="engUser", type="Column", dataType="String", initial="", history="")
	private String engUser;
	
	@CTORMTemplate(seq = "17", name="createTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp createTime;
	
	@CTORMTemplate(seq = "18", name="rsProcessOperationName", type="Column", dataType="String", initial="", history="")
	private String rsProcessOperationName;
	
	@CTORMTemplate(seq = "19", name="issueFlag", type="Column", dataType="String", initial="", history="")
	private String issueFlag;
	
	//instantiation
	public AbnormalSheetDetail()
	{
		
	}

	public String getAbnormalSheetName() {
		return abnormalSheetName;
	}

	public void setAbnormalSheetName(String abnormalSheetName) {
		this.abnormalSheetName = abnormalSheetName;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getAbnormalCode() {
		return abnormalCode;
	}

	public void setAbnormalCode(String abnormalCode) {
		this.abnormalCode = abnormalCode;
	}

	public String getProcessState() {
		return processState;
	}

	public void setProcessState(String processState) {
		this.processState = processState;
	}

	public String getActionCode() {
		return actionCode;
	}

	public void setActionCode(String actionCode) {
		this.actionCode = actionCode;
	}

	public String getProcessComment() {
		return processComment;
	}

	public void setProcessComment(String processComment) {
		this.processComment = processComment;
	}

	public String getTaUser() {
		return taUser;
	}

	public void setTaUser(String taUser) {
		this.taUser = taUser;
	}

	public String getEngDepartment() {
		return engDepartment;
	}

	public void setEngDepartment(String engDepartment) {
		this.engDepartment = engDepartment;
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

	public int getSlotPosition() {
		return slotPosition;
	}

	public void setSlotPosition(int slotPosition) {
		this.slotPosition = slotPosition;
	}

	public String getRsCode() {
		return rsCode;
	}

	public void setRsCode(String rsCode) {
		this.rsCode = rsCode;
	}

	public int getChangeDepartmentCount() {
		return changeDepartmentCount;
	}

	public void setChangeDepartmentCount(int changeDepartmentCount) {
		this.changeDepartmentCount = changeDepartmentCount;
	}

	public String getAbnormalComment() {
		return abnormalComment;
	}

	public void setAbnormalComment(String abnormalComment) {
		this.abnormalComment = abnormalComment;
	}

	public String getEngUser() {
		return engUser;
	}

	public void setEngUser(String engUser) {
		this.engUser = engUser;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public String getRsProcessOperationName() {
		return rsProcessOperationName;
	}

	public void setRsProcessOperationName(String rsProcessOperationName) {
		this.rsProcessOperationName = rsProcessOperationName;
	}

	public String getIssueFlag() {
		return issueFlag;
	}

	public void setIssueFlag(String issueFlag) {
		this.issueFlag = issueFlag;
	}
}
