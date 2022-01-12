package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class SampleProduct extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="productName", type="Key", dataType="String", initial="", history="")
	private String productName;
	@CTORMTemplate(seq = "2", name="lotName", type="Key", dataType="String", initial="", history="")
	private String lotName;
	@CTORMTemplate(seq = "3", name="factoryName", type="Key", dataType="String", initial="", history="")
	private String factoryName;
	@CTORMTemplate(seq = "4", name="productSpecName", type="Key", dataType="String", initial="", history="")
	private String productSpecName;
	@CTORMTemplate(seq = "5", name="productSpecVersion", type="Key", dataType="String", initial="", history="")
	private String productSpecVersion;
	@CTORMTemplate(seq = "6", name="processFlowName", type="Key", dataType="String", initial="", history="")
	private String processFlowName;
	@CTORMTemplate(seq = "7", name="processFlowVersion", type="Key", dataType="String", initial="", history="")
	private String processFlowVersion;
	@CTORMTemplate(seq = "8", name="processOperationName", type="Key", dataType="String", initial="", history="")
	private String processOperationName;
	@CTORMTemplate(seq = "9", name="processOperationVersion", type="Key", dataType="String", initial="", history="")
	private String processOperationVersion;
	@CTORMTemplate(seq = "10", name="machineName", type="Key", dataType="String", initial="", history="")
	private String machineName;
	@CTORMTemplate(seq = "11", name="toProcessFlowName", type="Key", dataType="String", initial="", history="")
	private String toProcessFlowName;
	@CTORMTemplate(seq = "12", name="toProcessFlowVersion", type="Key", dataType="String", initial="", history="")
	private String toProcessFlowVersion;
	@CTORMTemplate(seq = "13", name="toProcessOperationName", type="Key", dataType="String", initial="", history="")
	private String toProcessOperationName;
	@CTORMTemplate(seq = "14", name="toProcessOperationVersion", type="Key", dataType="String", initial="", history="")
	private String toProcessOperationVersion;
	@CTORMTemplate(seq = "15", name="productSampleFlag", type="Column", dataType="String", initial="", history="")
	private String productSampleFlag;
	@CTORMTemplate(seq = "16", name="productSampleCount", type="Column", dataType="String", initial="", history="")
	private String productSampleCount;
	@CTORMTemplate(seq = "17", name="productSamplePosition", type="Column", dataType="String", initial="", history="")
	private String productSamplePosition;
	@CTORMTemplate(seq = "18", name="actualSamplePosition", type="Column", dataType="String", initial="", history="")
	private String actualSamplePosition;
	@CTORMTemplate(seq = "19", name="actualSampleSlotPosition", type="Column", dataType="String", initial="", history="")
	private String actualSampleSlotPosition;
	@CTORMTemplate(seq = "20", name="manualSampleFlag", type="Column", dataType="String", initial="", history="")
	private String manualSampleFlag;
	@CTORMTemplate(seq = "21", name="forceSamplingFlag", type="Column", dataType="String", initial="", history="")
	private String forceSamplingFlag;
	@CTORMTemplate(seq = "22", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;
	@CTORMTemplate(seq = "23", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;
	@CTORMTemplate(seq = "24", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;
	@CTORMTemplate(seq = "25", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;
	@CTORMTemplate(seq = "26", name="department", type="Column", dataType="String", initial="", history="")
	private String department;
	
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
	public String getActualSampleSlotPosition()
	{
		return actualSampleSlotPosition;
	}
	public void setActualSampleSlotPosition(String actualSampleSlotPosition)
	{
		this.actualSampleSlotPosition = actualSampleSlotPosition;
	}
	public String getManualSampleFlag()
	{
		return manualSampleFlag;
	}
	public void setManualSampleFlag(String manualSampleFlag)
	{
		this.manualSampleFlag = manualSampleFlag;
	}
	public String getForceSamplingFlag()
	{
		return forceSamplingFlag;
	}
	public void setForceSamplingFlag(String forceSamplingFlag)
	{
		this.forceSamplingFlag = forceSamplingFlag;
	}
	public String getLastEventUser()
	{
		return lastEventUser;
	}
	public void setLastEventUser(String lastEventUser)
	{
		this.lastEventUser = lastEventUser;
	}
	public String getLastEventName()
	{
		return lastEventName;
	}
	public void setLastEventName(String lastEventName)
	{
		this.lastEventName = lastEventName;
	}
	public String getLastEventComment()
	{
		return lastEventComment;
	}
	public void setLastEventComment(String lastEventComment)
	{
		this.lastEventComment = lastEventComment;
	}
	public Timestamp getLastEventTime()
	{
		return lastEventTime;
	}
	public void setLastEventTime(Timestamp lastEventTime)
	{
		this.lastEventTime = lastEventTime;
	}
	public String getDepartment() {
		return department;
	}
	public void setDepartment(String department) {
		this.department = department;
	}
}
