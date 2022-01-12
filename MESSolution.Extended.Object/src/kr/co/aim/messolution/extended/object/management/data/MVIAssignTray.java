package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class MVIAssignTray extends UdfAccessor {
	@CTORMTemplate(seq = "1", name = "timekey", type = "Key", dataType = "String", initial = "", history = "")
	private String timekey;
	@CTORMTemplate(seq = "2", name = "machineName", type = "Key", dataType = "String", initial = "", history = "")
	private String machineName;
	@CTORMTemplate(seq = "3", name = "judge", type = "Column", dataType = "String", initial = "", history = "")
	private String judge;
	@CTORMTemplate(seq = "4", name = "trayName", type = "Column", dataType = "String", initial = "", history = "")
	private String trayName;
	@CTORMTemplate(seq = "5", name = "seq", type = "Column", dataType = "String", initial = "", history = "")
	private String seq;
	@CTORMTemplate(seq = "6", name = "productSpecName", type = "Column", dataType = "String", initial = "", history = "")
	private String productSpecName;
	@CTORMTemplate(seq = "7", name = "productSpecVersion", type = "Column", dataType = "String", initial = "", history = "")
	private String productSpecVersion;
	@CTORMTemplate(seq = "8", name = "processFlowName", type = "Column", dataType = "String", initial = "", history = "")
	private String processFlowName;
	@CTORMTemplate(seq = "9", name = "processFlowVersion", type = "Column", dataType = "String", initial = "", history = "")
	private String processFlowVersion;
	@CTORMTemplate(seq = "10", name = "processOperationName", type = "Column", dataType = "Timestamp", initial = "", history = "")
	private String processOperationName;
	@CTORMTemplate(seq = "11", name = "processOperationVersion", type = "Column", dataType = "String", initial = "", history = "")
	private String processOperationVersion;
	@CTORMTemplate(seq = "12", name = "productRequestName", type = "Column", dataType = "String", initial = "", history = "")
	private String productRequestName;
	@CTORMTemplate(seq = "13", name = "lastEventName", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventName;
	@CTORMTemplate(seq = "14", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventUser;
	@CTORMTemplate(seq = "15", name = "lastEventTime", type = "Column", dataType = "Timestamp", initial = "", history = "N")
	private Timestamp lastEventTime;
	@CTORMTemplate(seq = "16", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventComment;

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

	public String getJudge()
	{
		return judge;
	}

	public void setJudge(String judge)
	{
		this.judge = judge;
	}

	public String getTrayName()
	{
		return trayName;
	}

	public void setTrayName(String trayName)
	{
		this.trayName = trayName;
	}

	public String getSeq()
	{
		return seq;
	}

	public void setSeq(String seq)
	{
		this.seq = seq;
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

	public String getProcessFlowName()
	{
		return processFlowName;
	}

	public void setProcessFlowName(String processFlowName)
	{
		this.processFlowName = processFlowName;
	}

	public String getProcessFlowVersion()
	{
		return processFlowVersion;
	}

	public void setProcessFlowVersion(String processFlowVersion)
	{
		this.processFlowVersion = processFlowVersion;
	}

	public String getProcessOperationName()
	{
		return processOperationName;
	}

	public void setProcessOperationName(String processOperationName)
	{
		this.processOperationName = processOperationName;
	}

	public String getProcessOperationVersion()
	{
		return processOperationVersion;
	}

	public void setProcessOperationVersion(String processOperationVersion)
	{
		this.processOperationVersion = processOperationVersion;
	}

	public String getProductRequestName()
	{
		return productRequestName;
	}

	public void setProductRequestName(String productRequestName)
	{
		this.productRequestName = productRequestName;
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
}
