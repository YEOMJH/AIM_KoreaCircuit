package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class MaskGroupList extends UdfAccessor {
	
	
	@CTORMTemplate(seq = "1", name="MaskGroupName", type="Key", dataType="String", initial="", history="")
	private String MaskGroupName;	
	
	@CTORMTemplate(seq = "2", name="productSpecName", type="Column", dataType="String", initial="", history="")
	private String productSpecName;
	
	@CTORMTemplate(seq = "3", name="UseFlag", type="Column", dataType="String", initial="", history="")
	private String UseFlag;
	
	@CTORMTemplate(seq = "4", name="Priority", type="Column", dataType="String", initial="", history="")
	private String Priority;
	
	@CTORMTemplate(seq = "5", name="LastEventName", type="Column", dataType="String", initial="", history="")
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

	public String getProductSpecName() {
		return productSpecName;
	}

	public void setProductSpecName(String productSpecName) {
		this.productSpecName = productSpecName;
	}

	public String getUseFlag() {
		return UseFlag;
	}

	public void setUseFlag(String useFlag) {
		UseFlag = useFlag;
	}

	public String getPriority() {
		return Priority;
	}

	public void setPriority(String priority) {
		Priority = priority;
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
