package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class MVIJNDDefectCode extends UdfAccessor {
	@CTORMTemplate(seq = "1", name = "productSpecName", type = "Key", dataType = "String", initial = "", history = "")
	private String productSpecName;
	@CTORMTemplate(seq = "2", name = "defectCode", type = "Key", dataType = "String", initial = "", history = "")
	private String defectCode;
	@CTORMTemplate(seq = "3", name = "superDefectCode", type = "Key", dataType = "String", initial = "", history = "")
	private String superDefectCode;
	@CTORMTemplate(seq = "4", name = "pattern", type = "Key", dataType = "String", initial = "", history = "")
	private String pattern;
	@CTORMTemplate(seq = "5", name = "jndName", type = "Key", dataType = "String", initial = "", history = "")
	private String jndName;
	@CTORMTemplate(seq = "6", name = "panelGrade", type = "Key", dataType = "String", initial = "", history = "")
	private String panelGrade;
	@CTORMTemplate(seq = "7", name = "defectCodeDescription", type = "Column", dataType = "String", initial = "", history = "")
	private String defectCodeDescription;
	@CTORMTemplate(seq = "8", name = "superDefectCodeDescription", type = "Column", dataType = "String", initial = "", history = "")
	private String superDefectCodeDescription;
	@CTORMTemplate(seq = "9", name = "sign", type = "Column", dataType = "String", initial = "", history = "")
	private String sign;
	@CTORMTemplate(seq = "10", name = "lastEventName", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventName;
	@CTORMTemplate(seq = "11", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventUser;
	@CTORMTemplate(seq = "12", name = "lastEventTime", type = "Column", dataType = "Timestamp", initial = "", history = "N")
	private Timestamp lastEventTime;
	@CTORMTemplate(seq = "13", name = "lastEventTimekey", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventTimekey;
	@CTORMTemplate(seq = "14", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "N")
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

	public String getPattern()
	{
		return pattern;
	}

	public void setPattern(String pattern)
	{
		this.pattern = pattern;
	}

	public String getJndName()
	{
		return jndName;
	}

	public void setJndName(String jndName)
	{
		this.jndName = jndName;
	}

	public String getPanelGrade()
	{
		return panelGrade;
	}

	public void setPanelGrade(String panelGrade)
	{
		this.panelGrade = panelGrade;
	}

	public String getDefectCodeDescription()
	{
		return defectCodeDescription;
	}

	public void setDefectCodeDescription(String defectCodeDescription)
	{
		this.defectCodeDescription = defectCodeDescription;
	}

	public String getSuperDefectCodeDescription()
	{
		return superDefectCodeDescription;
	}

	public void setSuperDefectCodeDescription(String superDefectCodeDescription)
	{
		this.superDefectCodeDescription = superDefectCodeDescription;
	}

	public String getSign()
	{
		return sign;
	}

	public void setSign(String sign)
	{
		this.sign = sign;
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
