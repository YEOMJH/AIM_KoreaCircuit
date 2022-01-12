package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class MaskMQCPlan extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="jobName", type="Key", dataType="String", initial="", history="")
	private String jobName;

	@CTORMTemplate(seq = "2", name="factoryName", type="Column", dataType="String", initial="", history="")
	private String factoryName;

	@CTORMTemplate(seq = "3", name="maskSpecName", type="Column", dataType="String", initial="", history="")
	private String maskSpecName;

	@CTORMTemplate(seq = "4", name="maskProcessFlowName", type="Column", dataType="String", initial="", history="")
	private String maskProcessFlowName;

	@CTORMTemplate(seq = "5", name="maskProcessFlowVersion", type="Column", dataType="String", initial="", history="")
	private String maskProcessFlowVersion;

	@CTORMTemplate(seq = "6", name="maskLotName", type="Column", dataType="String", initial="", history="")
	private String maskLotName;

	@CTORMTemplate(seq = "7", name="MQCState", type="Column", dataType="String", initial="", history="")
	private String MQCState;

	@CTORMTemplate(seq = "8", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;

	@CTORMTemplate(seq = "9", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;

	@CTORMTemplate(seq = "10", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;

	@CTORMTemplate(seq = "11", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;

	@CTORMTemplate(seq = "12", name="createUser", type="Column", dataType="String", initial="", history="")
	private String createUser;
	
	@CTORMTemplate(seq = "13", name="returnFlowName", type="Column", dataType="String", initial="", history="")
	private String returnFlowName;

	@CTORMTemplate(seq = "14", name="returnFlowVersion", type="Column", dataType="String", initial="", history="")
	private String returnFlowVersion;
	
	@CTORMTemplate(seq = "15", name="returnOperationName", type="Column", dataType="String", initial="", history="")
	private String returnOperationName;
	
	@CTORMTemplate(seq = "16", name="returnOperationVersion", type="Column", dataType="String", initial="", history="")
	private String returnOperationVersion;
	
	//instantiation
	public MaskMQCPlan()
	{
		
	}
	
	public MaskMQCPlan(String jobName)
	{
		setJobName(jobName);
	}
	
	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	
	public String getFactoryName()
	{
		return factoryName;
	}

	public void setFactoryName(String factoryName)
	{
		this.factoryName = factoryName;
	}
	
	public String getMaskSpecName()
	{
		return maskSpecName;
	}

	public void setMaskSpecName(String maskSpecName)
	{
		this.maskSpecName = maskSpecName;
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

	public String getMaskLotName()
	{
		return maskLotName;
	}

	public void setMaskLotName(String maskLotName)
	{
		this.maskLotName = maskLotName;
	}

	public String getMQCState()
	{
		return MQCState;
	}

	public void setMQCState(String mQCState)
	{
		MQCState = mQCState;
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

	public String getCreateUser()
	{
		return createUser;
	}

	public void setCreateUser(String createUser)
	{
		this.createUser = createUser;
	}
	
	public String getReturnFlowName() {
		return returnFlowName;
	}

	public void setReturnFlowName(String returnFlowName) {
		this.returnFlowName = returnFlowName;
	}

	public String getReturnFlowVersion() {
		return returnFlowVersion;
	}

	public void setReturnFlowVersion(String returnFlowVersion) {
		this.returnFlowVersion = returnFlowVersion;
	}

	public String getReturnOperationName() {
		return returnOperationName;
	}

	public void setReturnOperationName(String returnOperationName) {
		this.returnOperationName = returnOperationName;
	}

	public String getReturnOperationVersion() {
		return returnOperationVersion;
	}

	public void setReturnOperationVersion(String returnOperationVersion) {
		this.returnOperationVersion = returnOperationVersion;
	}


}
