package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class FQCPanelJudge extends UdfAccessor {

	@CTORMTemplate(seq = "1", name = "fqcLotName", type = "Key", dataType = "String", initial = "", history = "")
	private String fqcLotName;
	@CTORMTemplate(seq = "2", name = "seq", type = "Key", dataType = "Number", initial = "", history = "")
	private long seq;
	@CTORMTemplate(seq = "3", name = "panelName", type = "Key", dataType = "String", initial = "", history = "")
	private String panelName;
	@CTORMTemplate(seq = "4", name = "beforeGrade", type = "Column", dataType = "String", initial = "", history = "")
	private String beforeGrade;
	@CTORMTemplate(seq = "5", name = "afterGrade", type = "Column", dataType = "String", initial = "", history = "")
	private String afterGrade;
	@CTORMTemplate(seq = "6", name = "opticalJudge", type = "Column", dataType = "String", initial = "", history = "")
	private String opticalJudge;
	@CTORMTemplate(seq = "7", name = "electricalJudge", type = "Column", dataType = "String", initial = "", history = "")
	private String electricalJudge;
	@CTORMTemplate(seq = "8", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "")
	private String lastEventUser;
	@CTORMTemplate(seq = "9", name = "lastEventTime", type = "Column", dataType = "Timestamp", initial = "", history = "")
	private Timestamp lastEventTime;
	@CTORMTemplate(seq = "10", name = "machineName", type = "Column", dataType = "String", initial = "", history = "")
	private String machineName;
	@CTORMTemplate(seq = "11", name = "lastEventName", type = "Column", dataType = "String", initial = "", history = "")
	private String lastEventName;
	@CTORMTemplate(seq = "12", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "")
	private String lastEventComment;
	@CTORMTemplate(seq = "13", name = "lastEventTimekey", type = "Column", dataType = "String", initial = "", history = "")
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

	public String getBeforeGrade()
	{
		return beforeGrade;
	}

	public void setBeforeGrade(String beforeGrade)
	{
		this.beforeGrade = beforeGrade;
	}

	public String getAfterGrade()
	{
		return afterGrade;
	}

	public void setAfterGrade(String afterGrade)
	{
		this.afterGrade = afterGrade;
	}

	public String getOpticalJudge()
	{
		return opticalJudge;
	}

	public void setOpticalJudge(String opticalJudge)
	{
		this.opticalJudge = opticalJudge;
	}

	public String getElectricalJudge()
	{
		return electricalJudge;
	}

	public void setElectricalJudge(String electricalJudge)
	{
		this.electricalJudge = electricalJudge;
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

	public String getMachineName()
	{
		return machineName;
	}

	public void setMachineName(String machineName)
	{
		this.machineName = machineName;
	}

	public String getLastEventName()
	{
		return lastEventName;
	}

	public void setLastEventName(String lastEventName)
	{
		this.lastEventName = lastEventName;
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