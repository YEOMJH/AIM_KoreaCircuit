package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class DummyProductAssign extends UdfAccessor {
	@CTORMTemplate(seq = "1", name = "productName", type = "Key", dataType = "String", initial = "", history = "")
	private String productName;
	@CTORMTemplate(seq = "2", name = "factoryName", type = "Column", dataType = "String", initial = "", history = "")
	private String factoryName;
	@CTORMTemplate(seq = "3", name = "productSpecName", type = "Column", dataType = "String", initial = "", history = "")
	private String productSpecName;
	@CTORMTemplate(seq = "4", name = "productSpecVersion", type = "Column", dataType = "String", initial = "", history = "")
	private String productSpecVersion;
	@CTORMTemplate(seq = "5", name = "processFlowName", type = "Column", dataType = "String", initial = "", history = "")
	private String processFlowName;
	@CTORMTemplate(seq = "6", name = "processFlowVersion", type = "Column", dataType = "String", initial = "", history = "")
	private String processFlowVersion;
	@CTORMTemplate(seq = "7", name = "processOperationName", type = "Column", dataType = "String", initial = "", history = "")
	private String processOperationName;
	@CTORMTemplate(seq = "8", name = "processOperationVersion", type = "Column", dataType = "Timestamp", initial = "", history = "")
	private String processOperationVersion;
	@CTORMTemplate(seq = "9", name = "productRequestName", type = "Column", dataType = "String", initial = "", history = "")
	private String productRequestName;
	@CTORMTemplate(seq = "10", name = "returnProductSpecName", type = "Column", dataType = "Timestamp", initial = "", history = "")
	private String returnProductSpecName;
	@CTORMTemplate(seq = "11", name = "returnProductSpecVersion", type = "Column", dataType = "String", initial = "", history = "")
	private String returnProductSpecVersion;
	@CTORMTemplate(seq = "12", name = "returnProcessFlowName", type = "Column", dataType = "String", initial = "", history = "")
	private String returnProcessFlowName;
	@CTORMTemplate(seq = "13", name = "returnProcessFlowVersion", type = "Column", dataType = "String", initial = "", history = "")
	private String returnProcessFlowVersion;
	@CTORMTemplate(seq = "14", name = "returnProcessOperationName", type = "Column", dataType = "String", initial = "", history = "")
	private String returnProcessOperationName;
	@CTORMTemplate(seq = "15", name = "returnProcessOperationVersion", type = "Column", dataType = "Timestamp", initial = "", history = "")
	private String returnProcessOperationVersion;
	@CTORMTemplate(seq = "16", name = "returnProductRequestName", type = "Column", dataType = "String", initial = "", history = "")
	private String returnProductRequestName;
	@CTORMTemplate(seq = "17", name = "originalLotName", type = "Column", dataType = "Timestamp", initial = "", history = "")
	private String originalLotName;
	@CTORMTemplate(seq = "18", name = "lastEventName", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventName;
	@CTORMTemplate(seq = "19", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventUser;
	@CTORMTemplate(seq = "20", name = "lastEventTime", type = "Column", dataType = "Timestamp", initial = "", history = "N")
	private Timestamp lastEventTime;
	@CTORMTemplate(seq = "21", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventComment;
	@CTORMTemplate(seq = "22", name = "lastEventTimekey", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventTimekey;

	public String getProductName()
	{
		return productName;
	}

	public void setProductName(String productName)
	{
		this.productName = productName;
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

	public String getProductRequestName()
	{
		return productRequestName;
	}

	public void setProductRequestName(String productRequestName)
	{
		this.productRequestName = productRequestName;
	}

	public String getReturnProductSpecName()
	{
		return returnProductSpecName;
	}

	public void setReturnProductSpecName(String returnProductSpecName)
	{
		this.returnProductSpecName = returnProductSpecName;
	}

	public String getReturnProductSpecVersion()
	{
		return returnProductSpecVersion;
	}

	public void setReturnProductSpecVersion(String returnProductSpecVersion)
	{
		this.returnProductSpecVersion = returnProductSpecVersion;
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

	public String getReturnProductRequestName()
	{
		return returnProductRequestName;
	}

	public void setReturnProductRequestName(String returnProductRequestName)
	{
		this.returnProductRequestName = returnProductRequestName;
	}

	public String getOriginalLotName()
	{
		return originalLotName;
	}

	public void setOriginalLotName(String originalLotName)
	{
		this.originalLotName = originalLotName;
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

	public String getLastEventTimekey()
	{
		return lastEventTimekey;
	}

	public void setLastEventTimekey(String lastEventTimekey)
	{
		this.lastEventTimekey = lastEventTimekey;
	}
}
