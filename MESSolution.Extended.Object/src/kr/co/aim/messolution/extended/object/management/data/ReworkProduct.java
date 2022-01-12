package kr.co.aim.messolution.extended.object.management.data;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class ReworkProduct extends UdfAccessor {
	@CTORMTemplate(seq = "1", name = "productName", type = "Key", dataType = "String", initial = "", history = "")
	private String productName;
	@CTORMTemplate(seq = "2", name = "reworkType", type = "Key", dataType = "String", initial = "", history = "")
	private String reworkType;
	@CTORMTemplate(seq = "3", name = "factoryName", type = "Column", dataType = "String", initial = "", history = "")
	private String factoryName;
	@CTORMTemplate(seq = "4", name = "processFlowName", type = "Column", dataType = "String", initial = "", history = "")
	private String processFlowName;
	@CTORMTemplate(seq = "5", name = "processFlowVersion", type = "Column", dataType = "String", initial = "", history = "N")
	private String processFlowVersion;
	@CTORMTemplate(seq = "6", name = "processOperationName", type = "Column", dataType = "String", initial = "", history = "N")
	private String processOperationName;
	@CTORMTemplate(seq = "7", name = "processOperationVersion", type = "Column", dataType = "String", initial = "", history = "N")
	private String processOperationVersion;
	@CTORMTemplate(seq = "8", name = "reworkCount", type = "Column", dataType = "Number", initial = "", history = "N")
	private long reworkCount;
	@CTORMTemplate(seq = "9", name = "reworkCountLimit", type = "Column", dataType = "String", initial = "", history = "N")
	private String reworkCountLimit;
	@CTORMTemplate(seq = "10", name = "actualReworkCount", type = "Column", dataType = "Number", initial = "", history = "N")
	private long actualReworkCount;

	public String getProductName()
	{
		return productName;
	}

	public void setProductName(String productName)
	{
		this.productName = productName;
	}

	public String getReworkType()
	{
		return reworkType;
	}

	public void setReworkType(String reworkType)
	{
		this.reworkType = reworkType;
	}

	public String getFactoryName()
	{
		return factoryName;
	}

	public void setFactoryName(String factoryName)
	{
		this.factoryName = factoryName;
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

	public long getReworkCount()
	{
		return reworkCount;
	}

	public void setReworkCount(long reworkCount)
	{
		this.reworkCount = reworkCount;
	}

	public String getReworkCountLimit()
	{
		return reworkCountLimit;
	}

	public void setReworkCountLimit(String reworkCountLimit)
	{
		this.reworkCountLimit = reworkCountLimit;
	}

	public long getActualReworkCount() {
		return actualReworkCount;
	}

	public void setActualReworkCount(long actualReworkCount) {
		this.actualReworkCount = actualReworkCount;
	}

}
