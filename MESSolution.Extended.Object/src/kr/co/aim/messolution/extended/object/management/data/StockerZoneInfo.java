package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class StockerZoneInfo extends UdfAccessor 
{
	@CTORMTemplate(seq = "1", name="machineName", type="Key", dataType="String", initial="", history="")
	private String machineName;
	
	@CTORMTemplate(seq = "2", name="zoneName", type="key", dataType="String", initial="", history="")
	private String zoneName;
	
	@CTORMTemplate(seq = "3", name="totalCapacity", type="Column", dataType="String", initial="", history="")
	private String totalCapacity;
	
	@CTORMTemplate(seq = "4", name="usedShelfCount", type="Column", dataType="String", initial="", history="")
	private String usedShelfCount;
	
	@CTORMTemplate(seq = "5", name="emptyShelfCount", type="Column", dataType="String", initial="", history="")
	private String emptyShelfCount;
	
	@CTORMTemplate(seq = "6", name="prohibitedShelfCount", type="Column", dataType="String", initial="", history="")
	private String prohibitedShelfCount;
	
	@CTORMTemplate(seq = "7", name="eventName", type="Column", dataType="String", initial="", history="")
	private String eventName;
	
	@CTORMTemplate(seq = "8", name="timeKey", type="Column", dataType="String", initial="", history="")
	private String timeKey;
	
	@CTORMTemplate(seq = "9", name="eventUser", type="Column", dataType="String", initial="", history="")
	private String eventUser;
	
	@CTORMTemplate(seq = "10", name="eventComment", type="Column", dataType="String", initial="", history="")
	private String eventComment;
	
	public String getMachineName() {
		return machineName;
	}
	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}
	public String getZoneName() {
		return zoneName;
	}
	public void setZoneName(String zoneName) {
		this.zoneName = zoneName;
	}
	public String getTotalCapacity() {
		return totalCapacity;
	}
	public void setTotalCapacity(String totalCapacity) {
		this.totalCapacity = totalCapacity;
	}
	public String getUsedShelfCount() {
		return usedShelfCount;
	}
	public void setUsedShelfCount(String usedShelfCount) {
		this.usedShelfCount = usedShelfCount;
	}
	public String getEmptyShelfCount() {
		return emptyShelfCount;
	}
	public void setEmptyShelfCount(String emptyShelfCount) {
		this.emptyShelfCount = emptyShelfCount;
	}
	public String getProhibitedShelfCount() {
		return prohibitedShelfCount;
	}
	public void setProhibitedShelfCount(String prohibitedShelfCount) {
		this.prohibitedShelfCount = prohibitedShelfCount;
	}
	public String getEventName() {
		return eventName;
	}
	public void setEventName(String eventName) {
		this.eventName = eventName;
	}
	public String getTimeKey() {
		return timeKey;
	}
	public void setTimeKey(String timeKey) {
		this.timeKey = timeKey;
	}
	public String getEventUser() {
		return eventUser;
	}
	public void setEventUser(String eventUser) {
		this.eventUser = eventUser;
	}
	public String getEventComment() {
		return eventComment;
	}
	public void setEventComment(String eventComment) {
		this.eventComment = eventComment;
	}
	
	//instantiation
	public StockerZoneInfo()
	{
		
	}
	
	public StockerZoneInfo(String machineName, String zoneName)
	{
		setMachineName(machineName);
		setZoneName(zoneName);
	}
}