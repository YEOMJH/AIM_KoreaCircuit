package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class MVIDefectCode extends UdfAccessor {
	@CTORMTemplate(seq = "1", name = "productSpecName", type = "Key", dataType = "String", initial = "", history = "")
	private String productSpecName;
	@CTORMTemplate(seq = "2", name = "defectCode", type = "Key", dataType = "String", initial = "", history = "")
	private String defectCode;
	@CTORMTemplate(seq = "3", name = "superDefectCode", type = "Key", dataType = "String", initial = "", history = "")
	private String superDefectCode;
	@CTORMTemplate(seq = "4", name = "description", type = "Column", dataType = "String", initial = "", history = "")
	private String description;
	@CTORMTemplate(seq = "5", name = "levelNo", type = "Column", dataType = "Long", initial = "", history = "")
	private Long levelNo;
	@CTORMTemplate(seq = "6", name = "conditionFlag", type = "Column", dataType = "String", initial = "", history = "")
	private String conditionFlag;
	@CTORMTemplate(seq = "7", name = "panelGrade", type = "Column", dataType = "String", initial = "", history = "")
	private String panelGrade;
	@CTORMTemplate(seq = "8", name = "lastEventName", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventName;
	@CTORMTemplate(seq = "9", name = "lastEventTime", type = "Column", dataType = "Timestamp", initial = "", history = "N")
	private Timestamp lastEventTime;
	@CTORMTemplate(seq = "10", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventUser;
	@CTORMTemplate(seq = "11", name = "lastEventTimekey", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventTimekey;
	@CTORMTemplate(seq = "12", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventComment;

	public String getProductSpecName()
	{
		return productSpecName;
	}

	public void setProductSpecName(String productSpecName)
	{
		this.productSpecName = productSpecName;
	}

	public String getDefectCode()
	{
		return defectCode;
	}

	public void setDefectCode(String defectCode)
	{
		this.defectCode = defectCode;
	}

	public String getSuperDefectCode()
	{
		return superDefectCode;
	}

	public void setSuperDefectCode(String superDefectCode)
	{
		this.superDefectCode = superDefectCode;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public Long getLevelNo()
	{
		return levelNo;
	}

	public void setLevelNo(Long levelNo)
	{
		this.levelNo = levelNo;
	}

	public String getConditionFlag()
	{
		return conditionFlag;
	}

	public void setConditionFlag(String conditionFlag)
	{
		this.conditionFlag = conditionFlag;
	}

	public String getPanelGrade()
	{
		return panelGrade;
	}

	public void setPanelGrade(String panelGrade)
	{
		this.panelGrade = panelGrade;
	}

	public String getLastEventName()
	{
		return lastEventName;
	}

	public void setLastEventName(String lastEventName)
	{
		this.lastEventName = lastEventName;
	}

	public Timestamp getLastEventTime()
	{
		return lastEventTime;
	}

	public void setLastEventTime(Timestamp lastEventTime)
	{
		this.lastEventTime = lastEventTime;
	}

	public String getLastEventUser()
	{
		return lastEventUser;
	}

	public void setLastEventUser(String lastEventUser)
	{
		this.lastEventUser = lastEventUser;
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
