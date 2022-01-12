package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class HelpDeskInfo extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="workDate", type="Key", dataType="Timestamp", initial="", history="")
	private Timestamp workDate;
	
	@CTORMTemplate(seq = "2", name="workUserName", type="Column", dataType="String", initial="", history="")
	private String workUserName;

	@CTORMTemplate(seq = "3", name="workUserId", type="Key", dataType="String", initial="", history="")
	private String workUserId;

	@CTORMTemplate(seq = "4", name="workType", type="Column", dataType="String", initial="", history="")
	private String workType;

	public Timestamp getWorkDate() {
		return workDate;
	}

	public void setWorkDate(Timestamp workDate) {
		this.workDate = workDate;
	}

	public String getWorkUserName() {
		return workUserName;
	}

	public void setWorkUserName(String workUserName) {
		this.workUserName = workUserName;
	}

	public String getWorkUserId() {
		return workUserId;
	}

	public void setWorkUserId(String workUserId) {
		this.workUserId = workUserId;
	}

	public String getWorkType() {
		return workType;
	}

	public void setWorkType(String workType) {
		this.workType = workType;
	}

	
	
}