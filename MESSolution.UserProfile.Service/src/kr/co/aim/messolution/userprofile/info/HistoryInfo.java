package kr.co.aim.messolution.userprofile.info;

import java.sql.Timestamp;

import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class HistoryInfo extends UdfAccessor {
	
	private String timeKey;
	private String eventName;
	private Timestamp eventTime;
	private String eventComment;
	private String eventUser;
	public String getEventName()
	{
		return eventName;
	}
	public void setEventName(String eventName)
	{
		this.eventName = eventName;
	}
	
	public Timestamp getEventTime()
	{
		return eventTime;
	}
	public void setEventTime(Timestamp eventTime)
	{
		this.eventTime = eventTime;
	}
	public String getEventComment()
	{
		return eventComment;
	}
	public void setEventComment(String eventComment)
	{
		this.eventComment = eventComment;
	}
	public String getEventUser()
	{
		return eventUser;
	}
	public void setEventUser(String eventUser)
	{
		this.eventUser = eventUser;
	}
	public String getTimeKey()
	{
		return timeKey;
	}
	public void setTimeKey(String timeKey)
	{
		this.timeKey = timeKey;
	}
	
}
