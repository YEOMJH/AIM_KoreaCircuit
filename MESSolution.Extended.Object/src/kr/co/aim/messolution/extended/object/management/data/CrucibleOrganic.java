package kr.co.aim.messolution.extended.object.management.data;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class CrucibleOrganic extends UdfAccessor {
	@CTORMTemplate(seq = "1", name="machineName", type="Key", dataType="String", initial="", history="")
	private String machineName;
	
	@CTORMTemplate(seq = "2", name="chamberName", type="Key", dataType="String", initial="", history="")
	private String chamberName;
	
	@CTORMTemplate(seq = "3", name="crucibleName", type="Key", dataType="String", initial="", history="")
	private String crucibleName;
	
	@CTORMTemplate(seq = "4", name="consumableSpecName", type="Column", dataType="String", initial="", history="")
	private String consumableSpecName;
	
	@CTORMTemplate(seq = "5", name="consumableSpecVersion", type="Column", dataType="String", initial="", history="")
	private String consumableSpecVersion;
	
	@CTORMTemplate(seq = "6", name="planQuantity", type="Column", dataType="String", initial="", history="")
	private String planQuantity;
	
	@CTORMTemplate(seq = "7", name="lastEventName", type="Column", dataType="String", initial="", history="")
	private String lastEventName;
	
	@CTORMTemplate(seq = "8", name="lastEventUser", type="Column", dataType="String", initial="", history="")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "9", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="")
	private String lastEventTime;
	
	@CTORMTemplate(seq = "10", name="lastEventComment", type="Column", dataType="String", initial="", history="")
	private String lastEventComment;
	
	@CTORMTemplate(seq = "11", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="")
	private String lastEventTimeKey;
	
	public CrucibleOrganic()
	{
		
	}

	public String getMachineName() {
		return machineName;
	}

	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}

	public String getChamberName() {
		return chamberName;
	}

	public void setChamberName(String chamberName) {
		this.chamberName = chamberName;
	}

	public String getCrucibleName() {
		return crucibleName;
	}

	public void setCrucibleName(String crucibleName) {
		this.crucibleName = crucibleName;
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

	public String getPlanQuantity() {
		return planQuantity;
	}

	public void setPlanQuantity(String planQuantity) {
		this.planQuantity = planQuantity;
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

	public String getLastEventTime() {
		return lastEventTime;
	}

	public void setLastEventTime(String lastEventTime) {
		this.lastEventTime = lastEventTime;
	}

	public String getLastEventComment() {
		return lastEventComment;
	}

	public void setLastEventComment(String lastEventComment) {
		this.lastEventComment = lastEventComment;
	}

	public String getLastEventTimeKey() {
		return lastEventTimeKey;
	}

	public void setLastEventTimeKey(String lastEventTimeKey) {
		this.lastEventTimeKey = lastEventTimeKey;
	}
}
