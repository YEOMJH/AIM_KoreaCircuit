package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class AVIPanelJudge extends UdfAccessor {

	@CTORMTemplate(seq = "1", name = "panelName", type = "Key", dataType = "String", initial = "", history = "")
	private String panelName;

	@CTORMTemplate(seq = "2", name = "productSpecName", type = "Column", dataType = "String", initial = "", history = "")
	private String productSpecName;

	@CTORMTemplate(seq = "3", name = "processFlowName", type = "Column", dataType = "String", initial = "", history = "")
	private String processFlowName;

	@CTORMTemplate(seq = "4", name = "processOperationName", type = "Column", dataType = "String", initial = "", history = "")
	private String processOperationName;

	@CTORMTemplate(seq = "5", name = "machineName", type = "Column", dataType = "String", initial = "", history = "")
	private String machineName;

	@CTORMTemplate(seq = "6", name = "unitName", type = "Column", dataType = "String", initial = "", history = "")
	private String unitName;

	@CTORMTemplate(seq = "7", name = "machineRecipeName", type = "Column", dataType = "String", initial = "", history = "")
	private String machineRecipeName;

	@CTORMTemplate(seq = "8", name = "panelJudge", type = "Column", dataType = "String", initial = "", history = "")
	private String panelJudge;

	@CTORMTemplate(seq = "9", name = "panelGrade", type = "Column", dataType = "String", initial = "", history = "")
	private String panelGrade;

	@CTORMTemplate(seq = "10", name = "gamJudge", type = "Column", dataType = "String", initial = "", history = "")
	private String gamJudge;

	@CTORMTemplate(seq = "11", name = "aoiJudge", type = "Column", dataType = "String", initial = "", history = "")
	private String aoiJudge;

	@CTORMTemplate(seq = "12", name = "murJudge", type = "Column", dataType = "String", initial = "", history = "")
	private String murJudge;

	@CTORMTemplate(seq = "13", name = "tpJudge", type = "Column", dataType = "String", initial = "", history = "")
	private String tpJudge;

	@CTORMTemplate(seq = "14", name = "manJudge", type = "Column", dataType = "String", initial = "", history = "")
	private String manJudge;

	@CTORMTemplate(seq = "15", name = "appJudge", type = "Column", dataType = "String", initial = "", history = "")
	private String appJudge;

	@CTORMTemplate(seq = "16", name = "lastEventName", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventName;

	@CTORMTemplate(seq = "17", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventUser;

	@CTORMTemplate(seq = "18", name = "lastEventTime", type = "Column", dataType = "Timestamp", initial = "", history = "N")
	private Timestamp lastEventTime;

	@CTORMTemplate(seq = "19", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventComment;

	@CTORMTemplate(seq = "20", name = "lastEventTimekey", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventTimekey;

	public String getPanelName()
	{
		return panelName;
	}

	public void setPanelName(String panelName)
	{
		this.panelName = panelName;
	}

	public String getProductSpecName()
	{
		return productSpecName;
	}

	public void setProductSpecName(String productSpecName)
	{
		this.productSpecName = productSpecName;
	}

	public String getProcessFlowName()
	{
		return processFlowName;
	}

	public void setProcessFlowName(String processFlowName)
	{
		this.processFlowName = processFlowName;
	}

	public String getProcessOperationName()
	{
		return processOperationName;
	}

	public void setProcessOperationName(String processOperationName)
	{
		this.processOperationName = processOperationName;
	}

	public String getMachineName()
	{
		return machineName;
	}

	public void setMachineName(String machineName)
	{
		this.machineName = machineName;
	}

	public String getUnitName()
	{
		return unitName;
	}

	public void setUnitName(String unitName)
	{
		this.unitName = unitName;
	}

	public String getMachineRecipeName()
	{
		return machineRecipeName;
	}

	public void setMachineRecipeName(String machineRecipeName)
	{
		this.machineRecipeName = machineRecipeName;
	}

	public String getPanelJudge()
	{
		return panelJudge;
	}

	public void setPanelJudge(String panelJudge)
	{
		this.panelJudge = panelJudge;
	}

	public String getPanelGrade()
	{
		return panelGrade;
	}

	public void setPanelGrade(String panelGrade)
	{
		this.panelGrade = panelGrade;
	}

	public String getGamJudge()
	{
		return gamJudge;
	}

	public void setGamJudge(String gamJudge)
	{
		this.gamJudge = gamJudge;
	}

	public String getAoiJudge()
	{
		return aoiJudge;
	}

	public void setAoiJudge(String aoiJudge)
	{
		this.aoiJudge = aoiJudge;
	}

	public String getMurJudge()
	{
		return murJudge;
	}

	public void setMurJudge(String murJudge)
	{
		this.murJudge = murJudge;
	}

	public String getTpJudge()
	{
		return tpJudge;
	}

	public void setTpJudge(String tpJudge)
	{
		this.tpJudge = tpJudge;
	}

	public String getManJudge()
	{
		return manJudge;
	}

	public void setManJudge(String manJudge)
	{
		this.manJudge = manJudge;
	}

	public String getAppJudge()
	{
		return appJudge;
	}

	public void setAppJudge(String appJudge)
	{
		this.appJudge = appJudge;
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