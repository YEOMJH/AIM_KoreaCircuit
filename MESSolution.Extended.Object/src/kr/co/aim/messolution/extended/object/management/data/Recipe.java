package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

/**
 * @author han90
 *
 */
public class Recipe extends UdfAccessor {

	@CTORMTemplate(seq = "1", name = "machineName", type = "Key", dataType = "String", initial = "", history = "")
	private String machineName;

	@CTORMTemplate(seq = "2", name = "recipeName", type = "Key", dataType = "String", initial = "", history = "")
	private String recipeName;

	@CTORMTemplate(seq = "3", name = "description", type = "Column", dataType = "String", initial = "", history = "")
	private String description;

	@CTORMTemplate(seq = "4", name = "recipeType", type = "Column", dataType = "String", initial = "", history = "")
	private String recipeType;

	@CTORMTemplate(seq = "5", name = "timeUsedLimit", type = "Column", dataType = "Number", initial = "", history = "")
	private long timeUsedLimit;

	@CTORMTemplate(seq = "6", name = "durationUsedLimit", type = "Column", dataType = "Number", initial = "", history = "")
	private double durationUsedLimit;

	@CTORMTemplate(seq = "7", name = "recipeState", type = "Column", dataType = "String", initial = "", history = "")
	private String recipeState;

	@CTORMTemplate(seq = "8", name = "totalTimeUsed", type = "Column", dataType = "Number", initial = "", history = "")
	private long totalTimeUsed;

	@CTORMTemplate(seq = "9", name = "timeUsed", type = "Column", dataType = "Number", initial = "", history = "")
	private long timeUsed;

	@CTORMTemplate(seq = "10", name = "lastApproveTime", type = "Column", dataType = "Timestamp", initial = "", history = "")
	private Timestamp lastApproveTime;

	@CTORMTemplate(seq = "11", name = "result", type = "Column", dataType = "String", initial = "", history = "")
	private String result;

