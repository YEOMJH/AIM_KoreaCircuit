package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class FQCInspection extends UdfAccessor {
	@CTORMTemplate(seq = "1", name = "seq", type = "Key", dataType = "Number", initial = "", history = "")
	private Number seq;
	@CTORMTemplate(seq = "2", name = "panelName", type = "Key", dataType = "String", initial = "", history = "")
	private String panelName;
	@CTORMTemplate(seq = "3", name = "defectCode", type = "key", dataType = "String", initial = "", history = "")
	private String defectCode;
	@CTORMTemplate(seq = "4", name = "defectOrder", type = "Column", dataType = "String", initial = "", history = "")
	private String defectOrder;
	@CTORMTemplate(seq = "5", name = "area", type = "Column", dataType = "String", initial = "", history = "")
	private String area;
	@CTORMTemplate(seq = "6", name = "quantity", type = "Column", dataType = "Number", initial = "", history = "")
	private Number quantity;
	@CTORMTemplate(seq = "7", name = "lastEventName", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventName;
	@CTORMTemplate(seq = "8", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventUser;
	@CTORMTemplate(seq = "9", name = "lastEventTime", type = "Column", dataType = "Timestamp", initial = "", history = "N")
	private Timestamp lastEventTime;
	@CTORMTemplate(seq = "10", name = "lastEventTimekey", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventTimekey;
	@CTORMTemplate(seq = "11", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventComment;

	public Number getSeq()
	{
		return seq;
	}

	public void setSeq(Number seq)
	{
		this.seq = seq;
	}

	public String getPanelName()
	{
		return panelName;
	}

	public void setPanelName(String panelName)
	{
		this.panelName = panelName;
	}

	public String getDefectCode()
	{
		return defectCode;
	}

	public void setDefectCode(String defectCode)
	{
		this.defectCode = defectCode;
	}

	public String getDefectOrder()
	{
		return defectOrder;
	}

	public void setDefectOrder(String defectOrder)
	{
		this.defectOrder = defectOrder;
	}

	public String getArea()
	{
		return area;
	}

	public void setArea(String area)
	{
		this.area = area;
	}

	public Number getQuantity()
	{
		return quantity;
	}

	public void setQuantity(Number quantity)
	{
		this.quantity = quantity;
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

	public String getLastEventTimekey()
	{
		return lastEventTimekey;
	}

	public void setLastEventTimekey(String lastEventTimekey)
	{
		this.lastEventTimekey = lastEventTimekey;
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
