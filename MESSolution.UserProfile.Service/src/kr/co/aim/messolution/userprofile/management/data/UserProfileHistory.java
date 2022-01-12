package kr.co.aim.messolution.userprofile.management.data;

import java.sql.Timestamp;

import kr.co.aim.greenframe.orm.info.DataInfo;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class UserProfileHistory extends UdfAccessor implements DataInfo<UserProfileHistoryKey> 
{
	private UserProfileHistoryKey key;

	public UserProfileHistoryKey getKey()
	{
		return key;
	}
	public void setKey(UserProfileHistoryKey key)
	{
		this.key = key;
	}

	public String getPassword()
	{
		return password;
	}
	public void setPassword(String password)
	{
		this.password = password;
	}
	public String getUserName()
	{
		return userName;
	}
	public void setUserName(String userName)
	{
		this.userName = userName;
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

	public String getUserGroupName()
	{
		return userGroupName;
	}
	public void setUserGroupName(String userGroupName)
	{
		this.userGroupName = userGroupName;
	}



	private String password;
	private String userName;
	private String userGroupName;
	private Timestamp eventTime;
	private String eventName;
	private String eventUser;
	private String eventComment;
}
