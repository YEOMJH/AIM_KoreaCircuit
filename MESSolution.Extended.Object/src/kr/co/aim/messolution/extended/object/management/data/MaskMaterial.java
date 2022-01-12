package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class MaskMaterial extends UdfAccessor{
	@CTORMTemplate(seq = "1", name="maskLotName", type="Key", dataType="String", initial="", history="")
	private String maskLotName;
	@CTORMTemplate(seq = "2", name="materialType", type="Key", dataType="String", initial="", history="")
	private String materialType;
	@CTORMTemplate(seq = "3", name="materialName", type="Key", dataType="String", initial="", history="")
	private String materialName;
	@CTORMTemplate(seq = "4", name="position", type="Column", dataType="String", initial="", history="")
	private String position;
	@CTORMTemplate(seq = "5", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;
	@CTORMTemplate(seq = "6", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;
	@CTORMTemplate(seq = "7", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;
	@CTORMTemplate(seq = "8", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimeKey;
	@CTORMTemplate(seq = "9", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;
	public String getMaskLotName() {
		return maskLotName;
	}
	public void setMaskLotName(String maskLotName) {
		this.maskLotName = maskLotName;
	}
	public String getMaterialType() {
		return materialType;
	}
	public void setMaterialType(String materialType) {
		this.materialType = materialType;
	}
	public String getMaterialName() {
		return materialName;
	}
	public void setMaterialName(String materialName) {
		this.materialName = materialName;
	}
	public String getPosition() {
		return position;
	}
	public void setPosition(String position) {
		this.position = position;
	}
	public String getLastEventComment() {
		return lastEventComment;
	}
	public void setLastEventComment(String lastEventComment) {
		this.lastEventComment = lastEventComment;
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
	public String getLastEventTimeKey() {
		return lastEventTimeKey;
	}
	public void setLastEventTimeKey(String lastEventTimeKey) {
		this.lastEventTimeKey = lastEventTimeKey;
	}
	public String getLastEventUser() {
		return lastEventUser;
	}
	public void setLastEventUser(String lastEventUser) {
		this.lastEventUser = lastEventUser;
	}
	
	
}
