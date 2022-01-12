package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class ReserveRepairProduct extends UdfAccessor{
	@CTORMTemplate(seq = "1", name="productName", type="Key", dataType="String", initial="", history="")
	private String productName;
	@CTORMTemplate(seq = "2", name="processFlowName", type="Column", dataType="String", initial="", history="")
	private String processFlowName;
	@CTORMTemplate(seq = "3", name="processFlowVersion", type="Column", dataType="String", initial="", history="")
	private String processFlowVersion;
	@CTORMTemplate(seq = "4", name="processOperationName", type="Column", dataType="String", initial="", history="")
	private String processOperationName;
	@CTORMTemplate(seq = "5", name="processOperationVersion", type="Column", dataType="String", initial="", history="")
	private String processOperationVersion;
	@CTORMTemplate(seq = "6", name="repairFlowName", type="Column", dataType="String", initial="", history="")
	private String repairFlowName;
	@CTORMTemplate(seq = "7", name="repairFlowVersion", type="Column", dataType="String", initial="", history="")
	private String repairFlowVersion;
	@CTORMTemplate(seq = "8", name="repairOperationName", type="Column", dataType="String", initial="", history="")
	private String repairOperationName;
	@CTORMTemplate(seq = "9", name="repairOperationVersino", type="Column", dataType="String", initial="", history="")
	private String repairOperationVersino;
	@CTORMTemplate(seq = "10", name="productGrade", type="Column", dataType="String", initial="", history="")
	private String productGrade;
	@CTORMTemplate(seq = "11", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;
	@CTORMTemplate(seq = "12", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimeKey;
	@CTORMTemplate(seq = "13", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;
	@CTORMTemplate(seq = "14", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;
	@CTORMTemplate(seq = "15", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;
	
	
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
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
	public String getRepairOperationVersino() {
		return repairOperationVersino;
	}
	public void setRepairOperationVersino(String repairOperationVersino) {
		this.repairOperationVersino = repairOperationVersino;
	}
	public String getProductGrade() {
		return productGrade;
	}
	public void setProductGrade(String productGrade) {
		this.productGrade = productGrade;
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
}
