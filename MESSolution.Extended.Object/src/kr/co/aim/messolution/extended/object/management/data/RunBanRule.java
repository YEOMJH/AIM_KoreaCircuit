package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class RunBanRule extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="factoryName", type="Key", dataType="String", initial="", history="N")
	private String factoryName;
	
	@CTORMTemplate(seq = "2", name="processFlowType", type="Key", dataType="String", initial="", history="N")
	private String processFlowType;
	
	@CTORMTemplate(seq = "3", name="processOperationName", type="Key", dataType="String", initial="", history="N")
	private String processOperationName;
	
	@CTORMTemplate(seq = "4", name="processOperationVersion", type="Key", dataType="String", initial="", history="N")
	private String processOperationVersion;
	
	@CTORMTemplate(seq = "5", name="machineName", type="Key", dataType="String", initial="", history="N")
	private String machineName;
	
	@CTORMTemplate(seq = "6", name="unitName", type="Key", dataType="String", initial="", history="N")
	private String unitName;
	
	@CTORMTemplate(seq = "7", name="subUnitName", type="Key", dataType="String", initial="", history="N")
	private String subUnitName;
	
	@CTORMTemplate(seq = "8", name="addFlag", type="Column", dataType="String", initial="", history="N")
	private String addFlag;

	@CTORMTemplate(seq = "9", name="deleteFlag", type="Column", dataType="String", initial="", history="N")
	private String deleteFlag;

	@CTORMTemplate(seq = "10", name = "lastEventTime", type = "Column", dataType = "String", initial = "", history = "N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "11", name = "lastEventTimeKey", type = "Column", dataType = "Timestamp", initial = "", history = "N")
	private String lastEventTimeKey;
	
	@CTORMTemplate(seq = "12", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "13", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventComment;

	public String getFactoryName()
	{
		return factoryName;
	}

	public void setFactoryName(String factoryName)
	{
		this.factoryName = factoryName;
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

	public String getAddFlag()
	{
		return addFlag;
	}

	public void setAddFlag(String addFlag)
	{
		this.addFlag = addFlag;
	}

	public String getDeleteFlag()
	{
		return deleteFlag;
	}

	public void setDeleteFlag(String deleteFlag)
	{
		this.deleteFlag = deleteFlag;
	}

	public String getLastEventUser()
	{
		return lastEventUser;
	}

	public void setLastEventUser(String lastEventUser)
	{
		this.lastEventUser = lastEventUser;
	}

	public String getLastEventTimeKey()
	{
		return lastEventTimeKey;
	}

	public void setLastEventTimeKey(String lastEventTimeKey)
	{
		this.lastEventTimeKey = lastEventTimeKey;
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

	public String getProcessFlowType() {
		return processFlowType;
	}

	public void setProcessFlowType(String processFlowType) {
		this.processFlowType = processFlowType;
	}
	
}
