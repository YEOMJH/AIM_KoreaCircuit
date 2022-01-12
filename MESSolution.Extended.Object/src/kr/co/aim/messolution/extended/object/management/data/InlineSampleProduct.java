package kr.co.aim.messolution.extended.object.management.data;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class InlineSampleProduct extends UdfAccessor {
	@CTORMTemplate(seq = "1", name = "productName", type = "Key", dataType = "String", initial = "", history = "")
	private String productName;

	@CTORMTemplate(seq = "2", name = "lotName", type = "Key", dataType = "String", initial = "", history = "")
	private String lotName;

	@CTORMTemplate(seq = "3", name = "factoryName", type = "Key", dataType = "String", initial = "", history = "")
	private String factoryName;

	@CTORMTemplate(seq = "4", name = "productSpecName", type = "Key", dataType = "String", initial = "", history = "")
	private String productSpecName;

	@CTORMTemplate(seq = "5", name = "productSpecVersion", type = "Key", dataType = "String", initial = "", history = "")
	private String productSpecVersion;

	@CTORMTemplate(seq = "6", name = "processFlowName", type = "Key", dataType = "String", initial = "", history = "")
	private String processFlowName;

	@CTORMTemplate(seq = "7", name = "processFlowVersion", type = "Key", dataType = "String", initial = "", history = "")
	private String processFlowVersion;

	@CTORMTemplate(seq = "8", name = "processOperationName", type = "Key", dataType = "String", initial = "", history = "")
	private String processOperationName;

	@CTORMTemplate(seq = "9", name = "processOperationVersion", type = "Key", dataType = "String", initial = "", history = "")
	private String processOperationVersion;

	@CTORMTemplate(seq = "10", name = "machineName", type = "Key", dataType = "String", initial = "", history = "")
	private String machineName;

	@CTORMTemplate(seq = "11", name = "productSampleFlag", type = "Column", dataType = "String", initial = "", history = "")
	private String productSampleFlag;

	@CTORMTemplate(seq = "12", name = "productSampleCount", type = "Column", dataType = "String", initial = "", history = "")
	private String productSampleCount;

	@CTORMTemplate(seq = "13", name = "productSamplePosition", type = "Column", dataType = "String", initial = "", history = "")
	private String productSamplePosition;

	@CTORMTemplate(seq = "14", name = "actualSamplePosition", type = "Column", dataType = "String", initial = "", history = "")
	private String actualSamplePosition;

	@CTORMTemplate(seq = "15", name = "actualSamplesLotPosition", type = "Column", dataType = "String", initial = "", history = "")
	private String actualSamplesLotPosition;

	@CTORMTemplate(seq = "16", name = "manualSampleflag", type = "Column", dataType = "String", initial = "", history = "")
	private String manualSampleflag;

	@CTORMTemplate(seq = "17", name = "eventUser", type = "Column", dataType = "String", initial = "", history = "N")
	private String eventUser;

	@CTORMTemplate(seq = "18", name = "eventComment", type = "Column", dataType = "String", initial = "", history = "N")
	private String eventComment;

	@CTORMTemplate(seq = "19", name = "inspectionFlag", type = "Column", dataType = "String", initial = "", history = "")
	private String inspectionFlag;

	public String getProductName()
	{
		return productName;
	}

	public void setProductName(String productName)
	{
		this.productName = productName;
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

	public String getProductSampleFlag()
	{
		return productSampleFlag;
	}

	public void setProductSampleFlag(String productSampleFlag)
	{
		this.productSampleFlag = productSampleFlag;
	}

	public String getProductSampleCount()
	{
		return productSampleCount;
	}

	public void setProductSampleCount(String productSampleCount)
	{
		this.productSampleCount = productSampleCount;
	}

	public String getProductSamplePosition()
	{
		return productSamplePosition;
	}

	public void setProductSamplePosition(String productSamplePosition)
	{
		this.productSamplePosition = productSamplePosition;
	}

	public String getActualSamplePosition()
	{
		return actualSamplePosition;
	}

	public void setActualSamplePosition(String actualSamplePosition)
	{
		this.actualSamplePosition = actualSamplePosition;
	}

	public String getActualSamplesLotPosition()
	{
		return actualSamplesLotPosition;
	}

	public void setActualSamplesLotPosition(String actualSamplesLotPosition)
	{
		this.actualSamplesLotPosition = actualSamplesLotPosition;
	}

	public String getManualSampleflag()
	{
		return manualSampleflag;
	}

	public void setManualSampleflag(String manualSampleflag)
	{
		this.manualSampleflag = manualSampleflag;
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

	public String getInspectionFlag()
	{
		return inspectionFlag;
	}

	public void setInspectionFlag(String inspectionFlag)
	{
		this.inspectionFlag = inspectionFlag;
	}
	
}