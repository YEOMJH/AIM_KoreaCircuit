package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class FQCRule extends UdfAccessor {

	@CTORMTemplate(seq = "1", name = "seq", type = "Key", dataType = "Number", initial = "", history = "")
	private long seq;
	@CTORMTemplate(seq = "2", name = "ruleName", type = "Column", dataType = "String", initial = "", history = "")
	private String ruleName;
	@CTORMTemplate(seq = "3", name = "ruleType", type = "Column", dataType = "String", initial = "", history = "")
	private String ruleType;
	@CTORMTemplate(seq = "4", name = "minQuantity", type = "Column", dataType = "String", initial = "", history = "")
	private String minQuantity;
	@CTORMTemplate(seq = "5", name = "maxQuantity", type = "Column", dataType = "String", initial = "", history = "")
	private String maxQuantity;
	@CTORMTemplate(seq = "6", name = "trayQuantity", type = "Column", dataType = "Number", initial = "", history = "")
	private long trayQuantity;
	@CTORMTemplate(seq = "7", name = "panelQuantityPerTray", type = "Column", dataType = "Number", initial = "", history = "")
	private long panelQuantityPerTray;
	@CTORMTemplate(seq = "8", name = "panelQuantity", type = "Column", dataType = "Number", initial = "", history = "")
	private long panelQuantity;
	@CTORMTemplate(seq = "9", name = "allowNGQuantity", type = "Column", dataType = "Number", initial = "", history = "")
	private long allowNGQuantity;
	@CTORMTemplate(seq = "10", name = "lastEventName", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventName;
	@CTORMTemplate(seq = "11", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventUser;
	@CTORMTemplate(seq = "12", name = "lastEventTime", type = "Column", dataType = "Timestamp", initial = "", history = "N")
	private Timestamp lastEventTime;
	@CTORMTemplate(seq = "13", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventComment;
	@CTORMTemplate(seq = "14", name = "lastEventTimekey", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventTimekey;

	public long getSeq()
	{
		return seq;
	}

	public void setSeq(long seq)
	{
		this.seq = seq;
	}

	public String getRuleName()
	{
		return ruleName;
	}

	public void setRuleName(String ruleName)
	{
		this.ruleName = ruleName;
	}

	public String getRuleType()
	{
		return ruleType;
	}

	public void setRuleType(String ruleType)
	{
		this.ruleType = ruleType;
	}

	public String getMinQuantity()
	{
		return minQuantity;
	}

	public void setMinQuantity(String minQuantity)
	{
		this.minQuantity = minQuantity;
	}

	public String getMaxQuantity()
	{
		return maxQuantity;
	}

	public void setMaxQuantity(String maxQuantity)
	{
		this.maxQuantity = maxQuantity;
	}

	public long getTrayQuantity()
	{
		return trayQuantity;
	}

	public void setTrayQuantity(long trayQuantity)
	{
		this.trayQuantity = trayQuantity;
	}

	public long getPanelQuantityPerTray()
	{
		return panelQuantityPerTray;
	}

	public void setPanelQuantityPerTray(long panelQuantityPerTray)
	{
		this.panelQuantityPerTray = panelQuantityPerTray;
	}

	public long getPanelQuantity()
	{
		return panelQuantity;
	}

	public void setPanelQuantity(long panelQuantity)
	{
		this.panelQuantity = panelQuantity;
	}

	public long getAllowNGQuantity()
	{
		return allowNGQuantity;
	}

	public void setAllowNGQuantity(long allowNGQuantity)
	{
		this.allowNGQuantity = allowNGQuantity;
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