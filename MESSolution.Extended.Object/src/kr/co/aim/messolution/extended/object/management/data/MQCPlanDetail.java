package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class MQCPlanDetail extends UdfAccessor {

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
	@CTORMTemplate(seq = "6", name = "dummyUsedLimit", type = "Column", dataType = "Number", initial = "", history = "")
	private Number dummyUsedLimit;
	@CTORMTemplate(seq = "7", name = "lotName", type = "Column", dataType = "String", initial = "", history = "")
	private String lotName;
	@CTORMTemplate(seq = "8", name = "carrierName", type = "Column", dataType = "String", initial = "", history = "")
	private String carrierName;
	@CTORMTemplate(seq = "9", name = "position", type = "Column", dataType = "String", initial = "", history = "")
	private String position;
	@CTORMTemplate(seq = "10", name = "machineName", type = "Column", dataType = "String", initial = "", history = "")
	private String machineName;
	@CTORMTemplate(seq = "11", name = "recipeName", type = "Column", dataType = "String", initial = "", history = "")
	private String recipeName;
	@CTORMTemplate(seq = "12", name = "lastEventTime", type = "Column", dataType = "Timestamp", initial = "", history = "N")
	private Timestamp lastEventTime;
	@CTORMTemplate(seq = "13", name = "lastEventName", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventName;
	@CTORMTemplate(seq = "14", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventUser;
	@CTORMTemplate(seq = "15", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventComment;
	@CTORMTemplate(seq = "16", name = "MQCReleaseFlag", type = "Column", dataType = "String", initial = "", history = "")
	private String MQCReleaseFlag;
	@CTORMTemplate(seq = "17", name = "countingFlag", type = "Column", dataType = "String", initial = "", history = "")
	private String countingFlag;

	public MQCPlanDetail()
	{

	}

	public MQCPlanDetail(String jobName, String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion)
	{
		setJobName(jobName);
		setProcessFlowName(processFlowName);
		setProcessFlowVersion(processFlowVersion);
		setProcessOperationName(processOperationName);
		setProcessOperationVersion(processOperationVersion);
	}

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

	public Number getDummyUsedLimit()
	{
		return dummyUsedLimit;
	}

	public void setDummyUsedLimit(Number dummyUsedLimit)
	{
		this.dummyUsedLimit = dummyUsedLimit;
	}

	public String getLotName()
	{
		return lotName;
	}

	public void setLotName(String lotName)
	{
		this.lotName = lotName;
	}

	public String getCarrierName()
	{
		return carrierName;
	}

	public void setCarrierName(String carrierName)
	{
		this.carrierName = carrierName;
	}

	public String getPosition()
	{
		return position;
	}

	public void setPosition(String position)
	{
		this.position = position;
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

	public String getMQCReleaseFlag()
	{
		return MQCReleaseFlag;
	}

	public void setMQCReleaseFlag(String mQCReleaseFlag)
	{
		MQCReleaseFlag = mQCReleaseFlag;
	}

	public String getCountingFlag()
	{
		return countingFlag;
	}

	public void setCountingFlag(String countingFlag)
	{
		this.countingFlag = countingFlag;
	}

}
