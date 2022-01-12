package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;

public class ScrapProduct {
	@CTORMTemplate(seq = "1", name="productName", type="Key", dataType="String", initial="", history="N")
	private String productName;	
	
	@CTORMTemplate(seq = "2", name="lotName", type="Column", dataType="String", initial="", history="N")
	private String lotName;
	
	@CTORMTemplate(seq = "3", name="productType", type="Column", dataType="String", initial="", history="N")
	private String productType;
	
	@CTORMTemplate(seq = "4", name="productGrade", type="Column", dataType="String", initial="", history="N")
	private String productGrade;
	
	@CTORMTemplate(seq = "5", name="productState", type="Column", dataType="String", initial="", history="N")
	private String productState;
	
	@CTORMTemplate(seq = "6", name="lastEventTimekey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimekey;
	
	@CTORMTemplate(seq = "7", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "8", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "9", name="reasonCodeType", type="Column", dataType="String", initial="", history="N")
	private String reasonCodeType;
	
	@CTORMTemplate(seq = "10", name="reasonCode", type="Column", dataType="String", initial="", history="N")
	private String reasonCode;
	
	@CTORMTemplate(seq = "11", name="scrapDeparment", type="Column", dataType="String", initial="", history="N")
	private String scrapDeparment;
	
	@CTORMTemplate(seq = "12", name="scrapOperationName", type="Column", dataType="String", initial="", history="N")
	private String scrapOperationName;
	
	@CTORMTemplate(seq = "13", name="scrapMachineName", type="Column", dataType="String", initial="", history="N")
	private String scrapMachineName;
	
	@CTORMTemplate(seq = "14", name="scrapUnitName", type="Column", dataType="String", initial="", history="N")
	private String scrapUnitName;
	
	@CTORMTemplate(seq = "15", name="scrapSubUnitName", type="Column", dataType="String", initial="", history="N")
	private String scrapSubUnitName;
	
	@CTORMTemplate(seq = "16", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;
	

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}
	
	public String getLotName() {
		return lotName;
	}
	

	public void setLotName(String lotName) {
		this.lotName = lotName;
	}
	
	
	public String getProductType() {
		return productType;
	}

	public void setProductType(String productType) {
		this.productType = productType;
	}
	
	public String getProductGrade() {
		return productGrade;
	}

	public void setProductGrade(String productGrade) {
		this.productGrade = productGrade;
	}
	
	public String getProductState() {
		return productState;
	}

	public void setProductState(String productState) {
		this.productState = productState;
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

	public String getLastEventUser() {
		return lastEventUser;
	}

	public void setLastEventUser(String lastEventUser) {
		this.lastEventUser = lastEventUser;
	}
	
	public String getReasonCodeType() {
		return reasonCodeType;
	}

	public void setReasonCodeType(String reasonCodeType) {
		this.reasonCodeType = reasonCodeType;
	}

	public String getReasonCode() {
		return reasonCode;
	}

	public void setReasonCode(String reasonCode) {
		this.reasonCode = reasonCode;
	}
    
	public String getScrapDeparment() {
		return scrapDeparment;
	}

	public void setScrapDeparment(String scrapDeparment) {
		this.scrapDeparment = scrapDeparment;
	}

	public String getScrapOperationName() {
		return scrapOperationName;
	}

	public void setScrapOperationName(String scrapOperationName) {
		this.scrapOperationName = scrapOperationName;
	}
	
	public String getScrapMachineName() {
		return scrapMachineName;
	}

	public void setScrapMachineName(String scrapMachineName) {
		this.scrapMachineName = scrapMachineName;
	}
	
	public String getScrapUnitName() {
		return scrapUnitName;
	}

	public void setScrapUnitName(String scrapUnitName) {
		this.scrapUnitName = scrapUnitName;
	}
	
	public String getScrapSubUnitName() {
		return scrapSubUnitName;
	}

	public void setScrapSubUnitName(String scrapSubUnitName) {
		this.scrapSubUnitName = scrapSubUnitName;
	}
	
	public String getLastEventComment() {
		return lastEventComment;
	}

	public void setLastEventComment(String lastEventComment) {
		this.lastEventComment = lastEventComment;
	}
	
}
