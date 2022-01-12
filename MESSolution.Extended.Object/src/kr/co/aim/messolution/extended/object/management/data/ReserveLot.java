package kr.co.aim.messolution.extended.object.management.data;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class ReserveLot extends UdfAccessor {

	@CTORMTemplate(seq = "1", name = "lotName", type = "Key", dataType = "String", initial = "", history = "")
	private String lotName;

	@CTORMTemplate(seq = "2", name = "processOperationName", type = "Key", dataType = "String", initial = "", history = "")
	private String processOperationName;

	@CTORMTemplate(seq = "3", name = "processOperationVersion", type = "Key", dataType = "String", initial = "", history = "")
	private String processOperationVersion;

	@CTORMTemplate(seq = "4", name = "position", type = "Column", dataType = "Number", initial = "", history = "")
	private long position;

	@CTORMTemplate(seq = "5", name = "reserveState", type = "Column", dataType = "String", initial = "", history = "")
	private String reserveState;

	@CTORMTemplate(seq = "6", name = "productSpecName", type = "Column", dataType = "String", initial = "", history = "")
	private String productSpecName;

	@CTORMTemplate(seq = "7", name = "productSpecVersion", type = "Column", dataType = "String", initial = "", history = "")
	private String productSpecVersion;

	@CTORMTemplate(seq = "8", name = "machineName", type = "Column", dataType = "String", initial = "", history = "")
	private String machineName;

	@CTORMTemplate(seq = "9", name = "reserveTimeKey", type = "Column", dataType = "String", initial = "", history = "")
	private String reserveTimeKey;

	@CTORMTemplate(seq = "10", name = "inputTimeKey", type = "Column", dataType = "String", initial = "", history = "")
	private String inputTimeKey;

	@CTORMTemplate(seq = "11", name = "completeTimeKey", type = "Column", dataType = "String", initial = "", history = "")
	private String completeTimeKey;

	@CTORMTemplate(seq = "12", name = "productRequestName", type = "Column", dataType = "String", initial = "", history = "")
	private String productRequestName;

	@CTORMTemplate(seq = "13", name = "reserveUser", type = "Column", dataType = "String", initial = "", history = "")
	private String reserveUser;

	@CTORMTemplate(seq = "14", name = "factoryName", type = "Column", dataType = "String", initial = "", history = "")
	private String factoryName;

	@CTORMTemplate(seq = "15", name = "planDate", type = "Column", dataType = "String", initial = "", history = "")
	private String planDate;

	@CTORMTemplate(seq = "16", name = "priorityPass", type = "Column", dataType = "String", initial = "", history = "")
	private String priorityPass;

	public String getLotName()
	{
		return lotName;
	}

	public void setLotName(String lotName)
	{
		this.lotName = lotName;
	}

	public String getProductSpecVersion()
	{
		return productSpecVersion;
	}

	public void setProductSpecVersion(String productSpecVersion)
	{
		this.productSpecVersion = productSpecVersion;
	}

	public String getProcessOperationName()
	{
		return processOperationName;
	}

	public void setProcessOperationName(String processOperationName)
	{
		this.processOperationName = processOperationName;
	}

	public long getPosition()
	{
		return position;
	}

	public void setPosition(long position)
	{
		this.position = position;
	}

	public String getReserveState()
	{
		return reserveState;
	}

	public void setReserveState(String reserveState)
	{
		this.reserveState = reserveState;
	}

	public String getProductSpecName()
	{
		return productSpecName;
	}

	public void setProductSpecName(String productSpecName)
	{
		this.productSpecName = productSpecName;
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

	public String getReserveTimeKey()
	{
		return reserveTimeKey;
	}

	public void setReserveTimeKey(String reserveTimeKey)
	{
		this.reserveTimeKey = reserveTimeKey;
	}

	public String getInputTimeKey()
	{
		return inputTimeKey;
	}

	public void setInputTimeKey(String inputTimeKey)
	{
		this.inputTimeKey = inputTimeKey;
	}

	public String getCompleteTimeKey()
	{
		return completeTimeKey;
	}

	public void setCompleteTimeKey(String completeTimeKey)
	{
		this.completeTimeKey = completeTimeKey;
	}

	public String getProductRequestName()
	{
		return productRequestName;
	}

	public void setProductRequestName(String productRequestName)
	{
		this.productRequestName = productRequestName;
	}

	public String getReserveUser()
	{
		return reserveUser;
	}

	public void setReserveUser(String reserveUser)
	{
		this.reserveUser = reserveUser;
	}

	public String getFactoryName()
	{
		return factoryName;
	}

	public void setFactoryName(String factoryName)
	{
		this.factoryName = factoryName;
	}

	public String getPlanDate()
	{
		return planDate;
	}

	public void setPlanDate(String planDate)
	{
		this.planDate = planDate;
	}

	public String getPriorityPass()
	{
		return priorityPass;
	}

	public void setPriorityPass(String priorityPass)
	{
		this.priorityPass = priorityPass;
	}

	// instantiation
	public ReserveLot()
	{

	}

	public ReserveLot(String machineName, String lotName)
	{
		setMachineName(machineName);
		setLotName(lotName);
	}

	public ReserveLot(String machineName, String lotName, String factoryName)
	{
		setMachineName(machineName);
		setLotName(lotName);
		setFactoryName(factoryName);
	}
}
