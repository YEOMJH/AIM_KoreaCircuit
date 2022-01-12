package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class SorterPickPrintInfo extends UdfAccessor 
{
	@CTORMTemplate(seq = "1", name = "lotName", type = "Key", dataType = "String", initial = "", history = "")
	private String lotName;
	@CTORMTemplate(seq = "2", name = "pickPrintMode", type = "Column", dataType = "String", initial = "", history = "")
	private String pickPrintMode;
	@CTORMTemplate(seq = "3", name = "code", type = "Column", dataType = "String", initial = "", history = "")
	private String code;
	@CTORMTemplate(seq = "4", name = "lastEventName", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventName;
	@CTORMTemplate(seq = "5", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventUser;
	@CTORMTemplate(seq = "6", name = "lastEventTime", type = "Column", dataType = "TimeStamp", initial = "", history = "N")
	private Timestamp lastEventTime;
	@CTORMTemplate(seq = "7", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventComment;
	@CTORMTemplate(seq = "8", name = "lastEventTimeKey", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventTimeKey;
	public String getLotName()
	{
		return lotName;
	}
	public void setLotName(String lotName)
	{
		this.lotName = lotName;
	}
	
	public String getPickPrintMode()
	{
		return pickPrintMode;
	}
	public void setPickPrintMode(String pickPrintMode)
	{
		this.pickPrintMode = pickPrintMode;
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
	public String getCode()
	{
		return code;
	}
	public void setCode(String code)
	{
		this.code = code;
	}
	
}
