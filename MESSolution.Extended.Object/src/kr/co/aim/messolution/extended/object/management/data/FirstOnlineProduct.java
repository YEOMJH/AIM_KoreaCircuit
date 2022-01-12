package kr.co.aim.messolution.extended.object.management.data;


import java.sql.Timestamp;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class FirstOnlineProduct extends UdfAccessor  {
	
	@CTORMTemplate(seq = "1", name = "lotName", type = "Key", dataType = "String", initial = "", history = "")
	private String lotName;
	@CTORMTemplate(seq = "2", name = "productSpecName", type = "Column", dataType = "String", initial = "", history = "")
	private String productSpecName;
	@CTORMTemplate(seq = "3", name = "productType", type = "Column", dataType = "String", initial = "", history = "")
	private String productType;
	@CTORMTemplate(seq = "4", name = "LT", type = "Column", dataType = "String", initial = "", history = "")
	private String LT;
	@CTORMTemplate(seq = "5", name = "LB", type = "Column", dataType = "String", initial = "", history = "")
	private String LB;
	@CTORMTemplate(seq = "6", name = "RT", type = "Column", dataType = "String", initial = "", history = "")
	private String RT;
	@CTORMTemplate(seq = "7", name = "RB", type = "Column", dataType = "String", initial = "", history = "")
	private String RB;
	@CTORMTemplate(seq = "8", name = "lineFlag", type = "Column", dataType = "String", initial = "", history = "")
	private String lineFlag;
	@CTORMTemplate(seq = "9", name = "muraFlag", type = "Column", dataType = "String", initial = "", history = "")
	private String muraFlag;
	@CTORMTemplate(seq = "10", name = "surfaceFlag", type = "Column", dataType = "String", initial = "", history = "")
	private String surfaceFlag;
	@CTORMTemplate(seq = "11", name = "MVIUser", type = "Column", dataType = "String", initial = "", history = "")
	private String MVIUser;
	@CTORMTemplate(seq = "12", name = "surfaceUser", type = "Column", dataType = "String", initial = "", history = "")
	private String surfaceUser;
	@CTORMTemplate(seq = "13", name = "checkUser", type = "Column", dataType = "String", initial = "", history = "")
	private String checkUser;
	@CTORMTemplate(seq = "14", name = "checkTime", type = "Column", dataType = "String", initial = "", history = "")
	private String checkTime;
	@CTORMTemplate(seq = "15", name = "lastEventName", type = "Column", dataType = "String", initial = "", history = "")
	private String lastEventName;
	@CTORMTemplate(seq = "16", name = "lastEventTime", type = "Column", dataType = "Timestamp", initial = "", history = "N")
	private Timestamp lastEventTime;
	@CTORMTemplate(seq = "17", name = "lastEventTimeKey", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventTimeKey;
	@CTORMTemplate(seq = "18", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventUser;
	@CTORMTemplate(seq = "19", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventComment;
	
	
	
	public String getLotName() {
		return lotName;
	}
	public void setLotName(String lotName) {
		this.lotName = lotName;
	}
	public String getProductSpecName() {
		return productSpecName;
	}
	public void setProductSpecName(String productSpecName) {
		this.productSpecName = productSpecName;
	}
	public String getProductType() {
		return productType;
	}
	public void setProductType(String productType) {
		this.productType = productType;
	}
	public String getLT() {
		return LT;
	}
	public void setLT(String lT) {
		LT = lT;
	}
	public String getLB() {
		return LB;
	}
	public void setLB(String lB) {
		LB = lB;
	}
	public String getRT() {
		return RT;
	}
	public void setRT(String rT) {
		RT = rT;
	}
	public String getRB() {
		return RB;
	}
	public void setRB(String rB) {
		RB = rB;
	}
	public String getLineFlag() {
		return lineFlag;
	}
	public void setLineFlag(String lineFlag) {
		this.lineFlag = lineFlag;
	}
	public String getMuraFlag() {
		return muraFlag;
	}
	public void setMuraFlag(String muraFlag) {
		this.muraFlag = muraFlag;
	}
	public String getSurfaceFlag() {
		return surfaceFlag;
	}
	public void setSurfaceFlag(String surfaceFlag) {
		this.surfaceFlag = surfaceFlag;
	}
	public String getMVIUser() {
		return MVIUser;
	}
	public void setMVIUser(String mVIUser) {
		MVIUser = mVIUser;
	}
	public String getSurfaceUser() {
		return surfaceUser;
	}
	public void setSurfaceUser(String surfaceUser) {
		this.surfaceUser = surfaceUser;
	}
	public String getCheckUser() {
		return checkUser;
	}
	public void setCheckUser(String lastCheckUser) {
		this.checkUser = lastCheckUser;
	}
	public String getCheckTime() {
		return checkTime;
	}
	public void setCheckTime(String checkTime) {
		this.checkTime = checkTime;
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
	public String getLastEventComment() {
		return lastEventComment;
	}
	public void setLastEventComment(String lastEventComment) {
		this.lastEventComment = lastEventComment;
	}
	

}
