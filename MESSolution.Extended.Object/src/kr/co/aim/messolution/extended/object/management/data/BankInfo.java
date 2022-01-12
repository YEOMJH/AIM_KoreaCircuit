package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class BankInfo extends UdfAccessor{
	
	@CTORMTemplate(seq = "1", name = "factoryName", type = "Key", dataType = "String", initial = "", history = "")
	private String factoryName;
	
	@CTORMTemplate(seq = "2", name = "toFactoryName", type = "Key", dataType = "String", initial = "", history = "")
	private String toFactoryName;
	
	@CTORMTemplate(seq = "3", name = "bankType", type = "Key", dataType = "String", initial = "", history = "")
	private String bankType;
	
	@CTORMTemplate(seq = "4", name = "description", type = "Column", dataType = "String", initial = "", history = "")
	private String description;
	
	@CTORMTemplate(seq = "5", name = "wipLimit", type = "Column", dataType = "String", initial = "", history = "")
	private String wipLimit;
	
	@CTORMTemplate(seq = "6", name = "alarmType", type = "Column", dataType = "String", initial = "", history = "")
	private String alarmType;
	
	@CTORMTemplate(seq = "8", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "9", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "10", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "")
	private String lastEventComment;
	
	@CTORMTemplate(seq = "11", name = "lastEventTimekey", type = "Column", dataType = "String", initial = "", history = "")
	private String lastEventTimekey;

	public String getBankType() {
		return bankType;
	}

	public void setBankType(String bankType) {
		this.bankType = bankType;
	}

	public String getFactoryName() {
		return factoryName;
	}

	public void setFactoryName(String factoryName) {
		this.factoryName = factoryName;
	}

	public String getToFactoryName() {
		return toFactoryName;
	}

	public void setToFactoryName(String toFactoryName) {
		this.toFactoryName = toFactoryName;
	}

	public String getAlarmType() {
		return alarmType;
	}

	public void setAlarmType(String alarmType) {
		this.alarmType = alarmType;
	}
	
	public String getWipLimit() {
		return wipLimit;
	}

	public void setWipLimit(String wipLimit) {
		this.wipLimit = wipLimit;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLastEventUser() {
		return lastEventUser;
	}

	public void setLastEventUser(String lastEventUser) {
		this.lastEventUser = lastEventUser;
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
	
	public String getLastEventComment() {
		return lastEventComment;
	}

	public void setLastEventComment(String lastEventComment) {
		this.lastEventComment = lastEventComment;
	}
	
	
	
}