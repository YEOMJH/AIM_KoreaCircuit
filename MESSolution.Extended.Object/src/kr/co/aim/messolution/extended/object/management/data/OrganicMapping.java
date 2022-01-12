package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class OrganicMapping extends UdfAccessor {

	@CTORMTemplate(seq = "1", name = "crucibleName", type = "Key", dataType = "String", initial = "", history = "")
	private String crucibleName;

	@CTORMTemplate(seq = "2", name = "materialSpecName", type = "Column", dataType = "String", initial = "", history = "")
	private String materialSpecName;

	@CTORMTemplate(seq = "3", name = "materialSpecVersion", type = "Column", dataType = "String", initial = "", history = "")
	private String materialSpecVersion;

	@CTORMTemplate(seq = "4", name = "machineName", type = "Column", dataType = "String", initial = "", history = "")
	private String machineName;

	@CTORMTemplate(seq = "5", name = "chamberName", type = "Column", dataType = "String", initial = "", history = "")
	private String chamberName;

	@CTORMTemplate(seq = "6", name = "lastEventName", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventName;

	@CTORMTemplate(seq = "7", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventUser;

	@CTORMTemplate(seq = "8", name = "lastEventTime", type = "Column", dataType = "Timestamp", initial = "", history = "N")
	private Timestamp lastEventTime;

	@CTORMTemplate(seq = "9", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventComment;

	@CTORMTemplate(seq = "10", name = "lastEventTimeKey", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventTimeKey;

	public String getCrucibleName()
	{
		return crucibleName;
	}

	public void setCrucibleName(String crucibleName)
	{
		this.crucibleName = crucibleName;
	}

	public String getMaterialSpecName()
	{
		return materialSpecName;
	}

	public void setMaterialSpecName(String materialSpecName)
	{
		this.materialSpecName = materialSpecName;
	}

	public String getMaterialSpecVersion()
	{
		return materialSpecVersion;
	}

	public void setMaterialSpecVersion(String materialSpecVersion)
	{
		this.materialSpecVersion = materialSpecVersion;
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

	public String getLastEventComment()
	{
		return lastEventComment;
	}

	public void setLastEventComment(String lastEventComment)
	{
		this.lastEventComment = lastEventComment;
	}

	public String getLastEventTimeKey()
	{
		return lastEventTimeKey;
	}

	public void setLastEventTimeKey(String lastEventTimeKey)
	{
		this.lastEventTimeKey = lastEventTimeKey;
	}
}
