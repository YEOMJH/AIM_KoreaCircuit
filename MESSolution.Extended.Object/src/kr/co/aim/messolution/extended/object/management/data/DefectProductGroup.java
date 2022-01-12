package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class DefectProductGroup extends UdfAccessor {
	@CTORMTemplate(seq = "1", name = "defectFunction", type = "Key", dataType = "String", initial = "", history = "")
	private String defectFunction;
	@CTORMTemplate(seq = "2", name = "factoryName", type = "Key", dataType = "String", initial = "", history = "")
	private String factoryName;
	@CTORMTemplate(seq = "3", name = "productSpecName", type = "Key", dataType = "String", initial = "", history = "")
	private String productSpecName;
	@CTORMTemplate(seq = "4", name = "productSpecVersion", type = "Key", dataType = "String", initial = "", history = "")
	private String productSpecVersion;
	@CTORMTemplate(seq = "5", name = "defectGroupName", type = "Column", dataType = "String", initial = "", history = "")
	private String defectGroupName;
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
	
	public String getDefectFunction() {
		return defectFunction;
	}
	public void setDefectFunction(String defectFunction) {
		this.defectFunction = defectFunction;
	}
	public String getFactoryName() {
		return factoryName;
	}
	public void setFactoryName(String factoryName) {
		this.factoryName = factoryName;
	}
	public String getProductSpecName() {
		return productSpecName;
	}
	public void setProductSpecName(String productSpecName) {
		this.productSpecName = productSpecName;
	}
	public String getProductSpecVersion() {
		return productSpecVersion;
	}
	public void setProductSpecVersion(String productSpecVersion) {
		this.productSpecVersion = productSpecVersion;
	}
	public String getDefectGroupName() {
		return defectGroupName;
	}
	public void setDefectGroupName(String defectGroupName) {
		this.defectGroupName = defectGroupName;
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
