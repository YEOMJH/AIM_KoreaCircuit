package kr.co.aim.messolution.extended.object.management.data;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class InlineSampleLot extends UdfAccessor {

	@CTORMTemplate(seq = "1", name = "lotName", type = "Key", dataType = "String", initial = "", history = "")
	private String lotName;

	@CTORMTemplate(seq = "2", name = "factoryName", type = "Key", dataType = "String", initial = "", history = "")
	private String factoryName;

	@CTORMTemplate(seq = "3", name = "productSpecName", type = "Key", dataType = "String", initial = "", history = "")
	private String productSpecName;

	@CTORMTemplate(seq = "4", name = "productSpecVersion", type = "Key", dataType = "String", initial = "", history = "")
	private String productSpecVersion;

	@CTORMTemplate(seq = "5", name = "processFlowName", type = "Key", dataType = "String", initial = "", history = "")
	private String processFlowName;

	@CTORMTemplate(seq = "6", name = "processFlowVersion", type = "Key", dataType = "String", initial = "", history = "")
	private String processFlowVersion;

	@CTORMTemplate(seq = "7", name = "processOperationName", type = "Key", dataType = "String", initial = "", history = "")
	private String processOperationName;

	@CTORMTemplate(seq = "8", name = "processOperationVersion", type = "Key", dataType = "String", initial = "", history = "")
	private String processOperationVersion;

	@CTORMTemplate(seq = "9", name = "machineName", type = "Key", dataType = "String", initial = "", history = "")
	private String machineName;

	@CTORMTemplate(seq = "10", name = "lotSampleFlag", type = "Column", dataType = "String", initial = "", history = "")
	private String lotSampleFlag;

	@CTORMTemplate(seq = "11", name = "lotSampleCount", type = "Column", dataType = "String", initial = "", history = "")
	private String lotSampleCount;

	@CTORMTemplate(seq = "12", name = "currentLotCount", type = "Column", dataType = "String", initial = "", history = "")
	private String currentLotCount;

	@CTORMTemplate(seq = "13", name = "totalLotCount", type = "Column", dataType = "String", initial = "", history = "")
	private String totalLotCount;

	@CTORMTemplate(seq = "14", name = "productSampleCount", type = "Column", dataType = "String", initial = "", history = "")
	private String productSampleCount;

	@CTORMTemplate(seq = "15", name = "productSampleposition", type = "Column", dataType = "String", initial = "", history = "")
	private String productSampleposition;

	@CTORMTemplate(seq = "16", name = "actualProductCount", type = "Column", dataType = "String", initial = "", history = "")
	private String actualProductCount;

	@CTORMTemplate(seq = "17", name = "actualSampleposition", type = "Column", dataType = "String", initial = "", history = "")
	private String actualSampleposition;

	@CTORMTemplate(seq = "18", name = "manualSampleFlag", type = "Column", dataType = "String", initial = "", history = "")
	private String manualSampleFlag;

	@CTORMTemplate(seq = "19", name = "eventUser", type = "Column", dataType = "String", initial = "", history = "N")
	private String eventUser;

	@CTORMTemplate(seq = "20", name = "eventComment", type = "Column", dataType = "String", initial = "", history = "N")
	private String eventComment;

	@CTORMTemplate(seq = "21", name = "lotGrade", type = "Column", dataType = "String", initial = "", history = "")
	private String lotGrade;

	@CTORMTemplate(seq = "22", name = "priority", type = "Column", dataType = "Number", initial = "", history = "")
	private Number priority;


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

	public String getLotSampleFlag()
	{
		return lotSampleFlag;
	}

	public void setLotSampleFlag(String lotSampleFlag)
	{
		this.lotSampleFlag = lotSampleFlag;
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

	public String getProductSampleCount()
	{
		return productSampleCount;
	}

	public void setProductSampleCount(String productSampleCount)
	{
		this.productSampleCount = productSampleCount;
	}

	public String getProductSampleposition()
	{
		return productSampleposition;
	}

	public void setProductSampleposition(String productSampleposition)
	{
		this.productSampleposition = productSampleposition;
	}

	public String getActualProductCount()
	{
		return actualProductCount;
	}

	public void setActualProductCount(String actualProductCount)
	{
		this.actualProductCount = actualProductCount;
	}

	public String getActualSampleposition()
	{
		return actualSampleposition;
	}

	public void setActualSampleposition(String actualSampleposition)
	{
		this.actualSampleposition = actualSampleposition;
	}

	public String getManualSampleFlag()
	{
		return manualSampleFlag;
	}

	public void setManualSampleFlag(String manualSampleFlag)
	{
		this.manualSampleFlag = manualSampleFlag;
	}

	public String getEventUser()
	{
		return eventUser;
	}

	public void setEventUser(String eventUser)
	{
		this.eventUser = eventUser;
	}

	public String getEventComment()
	{
		return eventComment;
	}

	public void setEventComment(String eventComment)
	{
		this.eventComment = eventComment;
	}

	public String getLotGrade()
	{
		return lotGrade;
	}

	public void setLotGrade(String lotGrade)
	{
		this.lotGrade = lotGrade;
	}

	public Number getPriority()
	{
		return priority;
	}

	public void setPriority(Number priority)
	{
		this.priority = priority;
	}

}