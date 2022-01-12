package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class MaskGroupLot extends UdfAccessor {
	
	
	@CTORMTemplate(seq = "1", name="MaskGroupName", type="Key", dataType="String", initial="", history="")
	private String MaskGroupName;	
	
	@CTORMTemplate(seq = "2", name="MaskLotName", type="Key", dataType="String", initial="", history="")
	private String maskLotName;
	
	@CTORMTemplate(seq = "3", name="MaskType", type="Column", dataType="String", initial="", history="")
	private String maskType;
	
	@CTORMTemplate(seq = "4", name="stage", type="Column", dataType="String", initial="", history="")
	private String stage;
	
	@CTORMTemplate(seq = "5", name="LastEventName", type="Column", dataType="String", initial="", history="N")
	private String LastEventName;
	
	@CTORMTemplate(seq = "6", name="lastEventTimekey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimekey;
	
	@CTORMTemplate(seq = "7", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "8", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "9", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;

	public String getMaskGroupName() {
		return MaskGroupName;
	}

	public void setMaskGroupName(String maskGroupName) {
		MaskGroupName = maskGroupName;
	}

	public String getMaskLotName() {
		return maskLotName;
	}

	public void setMaskLotName(String maskLotName) {
		this.maskLotName = maskLotName;
	}

	public String getMaskType() {
		return maskType;
	}

	public void setMaskType(String maskType) {
		this.maskType = maskType;
	}

	public String getStage() {
		return stage;
	}

	public void setStage(String stage) {
		this.stage = stage;
	}

	public String getLastEventName() {
		return LastEventName;
	}

	public void setLastEventName(String lastEventName) {
		LastEventName = lastEventName;
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

	public String getLastEventComment() {
		return lastEventComment;
	}

	public void setLastEventComment(String lastEventComment) {
		this.lastEventComment = lastEventComment;
	}

}
