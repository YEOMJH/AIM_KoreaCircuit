package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class AbnormalEQPCommand extends UdfAccessor{
	@CTORMTemplate(seq = "1", name="abnormalName", type="Key", dataType="String", initial="", history="")
	private String abnormalName;	
	
	@CTORMTemplate(seq = "4", name="commandTime", type="Key", dataType="Timestamp", initial="", history="")
	private Timestamp commandTime;
	
	@CTORMTemplate(seq = "2", name="abnormalEQPName", type="Column", dataType="String", initial="", history="")
	private String abnormalEQPName;
	
	@CTORMTemplate(seq = "3", name="command", type="Column", dataType="String", initial="", history="")
	private String command;
	
	@CTORMTemplate(seq = "5", name="commandReply", type="Column", dataType="String", initial="", history="")
	private String commandReply;
	
	@CTORMTemplate(seq = "6", name="commandReplyTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp commandReplyTime;
	
	@CTORMTemplate(seq = "7", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;
	
	@CTORMTemplate(seq = "8", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;
	
	@CTORMTemplate(seq = "9", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimeKey;
	
	@CTORMTemplate(seq = "10", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "11", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;

	@CTORMTemplate(seq = "12", name="department", type="Column", dataType="String", initial="", history="")
	private String department;
	
	//instantiation
	public AbnormalEQPCommand()
	{
		
	}
	
	public String getAbnormalName() {
		return abnormalName;
	}

	public void setAbnormalName(String abnormalName) {
		this.abnormalName = abnormalName;
	}

	public String getAbnormalEQPName() {
		return abnormalEQPName;
	}

	public void setAbnormalEQPName(String abnormalEQPName) {
		this.abnormalEQPName = abnormalEQPName;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public Timestamp getCommandTime() {
		return commandTime;
	}

	public void setCommandTime(Timestamp commandTime) {
		this.commandTime = commandTime;
	}

	public String getCommandReply() {
		return commandReply;
	}

	public void setCommandReply(String commandReply) {
		this.commandReply = commandReply;
	}

	public Timestamp getCommandReplyTime() {
		return commandReplyTime;
	}

	public void setCommandReplyTime(Timestamp commandReplyTime) {
		this.commandReplyTime = commandReplyTime;
	}

	public String getLastEventName() {
		return lastEventName;
	}

	public void setLastEventName(String lastEventName) {
		this.lastEventName = lastEventName;
	}

	public String getLastEventComment() {
		return lastEventComment;
	}

	public void setLastEventComment(String lastEventComment) {
		this.lastEventComment = lastEventComment;
	}

	public String getLastEventTimeKey() {
		return lastEventTimeKey;
	}

	public void setLastEventTimeKey(String lastEventTimeKey) {
		this.lastEventTimeKey = lastEventTimeKey;
	}

	public String getLastEventUser() {
		return lastEventUser;
	}

	public void setLastEventUser(String lastEventUser) {
		this.lastEventUser = lastEventUser;
	}

	public Timestamp getLastEventTime() {
		return lastEventTime;
	}

	public void setLastEventTime(Timestamp lastEventTime) {
		this.lastEventTime = lastEventTime;
	}
	
	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}
}
