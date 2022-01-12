package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class MaskStickRule extends UdfAccessor{
	
	@CTORMTemplate(seq = "1", name="MaskFilmLayer", type="Key", dataType="String", initial="", history="")
	private String MaskFilmLayer;
	
	@CTORMTemplate(seq = "2", name="stickType", type="Key", dataType="String", initial="", history="")
	private String stickType;
	
	@CTORMTemplate(seq = "3", name="StickFilmLayer", type="Key", dataType="String", initial="", history="")
	private String StickFilmLayer;
	
	@CTORMTemplate(seq = "4", name="Description", type="Column", dataType="String", initial="", history="")
	private String description;

	@CTORMTemplate(seq = "5", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;
	
	@CTORMTemplate(seq = "6", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "7", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "8", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;
	
	@CTORMTemplate(seq = "9", name="lastEventTimekey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimekey;
	
	public String getMaskFilmLayer() {
		return MaskFilmLayer;
	}

	public void setMaskFilmLayer(String maskFilmLayer) {
		MaskFilmLayer = maskFilmLayer;
	}

	public String getStickType() {
		return stickType;
	}

	public void setStickType(String stickType) {
		this.stickType = stickType;
	}

	public String getStickFilmLayer() {
		return StickFilmLayer;
	}

	public void setStickFilmLayer(String stickFilmLayer) {
		StickFilmLayer = stickFilmLayer;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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