	@CTORMTemplate(seq = "12", name = "lastEventTimeKey", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventTimeKey;

	@CTORMTemplate(seq = "13", name = "lastEventName", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventName;

	@CTORMTemplate(seq = "14", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventUser;

	@CTORMTemplate(seq = "15", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventComment;

	@CTORMTemplate(seq = "16", name = "activeState", type = "Column", dataType = "String", initial = "", history = "")
	private String activeState;

	@CTORMTemplate(seq = "17", name = "lastModifiedTime", type = "Column", dataType = "Timestamp", initial = "", history = "")
	private Timestamp lastModifiedTime;

	@CTORMTemplate(seq = "18", name = "lastActivatedTime", type = "Column", dataType = "Timestamp", initial = "", history = "")
	private Timestamp lastActivatedTime;

	@CTORMTemplate(seq = "19", name = "LastChangeTime", type = "Column", dataType = "Timestamp", initial = "", history = "")
	private Timestamp LastChangeTime;

	@CTORMTemplate(seq = "20", name = "VERSION", type = "Column", dataType = "String", initial = "", history = "")
	private String VERSION;

	@CTORMTemplate(seq = "21", name = "INTFlag", type = "Column", dataType = "String", initial = "", history = "")
	private String INTFlag;

	@CTORMTemplate(seq = "22", name = "MFGFlag", type = "Column", dataType = "String", initial = "", history = "")
	private String MFGFlag;

	@CTORMTemplate(seq = "23", name = "lastTrackOutTimeKey", type = "Column", dataType = "String", initial = "", history = "")
	private String lastTrackOutTimeKey;

	@CTORMTemplate(seq = "24", name = "lastFlagYTimeKey", type = "Column", dataType = "String", initial = "", history = "")
	private String lastFlagYTimeKey;

	@CTORMTemplate(seq = "25", name = "autoChangeFlag", type = "Column", dataType = "String", initial = "", history = "")
	private String autoChangeFlag;

	@CTORMTemplate(seq = "26", name = "totalDurationUsed", type = "Column", dataType = "Number", initial = "", history = "")
	private long totalDurationUsed;

	@CTORMTemplate(seq = "27", name = "durationUsed", type = "Column", dataType = "Number", initial = "", history = "")
	private long durationUsed;

	@CTORMTemplate(seq = "28", name = "ENGFlag", type = "Column", dataType = "String", initial = "", history = "")
	private String ENGFlag;

	@CTORMTemplate(seq = "29", name = "RMSFlag", type = "Column", dataType = "String", initial = "", history = "")
	private String RMSFlag;

	@CTORMTemplate(seq = "30", name = "unitCheckFlag", type = "Column", dataType = "String", initial = "", history = "")
	private String unitCheckFlag;

	@CTORMTemplate(seq = "31", name = "versionCheckFlag", type = "Column", dataType = "String", initial = "", history = "")
	private String versionCheckFlag;
	
	@CTORMTemplate(seq = "32", name = "maxDurationUsedLimit", type = "Column", dataType = "Timestamp", initial = "", history = "")
	private Timestamp maxDurationUsedLimit;
	
	// instantiation
	public Recipe()
	{

	}

	public String getVERSION()
	{
		return VERSION;
	}

	public void setVERSION(String version)
	{
		VERSION = version;
	}

	/**
	 * add by dingjipeng 2015.9.1
	 * */
	public Recipe(String machineName, String recipeName, String recipeType)

	{
		setMachineName(machineName);
		setRecipeName(recipeName);
		setRecipeType(recipeType);

	}

	public Recipe(String machineName, String recipeName)
	{
		setMachineName(machineName);
		setRecipeName(recipeName);
	}

	public String getMachineName()
	{
		return machineName;
	}

	public void setMachineName(String machineName)
	{
		this.machineName = machineName;
	}

	public String getRecipeName()
	{
		return recipeName;
	}

	public void setRecipeName(String recipeName)
	{
		this.recipeName = recipeName;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getRecipeType()
	{
		return recipeType;
	}

	public void setRecipeType(String recipeType)
	{
		this.recipeType = recipeType;
	}

	public long getTimeUsedLimit()
	{
		return timeUsedLimit;
	}

	public void setTimeUsedLimit(long timeUsedLimit)
	{
		this.timeUsedLimit = timeUsedLimit;
	}

	public double getDurationUsedLimit()
	{
		return durationUsedLimit;
	}

	public void setDurationUsedLimit(double durationUsedLimit)
	{
		this.durationUsedLimit = durationUsedLimit;
	}

	public String getRecipeState()
	{
		return recipeState;
	}

	public void setRecipeState(String recipeState)
	{
		this.recipeState = recipeState;
	}

	public long getTotalTimeUsed()
	{
		return totalTimeUsed;
	}

	public void setTotalTimeUsed(long totalTimeUsed)
	{
		this.totalTimeUsed = totalTimeUsed;
	}

	public long getTimeUsed()
	{
		return timeUsed;
	}

	public void setTimeUsed(long timeUsed)
	{
		this.timeUsed = timeUsed;
	}

	public Timestamp getLastApporveTime()
	{
		return lastApproveTime;
	}

	public void setLastApporveTime(Timestamp lastApporveTime)
	{
		this.lastApproveTime = lastApporveTime;
	}

	public String getResult()
	{
		return result;
	}

	public void setResult(String result)
	{
		this.result = result;
	}

	public String getLastEventTimeKey()
	{
		return lastEventTimeKey;
	}

	public void setLastEventTimeKey(String lastEventTimeKey)
	{
		this.lastEventTimeKey = lastEventTimeKey;
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

	public String getLastEventComment()
	{
		return lastEventComment;
	}

	public void setLastEventComment(String lastEventComment)
	{
		this.lastEventComment = lastEventComment;
	}

	public String getActiveState()
	{
		return activeState;
	}

	public void setActiveState(String activeState)
	{
		this.activeState = activeState;
	}

	public Timestamp getLastModifiedTime()
	{
		return lastModifiedTime;
	}

	public void setLastModifiedTime(Timestamp lastModifiedTime)
	{
		this.lastModifiedTime = lastModifiedTime;
	}

	public Timestamp getLastActivatedTime()
	{
		return lastActivatedTime;
	}

	public void setLastActivatedTime(Timestamp lastActivatedTime)
	{
		this.lastActivatedTime = lastActivatedTime;
	}

	public Timestamp getLastChangeTime()
	{
		return LastChangeTime;
	}

	public void setLastChangeTime(Timestamp LastChangeTime)
	{
		this.LastChangeTime = LastChangeTime;
	}

	public String getINTFlag()
	{
		return INTFlag;
	}

	public void setINTFlag(String INTFlag)
	{
		this.INTFlag = INTFlag;
	}

	public String getMFGFlag()
	{
		return MFGFlag;
	}

	public void setMFGFlag(String MFGFlag)
	{
		this.MFGFlag = MFGFlag;
	}

	public String getLastTrackOutTimeKey()
	{
		return lastTrackOutTimeKey;
	}

	public void setLastTrackOutTimeKey(String lastTrackOutTimeKey)
	{
		this.lastTrackOutTimeKey = lastTrackOutTimeKey;
	}

	public String getLastFlagYTimeKey()
	{
		return lastFlagYTimeKey;
	}

	public void setLastFlagYTimeKey(String lastFlagYTimeKey)
	{
		this.lastFlagYTimeKey = lastFlagYTimeKey;
	}

	public String getAutoChangeFlag()
	{
		return autoChangeFlag;
	}

	public void setAutoChangeFlag(String autoChangeFlag)
	{
		this.autoChangeFlag = autoChangeFlag;
	}

	public long getTotalDurationUsed()
	{
		return totalDurationUsed;
	}

	public void setTotalDurationUsed(long totalDurationUsed)
	{
		this.totalDurationUsed = totalDurationUsed;
	}

	public long getDurationUsed()
	{
		return durationUsed;
	}

	public void setDurationUsed(long durationUsed)
	{
		this.durationUsed = durationUsed;
	}

	public Timestamp getLastApproveTime()
	{
		return lastApproveTime;
	}

	public void setLastApproveTime(Timestamp lastApproveTime)
	{
		this.lastApproveTime = lastApproveTime;
	}

	public String getENGFlag()
	{
		return ENGFlag;
	}

	public void setENGFlag(String ENGFlag)
	{
		this.ENGFlag = ENGFlag;
	}

	public String getRMSFlag()
	{
		return RMSFlag;
	}

	public void setRMSFlag(String RMSFlag)
	{
		this.RMSFlag = RMSFlag;
	}

	public String getUnitCheckFlag()
	{
		return unitCheckFlag;
	}

	public void setUnitCheckFlag(String unitCheckFlag)
	{
		this.unitCheckFlag = unitCheckFlag;
	}

	public String getVersionCheckFlag()
	{
		return versionCheckFlag;
	}

	public void setVersionCheckFlag(String versionCheckFlag)
	{
		this.versionCheckFlag = versionCheckFlag;
	}

	public Timestamp getMaxDurationUsedLimit() 
	{
		return maxDurationUsedLimit;
	}

	public void setMaxDurationUsedLimit(Timestamp maxDurationUsedLimit) 
	{
		this.maxDurationUsedLimit = maxDurationUsedLimit;
	}
}
