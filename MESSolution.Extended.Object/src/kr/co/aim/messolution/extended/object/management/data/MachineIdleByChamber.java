package kr.co.aim.messolution.extended.object.management.data;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class MachineIdleByChamber extends UdfAccessor {
	@CTORMTemplate(seq = "1", name = "machineName", type = "Key", dataType = "String", initial = "", history = "")
	private String machineName;
	@CTORMTemplate(seq = "2", name = "chamberName", type = "Key", dataType = "String", initial = "", history = "")
	private String chamberName;
	@CTORMTemplate(seq = "3", name = "processOperationName", type = "Key", dataType = "String", initial = "", history = "")
	private String processOperationName;
	@CTORMTemplate(seq = "4", name = "processOperationVersion", type = "Key", dataType = "String", initial = "", history = "")
	private String processOperationVersion;
	@CTORMTemplate(seq = "5", name = "idleGroupName", type = "Column", dataType = "String", initial = "", history = "")
	private String idleGroupName;
	@CTORMTemplate(seq = "6", name = "recipeName", type = "Column", dataType = "String", initial = "", history = "")
	private String recipeName;
	@CTORMTemplate(seq = "7", name = "recipeGroupName", type = "Column", dataType = "String", initial = "", history = "")
	private String recipeGroupName;
	@CTORMTemplate(seq = "8", name = "controlSwitch", type = "Column", dataType = "String", initial = "", history = "")
	private String controlSwitch;
	@CTORMTemplate(seq = "9", name = "lotCount", type = "Column", dataType = "String", initial = "", history = "")
	private String lotCount;
	@CTORMTemplate(seq = "10", name = "lotCountLimit", type = "Column", dataType = "String", initial = "", history = "")
	private String lotCountLimit;
	@CTORMTemplate(seq = "11", name = "lastEventName", type = "Column", dataType = "String", initial = "", history = "")
	private String lastEventName;
	@CTORMTemplate(seq = "12", name = "lastEventTime", type = "Column", dataType = "String", initial = "", history = "")
	private String lastEventTime;
	@CTORMTemplate(seq = "13", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "")
	private String lastEventUser;
	@CTORMTemplate(seq = "14", name = "lastEventTimekey", type = "Column", dataType = "String", initial = "", history = "")
	private String lastEventTimekey;
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
	public String getIdleGroupName()
	{
		return idleGroupName;
	}
	public void setIdleGroupName(String idleGroupName)
	{
		this.idleGroupName = idleGroupName;
	}
	public String getRecipeName()
	{
		return recipeName;
	}
	public void setRecipeName(String recipeName)
	{
		this.recipeName = recipeName;
	}
	public String getRecipeGroupName()
	{
		return recipeGroupName;
	}
	public void setRecipeGroupName(String recipeGroupName)
	{
		this.recipeGroupName = recipeGroupName;
	}
	public String getControlSwitch()
	{
		return controlSwitch;
	}
	public void setControlSwitch(String controlSwitch)
	{
		this.controlSwitch = controlSwitch;
	}
	public String getLotCount()
	{
		return lotCount;
	}
	public void setLotCount(String lotCount)
	{
		this.lotCount = lotCount;
	}
	public String getLotCountLimit()
	{
		return lotCountLimit;
	}
	public void setLotCountLimit(String lotCountLimit)
	{
		this.lotCountLimit = lotCountLimit;
	}
	public String getLastEventName()
	{
		return lastEventName;
	}
	public void setLastEventName(String lastEventName)
	{
		this.lastEventName = lastEventName;
	}
	public String getLastEventTime()
	{
		return lastEventTime;
	}
	public void setLastEventTime(String lastEventTime)
	{
		this.lastEventTime = lastEventTime;
	}
	public String getLastEventUser()
	{
		return lastEventUser;
	}
	public void setLastEventUser(String lastEventUser)
	{
		this.lastEventUser = lastEventUser;
	}
	public String getLastEventTimekey()
	{
		return lastEventTimekey;
	}
	public void setLastEventTimekey(String lastEventTimekey)
	{
		this.lastEventTimekey = lastEventTimekey;
	}
}
