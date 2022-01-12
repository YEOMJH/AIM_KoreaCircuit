package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class OrganicExtractCard extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="materialID", type="Key", dataType="String", initial="", history="")
	private String materialID;
	
	@CTORMTemplate(seq = "2", name="description", type="Column", dataType="String", initial="", history="")
	private String description;
	
	@CTORMTemplate(seq = "3", name="location", type="Column", dataType="String", initial="", history="")
	private String location;
	
	@CTORMTemplate(seq = "4", name="quantity", type="Column", dataType="Number", initial="", history="")
	private Number quantity;
	
	@CTORMTemplate(seq = "5", name="totalQuantity", type="Column", dataType="Number", initial="", history="")
	private Number totalQuantity;
	
	@CTORMTemplate(seq = "6", name="state", type="Column", dataType="String", initial="", history="")
	private String state;
	
	@CTORMTemplate(seq = "7", name="grade", type="Column", dataType="String", initial="", history="")
	private String grade;
	
	@CTORMTemplate(seq = "8", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;
	
	@CTORMTemplate(seq = "9", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "10", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "11", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;
	
	@CTORMTemplate(seq = "12", name="lastEventTimekey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimekey;

	public String getMaterialID() {
		return materialID;
	}

	public void setMaterialID(String materialID) {
		this.materialID = materialID;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public Number getQuantity() {
		return quantity;
	}

	public void setQuantity(Number quantity) {
		this.quantity = quantity;
	}

	public Number getTotalQuantity() {
		return totalQuantity;
	}

	public void setTotalQuantity(Number totalQuantity) {
		this.totalQuantity = totalQuantity;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getGrade() {
		return grade;
	}

	public void setGrade(String grade) {
		this.grade = grade;
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

	public String getLastEventComment() {
		return lastEventComment;
	}

	public void setLastEventComment(String lastEventComment) {
		this.lastEventComment = lastEventComment;
	}

	public String getLastEventTimekey() {
		return lastEventTimekey;
	}

	public void setLastEventTimekey(String lastEventTimekey) {
		this.lastEventTimekey = lastEventTimekey;
	}
}
