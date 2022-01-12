package kr.co.aim.messolution.extended.object.management.data;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class CustomAlarm extends UdfAccessor {

	@CTORMTemplate(seq = "1", name = "alarmCode", type = "Key", dataType = "String", initial = "", history = "")
	private String alarmCode;

	@CTORMTemplate(seq = "2", name = "alarmTimeKey", type = "Key", dataType = "String", initial = "", history = "")
	private String alarmTimeKey;

	@CTORMTemplate(seq = "3", name = "alarmState", type = "Column", dataType = "String", initial = "", history = "")
	private String alarmState;

	@CTORMTemplate(seq = "4", name = "alarmType", type = "Column", dataType = "String", initial = "", history = "")
	private String alarmType;

	@CTORMTemplate(seq = "5", name = "alarmSeverity", type = "Column", dataType = "String", initial = "", history = "")
	private String alarmSeverity;

	@CTORMTemplate(seq = "6", name = "description", type = "Column", dataType = "String", initial = "", history = "")
	private String description;

	@CTORMTemplate(seq = "7", name = "lotName", type = "Column", dataType = "String", initial = "", history = "")
	private String lotName;

	@CTORMTemplate(seq = "8", name = "factoryName", type = "Column", dataType = "String", initial = "", history = "")
	private String factoryName;

	@CTORMTemplate(seq = "9", name = "productSpecName", type = "Column", dataType = "String", initial = "", history = "")
	private String productSpecName;

	@CTORMTemplate(seq = "10", name = "productSpecVersion", type = "Column", dataType = "String", initial = "", history = "")
	private String productSpecVersion;

	@CTORMTemplate(seq = "11", name = "processFlowName", type = "Column", dataType = "String", initial = "", history = "")
	private String processFlowName;

	@CTORMTemplate(seq = "12", name = "processFlowVersion", type = "Column", dataType = "String", initial = "", history = "")
	private String processFlowVersion;

	@CTORMTemplate(seq = "13", name = "processOperationName", type = "Column", dataType = "String", initial = "", history = "")
	private String processOperationName;

	@CTORMTemplate(seq = "14", name = "processOperationVersion", type = "Column", dataType = "String", initial = "", history = "")
	private String processOperationVersion;

	@CTORMTemplate(seq = "15", name = "createTimeKey", type = "Column", dataType = "String", initial = "", history = "")
	private String createTimeKey;

	@CTORMTemplate(seq = "16", name = "createUser", type = "Column", dataType = "String", initial = "", history = "")
	private String createUser;

	@CTORMTemplate(seq = "17", name = "resolveTimeKey", type = "Column", dataType = "String", initial = "", history = "")
	private String resolveTimeKey;

	@CTORMTemplate(seq = "18", name = "resolveUser", type = "Column", dataType = "String", initial = "", history = "")
	private String resolveUser;

	@CTORMTemplate(seq = "19", name = "lastEventName", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventName;

	@CTORMTemplate(seq = "20", name = "lastEventTimeKey", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventTimeKey;

	@CTORMTemplate(seq = "21", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventUser;

	@CTORMTemplate(seq = "22", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventComment;

	@CTORMTemplate(seq = "23", name = "machineName", type = "Column", dataType = "String", initial = "", history = "")
	private String machineName;

	@CTORMTemplate(seq = "24", name = "unitName", type = "Column", dataType = "String", initial = "", history = "")
	private String unitName;

	@CTORMTemplate(seq = "25", name = "subUnitName", type = "Column", dataType = "String", initial = "", history = "")
	private String subUnitName;

	@CTORMTemplate(seq = "26", name = "enableFlag", type = "Column", dataType = "String", initial = "", history = "")
	private String enableFlag;

	@CTORMTemplate(seq = "27", name = "alarmIndex", type = "Column", dataType = "String", initial = "", history = "")
	private String alarmIndex;

	@CTORMTemplate(seq = "28", name = "releaseHoldFlag", type = "Column", dataType = "String", initial = "", history = "")
	private String releaseHoldFlag;

	@CTORMTemplate(seq = "29", name = "RULE", type = "Column", dataType = "String", initial = "", history = "")
	private String RULE;

	@CTORMTemplate(seq = "30", name = "modualId", type = "Column", dataType = "String", initial = "", history = "")
	private String modualId;

	@CTORMTemplate(seq = "31", name = "itemName", type = "Column", dataType = "String", initial = "", history = "")
	private String itemName;

	@CTORMTemplate(seq = "32", name = "UCL", type = "Column", dataType = "String", initial = "", history = "")
	private String UCL;

	@CTORMTemplate(seq = "33", name = "LCL", type = "Column", dataType = "String", initial = "", history = "")
	private String LCL;

	@CTORMTemplate(seq = "34", name = "value", type = "Column", dataType = "String", initial = "", history = "")
	private String value;

	@CTORMTemplate(seq = "35", name = "productionType", type = "Column", dataType = "String", initial = "", history = "")
	private String productionType;

	@CTORMTemplate(seq = "36", name = "productType", type = "Column", dataType = "String", initial = "", history = "")
	private String productType;

	public String getAlarmTimeKey()
	{
		return alarmTimeKey;
	}

	public void setAlarmTimeKey(String alarmTimeKey)
	{
		this.alarmTimeKey = alarmTimeKey;
	}

	// instantiation
	public CustomAlarm()
	{

	}

	public CustomAlarm(String alarmCode, String alarmTimeKey)
	{
		setAlarmCode(alarmCode);
		setAlarmTimeKey(alarmTimeKey);
	}

	public String getAlarmCode()
	{
		return alarmCode;
	}

	public void setAlarmCode(String alarmCode)
	{
		this.alarmCode = alarmCode;
	}

	public String getFactoryName()
	{
		return factoryName;
	}

	public void setFactoryName(String factoryName)
	{
		this.factoryName = factoryName;
	}

	public String getAlarmState()
	{
		return alarmState;
	}

	public void setAlarmState(String alarmState)
	{
		this.alarmState = alarmState;
	}

	public String getAlarmType()
	{
		return alarmType;
	}

	public void setAlarmType(String alarmType)
	{
		this.alarmType = alarmType;
	}

	public String getAlarmSeverity()
	{
		return alarmSeverity;
	}

	public void setAlarmSeverity(String alarmSeverity)
	{
		this.alarmSeverity = alarmSeverity;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getCreateTimeKey()
	{
		return createTimeKey;
	}

	public void setCreateTimeKey(String createTimeKey)
	{
		this.createTimeKey = createTimeKey;
	}

	public String getCreateUser()
	{
		return createUser;
	}

	public void setCreateUser(String createUser)
	{
		this.createUser = createUser;
	}

	public String getResolveTimeKey()
	{
		return resolveTimeKey;
	}

	public void setResolveTimeKey(String resolveTimeKey)
	{
		this.resolveTimeKey = resolveTimeKey;
	}

	public String getResolveUser()
	{
		return resolveUser;
	}

	public void setResolveUser(String resolveUser)
	{
		this.resolveUser = resolveUser;
	}

	public String getLastEventName()
	{
		return lastEventName;
	}

	public void setLastEventName(String lastEventName)
	{
		this.lastEventName = lastEventName;
	}

	public String getLastEventTimeKey()
	{
		return lastEventTimeKey;
	}

	public void setLastEventTimeKey(String lastEventTimeKey)
	{
		this.lastEventTimeKey = lastEventTimeKey;
	}

	public String getLastEventUser()
	{
		return lastEventUser;
	}

	public void setLastEventUser(String lastEventUser)
	{
		this.lastEventUser = lastEventUser;
	}

	public String getLastEventComment()
	{
		return lastEventComment;
	}

	public void setLastEventComment(String lastEventComment)
	{
		this.lastEventComment = lastEventComment;
	}

	public String getMachineName()
	{
		return machineName;
	}

	public void setMachineName(String machineName)
	{
		this.machineName = machineName;
	}

	public String getUnitName()
	{
		return unitName;
	}

	public void setUnitName(String unitName)
	{
		this.unitName = unitName;
	}

	public String getSubUnitName()
	{
		return subUnitName;
	}

	public void setSubUnitName(String subUnitName)
	{
		this.subUnitName = subUnitName;
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

	public String getLotName()
	{
		return lotName;
	}

	public void setLotName(String lotName)
	{
		this.lotName = lotName;
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

	public String getAlarmIndex()
	{
		return alarmIndex;
	}

	public void setAlarmIndex(String alarmIndex)
	{
		this.alarmIndex = alarmIndex;
	}

	public String getRULE()
	{
		return RULE;
	}

	public void setRULE(String RULE)
	{
		this.RULE = RULE;
	}

	public String getReleaseHoldFlag()
	{
		return releaseHoldFlag;
	}

	public void setReleaseHoldFlag(String releaseHoldFlag)
	{
		this.releaseHoldFlag = releaseHoldFlag;
	}

	public String getModualId()
	{
		return modualId;
	}

	public void setModualId(String modualId)
	{
		this.modualId = modualId;
	}

	public String getEnableFlag()
	{
		return enableFlag;
	}

	public void setEnableFlag(String enableFlag)
	{
		this.enableFlag = enableFlag;
	}

	public String getItemName()
	{
		return itemName;
	}

	public void setItemName(String itemName)
	{
		this.itemName = itemName;
	}

	public String getUCL()
	{
		return UCL;
	}

	public void setUCL(String uCL)
	{
		UCL = uCL;
	}

	public String getLCL()
	{
		return LCL;
	}

	public void setLCL(String lCL)
	{
		LCL = lCL;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	public String getProductionType()
	{
		return productionType;
	}

	public void setProductionType(String productionType)
	{
		this.productionType = productionType;
	}

	public String getProductType()
	{
		return productType;
	}

	public void setProductType(String productType)
	{
		this.productType = productType;
	}

}
