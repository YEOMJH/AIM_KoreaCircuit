package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class Alarm extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="alarmCode", type="Key", dataType="String", initial="", history="")
	private String alarmCode;	
	
	@CTORMTemplate(seq = "2", name="alarmTimeKey", type="Key", dataType="String", initial="", history="")
	private String alarmTimeKey;
	
	@CTORMTemplate(seq = "15", name="machineName", type="Column", dataType="String", initial="", history="")
	private String machineName;
	
	@CTORMTemplate(seq = "16", name="unitName", type="Column", dataType="String", initial="", history="")
	private String unitName;
	
	@CTORMTemplate(seq = "3", name="alarmState", type="Column", dataType="String", initial="", history="")
	private String alarmState;
	
	@CTORMTemplate(seq = "4", name="alarmType", type="Column", dataType="String", initial="", history="")
	private String alarmType;
	
	@CTORMTemplate(seq = "5", name="alarmSeverity", type="Column", dataType="String", initial="", history="")
	private String alarmSeverity;
	
	@CTORMTemplate(seq = "6", name="description", type="Column", dataType="String", initial="", history="")
	private String description;
	
	@CTORMTemplate(seq = "7", name="createTimeKey", type="Column", dataType="String", initial="", history="")
	private String createTimeKey;
	
	@CTORMTemplate(seq = "8", name="createUser", type="Column", dataType="String", initial="", history="")
	private String createUser;
	
	@CTORMTemplate(seq = "9", name="resolveTimeKey", type="Column", dataType="String", initial="", history="")
	private String resolveTimeKey;
	
	@CTORMTemplate(seq = "10", name="resolveUser", type="Column", dataType="String", initial="", history="")
	private String resolveUser;
	
	@CTORMTemplate(seq = "11", name="lastEventName", type="Column", dataType="String", initial="", history="")
	private String lastEventName;
	
	@CTORMTemplate(seq = "12", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="")
	private String lastEventTimeKey;
	
	@CTORMTemplate(seq = "13", name="lastEventUser", type="Column", dataType="String", initial="", history="")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "14", name="lastEventComment", type="Column", dataType="String", initial="", history="")
	private String lastEventComment;
	
	@CTORMTemplate(seq = "17", name="productList", type="Column", dataType="String", initial="", history="")
	private String productList;
	
	
	public String getAlarmTimeKey() {
		return alarmTimeKey;
	}

	public void setAlarmTimeKey(String alarmTimeKey) {
		this.alarmTimeKey = alarmTimeKey;
	}

	//instantiation
	public Alarm()
	{
		
	}

	public Alarm(String alarmCode,  String alarmTimeKey)
	{
		setAlarmCode(alarmCode);
		setAlarmTimeKey(alarmTimeKey);
	}
	
	public String getAlarmCode() {
		return alarmCode;
	}

	public void setAlarmCode(String alarmCode) {
		this.alarmCode = alarmCode;
	}

	public String getAlarmState() {
		return alarmState;
	}

	public void setAlarmState(String alarmState) {
		this.alarmState = alarmState;
	}

	public String getAlarmType() {
		return alarmType;
	}

	public void setAlarmType(String alarmType) {
		this.alarmType = alarmType;
	}

	public String getAlarmSeverity() {
		return alarmSeverity;
	}

	public void setAlarmSeverity(String alarmSeverity) {
		this.alarmSeverity = alarmSeverity;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCreateTimeKey() {
		return createTimeKey;
	}

	public void setCreateTimeKey(String createTimeKey) {
		this.createTimeKey = createTimeKey;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public String getResolveTimeKey() {
		return resolveTimeKey;
	}

	public void setResolveTimeKey(String resolveTimeKey) {
		this.resolveTimeKey = resolveTimeKey;
	}

	public String getResolveUser() {
		return resolveUser;
	}

	public void setResolveUser(String resolveUser) {
		this.resolveUser = resolveUser;
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
	
	public String getMachineName() {
		return machineName;
	}
	
	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}
	
	public String getUnitName() {
		return unitName;
	}
	
	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}
	
	public String getProductList() {
		return productList;
	}
	
	public void setProductList(String productList) {
		this.productList = productList;
	}
}
