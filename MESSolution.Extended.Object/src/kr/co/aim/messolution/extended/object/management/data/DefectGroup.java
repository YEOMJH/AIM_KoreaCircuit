package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class DefectGroup extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name = "defectGroupName", type = "Key", dataType = "String", initial = "", history = "")
	private String defectGroupName;
	@CTORMTemplate(seq = "2", name = "defectCode", type = "Key", dataType = "String", initial = "", history = "")
	private String defectCode;
	@CTORMTemplate(seq = "3", name = "judge", type = "Key", dataType = "String", initial = "", history = "")
	private String judge;
	@CTORMTemplate(seq = "4", name = "lowerQty", type = "Column", dataType = "String", initial = "", history = "")
	private String lowerQty;
	@CTORMTemplate(seq = "5", name = "upperQty", type = "Column", dataType = "String", initial = "", history = "")
	private String upperQty;
	@CTORMTemplate(seq = "6", name = "useFlag", type = "Column", dataType = "String", initial = "", history = "")
	private String useFlag;
	@CTORMTemplate(seq = "7", name = "lastEventName", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventName;
	@CTORMTemplate(seq = "8", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventUser;
	@CTORMTemplate(seq = "9", name = "lastEventTime", type = "Column", dataType = "Timestamp", initial = "", history = "N")
	private Timestamp lastEventTime;
	@CTORMTemplate(seq = "10", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventComment;
	@CTORMTemplate(seq = "11", name = "lastEventTimekey", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventTimekey;
	
	public String getDefectGroupName() {
		return defectGroupName;
	}
	public void setDefectGroupName(String defectGroupName) {
		this.defectGroupName = defectGroupName;
	}
	public String getDefectCode() {
		return defectCode;
	}
	public void setDefectCode(String defectCode) {
		this.defectCode = defectCode;
	}
	public String getJudge() {
		return judge;
	}
	public void setJudge(String judge) {
		this.judge = judge;
	}
	public String getLowerQty() {
		return lowerQty;
	}
	public void setLowerQty(String lowerQty) {
		this.lowerQty = lowerQty;
	}
	public String getUpperQty() {
		return upperQty;
	}
	public void setUpperQty(String upperQty) {
		this.upperQty = upperQty;
	}
	public String getUseFlag() {
		return useFlag;
	}
	public void setUseFlag(String useFlag) {
		this.useFlag = useFlag;
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
