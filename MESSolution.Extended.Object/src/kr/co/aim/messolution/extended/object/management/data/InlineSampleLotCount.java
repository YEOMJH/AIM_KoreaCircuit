package kr.co.aim.messolution.extended.object.management.data;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class InlineSampleLotCount extends UdfAccessor {

	@CTORMTemplate(seq = "1", name = "factoryName", type = "Key", dataType = "String", initial = "", history = "")
	private String factoryName;

	@CTORMTemplate(seq = "2", name = "productSpecName", type = "Key", dataType = "String", initial = "", history = "")
	private String productSpecName;

	@CTORMTemplate(seq = "3", name = "productSpecVersion", type = "Key", dataType = "String", initial = "", history = "")
	private String productSpecVersion;

	@CTORMTemplate(seq = "4", name = "processFlowName", type = "Key", dataType = "String", initial = "", history = "")
	private String processFlowName;

	@CTORMTemplate(seq = "5", name = "processFlowVersion", type = "Key", dataType = "String", initial = "", history = "")
	private String processFlowVersion;

	@CTORMTemplate(seq = "6", name = "processOperationName", type = "Key", dataType = "String", initial = "", history = "")
	private String processOperationName;

	@CTORMTemplate(seq = "7", name = "processOperationVersion", type = "Key", dataType = "String", initial = "", history = "")
	private String processOperationVersion;

	@CTORMTemplate(seq = "8", name = "machineName", type = "Key", dataType = "String", initial = "", history = "")
	private String machineName;

	@CTORMTemplate(seq = "9", name = "lotSampleCount", type = "Column", dataType = "String", initial = "", history = "")
	private String lotSampleCount;

	@CTORMTemplate(seq = "10", name = "currentLotCount", type = "Column", dataType = "String", initial = "", history = "")
	private String currentLotCount;

	@CTORMTemplate(seq = "11", name = "totalLotCount", type = "Column", dataType = "String", initial = "", history = "")
	private String totalLotCount;


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

	public String getLotSampleCount()
	{
		return lotSampleCount;
	}

	public void setLotSampleCount(String lotSampleCount)
	{
		this.lotSampleCount = lotSampleCount;
	}

	public String getCurrentLotCount()
	{
		return currentLotCount;
	}

	public void setCurrentLotCount(String currentLotCount)
	{
		this.currentLotCount = currentLotCount;
	}

	public String getTotalLotCount()
	{
		return totalLotCount;
	}

	public void setTotalLotCount(String totalLotCount)
	{
		this.totalLotCount = totalLotCount;
	}

}