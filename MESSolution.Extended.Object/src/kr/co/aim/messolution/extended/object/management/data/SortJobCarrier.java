package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class SortJobCarrier extends UdfAccessor {
	@CTORMTemplate(seq = "1", name = "jobName", type = "Key", dataType = "String", initial = "", history = "")
	private String jobName;

	@CTORMTemplate(seq = "2", name = "carrierName", type = "Key", dataType = "String", initial = "", history = "")
	private String carrierName;

	@CTORMTemplate(seq = "3", name = "lotName", type = "Column", dataType = "String", initial = "", history = "")
	private String lotName;

	@CTORMTemplate(seq = "4", name = "machineName", type = "Column", dataType = "String", initial = "", history = "")
	private String machineName;

	@CTORMTemplate(seq = "5", name = "portName", type = "Column", dataType = "String", initial = "", history = "")
	private String portName;

	@CTORMTemplate(seq = "6", name = "transferDirection", type = "Column", dataType = "String", initial = "", history = "")
	private String transferDirection;

	@CTORMTemplate(seq = "7", name = "loadFlag", type = "Column", dataType = "String", initial = "", history = "")
	private String loadFlag;

	@CTORMTemplate(seq = "8", name = "loadTimekey", type = "Column", dataType = "String", initial = "", history = "")
	private String loadTimekey;

	@CTORMTemplate(seq = "9", name = "lastEventName", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventName;

	@CTORMTemplate(seq = "10", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventUser;

	@CTORMTemplate(seq = "11", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventComment;

	@CTORMTemplate(seq = "12", name = "lastEventTime", type = "Column", dataType = "Timestamp", initial = "", history = "N")
	private Timestamp lastEventTime;

	public String getJobName()
	{
		return jobName;
	}

	public void setJobName(String jobName)
	{
		this.jobName = jobName;
	}

	public String getCarrierName()
	{
		return carrierName;
	}

	public void setCarrierName(String carrierName)
	{
		this.carrierName = carrierName;
	}

	public String getLotName()
	{
		return lotName;
	}

	public void setLotName(String lotName)
	{
		this.lotName = lotName;
	}

	public String getMachineName()
	{
		return machineName;
	}

	public void setMachineName(String machineName)
	{
		this.machineName = machineName;
	}

	public String getPortName()
	{
		return portName;
	}

	public void setPortName(String portName)
	{
		this.portName = portName;
	}

	public String getTransferDirection()
	{
		return transferDirection;
	}

	public void setTransferDirection(String transferDirection)
	{
		this.transferDirection = transferDirection;
	}

	public String getLoadFlag()
	{
		return loadFlag;
	}

	public void setLoadFlag(String loadFlag)
	{
		this.loadFlag = loadFlag;
	}

	public String getLoadTimekey()
	{
		return loadTimekey;
	}

	public void setLoadTimekey(String loadTimekey)
	{
		this.loadTimekey = loadTimekey;
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

	public String getLastEventComment()
	{
		return lastEventComment;
	}

	public void setLastEventComment(String lastEventComment)
	{
		this.lastEventComment = lastEventComment;
	}

	public Timestamp getLastEventTime()
	{
		return lastEventTime;
	}

	public void setLastEventTime(Timestamp lastEventTime)
	{
		this.lastEventTime = lastEventTime;
	}
}
