package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class ScrapCrate extends UdfAccessor{

	@CTORMTemplate(seq = "1", name="timekey", type="Key", dataType="String", initial="", history="")
	private String timekey;
	
	@CTORMTemplate(seq = "2", name="virtualGlassID", type="Key", dataType="String", initial="", history="")
	private String virtualGlassID;
	
	@CTORMTemplate(seq = "3", name="crateName", type="Key", dataType="String", initial="", history="")
	private String crateName;
	
	@CTORMTemplate(seq = "4", name="factoryName", type="Column", dataType="String", initial="", history="")
	private String factoryName;
	
	@CTORMTemplate(seq = "5", name="workOrder", type="Column", dataType="String", initial="", history="")
	private String workOrder;
	
	@CTORMTemplate(seq = "6", name="scrapQty", type="Column", dataType="Number", initial="", history="")
	private long scrapQty;
	
	@CTORMTemplate(seq = "7", name="consumableSpecName", type="Column", dataType="String", initial="", history="")
	private String consumableSpecName;
	
	@CTORMTemplate(seq = "8", name="consumableSpecVersion", type="Column", dataType="String", initial="", history="")
	private String consumableSpecVersion;

	@CTORMTemplate(seq = "9", name="scrapCode", type="Column", dataType="String", initial="", history="N")
	private String scrapCode;
	
	@CTORMTemplate(seq = "10", name="eventName", type="Column", dataType="String", initial="", history="N")
	private String eventName;
	
	@CTORMTemplate(seq = "11", name="eventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp eventTime;
	
	@CTORMTemplate(seq = "12", name="scrapText", type="Column", dataType="String", initial="", history="N")
	private String scrapText;
	
	//instantiation
	public ScrapCrate()
	{
		
	}

	//instantiation
	public ScrapCrate(String timekey, String virtualGlassID, String crateName)
	{
		setTimekey(timekey);
		setVirtualGlassID(virtualGlassID);
		setCrateName(crateName);
	}

	
	public String getTimekey() {
		return timekey;
	}




	public void setTimekey(String timekey) {
		this.timekey = timekey;
	}



	public String getVirtualGlassID() {
		return virtualGlassID;
	}




	public void setVirtualGlassID(String virtualGlassID) {
		this.virtualGlassID = virtualGlassID;
	}




	public String getCrateName() {
		return crateName;
	}




	public void setCrateName(String crateName) {
		this.crateName = crateName;
	}




	public String getFactoryName() {
		return factoryName;
	}




	public void setFactoryName(String factoryName) {
		this.factoryName = factoryName;
	}




	public String getWorkOrder() {
		return workOrder;
	}




	public void setWorkOrder(String workOrder) {
		this.workOrder = workOrder;
	}




	public long getScrapQty() {
		return scrapQty;
	}




	public void setScrapQty(long scrapQty) {
		this.scrapQty = scrapQty;
	}




	public String getConsumableSpecName() {
		return consumableSpecName;
	}




	public void setConsumableSpecName(String consumableSpecName) {
		this.consumableSpecName = consumableSpecName;
	}




	public String getConsumableSpecVersion() {
		return consumableSpecVersion;
	}




	public void setConsumableSpecVersion(String consumableSpecVersion) {
		this.consumableSpecVersion = consumableSpecVersion;
	}




	public String getScrapCode() {
		return scrapCode;
	}




	public void setScrapCode(String scrapCode) {
		this.scrapCode = scrapCode;
	}
	

	
	
	public String getEventName() {
		return eventName;
	}




	public void setEventName(String eventName) {
		this.eventName = eventName;
	}


	
	
	public Timestamp getEventTime() {
		return eventTime;
	}




	public void setEventTime(Timestamp eventTime) {
		this.eventTime = eventTime;
	}

	public String getScrapText() {
		return scrapText;
	}

	public void setScrapText(String scrapText) {
		this.scrapText = scrapText;
	}

	
}
