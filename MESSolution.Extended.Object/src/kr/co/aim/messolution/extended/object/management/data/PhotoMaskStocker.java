package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class PhotoMaskStocker extends UdfAccessor 
{
	@CTORMTemplate(seq = "1", name = "maskStockerName", type = "Key", dataType = "String", initial = "", history = "")
	private String maskStockerName;
	@CTORMTemplate(seq = "2", name = "lineName", type = "Column", dataType = "String", initial = "", history = "")
	private String lineName;
	@CTORMTemplate(seq = "3", name = "slotId", type = "Key", dataType = "String", initial = "", history = "")
	private String slotId;
	@CTORMTemplate(seq = "4", name = "slotStatus", type = "Column", dataType = "String", initial = "", history = "")
	private String slotStatus;
	@CTORMTemplate(seq = "5", name = "maskName", type = "Column", dataType = "String", initial = "", history = "")
	private String maskName;
	@CTORMTemplate(seq = "6", name = "lastEventTime", type = "Column", dataType = "TimeStamp", initial = "", history = "eventTime")
	private Timestamp lastEventTime;
	@CTORMTemplate(seq = "7", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "eventUser")
	private String lastEventUser;
	@CTORMTemplate(seq = "8", name = "lastEventName", type = "Column", dataType = "String", initial = "", history = "eventName")
	private String lastEventName;
	@CTORMTemplate(seq = "9", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "eventComment")
	private String lastEventComment;
	@CTORMTemplate(seq = "10", name = "lastEventTimeKey", type = "Column", dataType = "String", initial = "", history = "timeKey")
	private String lastEventTimeKey;
	
	public String getMaskStockerName()
	{
		return maskStockerName;
	}
	public void setMaskStockerName(String maskStockerName)
	{
		this.maskStockerName = maskStockerName;
	}
	public String getLineName()
	{
		return lineName;
	}
	public void setLineName(String lineName)
	{
		this.lineName = lineName;
	}
	public String getSlotId()
	{
		return slotId;
	}
	public void setSlotId(String slotId)
	{
		this.slotId = slotId;
	}
	public String getSlotStatus()
	{
		return slotStatus;
	}
	public void setSlotStatus(String slotStatus)
	{
		this.slotStatus = slotStatus;
	}
	public String getMaskName()
	{
		return maskName;
	}
	public void setMaskName(String maskName)
	{
		this.maskName = maskName;
	}
	public Timestamp getLastEventTime()
	{
		return lastEventTime;
	}
	public void setLastEventTime(Timestamp lastEventTime)
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
	public String getLastEventName()
	{
		return lastEventName;
	}
	public void setLastEventName(String lastEventName)
	{
		this.lastEventName = lastEventName;
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
