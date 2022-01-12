package kr.co.aim.messolution.extended.object.management.data;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class MVIUserDefectHist extends UdfAccessor {
	@CTORMTemplate(seq = "1", name = "panelName", type = "Key", dataType = "String", initial = "", history = "")
	private String panelName;

	@CTORMTemplate(seq = "2", name = "eventUser", type = "Key", dataType = "String", initial = "", history = "")
	private String eventUser;

	@CTORMTemplate(seq = "3", name = "timekey", type = "Key", dataType = "String", initial = "", history = "")
	private String timekey;

	@CTORMTemplate(seq = "4", name = "machineName", type = "Column", dataType = "String", initial = "", history = "")
	private String machineName;

	@CTORMTemplate(seq = "5", name = "productSpecName", type = "Column", dataType = "String", initial = "", history = "")
	private String productSpecName;

	@CTORMTemplate(seq = "6", name = "productSpecVersion", type = "Column", dataType = "String", initial = "", history = "")
	private String productSpecVersion;

	@CTORMTemplate(seq = "7", name = "defectCode", type = "Column", dataType = "String", initial = "", history = "")
	private String defectCode;

	@CTORMTemplate(seq = "8", name = "panelJudge", type = "Column", dataType = "String", initial = "", history = "")
	private String panelJudge;

	@CTORMTemplate(seq = "9", name = "subProductionType", type = "Column", dataType = "String", initial = "", history = "")
	private String subProductionType;
	
	@CTORMTemplate(seq = "10", name = "processOperationName", type = "Column", dataType = "String", initial = "", history = "")
	private String processOperationName;

	public String getPanelName()
	{
		return panelName;
	}

	public void setPanelName(String panelName)
	{
		this.panelName = panelName;
	}

	public String getEventUser()
	{
		return eventUser;
	}

	public void setEventUser(String eventUser)
	{
		this.eventUser = eventUser;
	}

	public String getTimekey()
	{
		return timekey;
	}

	public void setTimekey(String timekey)
	{
		this.timekey = timekey;
	}

	public String getMachineName()
	{
		return machineName;
	}

	public void setMachineName(String machineName)
	{
		this.machineName = machineName;
	}

	public String getProductSpecName()
	{
		return productSpecName;
	}

	public void setProductSpecName(String productSpecName)
	{
		this.productSpecName = productSpecName;
	}

	public String getProductSpecVersion()
	{
		return productSpecVersion;
	}

	public void setProductSpecVersion(String productSpecVersion)
	{
		this.productSpecVersion = productSpecVersion;
	}

	public String getDefectCode()
	{
		return defectCode;
	}

	public void setDefectCode(String defectCode)
	{
		this.defectCode = defectCode;
	}

	public String getPanelJudge()
	{
		return panelJudge;
	}

	public void setPanelJudge(String panelJudge)
	{
		this.panelJudge = panelJudge;
	}

	public String getSubProductionType()
	{
		return subProductionType;
	}

	public void setSubProductionType(String subProductionType)
	{
		this.subProductionType = subProductionType;
	}

	public String getProcessOperationName() {
		return processOperationName;
	}

	public void setProcessOperationName(String processOperationName) {
		this.processOperationName = processOperationName;
	}
}
