package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;
import java.util.Date;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;
public class RiskLot extends UdfAccessor{
	
	@CTORMTemplate(seq = "1", name="lotName", type="Key", dataType="String", initial="", history="")
	private String lotName;
	
	@CTORMTemplate(seq = "2", name="factoryName", type="Column", dataType="Number", initial="", history="")
	private String factoryName;

	@CTORMTemplate(seq = "3", name="productType", type="Column", dataType="String", initial="", history="")
	private String productType;

	@CTORMTemplate(seq = "4", name="allowableGrade", type="Column", dataType="Number", initial="", history="")
	private String allowableGrade;
	
	@CTORMTemplate(seq = "5", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;
	
	@CTORMTemplate(seq = "6", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimeKey;

	@CTORMTemplate(seq = "7", name="lastEventTime", type="Column", dataType="Date", initial="", history="N")
	private Date lastEventTime;
	
	@CTORMTemplate(seq = "8", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "9", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;

	@CTORMTemplate(seq = "10", name="createTime", type="Column", dataType="String", initial="", history="")
	private Date createTime;

	@CTORMTemplate(seq = "11", name="createUser", type="Column", dataType="Date", initial="", history="")
	private String createUser;
	
	@CTORMTemplate(seq = "12", name="pickFlag", type="Column", dataType="Date", initial="", history="")
	private String pickFlag;
	
	@CTORMTemplate(seq = "13", name="reasonCode", type="Column", dataType="String", initial="", history="")
	private String reasonCode;
	
	@CTORMTemplate(seq = "14", name="reasonCodeType", type="Column", dataType="String", initial="", history="")
	private String reasonCodeType;
	
	@CTORMTemplate(seq = "15", name="checkFlag", type="Column", dataType="String", initial="", history="")
	private String checkFlag;

	public String getLotName() {
		return lotName;
	}

	public void setLotName(String lotName) {
		this.lotName = lotName;
	}

	public String getFactoryName() {
		return factoryName;
	}

	public void setFactoryName(String factoryName) {
		this.factoryName = factoryName;
	}

	public String getProductType() {
		return productType;
	}

	public void setProductType(String productType) {
		this.productType = productType;
	}

	public String getAllowableGrade() {
		return allowableGrade;
	}

	public void setAllowableGrade(String allowableGrade) {
		this.allowableGrade = allowableGrade;
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

	public String getLastEventComment() {
		return lastEventComment;
	}

	public void setLastEventComment(String lastEventComment) {
		this.lastEventComment = lastEventComment;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public String getPickFlag() {
		return pickFlag;
	}

	public void setPickFlag(String pickFlag) {
		this.pickFlag = pickFlag;
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

	public String getCheckFlag() {
		return checkFlag;
	}

	public void setCheckFlag(String checkFlag) {
		this.checkFlag = checkFlag;
	}
	
	
}
