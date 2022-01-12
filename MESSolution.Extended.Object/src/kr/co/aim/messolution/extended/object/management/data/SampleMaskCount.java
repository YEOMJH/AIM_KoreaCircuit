package kr.co.aim.messolution.extended.object.management.data;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class SampleMaskCount extends UdfAccessor {
	@CTORMTemplate(seq = "1", name="processFlowName", type="Key", dataType="String", initial="", history="N")
	private String processFlowName;

	@CTORMTemplate(seq = "2", name="processFlowVersion", type="Key", dataType="String", initial="", history="N")
	private String processFlowVersion;

	@CTORMTemplate(seq = "3", name="processOperationName", type="Key", dataType="String", initial="", history="N")
	private String processOperationName;

	@CTORMTemplate(seq = "4", name="processOperationVersion", type="Key", dataType="String", initial="", history="N")
	private String processOperationVersion;

	@CTORMTemplate(seq = "5", name="machineName", type="Key", dataType="String", initial="", history="N")
	private String machineName;

	@CTORMTemplate(seq = "6", name="toProcessFlowName", type="Column", dataType="String", initial="", history="N")
	private String toProcessFlowName;

	@CTORMTemplate(seq = "7", name="toProcessFlowVersion", type="Column", dataType="String", initial="", history="N")
	private String toProcessFlowVersion;

	@CTORMTemplate(seq = "8", name="toProcessOperationName", type="Column", dataType="String", initial="", history="N")
	private String toProcessOperationName;

	@CTORMTemplate(seq = "9", name="toProcessOperationVersion", type="Column", dataType="String", initial="", history="N")
	private String toProcessOperationVersion;

	@CTORMTemplate(seq = "10", name="returnOperationName", type="Column", dataType="String", initial="", history="N")
	private String returnOperationName;

	@CTORMTemplate(seq = "11", name="returnOperationVersion", type="Column", dataType="String", initial="", history="N")
	private String returnOperationVersion;

	@CTORMTemplate(seq = "12", name="perCarrier", type="Column", dataType="Number", initial="", history="N")
	private int perCarrier;

	@CTORMTemplate(seq = "13", name="currentCarrierCount", type="Column", dataType="Number", initial="", history="N")
	private int currentCarrierCount;

	@CTORMTemplate(seq = "14", name="totalCarrierCount", type="Column", dataType="Number", initial="", history="N")
	private int totalCarrierCount;

	public SampleMaskCount()
	{
		super();
	}

	public SampleMaskCount(String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion, String machineName)
	{
		super();
		this.processFlowName = processFlowName;
		this.processFlowVersion = processFlowVersion;
		this.processOperationName = processOperationName;
		this.processOperationVersion = processOperationVersion;
		this.machineName = machineName;
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

	public int getPerCarrier()
	{
		return perCarrier;
	}

	public void setPerCarrier(int perCarrier)
	{
		this.perCarrier = perCarrier;
	}

	public int getCurrentCarrierCount()
	{
		return currentCarrierCount;
	}

	public void setCurrentCarrierCount(int currentCarrierCount)
	{
		this.currentCarrierCount = currentCarrierCount;
	}

	public int getTotalCarrierCount()
	{
		return totalCarrierCount;
	}

	public void setTotalCarrierCount(int totalCarrierCount)
	{
		this.totalCarrierCount = totalCarrierCount;
	}
}