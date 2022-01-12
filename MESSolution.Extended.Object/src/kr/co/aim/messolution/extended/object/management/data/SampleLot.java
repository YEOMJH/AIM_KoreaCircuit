package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class SampleLot extends UdfAccessor {

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
	@CTORMTemplate(seq = "10", name = "toProcessFlowName", type = "Key", dataType = "String", initial = "", history = "")
	private String toProcessFlowName;
	@CTORMTemplate(seq = "11", name = "toProcessFlowVersion", type = "Key", dataType = "String", initial = "", history = "")
	private String toProcessFlowVersion;
	@CTORMTemplate(seq = "12", name = "toProcessOperationName", type = "Key", dataType = "String", initial = "", history = "")
	private String toProcessOperationName;
	@CTORMTemplate(seq = "13", name = "toProcessOperationVersion", type = "Key", dataType = "String", initial = "", history = "")
	private String toProcessOperationVersion;
	@CTORMTemplate(seq = "14", name = "lotSampleFlag", type = "Column", dataType = "String", initial = "", history = "")
	private String lotSampleFlag;
	@CTORMTemplate(seq = "15", name = "lotSampleCount", type = "Column", dataType = "String", initial = "", history = "")
	private String lotSampleCount;
	@CTORMTemplate(seq = "16", name = "currentLotCount", type = "Column", dataType = "String", initial = "", history = "")
	private String currentLotCount;
	@CTORMTemplate(seq = "17", name = "totalLotCount", type = "Column", dataType = "String", initial = "", history = "")
	private String totalLotCount;
	@CTORMTemplate(seq = "18", name = "productSampleCount", type = "Column", dataType = "String", initial = "", history = "")
	private String productSampleCount;
	@CTORMTemplate(seq = "19", name = "productSamplePosition", type = "Column", dataType = "String", initial = "", history = "")
	private String productSamplePosition;
	@CTORMTemplate(seq = "20", name = "actualProductCount", type = "Column", dataType = "String", initial = "", history = "")
	private String actualProductCount;
	@CTORMTemplate(seq = "21", name = "actualSamplePosition", type = "Column", dataType = "String", initial = "", history = "")
	private String actualSamplePosition;
	@CTORMTemplate(seq = "22", name = "lotGrade", type = "Column", dataType = "String", initial = "", history = "")
	private String lotGrade;
	@CTORMTemplate(seq = "23", name = "priority", type = "Column", dataType = "Number", initial = "", history = "")
	private Number priority;
	@CTORMTemplate(seq = "24", name = "returnProcessFlowName", type = "Column", dataType = "String", initial = "", history = "")
	private String returnProcessFlowName;
	@CTORMTemplate(seq = "25", name = "returnProcessFlowVersion", type = "Column", dataType = "String", initial = "", history = "")
	private String returnProcessFlowVersion;
	@CTORMTemplate(seq = "26", name = "returnOperationName", type = "Column", dataType = "String", initial = "", history = "")
	private String returnOperationName;
	@CTORMTemplate(seq = "27", name = "returnOperationVersion", type = "Column", dataType = "String", initial = "", history = "")
	private String returnOperationVersion;
	@CTORMTemplate(seq = "28", name = "manualSampleFlag", type = "Column", dataType = "String", initial = "", history = "")
	private String manualSampleFlag;
	@CTORMTemplate(seq = "29", name = "forceSamplingFlag", type = "Column", dataType = "String", initial = "", history = "")
	private String forceSamplingFlag;
	@CTORMTemplate(seq = "30", name = "completeFlowName", type = "Column", dataType = "String", initial = "", history = "")
	private String completeFlowName;
	@CTORMTemplate(seq = "31", name = "completeFlowVersion", type = "Column", dataType = "String", initial = "", history = "")
	private String completeFlowVersion;
	@CTORMTemplate(seq = "32", name = "completeOperationName", type = "Column", dataType = "String", initial = "", history = "")
	private String completeOperationName;
	@CTORMTemplate(seq = "33", name = "completeOperationVersion", type = "Column", dataType = "String", initial = "", history = "")
	private String completeOperationVersion;
	@CTORMTemplate(seq = "34", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventUser;
	@CTORMTemplate(seq = "35", name = "lastEventName", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventName;
	@CTORMTemplate(seq = "36", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventComment;
	@CTORMTemplate(seq = "37", name = "lastEventTime", type = "Column", dataType = "Timestamp", initial = "", history = "N")
	private Timestamp lastEventTime;
	@CTORMTemplate(seq = "38", name = "backupSamplingFlag", type = "Column", dataType = "String", initial = "", history = "")
	private String backupSamplingFlag;

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

	public String getProductSamplePosition()
	{
		return productSamplePosition;
	}

	public void setProductSamplePosition(String productSamplePosition)
	{
		this.productSamplePosition = productSamplePosition;
	}

	public String getActualProductCount()
	{
		return actualProductCount;
	}

	public void setActualProductCount(String actualProductCount)
	{
		this.actualProductCount = actualProductCount;
	}

	public String getActualSamplePosition()
	{
		return actualSamplePosition;
	}

	public void setActualSamplePosition(String actualSamplePosition)
	{
		this.actualSamplePosition = actualSamplePosition;
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

	public String getCompleteFlowName()
	{
		return completeFlowName;
	}

	public void setCompleteFlowName(String completeFlowName)
	{
		this.completeFlowName = completeFlowName;
	}

	public String getCompleteFlowVersion()
	{
		return completeFlowVersion;
	}

	public void setCompleteFlowVersion(String completeFlowVersion)
	{
		this.completeFlowVersion = completeFlowVersion;
	}

	public String getCompleteOperationName()
	{
		return completeOperationName;
	}

	public void setCompleteOperationName(String completeOperationName)
	{
		this.completeOperationName = completeOperationName;
	}

	public String getCompleteOperationVersion()
	{
		return completeOperationVersion;
	}

	public void setCompleteOperationVersion(String completeOperationVersion)
	{
		this.completeOperationVersion = completeOperationVersion;
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
	
	public String getBackupSamplingFlag()
	{
		return backupSamplingFlag;
	}

	public void setBackupSamplingFlag(String backupSamplingFlag)
	{
		this.backupSamplingFlag = backupSamplingFlag;
	}
}
