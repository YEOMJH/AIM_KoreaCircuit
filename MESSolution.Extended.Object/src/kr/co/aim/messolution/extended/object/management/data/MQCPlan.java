package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class MQCPlan extends UdfAccessor {

	@CTORMTemplate(seq = "1", name = "jobName", type = "Key", dataType = "String", initial = "", history = "")
	private String jobName;
	@CTORMTemplate(seq = "2", name = "seq", type = "Column", dataType = "Number", initial = "", history = "")
	private long seq;
	@CTORMTemplate(seq = "3", name = "factoryName", type = "Column", dataType = "String", initial = "", history = "")
	private String factoryName;
	@CTORMTemplate(seq = "4", name = "productSpecName", type = "Column", dataType = "String", initial = "", history = "")
	private String productSpecName;
	@CTORMTemplate(seq = "5", name = "productSpecVersion", type = "Column", dataType = "String", initial = "", history = "")
	private String productSpecVersion;
	@CTORMTemplate(seq = "6", name = "processFlowName", type = "Column", dataType = "String", initial = "", history = "")
	private String processFlowName;
	@CTORMTemplate(seq = "7", name = "processFlowVersion", type = "Column", dataType = "String", initial = "", history = "")
	private String processFlowVersion;
	@CTORMTemplate(seq = "8", name = "lotName", type = "Column", dataType = "String", initial = "", history = "")
	private String lotName;
	@CTORMTemplate(seq = "9", name = "MQCState", type = "Column", dataType = "String", initial = "", history = "")
	private String MQCState;
	@CTORMTemplate(seq = "10", name = "reworkFlowName", type = "Column", dataType = "String", initial = "", history = "")
	private String reworkFlowName;
	@CTORMTemplate(seq = "11", name = "dummyUsedLimit", type = "Column", dataType = "Number", initial = "", history = "")
	private long dummyUsedLimit;
	@CTORMTemplate(seq = "12", name = "reworkCountLimit", type = "Column", dataType = "Number", initial = "", history = "")
	private long reworkCountLimit;
	@CTORMTemplate(seq = "13", name = "lastEventTime", type = "Column", dataType = "Timestamp", initial = "", history = "N")
	private Timestamp lastEventTime;
	@CTORMTemplate(seq = "14", name = "lastEventName", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventName;
	@CTORMTemplate(seq = "15", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventUser;
	@CTORMTemplate(seq = "16", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventComment;
	@CTORMTemplate(seq = "17", name = "returnFlowName", type = "Column", dataType = "String", initial = "", history = "")
	private String returnFlowName;
	@CTORMTemplate(seq = "18", name = "returnFlowVersion", type = "Column", dataType = "String", initial = "", history = "")
	private String returnFlowVersion;
	@CTORMTemplate(seq = "19", name = "returnOperationName", type = "Column", dataType = "String", initial = "", history = "")
	private String returnOperationName;
	@CTORMTemplate(seq = "20", name = "returnOperationVersion", type = "Column", dataType = "String", initial = "", history = "")
	private String returnOperationVersion;
	@CTORMTemplate(seq = "21", name = "createUser", type = "Column", dataType = "String", initial = "", history = "")
	private String createUser;
	@CTORMTemplate(seq = "22", name = "department", type = "Column", dataType = "String", initial = "", history = "")
	private String department;
	@CTORMTemplate(seq = "23", name = "recycleFlowName", type = "Column", dataType = "String", initial = "", history = "")
	private String recycleFlowName;
	@CTORMTemplate(seq = "24", name = "recycleFlowVersion", type = "Column", dataType = "String", initial = "", history = "")
	private String recycleFlowVersion;
	@CTORMTemplate(seq = "25", name = "prepareSpecName", type = "Column", dataType = "String", initial = "", history = "")
	private String prepareSpecName;
	@CTORMTemplate(seq = "26", name = "prepareSpecVersion", type = "Column", dataType = "String", initial = "", history = "")
	private String prepareSpecVersion;
	@CTORMTemplate(seq = "27", name = "recycleLimit", type = "Column", dataType = "Number", initial = "", history = "")
	private long recycleLimit;
	@CTORMTemplate(seq = "28", name = "recycleCount", type = "Column", dataType = "Number", initial = "", history = "")
	private long recycleCount;

	// instantiation
	public MQCPlan()
	{

	}

	public MQCPlan(String jobName)
	{
		setJobName(jobName);
	}

	public String getJobName()
	{
		return jobName;
	}

	public void setJobName(String jobName)
	{
		this.jobName = jobName;
	}

	public long getSeq()
	{
		return seq;
	}

	public void setSeq(long seq)
	{
		this.seq = seq;
	}

	public String getFactoryName()
	{
		return factoryName;
	}

	public void setFactoryName(String factoryName)
	{
		this.factoryName = factoryName;
	}

	public String getProductSpecName()
	{
		return productSpecName;
	}

	public void setProductSpecName(String productSpecName)
	{
		this.productSpecName = productSpecName;
	}

	public String getProductSpecVersion()
	{
		return productSpecVersion;
	}

	public void setProductSpecVersion(String productSpecVersion)
	{
		this.productSpecVersion = productSpecVersion;
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

	public String getLotName()
	{
		return lotName;
	}

	public void setLotName(String lotName)
	{
		this.lotName = lotName;
	}

	public String getMQCState()
	{
		return MQCState;
	}

	public void setMQCState(String mQCState)
	{
		MQCState = mQCState;
	}

	public String getReworkFlowName()
	{
		return reworkFlowName;
	}

	public void setReworkFlowName(String reworkFlowName)
	{
		this.reworkFlowName = reworkFlowName;
	}

	public long getDummyUsedLimit()
	{
		return dummyUsedLimit;
	}

	public void setDummyUsedLimit(long dummyUsedLimit)
	{
		this.dummyUsedLimit = dummyUsedLimit;
	}

	public long getReworkCountLimit()
	{
		return reworkCountLimit;
	}

	public void setReworkCountLimit(long reworkCountLimit)
	{
		this.reworkCountLimit = reworkCountLimit;
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

	public String getReturnFlowName()
	{
		return returnFlowName;
	}

	public void setReturnFlowName(String returnFlowName)
	{
		this.returnFlowName = returnFlowName;
	}

	public String getReturnFlowVersion()
	{
		return returnFlowVersion;
	}

	public void setReturnFlowVersion(String returnFlowVersion)
	{
		this.returnFlowVersion = returnFlowVersion;
	}

	public String getReturnOperationName()
	{
		return returnOperationName;
	}

	public void setReturnOperationName(String returnOperationName)
	{
		this.returnOperationName = returnOperationName;
	}

	public String getReturnOperationVersion()
	{
		return returnOperationVersion;
	}

	public void setReturnOperationVersion(String returnOperationVersion)
	{
		this.returnOperationVersion = returnOperationVersion;
	}

	public void setCreateUser(String createUser)
	{
		this.createUser = createUser;
	}

	public String getCreateUser()
	{
		return createUser;
	}

	public String getDepartment()
	{
		return department;
	}

	public void setDepartment(String department)
	{
		this.department = department;
	}

	public String getRecycleFlowName()
	{
		return recycleFlowName;
	}

	public void setRecycleFlowName(String recycleFlowName)
	{
		this.recycleFlowName = recycleFlowName;
	}

	public String getRecycleFlowVersion()
	{
		return recycleFlowVersion;
	}

	public void setRecycleFlowVersion(String recycleFlowVersion)
	{
		this.recycleFlowVersion = recycleFlowVersion;
	}

	public String getPrepareSpecName()
	{
		return prepareSpecName;
	}

	public void setPrepareSpecName(String prepareSpecName)
	{
		this.prepareSpecName = prepareSpecName;
	}

	public String getPrepareSpecVersion()
	{
		return prepareSpecVersion;
	}

	public void setPrepareSpecVersion(String prepareSpecVersion)
	{
		this.prepareSpecVersion = prepareSpecVersion;
	}

	public long getRecycleLimit()
	{
		return recycleLimit;
	}

	public void setRecycleLimit(long recycleLimit)
	{
		this.recycleLimit = recycleLimit;
	}

	public long getRecycleCount()
	{
		return recycleCount;
	}

	public void setRecycleCount(long recycleCount)
	{
		this.recycleCount = recycleCount;
	}

}
