package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class LOIPanelCodeList extends UdfAccessor {
	@CTORMTemplate(seq = "1", name = "panelName", type = "Key", dataType = "String", initial = "", history = "")
	private String panelName;
	@CTORMTemplate(seq = "2", name = "codeLevel1", type = "Key", dataType = "String", initial = "", history = "")
	private String codeLevel1;
	@CTORMTemplate(seq = "3", name = "codeLevel2", type = "Key", dataType = "String", initial = "", history = "")
	private String codeLevel2;
	@CTORMTemplate(seq = "4", name = "glassName", type = "Column", dataType = "String", initial = "", history = "")
	private String glassName;
	@CTORMTemplate(seq = "5", name = "lotName", type = "Column", dataType = "String", initial = "", history = "")
	private String lotName;
	@CTORMTemplate(seq = "6", name = "area", type = "Column", dataType = "String", initial = "", history = "")
	private String area;
	@CTORMTemplate(seq = "7", name = "quantity", type = "Column", dataType = "Number", initial = "", history = "")
	private long quantity;
	@CTORMTemplate(seq = "8", name = "eventComment", type = "Column", dataType = "String", initial = "", history = "N")
	private String eventComment;
	@CTORMTemplate(seq = "9", name = "eventName", type = "Column", dataType = "String", initial = "", history = "N")
	private String eventName;
	@CTORMTemplate(seq = "10", name = "eventUser", type = "Column", dataType = "String", initial = "", history = "N")
	private String eventUser;
	@CTORMTemplate(seq = "11", name = "eventTime", type = "Column", dataType = "Timestamp", initial = "", history = "N")
	private Timestamp eventTime;
	@CTORMTemplate(seq = "12", name = "timeKey", type = "Column", dataType = "String", initial = "", history = "N")
	private String timeKey;
	public String getPanelName() {
		return panelName;
	}
	public void setPanelName(String panelName) {
		this.panelName = panelName;
	}
	public String getCodeLevel1() {
		return codeLevel1;
	}
	public void setCodeLevel1(String codeLevel1) {
		this.codeLevel1 = codeLevel1;
	}
	public String getCodeLevel2() {
		return codeLevel2;
	}
	public void setCodeLevel2(String codeLevel2) {
		this.codeLevel2 = codeLevel2;
	}
	public String getGlassName() {
		return glassName;
	}
	public void setGlassName(String glassName) {
		this.glassName = glassName;
	}
	public String getLotName() {
		return lotName;
	}
	public void setLotName(String lotName) {
		this.lotName = lotName;
	}
	public String getArea() {
		return area;
	}
	public void setArea(String area) {
		this.area = area;
	}
	public long getQuantity() {
		return quantity;
	}
	public void setQuantity(long quantity) {
		this.quantity = quantity;
	}
	public String getEventComment() {
		return eventComment;
	}
	public void setEventComment(String eventComment) {
		this.eventComment = eventComment;
	}
	public String getEventName() {
		return eventName;
	}
	public void setEventName(String eventName) {
		this.eventName = eventName;
	}
	public String getEventUser() {
		return eventUser;
	}
	public void setEventUser(String eventUser) {
		this.eventUser = eventUser;
	}
	public Timestamp getEventTime() {
		return eventTime;
	}
	public void setEventTime(Timestamp eventTime) {
		this.eventTime = eventTime;
	}
	public String getTimeKey() {
		return timeKey;
	}
	public void setTimeKey(String timeKey) {
		this.timeKey = timeKey;
	}
}
