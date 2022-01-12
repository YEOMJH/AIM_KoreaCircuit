package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class ProductQueueTime extends UdfAccessor {

	@CTORMTemplate(seq = "1", name = "productName", type = "Key", dataType = "String", initial = "", history = "")
	private String productName;

	@CTORMTemplate(seq = "2", name = "factoryName", type = "Key", dataType = "String", initial = "", history = "")
	private String factoryName;

	@CTORMTemplate(seq = "3", name = "processFlowName", type = "Key", dataType = "String", initial = "", history = "")
	private String processFlowName;

	@CTORMTemplate(seq = "4", name = "processOperationName", type = "Key", dataType = "String", initial = "", history = "")
	private String processOperationName;

	@CTORMTemplate(seq = "5", name = "toFactoryName", type = "Key", dataType = "String", initial = "", history = "")
	private String toFactoryName;

	@CTORMTemplate(seq = "6", name = "toProcessFlowName", type = "Key", dataType = "String", initial = "", history = "")
	private String toProcessFlowName;

	@CTORMTemplate(seq = "7", name = "toProcessOperationName", type = "Key", dataType = "String", initial = "", history = "")
	private String toProcessOperationName;

	@CTORMTemplate(seq = "8", name = "lotName", type = "", dataType = "String", initial = "", history = "")
	private String lotName;

	@CTORMTemplate(seq = "9", name = "warningDurationLimit", type = "Column", dataType = "String", initial = "", history = "")
	private String warningDurationLimit;

	@CTORMTemplate(seq = "10", name = "interlockDurationLimit", type = "Column", dataType = "String", initial = "", history = "")
	private String interlockDurationLimit;

	@CTORMTemplate(seq = "11", name = "fromEventName", type = "Column", dataType = "String", initial = "", history = "")
	private String fromEventName;

	@CTORMTemplate(seq = "12", name = "toEventName", type = "Column", dataType = "String", initial = "", history = "")
	private String toEventName;

	@CTORMTemplate(seq = "13", name = "queueTimeState", type = "Column", dataType = "String", initial = "", history = "")
	private String queueTimeState;

	@CTORMTemplate(seq = "14", name = "enterTime", type = "Column", dataType = "Timestamp", initial = "", history = "")
	private Timestamp enterTime;

	@CTORMTemplate(seq = "15", name = "exitTime", type = "Column", dataType = "Timestamp", initial = "", history = "")
	private Timestamp exitTime;

	@CTORMTemplate(seq = "16", name = "warningTime", type = "Column", dataType = "Timestamp", initial = "", history = "")
	private Timestamp warningTime;

	@CTORMTemplate(seq = "17", name = "interlockTime", type = "Column", dataType = "Timestamp", initial = "", history = "")
	private Timestamp interlockTime;

	@CTORMTemplate(seq = "18", name = "resolveTime", type = "Column", dataType = "Timestamp", initial = "", history = "")
	private Timestamp resolveTime;

	@CTORMTemplate(seq = "19", name = "resolveUser", type = "Column", dataType = "String", initial = "", history = "")
	private String resolveUser;

	@CTORMTemplate(seq = "19", name = "reasonCode", type = "Column", dataType = "String", initial = "", history = "")
	private String reasonCode;
	
	@CTORMTemplate(seq = "20", name = "department", type = "Column", dataType = "String", initial = "", history = "")
	private String department;
	
	public String getReasonCode() {
		return reasonCode;
	}

	public void setReasonCode(String reasonCode) {
		this.reasonCode = reasonCode;
	}


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

	public String getProcessFlowName()
	{
		return processFlowName;
	}

	public void setProcessFlowName(String processFlowName)
	{
		this.processFlowName = processFlowName;
	}

	public String getProcessOperationName()
	{
		return processOperationName;
	}

	public void setProcessOperationName(String processOperationName)
	{
		this.processOperationName = processOperationName;
	}

	public String getToFactoryName()
	{
		return toFactoryName;
	}

	public void setToFactoryName(String toFactoryName)
	{
		this.toFactoryName = toFactoryName;
	}

	public String getToProcessFlowName()
	{
		return toProcessFlowName;
	}

	public void setToProcessFlowName(String toProcessFlowName)
	{
		this.toProcessFlowName = toProcessFlowName;
	}

	public String getToProcessOperationName()
	{
		return toProcessOperationName;
	}

	public void setToProcessOperationName(String toProcessOperationName)
	{
		this.toProcessOperationName = toProcessOperationName;
	}

	public String getLotName()
	{
		return lotName;
	}

	public void setLotName(String lotName)
	{
		this.lotName = lotName;
	}

	public String getWarningDurationLimit()
	{
		return warningDurationLimit;
	}

	public void setWarningDurationLimit(String warningDurationLimit)
	{
		this.warningDurationLimit = warningDurationLimit;
	}

	public String getInterlockDurationLimit()
	{
		return interlockDurationLimit;
	}

	public void setInterlockDurationLimit(String interlockDurationLimit)
	{
		this.interlockDurationLimit = interlockDurationLimit;
	}

	public String getFromEventName()
	{
		return fromEventName;
	}

	public void setFromEventName(String fromEventName)
	{
		this.fromEventName = fromEventName;
	}

	public String getToEventName()
	{
		return toEventName;
	}

	public void setToEventName(String toEventName)
	{
		this.toEventName = toEventName;
	}

	public String getQueueTimeState()
	{
		return queueTimeState;
	}

	public void setQueueTimeState(String queueTimeState)
	{
		this.queueTimeState = queueTimeState;
	}

	public Timestamp getEnterTime()
	{
		return enterTime;
	}

	public void setEnterTime(Timestamp enterTime)
	{
		this.enterTime = enterTime;
	}

	public Timestamp getExitTime()
	{
		return exitTime;
	}

	public void setExitTime(Timestamp exitTime)
	{
		this.exitTime = exitTime;
	}

	public Timestamp getWarningTime()
	{
		return warningTime;
	}

	public void setWarningTime(Timestamp warningTime)
	{
		this.warningTime = warningTime;
	}

	public Timestamp getInterlockTime()
	{
		return interlockTime;
	}

	public void setInterlockTime(Timestamp interlockTime)
	{
		this.interlockTime = interlockTime;
	}

	public Timestamp getResolveTime()
	{
		return resolveTime;
	}

	public void setResolveTime(Timestamp resolveTime)
	{
		this.resolveTime = resolveTime;
	}

	public String getResolveUser()
	{
		return resolveUser;
	}

	public void setResolveUser(String resolveUser)
	{
		this.resolveUser = resolveUser;
	}

	public ProductQueueTime()
	{

	}

	public ProductQueueTime(String productName, String factoryName, String processFlowName, String processOperationName, String toFactoryName, String toProcessFlowName, String toProcessOperationName)
	{
		this.productName = productName;
		this.factoryName = factoryName;
		this.processFlowName = processFlowName;
		this.processOperationName = processOperationName;
		this.toFactoryName = toFactoryName;
		this.toProcessFlowName = toProcessFlowName;
		this.toProcessOperationName = toProcessOperationName;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}
}
