package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class FirstGlassJob extends UdfAccessor {

	@CTORMTemplate(seq = "1", name = "jobName", type = "Key", dataType = "String", initial = "", history = "")
	private String jobName;
	@CTORMTemplate(seq = "2", name = "lotName", type = "Column", dataType = "String", initial = "", history = "")
	private String lotName;
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
	@CTORMTemplate(seq = "8", name = "processOperationName", type = "Column", dataType = "String", initial = "", history = "")
	private String processOperationName;
	@CTORMTemplate(seq = "9", name = "processOperationVersion", type = "Column", dataType = "String", initial = "", history = "")
	private String processOperationVersion;
	@CTORMTemplate(seq = "10", name = "machineName", type = "Column", dataType = "String", initial = "", history = "")
	private String machineName;
	@CTORMTemplate(seq = "11", name = "toProcessFlowName", type = "Column", dataType = "String", initial = "", history = "")
	private String toProcessFlowName;
	@CTORMTemplate(seq = "12", name = "toProcessFlowVersion", type = "Column", dataType = "String", initial = "", history = "")
	private String toProcessFlowVersion;
	@CTORMTemplate(seq = "13", name = "toProcessOperationName", type = "Column", dataType = "String", initial = "", history = "")
	private String toProcessOperationName;
	@CTORMTemplate(seq = "14", name = "toProcessOperationVersion", type = "Column", dataType = "String", initial = "", history = "")
	private String toProcessOperationVersion;
	@CTORMTemplate(seq = "15", name = "jobState", type = "Column", dataType = "String", initial = "", history = "")
	private String jobState;
	@CTORMTemplate(seq = "16", name = "judge", type = "Column", dataType = "String", initial = "", history = "")
	private String judge;
	@CTORMTemplate(seq = "17", name = "createTime", type = "Column", dataType = "Timestamp", initial = "", history = "")
	private Timestamp createTime;
	@CTORMTemplate(seq = "18", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventUser;
	@CTORMTemplate(seq = "19", name = "lastEventTime", type = "Column", dataType = "Timestamp", initial = "", history = "N")
	private Timestamp lastEventTime;
	@CTORMTemplate(seq = "20", name = "lastEventTimekey", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventTimekey;
	@CTORMTemplate(seq = "21", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventComment;
	@CTORMTemplate(seq = "22", name = "toProcessFlowType", type = "Column", dataType = "String", initial = "", history = "")
	private String toProcessFlowType;
	@CTORMTemplate(seq = "23", name = "returnProcessFlowName", type = "Column", dataType = "String", initial = "", history = "")
	private String returnProcessFlowName;
	@CTORMTemplate(seq = "24", name = "returnProcessFlowVersion", type = "Column", dataType = "String", initial = "", history = "")
	private String returnProcessFlowVersion;
	@CTORMTemplate(seq = "25", name = "returnProcessOperationName", type = "Column", dataType = "String", initial = "", history = "")
	private String returnProcessOperationName;
	@CTORMTemplate(seq = "26", name = "returnProcessOperationVersion", type = "Column", dataType = "String", initial = "", history = "")
	private String returnProcessOperationVersion;
	@CTORMTemplate(seq = "27", name = "firstGlassAllPosition", type = "Column", dataType = "String", initial = "", history = "")
	private String firstGlassAllPosition;
	@CTORMTemplate(seq = "28", name = "offset", type = "Column", dataType = "String", initial = "", history = "")
	private String offset;
	
	public String getOffset() {
		return offset;
	}
	public void setOffset(String offset) {
		this.offset = offset;
	}
	public String getJobName()
	{
		return jobName;
	}
	public void setJobName(String jobName)
	{
		this.jobName = jobName;
	}
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
	public String getToProcessFlowName()
	{
		return toProcessFlowName;
	}
	public void setToProcessFlowName(String toProcessFlowName)
	{
		this.toProcessFlowName = toProcessFlowName;
	}
	public String getToProcessFlowVersion()
	{
		return toProcessFlowVersion;
	}
	public void setToProcessFlowVersion(String toProcessFlowVersion)
	{
		this.toProcessFlowVersion = toProcessFlowVersion;
	}
	public String getToProcessOperationName()
	{
		return toProcessOperationName;
	}
	public void setToProcessOperationName(String toProcessOperationName)
	{
		this.toProcessOperationName = toProcessOperationName;
	}
	public String getToProcessOperationVersion()
	{
		return toProcessOperationVersion;
	}
	public void setToProcessOperationVersion(String toProcessOperationVersion)
	{
		this.toProcessOperationVersion = toProcessOperationVersion;
	}
	public String getJobState()
	{
		return jobState;
	}
	public void setJobState(String jobState)
	{
		this.jobState = jobState;
	}
	public String getJudge()
	{
		return judge;
	}
	public void setJudge(String judge)
	{
		this.judge = judge;
	}
	public Timestamp getCreateTime()
	{
		return createTime;
	}
	public void setCreateTime(Timestamp createTime)
	{
		this.createTime = createTime;
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
	public String getToProcessFlowType()
	{
		return toProcessFlowType;
	}
	public void setToProcessFlowType(String toProcessFlowType)
	{
		this.toProcessFlowType = toProcessFlowType;
	}
	public String getReturnProcessFlowName()
	{
		return returnProcessFlowName;
	}
	public void setReturnProcessFlowName(String returnProcessFlowName)
	{
		this.returnProcessFlowName = returnProcessFlowName;
	}
	public String getReturnProcessFlowVersion()
	{
		return returnProcessFlowVersion;
	}
	public void setReturnProcessFlowVersion(String returnProcessFlowVersion)
	{
		this.returnProcessFlowVersion = returnProcessFlowVersion;
	}
	public String getReturnProcessOperationName()
	{
		return returnProcessOperationName;
	}
	public void setReturnProcessOperationName(String returnProcessOperationName)
	{
		this.returnProcessOperationName = returnProcessOperationName;
	}
	public String getReturnProcessOperationVersion()
	{
		return returnProcessOperationVersion;
	}
	public void setReturnProcessOperationVersion(String returnProcessOperationVersion)
	{
		this.returnProcessOperationVersion = returnProcessOperationVersion;
	}
	public String getFirstGlassAllPosition()
	{
		return firstGlassAllPosition;
	}
	public void setFirstGlassAllPosition(String firstGlassAllPosition)
	{
		this.firstGlassAllPosition = firstGlassAllPosition;
	}
}
