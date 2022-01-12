package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class ShieldChamberMap extends UdfAccessor {
	@CTORMTemplate(seq = "1", name = "shieldSpecName", type = "Key", dataType = "String", initial = "", history = "")
	private String shieldSpecName;
	@CTORMTemplate(seq = "2", name = "EVAChamberName", type = "Key", dataType = "String", initial = "", history = "")
	private String EVAChamberName;
	@CTORMTemplate(seq = "3", name = "shieldQuantity", type = "Column", dataType = "Number", initial = "", history = "")
	private long shieldQuantity;
	@CTORMTemplate(seq = "4", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventUser;
	@CTORMTemplate(seq = "5", name = "lastEventName", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventName;
	@CTORMTemplate(seq = "6", name = "lastEventTime", type = "Column", dataType = "Timestamp", initial = "", history = "N")
	private Timestamp lastEventTime;
	@CTORMTemplate(seq = "7", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventComment;
	@CTORMTemplate(seq = "8", name = "lastEventTimekey", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventTimekey;
	public String getShieldSpecName() {
		return shieldSpecName;
	}
	public void setShieldSpecName(String shieldSpecName) {
		this.shieldSpecName = shieldSpecName;
	}
	public String getEVAChamberName() {
		return EVAChamberName;
	}
	public void setEVAChamberName(String eVAChamberName) {
		EVAChamberName = eVAChamberName;
	}
	public long getShieldQuantity() {
		return shieldQuantity;
	}
	public void setShieldQuantity(long shieldQuantity) {
		this.shieldQuantity = shieldQuantity;
	}
	public String getLastEventUser() {
		return lastEventUser;
	}
	public void setLastEventUser(String lastEventUser) {
		this.lastEventUser = lastEventUser;
	}
	public String getLastEventName() {
		return lastEventName;
	}
	public void setLastEventName(String lastEventName) {
		this.lastEventName = lastEventName;
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
