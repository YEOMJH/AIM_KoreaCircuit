package kr.co.aim.messolution.userprofile.management.data;

import java.sql.Timestamp;

import kr.co.aim.greenframe.orm.info.DataInfo;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class UserProfileMenuHistory extends UdfAccessor implements DataInfo<UserProfileMenuHistoryKey> 
{
	private UserProfileMenuHistoryKey key;
	private String 			accessFlag;
	private Timestamp       systemTime;
	private String          cancelFlag;
	private String          cancelTimeKey;
	private Timestamp		eventTime;
	private String			eventName;
	private String			eventUser;
	private String			eventComment;

	public UserProfileMenuHistoryKey getKey()
	{
		return key;
	}
	public void setKey(UserProfileMenuHistoryKey key)
	{
		this.key = key;
	}
	public Timestamp getEventTime()
	{
		return eventTime;
	}
	public void setEventTime(Timestamp eventTime)
	{
		this.eventTime = eventTime;
	}
	public String getEventName()
	{
		return eventName;
	}
	public void setEventName(String eventName)
	{
		this.eventName = eventName;
	}
	public String getEventUser()
	{
		return eventUser;
	}
	public void setEventUser(String eventUser)
	{
		this.eventUser = eventUser;
	}
	public String getEventComment()
	{
		return eventComment;
	}
	public void setEventComment(String eventComment)
	{
		this.eventComment = eventComment;
	}
	public String getAccessFlag()
	{
		return accessFlag;
	}
	public void setAccessFlag(String accessFlag)
	{
		this.accessFlag = accessFlag;
	}
	public Timestamp getSystemTime()
	{
		return systemTime;
	}
	public void setSystemTime(Timestamp systemTime)
	{
		this.systemTime = systemTime;
	}
	public String getCancelFlag()
	{
		return cancelFlag;
	}
	public void setCancelFlag(String cancelFlag)
	{
		this.cancelFlag = cancelFlag;
	}
	public String getCancelTimeKey()
	{
		return cancelTimeKey;
	}
	public void setCancelTimeKey(String cancelTimeKey)
	{
		this.cancelTimeKey = cancelTimeKey;
	}
}
