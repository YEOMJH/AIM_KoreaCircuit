package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class SampleMask extends UdfAccessor
{
	@CTORMTemplate(seq = "1", name = "maskLotName", type = "Key", dataType = "String", initial = "", history = "")
	private String maskLotName;

	@CTORMTemplate(seq = "2", name = "factoryName", type = "Key", dataType = "String", initial = "", history = "")
	private String factoryName;

	@CTORMTemplate(seq = "3", name = "maskSpecName", type = "Key", dataType = "String", initial = "", history = "")
	private String maskSpecName;

	@CTORMTemplate(seq = "4", name = "processFlowName", type = "Key", dataType = "String", initial = "", history = "")
	private String processFlowName;

	@CTORMTemplate(seq = "5", name = "processFlowVersion", type = "Key", dataType = "String", initial = "", history = "")
	private String processFlowVersion;

	@CTORMTemplate(seq = "6", name = "processOperationName", type = "Key", dataType = "String", initial = "", history = "")
	private String processOperationName;

	@CTORMTemplate(seq = "7", name = "processOperationVersion", type = "Key", dataType = "String", initial = "", history = "")
	private String processOperationVersion;

	@CTORMTemplate(seq = "8", name = "returnProcessFlowName", type = "Column", dataType = "String", initial = "", history = "")
	private String returnProcessFlowName;

	@CTORMTemplate(seq = "9", name = "returnProcessFlowVersion", type = "Column", dataType = "String", initial = "", history = "")
	private String returnProcessFlowVersion;

	@CTORMTemplate(seq = "10", name = "returnOperationName", type = "Column", dataType = "String", initial = "", history = "")
	private String returnOperationName;

	@CTORMTemplate(seq = "11", name = "returnOperationVersion", type = "Column", dataType = "String", initial = "", history = "")
	private String returnOperationVersion;

	@CTORMTemplate(seq = "12", name = "lastEventName", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventName;

	@CTORMTemplate(seq = "13", name = "lastEventTimekey", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventTimekey;

	@CTORMTemplate(seq = "14", name = "lastEventTime", type = "Column", dataType = "Timestamp", initial = "", history = "N")
	private Timestamp lastEventTime;

	@CTORMTemplate(seq = "15", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventUser;

	@CTORMTemplate(seq = "16", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventComment;

	public SampleMask() {}
	
	public SampleMask(String maskLotName, String factoryName,
			String maskSpecName, String processFlowName,
			String processFlowVersion, String processOperationName,
			String processOperationVersion, String returnProcessFlowName,
			String returnProcessFlowVersion, String returnOperationName,
			String returnOperationVersion, String lastEventName,
			String lastEventTimekey, Timestamp lastEventTime,
			String lastEventUser, String lastEventComment)
	{
		super();
		this.maskLotName = maskLotName;
		this.factoryName = factoryName;
		this.maskSpecName = maskSpecName;
		this.processFlowName = processFlowName;
		this.processFlowVersion = processFlowVersion;
		this.processOperationName = processOperationName;
		this.processOperationVersion = processOperationVersion;
		this.returnProcessFlowName = returnProcessFlowName;
		this.returnProcessFlowVersion = returnProcessFlowVersion;
		this.returnOperationName = returnOperationName;
		this.returnOperationVersion = returnOperationVersion;
		this.lastEventName = lastEventName;
		this.lastEventTimekey = lastEventTimekey;
		this.lastEventTime = lastEventTime;
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

	public String getLastEventName()
	{
		return lastEventName;
	}

	public void setLastEventName(String lastEventName)
	{
		this.lastEventName = lastEventName;
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
