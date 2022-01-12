package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class MaskFutureAction extends UdfAccessor {
	@CTORMTemplate(seq = "1", name = "maskLotName", type = "Key", dataType = "String", initial = "", history = "")
	private String maskLotName;

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

	@CTORMTemplate(seq = "7", name = "position", type = "Key", dataType = "Number", initial = "", history = "")
	private Number position;

	@CTORMTemplate(seq = "8", name = "reasonCodeType", type = "Key", dataType = "String", initial = "", history = "")
	private String reasonCodeType;

	@CTORMTemplate(seq = "9", name = "reasonCode", type = "Key", dataType = "String", initial = "", history = "")
	private String reasonCode;

	@CTORMTemplate(seq = "10", name = "actionName", type = "Column", dataType = "String", initial = "", history = "")
	private String actionName;

	@CTORMTemplate(seq = "11", name = "actionType", type = "Column", dataType = "String", initial = "", history = "")
	private String actionType;

	@CTORMTemplate(seq = "12", name = "lastEventTime", type = "Column", dataType = "Timestamp", initial = "", history = "N")
	private Timestamp lastEventTime;

	@CTORMTemplate(seq = "13", name = "lastEventName", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventName;

	@CTORMTemplate(seq = "14", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventUser;

	@CTORMTemplate(seq = "15", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventComment;
	
	public MaskFutureAction()
	{
	}

	public MaskFutureAction(String maskLotName, String factoryName, String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion, Number position,
			String reasonCodeType, String reasonCode, String actionName, String actionType, Timestamp lastEventTime, String lastEventName, String lastEventUser, String lastEventComment)
	{
		super();
		this.maskLotName = maskLotName;
		this.factoryName = factoryName;
		this.processFlowName = processFlowName;
		this.processFlowVersion = processFlowVersion;
		this.processOperationName = processOperationName;
		this.processOperationVersion = processOperationVersion;
		this.position = position;
		this.reasonCodeType = reasonCodeType;
		this.reasonCode = reasonCode;
		this.actionName = actionName;
		this.actionType = actionType;
		this.lastEventTime = lastEventTime;
		this.lastEventName = lastEventName;
		this.lastEventUser = lastEventUser;
		this.lastEventComment = lastEventComment;
	}

	public String getMaskLotName()
	{
		return maskLotName;
	}

	public void setMaskLotName(String maskLotName)
	{
		this.maskLotName = maskLotName;
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

	public String getReasonCodeType()
	{
		return reasonCodeType;
	}

	public void setReasonCodeType(String reasonCodeType)
	{
		this.reasonCodeType = reasonCodeType;
	}

	public String getReasonCode()
	{
		return reasonCode;
	}

	public void setReasonCode(String reasonCode)
	{
		this.reasonCode = reasonCode;
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

	public Timestamp getLastEventTime()
	{
		return lastEventTime;
	}

	public void setLastEventTime(Timestamp lastEventTime)
	{
		this.lastEventTime = lastEventTime;
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

	public String getLastEventComment()
	{
		return lastEventComment;
	}

	public void setLastEventComment(String lastEventComment)
	{
		this.lastEventComment = lastEventComment;
	}
}
