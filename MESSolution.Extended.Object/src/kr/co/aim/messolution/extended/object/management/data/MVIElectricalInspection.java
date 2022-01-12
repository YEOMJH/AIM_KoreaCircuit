package kr.co.aim.messolution.extended.object.management.data;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class MVIElectricalInspection extends UdfAccessor {

	@CTORMTemplate(seq = "1", name = "seq", type = "Key", dataType = "Number", initial = "", history = "")
	private long seq;

	@CTORMTemplate(seq = "2", name = "panelName", type = "Key", dataType = "String", initial = "", history = "")
	private String panelName;

	@CTORMTemplate(seq = "3", name = "defectCode", type = "Key", dataType = "String", initial = "", history = "")
	private String defectCode;

	@CTORMTemplate(seq = "4", name = "defectOrder", type = "Key", dataType = "String", initial = "", history = "")
	private String defectOrder;

	@CTORMTemplate(seq = "5", name = "area", type = "Key", dataType = "String", initial = "", history = "")
	private String area;

	@CTORMTemplate(seq = "6", name = "lastEventTimeKey", type = "Column", dataType = "String", initial = "", history = "")
	private String lastEventTimeKey;

	@CTORMTemplate(seq = "7", name = "quantity", type = "Column", dataType = "long", initial = "", history = "")
	private long quantity;

	@CTORMTemplate(seq = "7", name = "jndName", type = "Column", dataType = "String", initial = "", history = "")
	private String jndName;

	@CTORMTemplate(seq = "8", name = "patternName", type = "Column", dataType = "String", initial = "", history = "")
	private String patternName;

	@CTORMTemplate(seq = "9", name = "description", type = "Column", dataType = "String", initial = "", history = "")
	private String description;

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

	public String getLastEventTimeKey()
	{
		return lastEventTimeKey;
	}

	public void setLastEventTimeKey(String lastEventTimeKey)
	{
		this.lastEventTimeKey = lastEventTimeKey;
	}

	public long getQuantity()
	{
		return quantity;
	}

	public void setQuantity(long quantity)
	{
		this.quantity = quantity;
	}

	public String getJndName()
	{
		return jndName;
	}

	public void setJndName(String jndName)
	{
		this.jndName = jndName;
	}

	public String getPatternName()
	{
		return patternName;
	}

	public void setPatternName(String patternName)
	{
		this.patternName = patternName;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}
}