package kr.co.aim.messolution.userprofile.management.data;


import kr.co.aim.greenframe.orm.info.KeyInfo;
import kr.co.aim.greenframe.orm.info.access.FieldAccessor;

public class UserInvisibleButtonKey extends FieldAccessor implements KeyInfo 
{
	private String userId;
	private String menuName   ;
	private String buttonName   ;
	
	public UserInvisibleButtonKey()
	{
	}
	
	public UserInvisibleButtonKey(String userId,String menuName, String buttonName)
	{
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
