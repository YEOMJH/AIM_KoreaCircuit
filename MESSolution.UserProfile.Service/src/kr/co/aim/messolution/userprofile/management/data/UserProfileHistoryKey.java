package kr.co.aim.messolution.userprofile.management.data;

import kr.co.aim.greenframe.orm.info.KeyInfo;
import kr.co.aim.greenframe.orm.info.access.FieldAccessor;

public class UserProfileHistoryKey extends FieldAccessor implements KeyInfo 
{
	private String userId;
	private String timeKey;

	public String getUserId()
	{
		return userId;
	}

	public void setUserId(String userId)
	{
		this.userId = userId;
	}

	public String getTimeKey()
	{
		return timeKey;
	}

	public void setTimeKey(String timeKey)
	{
		this.timeKey = timeKey;
	}

	public UserProfileHistoryKey(String timeKey,String userId)
	{
		this.userId = userId;
		this.timeKey = timeKey;
	}
	
	public UserProfileHistoryKey()
	{
	}
}
