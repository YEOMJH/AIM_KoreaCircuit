package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class ShieldLot extends UdfAccessor {
	@CTORMTemplate(seq = "1", name = "shieldLotName", type = "Key", dataType = "String", initial = "", history = "")
	private String shieldLotName;
	@CTORMTemplate(seq = "2", name = "line", type = "Column", dataType = "Number", initial = "", history = "")
	private long line;
	@CTORMTemplate(seq = "3", name = "chamberType", type = "Column", dataType = "String", initial = "", history = "")
	private String chamberType;
	@CTORMTemplate(seq = "4", name = "chamberNo", type = "Column", dataType = "String", initial = "", history = "")
	private String chamberNo;
	@CTORMTemplate(seq = "5", name = "setValue", type = "Column", dataType = "String", initial = "", history = "")
	private String setValue;
	@CTORMTemplate(seq = "6", name = "judge", type = "Column", dataType = "String", initial = "", history = "")
	private String judge;
	@CTORMTemplate(seq = "7", name = "factoryName", type = "Column", dataType = "String", initial = "", history = "")
	private String factoryName;
	@CTORMTemplate(seq = "8", name = "lotState", type = "Column", dataType = "String", initial = "", history = "")
	private String lotState;
	@CTORMTemplate(seq = "9", name = "lotProcessState", type = "Column", dataType = "String", initial = "", history = "")
	private String lotProcessState;
	@CTORMTemplate(seq = "10", name = "lotHoldState", type = "Column", dataType = "String", initial = "", history = "")
	private String lotHoldState;
	@CTORMTemplate(seq = "11", name = "nodeStack", type = "Column", dataType = "String", initial = "", history = "")
	private String nodeStack;
	@CTORMTemplate(seq = "12", name = "cleanState", type = "Column", dataType = "String", initial = "", history = "")
	private String cleanState;
	@CTORMTemplate(seq = "13", name = "carrierName", type = "Column", dataType = "String", initial = "", history = "")
	private String carrierName;
	@CTORMTemplate(seq = "14", name = "machineName", type = "Column", dataType = "String", initial = "", history = "")
	private String machineName;
	@CTORMTemplate(seq = "15", name = "chamberName", type = "Column", dataType = "String", initial = "", history = "")
	private String chamberName;
	@CTORMTemplate(seq = "16", name = "shieldSpecName", type = "Column", dataType = "String", initial = "", history = "")
	private String shieldSpecName;
	@CTORMTemplate(seq = "17", name = "processFlowName", type = "Column", dataType = "String", initial = "", history = "")
	private String processFlowName;
	@CTORMTemplate(seq = "18", name = "processFlowVersion", type = "Column", dataType = "String", initial = "", history = "")
	private String processFlowVersion;
	@CTORMTemplate(seq = "19", name = "processOperationName", type = "Column", dataType = "String", initial = "", history = "")
	private String processOperationName;
	@CTORMTemplate(seq = "20", name = "processOperationVersion", type = "Column", dataType = "String", initial = "", history = "")
	private String processOperationVersion;
	@CTORMTemplate(seq = "21", name = "reasonCodeType", type = "Column", dataType = "String", initial = "", history = "")
	private String reasonCodeType;
	@CTORMTemplate(seq = "22", name = "reasonCode", type = "Column", dataType = "String", initial = "", history = "")
	private String reasonCode;
	@CTORMTemplate(seq = "23", name = "reworkState", type = "Column", dataType = "String", initial = "", history = "")
	private String reworkState;
	@CTORMTemplate(seq = "24", name = "reworkCount", type = "Column", dataType = "Number", initial = "", history = "")
	private long reworkCount;
	@CTORMTemplate(seq = "25", name = "lastLoggedInTime", type = "Column", dataType = "Timestamp", initial = "", history = "")
	private Timestamp lastLoggedInTime;
	@CTORMTemplate(seq = "26", name = "lastLoggedInUser", type = "Column", dataType = "String", initial = "", history = "")
	private String lastLoggedInUser;
	@CTORMTemplate(seq = "27", name = "lastLoggedOutTime", type = "Column", dataType = "Timestamp", initial = "", history = "")
	private Timestamp lastLoggedOutTime;
	@CTORMTemplate(seq = "28", name = "lastLoggedOutUser", type = "Column", dataType = "String", initial = "", history = "")
	private String lastLoggedOutUser;
	@CTORMTemplate(seq = "29", name = "sampleFlag", type = "Column", dataType = "String", initial = "", history = "")
	private String sampleFlag;
	@CTORMTemplate(seq = "30", name = "createTime", type = "Column", dataType = "Timestamp", initial = "", history = "")
	private Timestamp createTime;
	@CTORMTemplate(seq = "31", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventComment;
	@CTORMTemplate(seq = "32", name = "lastEventName", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventName;
	@CTORMTemplate(seq = "33", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventUser;
	@CTORMTemplate(seq = "34", name = "lastEventTime", type = "Column", dataType = "Timestamp", initial = "", history = "N")
	private Timestamp lastEventTime;
	@CTORMTemplate(seq = "35", name = "lastEventTimekey", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventTimekey;
	@CTORMTemplate(seq = "36", name = "carGroupName", type = "Column", dataType = "String", initial = "", history = "")
	private String carGroupName;
	@CTORMTemplate(seq = "37", name = "basketGroupName", type = "Column", dataType = "String", initial = "", history = "")
	private String basketGroupName;
	
	private String branchEndNodeId;

	public String getShieldLotName()
	{
		return shieldLotName;
	}

	public void setShieldLotName(String shieldLotName)
	{
		this.shieldLotName = shieldLotName;
	}

	public long getLine()
	{
		return line;
	}

	public void setLine(long line)
	{
		this.line = line;
	}

	public String getChamberType()
	{
		return chamberType;
	}

	public void setChamberType(String chamberType)
	{
		this.chamberType = chamberType;
	}

	public String getChamberNo()
	{
		return chamberNo;
	}

	public void setChamberNo(String chamberNo)
	{
		this.chamberNo = chamberNo;
	}

	public String getSetValue()
	{
		return setValue;
	}

	public void setSetValue(String setValue)
	{
		this.setValue = setValue;
	}

	public String getJudge()
	{
		return judge;
	}

	public void setJudge(String judge)
	{
		this.judge = judge;
	}

	public String getFactoryName()
	{
		return factoryName;
	}

	public void setFactoryName(String factoryName)
	{
		this.factoryName = factoryName;
	}

	public String getLotState()
	{
		return lotState;
	}

	public void setLotState(String lotState)
	{
		this.lotState = lotState;
	}

	public String getLotProcessState()
	{
		return lotProcessState;
	}

	public void setLotProcessState(String lotProcessState)
	{
		this.lotProcessState = lotProcessState;
	}

	public String getLotHoldState()
	{
		return lotHoldState;
	}

	public void setLotHoldState(String lotHoldState)
	{
		this.lotHoldState = lotHoldState;
	}

	public String getNodeStack()
	{
		return nodeStack;
	}

	public void setNodeStack(String nodeStack)
	{
		this.nodeStack = nodeStack;
	}

	public String getCleanState()
	{
		return cleanState;
	}

	public void setCleanState(String cleanState)
	{
		this.cleanState = cleanState;
	}

	public String getCarrierName()
	{
		return carrierName;
	}

	public void setCarrierName(String carrierName)
	{
		this.carrierName = carrierName;
	}

	public String getMachineName()
	{
		return machineName;
	}

	public void setMachineName(String machineName)
	{
		this.machineName = machineName;
	}

	public String getChamberName()
	{
		return chamberName;
	}

	public void setChamberName(String chamberName)
	{
		this.chamberName = chamberName;
	}

	public String getShieldSpecName()
	{
		return shieldSpecName;
	}

	public void setShieldSpecName(String shieldSpecName)
	{
		this.shieldSpecName = shieldSpecName;
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

	public String getReasonCodeType()
	{
		return reasonCodeType;
	}

	public void setReasonCodeType(String reasonCodeType)
	{
		this.reasonCodeType = reasonCodeType;
	}

	public String getReasonCode()
	{
		return reasonCode;
	}

	public void setReasonCode(String reasonCode)
	{
		this.reasonCode = reasonCode;
	}

	public String getReworkState()
	{
		return reworkState;
	}

	public void setReworkState(String reworkState)
	{
		this.reworkState = reworkState;
	}

	public long getReworkCount()
	{
		return reworkCount;
	}

	public void setReworkCount(long reworkCount)
	{
		this.reworkCount = reworkCount;
	}

	public Timestamp getLastLoggedInTime()
	{
		return lastLoggedInTime;
	}

	public void setLastLoggedInTime(Timestamp lastLoggedInTime)
	{
		this.lastLoggedInTime = lastLoggedInTime;
	}

	public String getLastLoggedInUser()
	{
		return lastLoggedInUser;
	}

	public void setLastLoggedInUser(String lastLoggedInUser)
	{
		this.lastLoggedInUser = lastLoggedInUser;
	}

	public Timestamp getLastLoggedOutTime()
	{
		return lastLoggedOutTime;
	}

	public void setLastLoggedOutTime(Timestamp lastLoggedOutTime)
	{
		this.lastLoggedOutTime = lastLoggedOutTime;
	}

	public String getLastLoggedOutUser()
	{
		return lastLoggedOutUser;
	}

	public void setLastLoggedOutUser(String lastLoggedOutUser)
	{
		this.lastLoggedOutUser = lastLoggedOutUser;
	}

	public String getSampleFlag()
	{
		return sampleFlag;
	}

	public void setSampleFlag(String sampleFlag)
	{
		this.sampleFlag = sampleFlag;
	}

	public Timestamp getCreateTime()
	{
		return createTime;
	}

	public void setCreateTime(Timestamp createTime)
	{
		this.createTime = createTime;
	}

	public String getLastEventComment()
	{
		return lastEventComment;
	}

	public void setLastEventComment(String lastEventComment)
	{
		this.lastEventComment = lastEventComment;
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

	public String getLastEventTimekey()
	{
		return lastEventTimekey;
	}

	public void setLastEventTimekey(String lastEventTimekey)
	{
		this.lastEventTimekey = lastEventTimekey;
	}

	public String getBranchEndNodeId() {
		return branchEndNodeId;
	}

	public void setBranchEndNodeId(String branchEndNodeId) {
		this.branchEndNodeId = branchEndNodeId;
	}

	public String getCarGroupName() {
		return carGroupName;
	}

	public void setCarGroupName(String carGroupName) {
		this.carGroupName = carGroupName;
	}

	public String getBasketGroupName() {
		return basketGroupName;
	}

	public void setBasketGroupName(String basketGroupName) {
		this.basketGroupName = basketGroupName;
	}
}
