package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class SortJob extends UdfAccessor {
	@CTORMTemplate(seq = "1", name = "jobName", type = "Key", dataType = "String", initial = "", history = "")
	private String jobName;

	@CTORMTemplate(seq = "2", name = "jobState", type = "Column", dataType = "String", initial = "", history = "")
	private String jobState;

	@CTORMTemplate(seq = "3", name = "jobType", type = "Column", dataType = "String", initial = "", history = "")
	private String jobType;

	@CTORMTemplate(seq = "4", name = "lastEventTimekey", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventTimekey;

	@CTORMTemplate(seq = "5", name = "lastEventTime", type = "Column", dataType = "Timestamp", initial = "", history = "N")
	private Timestamp lastEventTime;

	@CTORMTemplate(seq = "6", name = "lastEventName", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventName;

	@CTORMTemplate(seq = "7", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventUser;

	@CTORMTemplate(seq = "8", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventComment;

	@CTORMTemplate(seq = "9", name = "createTime", type = "Column", dataType = "Timestamp", initial = "", history = "")
	private Timestamp createTime;

	@CTORMTemplate(seq = "10", name = "createUser", type = "Column", dataType = "String", initial = "", history = "")
	private String createUser;

	@CTORMTemplate(seq = "11", name = "priority", type = "Column", dataType = "Number", initial = "", history = "")
	private Number priority;

	@CTORMTemplate(seq = "12", name = "reasonType", type = "Column", dataType = "String", initial = "", history = "")
	private String reasonType;

	@CTORMTemplate(seq = "13", name = "reasonCode", type = "Column", dataType = "String", initial = "", history = "")
	private String reasonCode;

	public String getJobName()
	{
		return jobName;
	}

	public void setJobName(String jobName)
	{
		this.jobName = jobName;
	}

	public String getJobState()
	{
		return jobState;
	}

	public void setJobState(String jobState)
	{
		this.jobState = jobState;
	}

	public String getJobType()
	{
		return jobType;
	}

	public void setJobType(String jobType)
	{
		this.jobType = jobType;
	}

	public String getLastEventTimekey()
	{
		return lastEventTimekey;
	}

	public void setLastEventTimekey(String lastEventTimekey)
	{
		this.lastEventTimekey = lastEventTimekey;
	}

	public Timestamp getLastEventTime()
	{
		return lastEventTime;
	}

	public void setLastEventTime(Timestamp lastEventTime)
	{
		this.lastEventTime = lastEventTime;
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

	public Number getPriority()
	{
		return priority;
	}

	public void setPriority(Number priority)
	{
		this.priority = priority;
	}

	public String getReasonType()
	{
		return reasonType;
	}

	public void setReasonType(String reasonType)
	{
		this.reasonType = reasonType;
	}

	public String getReasonCode()
	{
		return reasonCode;
	}

	public void setReasonCode(String reasonCode)
	{
		this.reasonCode = reasonCode;
	}

}
