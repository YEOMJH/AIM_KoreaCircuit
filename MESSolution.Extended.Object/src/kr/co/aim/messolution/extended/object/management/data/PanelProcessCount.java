package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class PanelProcessCount extends UdfAccessor 
{
	@CTORMTemplate(seq = "1", name = "lotName", type = "Key", dataType = "String", initial = "", history = "")
	private String lotName;
	@CTORMTemplate(seq = "2", name = "detailProcessOperationType", type = "Key", dataType = "String", initial = "", history = "")
	private String detailProcessOperationType;
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
	@CTORMTemplate(seq = "8", name = "processCount", type = "Column", dataType = "Number", initial = "", history = "")
	private Number processCount;
	@CTORMTemplate(seq = "9", name = "processLimit", type = "Column", dataType = "Number", initial = "", history = "")
	private Number processLimit;
	@CTORMTemplate(seq = "10", name = "lastEventName", type = "Column", dataType = "String", initial = "", history = "eventName")
	private String lastEventName;
	@CTORMTemplate(seq = "11", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "eventUser")
	private String lastEventUser;
	@CTORMTemplate(seq = "12", name = "lastEventTime", type = "Column", dataType = "TimeStamp", initial = "", history = "eventTime")
	private Timestamp lastEventTime;
	@CTORMTemplate(seq = "13", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "eventComment")
	private String lastEventComment;
	@CTORMTemplate(seq = "14", name = "lastEventTimeKey", type = "Column", dataType = "String", initial = "", history = "timeKey")
	private String lastEventTimeKey;
	
	public String getLotName()
	{
		return lotName;
	}
	public void setLotName(String lotName)
	{
		this.lotName = lotName;
	}
	public String getDetailProcessOperationType()
	{
		return detailProcessOperationType;
	}
	public void setDetailProcessOperationType(String detailProcessOperationType)
	{
		this.detailProcessOperationType = detailProcessOperationType;
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
	public Number getProcessCount()
	{
		return processCount;
	}
	public void setProcessCount(Number processCount)
	{
		this.processCount = processCount;
	}
	public Number getProcessLimit()
	{
		return processLimit;
	}
	public void setProcessLimit(Number processLimit)
	{
		this.processLimit = processLimit;
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
	public Timestamp getLastEventTime()
	{
		return lastEventTime;
	}
	public void setLastEventTime(Timestamp lastEventTime)
	{
		this.lastEventTime = lastEventTime;
	}
	public String getLastEventComment()
	{
		return lastEventComment;
	}
	public void setLastEventComment(String lastEventComment)
	{
		this.lastEventComment = lastEventComment;
	}
	public String getLastEventTimeKey()
	{
		return lastEventTimeKey;
	}
	public void setLastEventTimeKey(String lastEventTimeKey)
	{
		this.lastEventTimeKey = lastEventTimeKey;
	}
}
