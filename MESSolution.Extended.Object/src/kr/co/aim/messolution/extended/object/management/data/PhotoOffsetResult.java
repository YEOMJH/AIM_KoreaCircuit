package kr.co.aim.messolution.extended.object.management.data;

import java.util.Date;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class PhotoOffsetResult extends UdfAccessor {
	@CTORMTemplate(seq = "1", name="machineName", type="Key", dataType="String", initial="", history="")
	private String machineName;
	@CTORMTemplate(seq = "2", name="offset", type="Key", dataType="String", initial="", history="")
	private String offset;
	@CTORMTemplate(seq = "3", name="lastUseTime", type="Column", dataType="Date", initial="", history="")
	private Date lastUseTime;
	@CTORMTemplate(seq = "4", name="lastEventUser", type="Column", dataType="String", initial="", history="")
	private String lastEventUser;
	@CTORMTemplate(seq = "5", name="lastEventComment", type="Column", dataType="String", initial="", history="")
	private String lastEventComment;
	
	public PhotoOffsetResult() {
		super();
	}
	public String getMachineName() {
		return machineName;
	}
	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}
	public String getOffset() {
		return offset;
	}
	public void setOffset(String offset) {
		this.offset = offset;
	}
	public Date getLastUseTime() {
		return lastUseTime;
	}
	public void setLastUseTime(Date lastUseTime) {
		this.lastUseTime = lastUseTime;
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
