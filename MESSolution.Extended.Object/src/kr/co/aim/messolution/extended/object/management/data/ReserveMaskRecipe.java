package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class ReserveMaskRecipe extends UdfAccessor
{
	@CTORMTemplate(seq = "1", name="maskLotName", type="Key", dataType="String", initial="", history="")
	private String maskLotName;

	@CTORMTemplate(seq = "2", name="maskSpecName", type="Key", dataType="String", initial="", history="")
	private String maskSpecName;

	@CTORMTemplate(seq = "3", name="processFlowName", type="Key", dataType="String", initial="", history="")
	private String processFlowName;

	@CTORMTemplate(seq = "4", name="processFlowVersion", type="Key", dataType="String", initial="", history="")
	private String processFlowVersion;

	@CTORMTemplate(seq = "5", name="processOperationName", type="Key", dataType="String", initial="", history="")
	private String processOperationName;

	@CTORMTemplate(seq = "6", name="processOperationVersion", type="Key", dataType="String", initial="", history="")
	private String processOperationVersion;

	@CTORMTemplate(seq = "7", name="machineName", type="Key", dataType="String", initial="", history="")
	private String machineName;

	@CTORMTemplate(seq = "8", name="recipeName", type="Column", dataType="String", initial="", history="")
	private String recipeName;

	@CTORMTemplate(seq = "9", name="RMSFlag", type="Column", dataType="String", initial="", history="")
	private String RMSFlag;

	@CTORMTemplate(seq = "10", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;

	@CTORMTemplate(seq = "11", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;

	@CTORMTemplate(seq = "12", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;

	@CTORMTemplate(seq = "13", name="lastEventTimekey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimekey;

	@CTORMTemplate(seq = "14", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;

	public ReserveMaskRecipe()
	{
		super();
	}

	public ReserveMaskRecipe(String maskLotName, String maskSpecName, String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion,String machineName)
	{
		super();
		this.maskLotName = maskLotName;
		this.maskSpecName = maskSpecName;
		this.processFlowName = processFlowName;
		this.processFlowVersion = processFlowVersion;
		this.processOperationName = processOperationName;
		this.processOperationVersion = processOperationVersion;
		this.machineName= machineName;
	}

	public String getMaskLotName()
	{
		return maskLotName;
	}

	public void setMaskLotName(String maskLotName)
	{
		this.maskLotName = maskLotName;
	}

	public String getMaskSpecName()
	{
		return maskSpecName;
	}

	public void setMaskSpecName(String maskSpecName)
	{
		this.maskSpecName = maskSpecName;
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

	public String getMachineName()
	{
		return machineName;
	}

	public void setMachineName(String machineName)
	{
		this.machineName = machineName;
	}

	public String getRecipeName()
	{
		return recipeName;
	}

	public void setRecipeName(String recipeName)
	{
		this.recipeName = recipeName;
	}

	public String getRMSFlag()
	{
		return RMSFlag;
	}

	public void setRMSFlag(String RMSFlag)
	{
		this.RMSFlag = RMSFlag;
	}

	public String getLastEventName()
	{
		return lastEventName;
	}

	public void setLastEventName(String lastEventName)
	{
		this.lastEventName = lastEventName;
	}

	public Timestamp getLastEventTime()
	{
		return lastEventTime;
	}

	public void setLastEventTime(Timestamp lastEventTime)
	{
		this.lastEventTime = lastEventTime;
	}

	public String getLastEventUser()
	{
		return lastEventUser;
	}

	public void setLastEventUser(String lastEventUser)
	{
		this.lastEventUser = lastEventUser;
	}

	public String getLastEventTimekey()
	{
		return lastEventTimekey;
	}

	public void setLastEventTimekey(String lastEventTimekey)
	{
		this.lastEventTimekey = lastEventTimekey;
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
