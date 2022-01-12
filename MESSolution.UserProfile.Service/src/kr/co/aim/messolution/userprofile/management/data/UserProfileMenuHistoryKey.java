package kr.co.aim.messolution.userprofile.management.data;

import kr.co.aim.greenframe.orm.info.KeyInfo;
import kr.co.aim.greenframe.orm.info.access.FieldAccessor;

public class UserProfileMenuHistoryKey extends FieldAccessor implements KeyInfo 
{
	private String timeKey;
	private String userId;
	private String UIName;
	private String menuName;
	
	public UserProfileMenuHistoryKey()
	{
	}
	
	public UserProfileMenuHistoryKey(String timeKey,String userId, String UIName, String menuName)
	{
		this.timeKey = timeKey;
        this.userId = userId;
		this.menuName = menuName;
		this.UIName = UIName;
	}

	public String getTimeKey()
	{
		return timeKey;
	}

	public void setTimeKey(String timeKey)
	{
		this.timeKey = timeKey;
	}

	public String getUserId()
	{
		return userId;
	}

	public void setUserId(String userId)
	{
		this.userId = userId;
	}

	public String getUIName()
	{
		return UIName;
	}

	public void setUIName(String uIName)
	{
		UIName = uIName;
	}

	public String getMenuName()
	{
		return menuName;
	}

	public void setMenuName(String menuName)
	{
		this.menuName = menuName;
	}
}
