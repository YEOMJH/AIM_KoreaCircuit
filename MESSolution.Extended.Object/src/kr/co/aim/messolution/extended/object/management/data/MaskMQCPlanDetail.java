package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class MaskMQCPlanDetail extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="jobName", type="Key", dataType="String", initial="", history="")
	private String jobName;

	@CTORMTemplate(seq = "2", name="maskProcessFlowName", type="Key", dataType="String", initial="", history="")
	private String maskProcessFlowName;

	@CTORMTemplate(seq = "3", name="maskProcessFlowVersion", type="Key", dataType="String", initial="", history="")
	private String maskProcessFlowVersion;

	@CTORMTemplate(seq = "4", name="maskProcessOperationName", type="Key", dataType="String", initial="", history="")
	private String maskProcessOperationName;

	@CTORMTemplate(seq = "5", name="maskProcessOperationVersion", type="Key", dataType="String", initial="", history="")
	private String maskProcessOperationVersion;

	@CTORMTemplate(seq = "6", name="maskLotName", type="Column", dataType="String", initial="", history="")
	private String maskLotName;

	@CTORMTemplate(seq = "7", name="machineName", type="Column", dataType="String", initial="", history="")
	private String machineName;

	@CTORMTemplate(seq = "8", name="recipeName", type="Column", dataType="String", initial="", history="")
	private String recipeName;

	@CTORMTemplate(seq = "9", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;

	@CTORMTemplate(seq = "10", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;

	@CTORMTemplate(seq = "11", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;

	@CTORMTemplate(seq = "12", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;
	
	@CTORMTemplate(seq = "13", name="RMSFlag", type="Column", dataType="String", initial="", history="N")
	private String RMSFlag;
	
	public MaskMQCPlanDetail()
	{
		
	}
	
	public MaskMQCPlanDetail(String jobName, String processFlowName, String processFlowVersion , String processOperationName, String processOperationVersion)
	{
		setJobName(jobName);
		setMaskProcessFlowName(processFlowName);
		setMaskProcessFlowVersion(processFlowVersion);
		setMaskProcessOperationName(processOperationName);
		setMaskProcessOperationVersion(processOperationVersion);
	}
	
	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getMaskProcessFlowName()
	{
		return maskProcessFlowName;
	}

	public void setMaskProcessFlowName(String maskProcessFlowName)
	{
		this.maskProcessFlowName = maskProcessFlowName;
	}

	public String getMaskProcessFlowVersion()
	{
		return maskProcessFlowVersion;
	}

	public void setMaskProcessFlowVersion(String maskProcessFlowVersion)
	{
		this.maskProcessFlowVersion = maskProcessFlowVersion;
	}

	public String getMaskProcessOperationName()
	{
		return maskProcessOperationName;
	}

	public void setMaskProcessOperationName(String maskProcessOperationName)
	{
		this.maskProcessOperationName = maskProcessOperationName;
	}

	public String getMaskProcessOperationVersion()
	{
		return maskProcessOperationVersion;
	}

	public void setMaskProcessOperationVersion(String maskProcessOperationVersion)
	{
		this.maskProcessOperationVersion = maskProcessOperationVersion;
	}

	public String getMaskLotName()
	{
		return maskLotName;
	}

	public void setMaskLotName(String maskLotName)
	{
		this.maskLotName = maskLotName;
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

	public String getRMSFlag() {
		return RMSFlag;
	}

	public void setRMSFlag(String rMSFlag) {
		RMSFlag = rMSFlag;
	}
	
}
