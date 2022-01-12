package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;
import java.util.Date;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class IPQCLot extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="ipqcLotName", type="Key", dataType="String", initial="", history="")
	private String ipqcLotName;
	
	@CTORMTemplate(seq = "2", name="seq", type="Key", dataType="Number", initial="", history="")
	private long seq;

	@CTORMTemplate(seq = "3", name="productSpec", type="Column", dataType="String", initial="", history="")
	private String productSpec;

	@CTORMTemplate(seq = "4", name="trayQuantity", type="Column", dataType="Number", initial="", history="")
	private long trayQuantity;

	@CTORMTemplate(seq = "5", name="panelQuantity", type="Column", dataType="Number", initial="", history="")
	private long panelQuantity;

	@CTORMTemplate(seq = "6", name="panelGrade", type="Column", dataType="String", initial="", history="")
	private String panelGrade;

	@CTORMTemplate(seq = "7", name="lotState", type="Column", dataType="String", initial="", history="")
	private String lotState;

	@CTORMTemplate(seq = "8", name="sampleRule", type="Column", dataType="String", initial="", history="")
	private String sampleRule;

	@CTORMTemplate(seq = "9", name="machineName", type="Column", dataType="String", initial="", history="")
	private String machineName;

	@CTORMTemplate(seq = "10", name="fqcResult", type="Column", dataType="String", initial="", history="")
	private String fqcResult;
	
	@CTORMTemplate(seq = "11", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;

	@CTORMTemplate(seq = "12", name="createUser", type="Column", dataType="String", initial="", history="")
	private String createUser;

	@CTORMTemplate(seq = "13", name="createTime", type="Column", dataType="Date", initial="", history="")
	private Date createTime;
	
	@CTORMTemplate(seq = "14", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;
	
	@CTORMTemplate(seq = "15", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimeKey;

	@CTORMTemplate(seq = "16", name="lastEventTime", type="Column", dataType="Date", initial="", history="N")
	private Date lastEventTime;
	
	@CTORMTemplate(seq = "17", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;

	public String getIpqcLotName() {
		return ipqcLotName;
	}

	public void setIpqcLotName(String ipqcLotName) {
		this.ipqcLotName = ipqcLotName;
	}

	public long getSeq() {
		return seq;
	}

	public void setSeq(long seq) {
		this.seq = seq;
	}

	public String getProductSpec() {
		return productSpec;
	}

	public void setProductSpec(String productSpec) {
		this.productSpec = productSpec;
	}

	public long getTrayQuantity() {
		return trayQuantity;
	}

	public void setTrayQuantity(long trayQuantity) {
		this.trayQuantity = trayQuantity;
	}

	public long getPanelQuantity() {
		return panelQuantity;
	}

	public void setPanelQuantity(long panelQuantity) {
		this.panelQuantity = panelQuantity;
	}

	public String getPanelGrade() {
		return panelGrade;
	}

	public void setPanelGrade(String panelGrade) {
		this.panelGrade = panelGrade;
	}

	public String getLotState() {
		return lotState;
	}

	public void setLotState(String lotState) {
		this.lotState = lotState;
	}

	public String getSampleRule() {
		return sampleRule;
	}

	public void setSampleRule(String sampleRule) {
		this.sampleRule = sampleRule;
	}

	public String getMachineName() {
		return machineName;
	}

	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}

	public String getFqcResult() {
		return fqcResult;
	}

	public void setFqcResult(String fqcResult) {
		this.fqcResult = fqcResult;
	}

	public String getLastEventComment() {
		return lastEventComment;
	}

	public void setLastEventComment(String lastEventComment) {
		this.lastEventComment = lastEventComment;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
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

	public Date getLastEventTime() {
		return lastEventTime;
	}

	public void setLastEventTime(Date lastEventTime) {
		this.lastEventTime = lastEventTime;
	}

	public String getLastEventUser() {
		return lastEventUser;
	}

	public void setLastEventUser(String lastEventUser) {
		this.lastEventUser = lastEventUser;
	}
}