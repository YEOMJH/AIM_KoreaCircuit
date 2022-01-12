package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class LotFutureAction extends UdfAccessor {
	@CTORMTemplate(seq = "1", name = "lotName", type = "Key", dataType = "String", initial = "", history = "")
	private String lotName;
	@CTORMTemplate(seq = "2", name = "factoryName", type = "Key", dataType = "String", initial = "", history = "")
	private String factoryName;
	@CTORMTemplate(seq = "3", name = "processFlowName", type = "Key", dataType = "String", initial = "", history = "")
	private String processFlowName;
	@CTORMTemplate(seq = "4", name = "processFlowVersion", type = "Key", dataType = "String", initial = "", history = "")
	private String processFlowVersion;
	@CTORMTemplate(seq = "5", name = "processOperationName", type = "Key", dataType = "String", initial = "", history = "")
	private String processOperationName;
	@CTORMTemplate(seq = "6", name = "processOperationVersion", type = "Key", dataType = "String", initial = "", history = "")
	private String processOperationVersion;
	@CTORMTemplate(seq = "7", name = "position", type = "Key", dataType = "String", initial = "", history = "")
	private Number position;
	@CTORMTemplate(seq = "8", name = "reasonCode", type = "Key", dataType = "Number", initial = "", history = "")
	private String reasonCode;
	@CTORMTemplate(seq = "9", name = "reasonCodeType", type = "Column", dataType = "String", initial = "", history = "")
	private String reasonCodeType;
	@CTORMTemplate(seq = "10", name = "actionName", type = "Column", dataType = "String", initial = "", history = "")
	private String actionName;
	@CTORMTemplate(seq = "11", name = "actionType", type = "Column", dataType = "String", initial = "", history = "")
	private String actionType;
	@CTORMTemplate(seq = "12", name = "attribute1", type = "Column", dataType = "String", initial = "", history = "")
	private String attribute1;
	@CTORMTemplate(seq = "13", name = "attribute2", type = "Column", dataType = "String", initial = "", history = "")
	private String attribute2;
	@CTORMTemplate(seq = "14", name = "attribute3", type = "Column", dataType = "String", initial = "", history = "")
	private String attribute3;
	@CTORMTemplate(seq = "15", name = "beforeAction", type = "Column", dataType = "String", initial = "", history = "")
	private String beforeAction;
	@CTORMTemplate(seq = "16", name = "afterAction", type = "Column", dataType = "String", initial = "", history = "")
	private String afterAction;
	@CTORMTemplate(seq = "17", name = "beforeActionComment", type = "Column", dataType = "String", initial = "", history = "")
	private String beforeActionComment;
	@CTORMTemplate(seq = "18", name = "afterActionComment", type = "Column", dataType = "String", initial = "", history = "")
	private String afterActionComment;
	@CTORMTemplate(seq = "19", name = "beforeActionUser", type = "Column", dataType = "String", initial = "", history = "")
	private String beforeActionUser;
	@CTORMTemplate(seq = "20", name = "afterActionUser", type = "Column", dataType = "String", initial = "", history = "")
	private String afterActionUser;
	@CTORMTemplate(seq = "21", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventUser;
	@CTORMTemplate(seq = "22", name = "lastEventName", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventName;
	@CTORMTemplate(seq = "23", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventComment;
	@CTORMTemplate(seq = "24", name = "lastEventTimekey", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventTimekey;
	@CTORMTemplate(seq = "25", name = "lastEventTime", type = "Column", dataType = "Timestamp", initial = "", history = "N")
	private Timestamp lastEventTime;
	@CTORMTemplate(seq = "26", name = "beforeMailFlag", type = "Column", dataType = "String", initial = "", history = "")
	private String beforeMailFlag;
	@CTORMTemplate(seq = "27", name = "afterMailFlag", type = "Column", dataType = "String", initial = "", history = "")
	private String afterMailFlag;
	@CTORMTemplate(seq = "28", name = "alarmCode", type = "Column", dataType = "String", initial = "", history = "")
	private String alarmCode;
	@CTORMTemplate(seq = "29", name = "releaseType", type = "Column", dataType = "String", initial = "", history = "")
	private String releaseType;
	@CTORMTemplate(seq = "30", name = "requestDepartment", type = "Column", dataType = "String", initial = "", history = "")
	private String requestDepartment;

	public String getLotName()
	{
		return lotName;
	}

	public void setLotName(String lotName)
	{
		this.lotName = lotName;
	}

	public String getFactoryName()
	{
		return factoryName;
	}

	public void setFactoryName(String factoryName)
	{
		this.factoryName = factoryName;
	}

	public String getProcessFlowName()
	{
		return processFlowName;
	}

	public void setProcessFlowName(String processFlowName)
	{
		this.processFlowName = processFlowName;
	}

	public String getProcessFlowVersion()
	{
		return processFlowVersion;
	}

	public void setProcessFlowVersion(String processFlowVersion)
	{
		this.processFlowVersion = processFlowVersion;
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

	public Number getPosition()
	{
		return position;
	}

	public void setPosition(Number position)
	{
		this.position = position;
	}

	public String getReasonCode()
	{
		return reasonCode;
	}

	public void setReasonCode(String reasonCode)
	{
		this.reasonCode = reasonCode;
	}

	public String getReasonCodeType()
	{
		return reasonCodeType;
	}

	public void setReasonCodeType(String reasonCodeType)
	{
		this.reasonCodeType = reasonCodeType;
	}

	public String getActionName()
	{
		return actionName;
	}

	public void setActionName(String actionName)
	{
		this.actionName = actionName;
	}

	public String getActionType()
	{
		return actionType;
	}

	public void setActionType(String actionType)
	{
		this.actionType = actionType;
	}

	public String getAttribute1()
	{
		return attribute1;
	}

	public void setAttribute1(String attribute1)
	{
		this.attribute1 = attribute1;
	}

	public String getAttribute2()
	{
		return attribute2;
	}

	public void setAttribute2(String attribute2)
	{
		this.attribute2 = attribute2;
	}

	public String getAttribute3()
	{
		return attribute3;
	}

	public void setAttribute3(String attribute3)
	{
		this.attribute3 = attribute3;
	}

	public String getBeforeAction()
	{
		return beforeAction;
	}

	public void setBeforeAction(String beforeAction)
	{
		this.beforeAction = beforeAction;
	}

	public String getAfterAction()
	{
		return afterAction;
	}

	public void setAfterAction(String afterAction)
	{
		this.afterAction = afterAction;
	}

	public String getBeforeActionComment()
	{
		return beforeActionComment;
	}

	public void setBeforeActionComment(String beforeActionComment)
	{
		this.beforeActionComment = beforeActionComment;
	}

	public String getAfterActionComment()
	{
		return afterActionComment;
	}

	public void setAfterActionComment(String afterActionComment)
	{
		this.afterActionComment = afterActionComment;
	}

	public String getBeforeActionUser()
	{
		return beforeActionUser;
	}

	public void setBeforeActionUser(String beforeActionUser)
	{
		this.beforeActionUser = beforeActionUser;
	}

	public String getAfterActionUser()
	{
		return afterActionUser;
	}

	public void setAfterActionUser(String afterActionUser)
	{
		this.afterActionUser = afterActionUser;
	}

	public String getLastEventUser()
	{
		return lastEventUser;
	}

	public void setLastEventUser(String lastEventUser)
	{
		this.lastEventUser = lastEventUser;
	}

	public String getLastEventName()
	{
		return lastEventName;
	}

	public void setLastEventName(String lastEventName)
	{
		this.lastEventName = lastEventName;
	}

	public String getLastEventComment()
	{
		return lastEventComment;
	}

	public void setLastEventComment(String lastEventComment)
	{
		this.lastEventComment = lastEventComment;
	}

	public String getLastEventTimekey()
	{
		return lastEventTimekey;
	}

	public void setLastEventTimekey(String lastEventTimekey)
	{
		this.lastEventTimekey = lastEventTimekey;
	}

	public Timestamp getLastEventTime()
	{
		return lastEventTime;
	}

	public void setLastEventTime(Timestamp lastEventTime)
	{
		this.lastEventTime = lastEventTime;
	}

	public String getBeforeMailFlag()
	{
		return beforeMailFlag;
	}

	public void setBeforeMailFlag(String beforeMailFlag)
	{
		this.beforeMailFlag = beforeMailFlag;
	}

	public String getAfterMailFlag()
	{
		return afterMailFlag;
	}

	public void setAfterMailFlag(String afterMailFlag)
	{
		this.afterMailFlag = afterMailFlag;
	}

	public String getAlarmCode() {
		return alarmCode;
	}

	public void setAlarmCode(String alarmCode) {
		this.alarmCode = alarmCode;
	}

	public String getReleaseType() {
		return releaseType;
	}

	public void setReleaseType(String releaseType) {
		this.releaseType = releaseType;
	}
	
	public String getRequestDepartment() {
		return requestDepartment;
	}

	public void setRequestDepartment(String requestDepartment) {
		this.requestDepartment = requestDepartment;
	}
}
