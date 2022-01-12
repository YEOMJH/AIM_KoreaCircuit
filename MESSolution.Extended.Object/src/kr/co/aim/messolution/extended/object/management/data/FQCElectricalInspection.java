package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class FQCElectricalInspection extends UdfAccessor {

	@CTORMTemplate(seq = "1", name = "fqcLotName", type = "Key", dataType = "String", initial = "", history = "")
	private String fqcLotName;
	@CTORMTemplate(seq = "2", name = "seq", type = "Key", dataType = "Number", initial = "", history = "")
	private long seq;
	@CTORMTemplate(seq = "3", name = "panelName", type = "Key", dataType = "String", initial = "", history = "")
	private String panelName;
	@CTORMTemplate(seq = "4", name = "defectCode", type = "Key", dataType = "String", initial = "", history = "")
	private String defectCode;
	@CTORMTemplate(seq = "5", name = "defectOrder", type = "Key", dataType = "String", initial = "", history = "")
	private String defectOrder;
	@CTORMTemplate(seq = "6", name = "area", type = "Key", dataType = "String", initial = "", history = "")
	private String area;
	@CTORMTemplate(seq = "7", name = "quantity", type = "Column", dataType = "Number", initial = "", history = "")
	private long quantity;
	@CTORMTemplate(seq = "8", name = "patternName", type = "Column", dataType = "String", initial = "", history = "")
	private String patternName;
	@CTORMTemplate(seq = "9", name = "JNDName", type = "Column", dataType = "String", initial = "", history = "")
	private String JNDName;
	@CTORMTemplate(seq = "10", name = "lastEventName", type = "Column", dataType = "String", initial = "", history = "")
	private String lastEventName;
	@CTORMTemplate(seq = "11", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "")
	private String lastEventUser;
	@CTORMTemplate(seq = "12", name = "lastEventTime", type = "Column", dataType = "Timestamp", initial = "", history = "")
	private Timestamp lastEventTime;
	@CTORMTemplate(seq = "13", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "")
	private String lastEventComment;
	@CTORMTemplate(seq = "14", name = "lastEventTimekey", type = "Column", dataType = "String", initial = "", history = "")
	private String lastEventTimekey;

	public String getFqcLotName()
	{
		return fqcLotName;
	}

	public void setFqcLotName(String fqcLotName)
	{
		this.fqcLotName = fqcLotName;
	}

	public long getSeq()
	{
		return seq;
	}

	public void setSeq(long seq)
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

	public long getQuantity()
	{
		return quantity;
	}

	public void setQuantity(long quantity)
	{
		this.quantity = quantity;
	}

	public String getPatternName()
	{
		return patternName;
	}

	public void setPatternName(String patternName)
	{
		this.patternName = patternName;
	}

	public String getJNDName()
	{
		return JNDName;
	}

	public void setJNDName(String jNDName)
	{
		JNDName = jNDName;
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

	public String getLastEventComment()
	{
		return lastEventComment;
	}

	public void setLastEventComment(String lastEventComment)
	{
		this.lastEventComment = lastEventComment;
	}

	public String getLastEventTimekey()
	{
		return lastEventTimekey;
	}

	public void setLastEventTimekey(String lastEventTimekey)
	{
		this.lastEventTimekey = lastEventTimekey;
	}
}