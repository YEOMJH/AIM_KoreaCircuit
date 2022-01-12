package kr.co.aim.messolution.extended.object.management.data;


import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class ValicationProduct  extends UdfAccessor{
	
	@CTORMTemplate(seq = "1", name="productName", type="Key", dataType="String", initial="", history="")
	private String productName;
	
	@CTORMTemplate(seq = "2", name="productSpecName", type="Key", dataType="String", initial="", history="")
	private String productSpecName;
	
	@CTORMTemplate(seq = "3", name="processFlowName", type="Key", dataType="String", initial="", history="")
	private String processFlowName;
	
	@CTORMTemplate(seq = "4", name="engOperationName", type="Key", dataType="String", initial="", history="")
	private String engOperationName;
	
	@CTORMTemplate(seq = "5", name="engMachineName", type="Key", dataType="String", initial="", history="")
	private String engMachineName;
	
	@CTORMTemplate(seq = "6", name="toFlowName", type="Key", dataType="String", initial="", history="")
	private String toFlowName;
	
	@CTORMTemplate(seq = "7", name="SampleOperationName", type="Key", dataType="String", initial="", history="")
	private String SampleOperationName;
	
	@CTORMTemplate(seq = "8", name="userID", type="Column", dataType="Column", initial="", history="")
	private String userID;
	
	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	@CTORMTemplate(seq = "9", name="lotName", type="Column", dataType="Column", initial="", history="")
	private String lotName;
	
	@CTORMTemplate(seq = "10", name="processFlowVersion", type="Column", dataType="String", initial="", history="")
	private String processFlowVersion;
	
	@CTORMTemplate(seq = "11", name="productSpecVersion", type="Column", dataType="String", initial="", history="")
	private String productSpecVersion;
	
	@CTORMTemplate(seq = "12", name = "lastEventName", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventName;
	
	@CTORMTemplate(seq = "13", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "14", name = "lastEventTime", type = "Column", dataType = "Timestamp", initial = "", history = "N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "15", name = "lastEventTimeKey", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventTimeKey;
	
	@CTORMTemplate(seq = "16", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventComment;
	
	@CTORMTemplate(seq = "17", name = "baseLineFlag", type = "Column", dataType = "String", initial = "", history = "")
	private String baseLineFlag;
	
	@CTORMTemplate(seq = "18", name = "ALLFlag", type = "Column", dataType = "String", initial = "", history = "")
	private String ALLFlag;

	public String getALLFlag() {
		return ALLFlag;
	}

	public void setALLFlag(String aLLFlag) {
		ALLFlag = aLLFlag;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getProductSpecName() {
		return productSpecName;
	}

	public void setProductSpecName(String productSpecName) {
		this.productSpecName = productSpecName;
	}

	public String getProcessFlowName() {
		return processFlowName;
	}

	public void setProcessFlowName(String processFlowName) {
		this.processFlowName = processFlowName;
	}

	public String getEngOperationName() {
		return engOperationName;
	}

	public void setEngOperationName(String engOperationName) {
		this.engOperationName = engOperationName;
	}

	public String getEngMachineName() {
		return engMachineName;
	}

	public void setEngMachineName(String engMachineName) {
		this.engMachineName = engMachineName;
	}

	public String getToFlowName() {
		return toFlowName;
	}

	public void setToFlowName(String toFlowName) {
		this.toFlowName = toFlowName;
	}

	public String getSampleOperationName() {
		return SampleOperationName;
	}

	public void setSampleOperationName(String sampleOperationName) {
		SampleOperationName = sampleOperationName;
	}

	

	public String getLotName() {
		return lotName;
	}

	public void setLotName(String lotName) {
		this.lotName = lotName;
	}

	public String getProcessFlowVersion() {
		return processFlowVersion;
	}

	public void setProcessFlowVersion(String processFlowVersion) {
		this.processFlowVersion = processFlowVersion;
	}

	public String getProductSpecVersion() {
		return productSpecVersion;
	}

	public void setProductSpecVersion(String productSpecVersion) {
		this.productSpecVersion = productSpecVersion;
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

	public String getBaseLineFlag() {
		return baseLineFlag;
	}

	public void setBaseLineFlag(String baseLineFlag) {
		this.baseLineFlag = baseLineFlag;
	}

}
