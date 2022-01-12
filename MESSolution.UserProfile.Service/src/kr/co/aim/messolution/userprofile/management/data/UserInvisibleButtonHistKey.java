package kr.co.aim.messolution.userprofile.management.data;

import kr.co.aim.greenframe.orm.info.KeyInfo;
import kr.co.aim.greenframe.orm.info.access.FieldAccessor;

public class UserInvisibleButtonHistKey extends FieldAccessor implements KeyInfo 
{
	private String timeKey;
	private String userId;
	private String menuName;
	private String buttonName;
	
	public UserInvisibleButtonHistKey()
	{
	}
	
	public UserInvisibleButtonHistKey(String timeKey,String userId, String menuName, String buttonName)
	{
		this.timeKey = timeKey;
        this.userId = userId;
		this.menuName = menuName;
		this.buttonName = buttonName;
	}
	
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
	
	public String getMenuName()
	{
		return menuName;
	}
	public void setMenuName(String menuName)
	{
		this.menuName = menuName;
	}
	public String getButtonName()
	{
		return buttonName;
	}
	public void setButtonName(String buttonName)
	{
		this.buttonName = buttonName;
	}
	
}
