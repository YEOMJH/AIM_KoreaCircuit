package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class EVAReserveSpec extends UdfAccessor {

	@CTORMTemplate(seq = "1", name="MSName", type="Key", dataType="String", initial="", history="")
	private String MSName;
	
	@CTORMTemplate(seq = "2", name="MaskKind", type="Column", dataType="String", initial="", history="")
	private String MaskKind;

	@CTORMTemplate(seq = "3", name="MaskType", type="Column", dataType="String", initial="", history="")
	private String MaskType;

	@CTORMTemplate(seq = "4", name="ReserveLimit", type="Column", dataType="Number", initial="", history="")
	private long ReserveLimit;
	
	@CTORMTemplate(seq = "5", name="AutoFlag", type="Column", dataType="String", initial="", history="")
	private String AutoFlag;
	
	@CTORMTemplate(seq = "6", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;
	
	@CTORMTemplate(seq = "7", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimeKey;

	@CTORMTemplate(seq = "8", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "9", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "10", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;

	public String getLastEventComment() {
		return lastEventComment;
	}

	public void setLastEventComment(String lastEventComment) {
		this.lastEventComment = lastEventComment;
	}

	public String getMSName() {
		return MSName;
	}

	public void setMSName(String mSName) {
		MSName = mSName;
	}

	public String getMaskKind() {
		return MaskKind;
	}

	public void setMaskKind(String maskKind) {
		MaskKind = maskKind;
	}

	public String getMaskType() {
		return MaskType;
	}

	public void setMaskType(String maskType) {
		MaskType = maskType;
	}

	public long getReserveLimit() {
		return ReserveLimit;
	}

	public void setReserveLimit(long reserveLimit) {
		ReserveLimit = reserveLimit;
	}

	public String getAutoFlag() {
		return AutoFlag;
	}

	public void setAutoFlag(String autoFlag) {
		AutoFlag = autoFlag;
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
	

}
