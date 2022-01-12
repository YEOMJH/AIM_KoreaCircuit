package kr.co.aim.messolution.extended.object.management.data;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class MQCPlanDetail_Extended extends UdfAccessor {
	@CTORMTemplate(seq = "1", name = "jobName", type = "Key", dataType = "String", initial = "", history = "")
	private String jobName;
	@CTORMTemplate(seq = "2", name = "processFlowName", type = "Key", dataType = "String", initial = "", history = "")
	private String processFlowName;
	@CTORMTemplate(seq = "3", name = "processFlowVersion", type = "Key", dataType = "String", initial = "", history = "")
	private String processFlowVersion;
	@CTORMTemplate(seq = "4", name = "processOperationName", type = "Key", dataType = "String", initial = "", history = "")
	private String processOperationName;
	@CTORMTemplate(seq = "5", name = "processOperationVersion", type = "Key", dataType = "String", initial = "", history = "")
	private String processOperationVersion;
	@CTORMTemplate(seq = "6", name = "productName", type = "Key", dataType = "String", initial = "", history = "")
	private String productName;
	@CTORMTemplate(seq = "7", name = "position", type = "Column", dataType = "String", initial = "", history = "")
	private String position;
	@CTORMTemplate(seq = "8", name = "lotName", type = "Column", dataType = "String", initial = "", history = "")
	private String lotName;
	@CTORMTemplate(seq = "9", name = "forbiddenCode", type = "Column", dataType = "String", initial = "", history = "")
	private String forbiddenCode;
	@CTORMTemplate(seq = "10", name = "oldForbiddenCode", type = "Column", dataType = "String", initial = "", history = "")
	private String oldForbiddenCode;
	@CTORMTemplate(seq = "11", name = "lastEventName", type = "Column", dataType = "String", initial = "", history = "")
	private String lastEventName;
	@CTORMTemplate(seq = "12", name = "lastEventTime", type = "Column", dataType = "String", initial = "", history = "")
	private String lastEventTime;
	@CTORMTemplate(seq = "13", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "")
	private String lastEventUser;
	@CTORMTemplate(seq = "14", name = "dummyUsedCount", type = "Column", dataType = "Number", initial = "", history = "")
	private Number dummyUsedCount;
	@CTORMTemplate(seq = "15", name = "recipeName", type = "Column", dataType = "String", initial = "", history = "")
	private String recipeName;
	@CTORMTemplate(seq = "16", name = "machineName", type = "Column", dataType = "String", initial = "", history = "")
	private String machineName;

	public String getJobName()
	{
		return jobName;
	}

	public void setJobName(String jobName)
	{
		this.jobName = jobName;
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

	public String getProductName()
	{
		return productName;
	}

	public void setProductName(String productName)
	{
		this.productName = productName;
	}

	public String getPosition()
	{
		return position;
	}

	public void setPosition(String position)
	{
		this.position = position;
	}

	public String getLotName()
	{
		return lotName;
	}

	public void setLotName(String lotName)
	{
		this.lotName = lotName;
	}

	public String getForbiddenCode()
	{
		return forbiddenCode;
	}

	public void setForbiddenCode(String forbiddenCode)
	{
		this.forbiddenCode = forbiddenCode;
	}

	public String getOldForbiddenCode()
	{
		return oldForbiddenCode;
	}

	public void setOldForbiddenCode(String oldForbiddenCode)
	{
		this.oldForbiddenCode = oldForbiddenCode;
	}

	public String getLastEventName()
	{
		return lastEventName;
	}

	public void setLastEventName(String lastEventName)
	{
		this.lastEventName = lastEventName;
	}

	public String getLastEventTime()
	{
		return lastEventTime;
	}

	public void setLastEventTime(String lastEventTime)
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

	public Number getDummyUsedCount()
	{
		return dummyUsedCount;
	}

	public void setDummyUsedCount(Number dummyUsedCount)
	{
		this.dummyUsedCount = dummyUsedCount;
	}

	public String getRecipeName()
	{
		return recipeName;
	}

	public void setRecipeName(String recipeName)
	{
		this.recipeName = recipeName;
	}

	public String getMachineName()
	{
		return machineName;
	}

	public void setMachineName(String machineName)
	{
		this.machineName = machineName;
	}
}
