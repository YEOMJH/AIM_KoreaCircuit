package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class ShieldSpec extends UdfAccessor {
	@CTORMTemplate(seq = "1", name = "factoryName", type = "Key", dataType = "String", initial = "", history = "")
	private String factoryName;
	@CTORMTemplate(seq = "2", name = "shieldSpecName", type = "Key", dataType = "String", initial = "", history = "")
	private String shieldSpecName;
	@CTORMTemplate(seq = "3", name = "description", type = "Column", dataType = "String", initial = "", history = "")
	private String description;
	@CTORMTemplate(seq = "4", name = "chamberType", type = "Column", dataType = "String", initial = "", history = "")
	private String chamberType;
	@CTORMTemplate(seq = "5", name = "processFlowName", type = "Column", dataType = "String", initial = "", history = "")
	private String processFlowName;
	@CTORMTemplate(seq = "6", name = "processFlowVersion", type = "Column", dataType = "String", initial = "", history = "")
	private String processFlowVersion;
	@CTORMTemplate(seq = "7", name = "checkState", type = "Column", dataType = "String", initial = "", history = "")
	private String checkState;
	@CTORMTemplate(seq = "8", name = "createTime", type = "Column", dataType = "Timestamp", initial = "", history = "")
	private Timestamp createTime;
	@CTORMTemplate(seq = "9", name = "createUser", type = "Column", dataType = "String", initial = "", history = "")
	private String createUser;
	@CTORMTemplate(seq = "10", name = "checkOutTime", type = "Column", dataType = "Timestamp", initial = "", history = "")
	private Timestamp checkOutTime;
	@CTORMTemplate(seq = "11", name = "checkOutUser", type = "Column", dataType = "String", initial = "", history = "")
	private String checkOutUser;
	@CTORMTemplate(seq = "12", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventUser;
	@CTORMTemplate(seq = "13", name = "lastEventName", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventName;
	@CTORMTemplate(seq = "14", name = "lastEventTime", type = "Column", dataType = "Timestamp", initial = "", history = "N")
	private Timestamp lastEventTime;
	@CTORMTemplate(seq = "15", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventComment;
	@CTORMTemplate(seq = "16", name = "lastEventTimekey", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventTimekey;

	public String getFactoryName()
	{
		return factoryName;
	}

	public void setFactoryName(String factoryName)
	{
		this.factoryName = factoryName;
	}

	public String getShieldSpecName()
	{
		return shieldSpecName;
	}

	public void setShieldSpecName(String shieldSpecName)
	{
		this.shieldSpecName = shieldSpecName;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getChamberType()
	{
		return chamberType;
	}

	public void setChamberType(String chamberType)
	{
		this.chamberType = chamberType;
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

	public String getCheckState()
	{
		return checkState;
	}

	public void setCheckState(String checkState)
	{
		this.checkState = checkState;
	}

	public Timestamp getCreateTime()
	{
		return createTime;
	}

	public void setCreateTime(Timestamp createTime)
	{
		this.createTime = createTime;
	}

	public String getCreateUser()
	{
		return createUser;
	}

	public void setCreateUser(String createUser)
	{
		this.createUser = createUser;
	}

	public Timestamp getCheckOutTime()
	{
		return checkOutTime;
	}

	public void setCheckOutTime(Timestamp checkOutTime)
	{
		this.checkOutTime = checkOutTime;
	}

	public String getCheckOutUser()
	{
		return checkOutUser;
	}

	public void setCheckOutUser(String checkOutUser)
	{
		this.checkOutUser = checkOutUser;
	}

	public String getLastEventUser()
	{
		return lastEventUser;
	}

	public void setLastEventUser(String lastEventUser)
	{
		this.lastEventUser = lastEventUser;
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
