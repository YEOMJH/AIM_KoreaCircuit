package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class SVIPickInfo extends UdfAccessor 
{
	@CTORMTemplate(seq = "1", name = "timeKey", type = "Key", dataType = "String", initial = "", history = "pickTimeKey")
	private String timeKey;
	@CTORMTemplate(seq = "2", name = "machineName", type = "Column", dataType = "String", initial = "", history = "")
	private String machineName;
	@CTORMTemplate(seq = "3", name = "processOperationName", type = "Column", dataType = "String", initial = "", history = "")
	private String processOperationName;
	@CTORMTemplate(seq = "4", name = "processOperationVersion", type = "Column", dataType = "String", initial = "", history = "")
	private String processOperationVersion;
	@CTORMTemplate(seq = "5", name = "judge", type = "Column", dataType = "String", initial = "", history = "")
	private String judge;
	@CTORMTemplate(seq = "6", name = "code", type = "Column", dataType = "String", initial = "", history = "")
	private String code;
	@CTORMTemplate(seq = "7", name = "quantity", type = "Column", dataType = "Number", initial = "", history = "")
	private Number quantity;
	@CTORMTemplate(seq = "8", name = "endQuantity", type = "Column", dataType = "Number", initial = "", history = "")
	private Number endQuantity;
	@CTORMTemplate(seq = "9", name = "downLoadFlag", type = "Column", dataType = "String", initial = "", history = "")
	private String downLoadFlag;
	@CTORMTemplate(seq = "10", name = "lastEventName", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventName;
	@CTORMTemplate(seq = "11", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventUser;
	@CTORMTemplate(seq = "12", name = "lastEventTime", type = "Column", dataType = "TimeStamp", initial = "", history = "N")
	private Timestamp lastEventTime;
	@CTORMTemplate(seq = "13", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventComment;
	@CTORMTemplate(seq = "14", name = "lastEventTimeKey", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventTimeKey;
	@CTORMTemplate(seq = "15", name = "productSpecName", type = "Column", dataType = "String", initial = "", history = "")
	private String productSpecName;
	@CTORMTemplate(seq = "16", name = "productSpecVersion", type = "Column", dataType = "String", initial = "", history = "")
	private String productSpecVersion;
	@CTORMTemplate(seq = "17", name = "workOrder", type = "Column", dataType = "String", initial = "", history = "")
	private String workOrder;
	@CTORMTemplate(seq = "18", name = "point", type = "Column", dataType = "String", initial = "", history = "")
	private String point;
	
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
	public String getWorkOrder() {
		return workOrder;
	}
	public void setWorkOrder(String workOrder) {
		this.workOrder = workOrder;
	}
	public String getPoint() {
		return point;
	}
	public void setPoint(String point) {
		this.point = point;
	}
	public String getTimeKey()
	{
		return timeKey;
	}
	public void setTimeKey(String timeKey)
	{
		this.timeKey = timeKey;
	}
	public String getMachineName()
	{
		return machineName;
	}
	public void setMachineName(String machineName)
	{
		this.machineName = machineName;
	}
	public String getProcessOperationName()
	{
		return processOperationName;
	}
	public void setProcessOperationName(String processOperationName)
	{
		this.processOperationName = processOperationName;
	}
	public String getProcessOperationVersion()
	{
		return processOperationVersion;
	}
	public void setProcessOperationVersion(String processOperationVersion)
	{
		this.processOperationVersion = processOperationVersion;
	}
	public String getJudge()
	{
		return judge;
	}
	public void setJudge(String judge)
	{
		this.judge = judge;
	}
	public String getCode()
	{
		return code;
	}
	public void setCode(String code)
	{
		this.code = code;
	}
	public Number getQuantity()
	{
		return quantity;
	}
	public void setQuantity(Number quantity)
	{
		this.quantity = quantity;
	}
	public Number getEndQuantity()
	{
		return endQuantity;
	}
	public void setEndQuantity(Number endQuantity)
	{
		this.endQuantity = endQuantity;
	}
	public String getDownLoadFlag()
	{
		return downLoadFlag;
	}
	public void setDownLoadFlag(String downLoadFlag)
	{
		this.downLoadFlag = downLoadFlag;
	}
	public String getLastEventName()
	{
		return lastEventName;
	}
	public void setLastEventName(String lastEventName)
	{
		this.lastEventName = lastEventName;
	}
	public String getLastEventUser()
	{
		return lastEventUser;
	}
	public void setLastEventUser(String lastEventUser)
	{
		this.lastEventUser = lastEventUser;
	}
	public Timestamp getLastEventTime()
	{
		return lastEventTime;
	}
	public void setLastEventTime(Timestamp lastEventTime)
	{
		this.lastEventTime = lastEventTime;
	}
	public String getLastEventComment()
	{
		return lastEventComment;
	}
	public void setLastEventComment(String lastEventComment)
	{
		this.lastEventComment = lastEventComment;
	}
	public String getLastEventTimeKey()
	{
		return lastEventTimeKey;
	}
	public void setLastEventTimeKey(String lastEventTimeKey)
	{
		this.lastEventTimeKey = lastEventTimeKey;
	}
}
