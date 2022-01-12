package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class OccupyItem extends UdfAccessor
{
	@CTORMTemplate(seq = "1", name = "itemType", type = "Key", dataType = "String", initial = "", history = "")
	private String itemType;

	@CTORMTemplate(seq = "2", name = "itemName", type = "Key", dataType = "String", initial = "", history = "")
	private String itemName;

	@CTORMTemplate(seq = "3", name = "userId", type = "Column", dataType = "String", initial = "", history = "")
	private String userId;

	@CTORMTemplate(seq = "4", name = "state", type = "Column", dataType = "String", initial = "", history = "")
	private String state;

	@CTORMTemplate(seq = "5", name = "startTime", type = "Column", dataType = "Timestamp", initial = "", history = "")
	private Timestamp startTime;

	@CTORMTemplate(seq = "6", name = "expireTime", type = "Column", dataType = "Timestamp", initial = "", history = "")
	private Timestamp expireTime;

	@CTORMTemplate(seq = "7", name = "uiName", type = "Column", dataType = "String", initial = "", history = "")
	private String uiName;

	@CTORMTemplate(seq = "8", name = "menuName", type = "Column", dataType = "String", initial = "", history = "")
	private String menuName;

	@CTORMTemplate(seq = "9", name = "lastEventTime", type = "Column", dataType = "Timestamp", initial = "", history = "N")
	private Timestamp lastEventTime;

	@CTORMTemplate(seq = "10", name = "lastEventTimeKey", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventTimeKey;

	@CTORMTemplate(seq = "11", name = "lastEventName", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventName;

	@CTORMTemplate(seq = "12", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventUser;

	@CTORMTemplate(seq = "13", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventComment;

	public OccupyItem()
	{
	}

	public OccupyItem(String itemType, String itemName)
	{
		super();
		this.itemType = itemType;
		this.itemName = itemName;
	}

	public String getItemType()
	{
		return itemType;
	}

	public void setItemType(String itemType)
	{
		this.itemType = itemType;
	}

	public String getItemName()
	{
		return itemName;
	}

	public void setItemName(String itemName)
	{
		this.itemName = itemName;
	}

	public String getUserId()
	{
		return userId;
	}

	public void setUserId(String userId)
	{
		this.userId = userId;
	}

	public String getState()
	{
		return state;
	}

	public void setState(String state)
	{
		this.state = state;
	}

	public Timestamp getStartTime()
	{
		return startTime;
	}

	public void setStartTime(Timestamp startTime)
	{
		this.startTime = startTime;
	}

	public Timestamp getExpireTime()
	{
		return expireTime;
	}

	public void setExpireTime(Timestamp expireTime)
	{
		this.expireTime = expireTime;
	}

	public String getUiName()
	{
		return uiName;
	}

	public void setUiName(String uiName)
	{
		this.uiName = uiName;
	}

	public String getMenuName()
	{
		return menuName;
	}

	public void setMenuName(String menuName)
	{
		this.menuName = menuName;
	}

	public Timestamp getLastEventTime()
	{
		return lastEventTime;
	}

	public void setLastEventTime(Timestamp lastEventTime)
	{
		this.lastEventTime = lastEventTime;
	}

	public String getLastEventTimeKey()
	{
		return lastEventTimeKey;
	}

	public void setLastEventTimeKey(String lastEventTimeKey)
	{
		this.lastEventTimeKey = lastEventTimeKey;
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

	public String getLastEventComment()
	{
		return lastEventComment;
	}

	public void setLastEventComment(String lastEventComment)
	{
		this.lastEventComment = lastEventComment;
	}
}