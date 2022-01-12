package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class RunBanPreventRule extends UdfAccessor {

	@CTORMTemplate(seq = "1", name="factoryName", type="Key", dataType="String", initial="", history="N")
	private String factoryName;
	
	@CTORMTemplate(seq = "2", name="processFlowType", type="Key", dataType="String", initial="", history="N")
	private String processFlowType;
	
	@CTORMTemplate(seq = "3", name="processOperationName", type="Key", dataType="String", initial="", history="N")
	private String processOperationName;
	
	@CTORMTemplate(seq = "4", name="processOperationVersion", type="Key", dataType="String", initial="", history="N")
	private String processOperationVersion;
	
	@CTORMTemplate(seq = "5", name="machineName", type="Key", dataType="String", initial="", history="N")
	private String machineName;
	
	@CTORMTemplate(seq = "6", name="flag", type="Key", dataType="String", initial="", history="N")
	private String flag;
	
	@CTORMTemplate(seq = "7", name="value", type="Column", dataType="String", initial="", history="N")
	private String value;

	@CTORMTemplate(seq = "8", name = "lastEventTimeKey", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventTimeKey;
	
	@CTORMTemplate(seq = "9", name = "lastEventUser", type = "Column", dataType = "Timestamp", initial = "", history = "N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "10", name = "lastEventTime", type = "Column", dataType = "String", initial = "", history = "N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "11", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventComment;
	
	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getLastEventUser() {
		return lastEventUser;
	}

	public void setLastEventUser(String lastEventUser) {
		this.lastEventUser = lastEventUser;
	}

	public String getFactoryName()
	{
		return factoryName;
	}

	public void setFactoryName(String factoryName)
	{
		this.factoryName = factoryName;
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

	public String getMachineName()
	{
		return machineName;
	}

	public void setMachineName(String machineName)
	{
		this.machineName = machineName;
	}

	public String getLastEventTimeKey()
	{
		return lastEventTimeKey;
	}

	public void setLastEventTimeKey(String lastEventTimeKey)
	{
		this.lastEventTimeKey = lastEventTimeKey;
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

	public String getProcessFlowType() {
		return processFlowType;
	}

	public void setProcessFlowType(String processFlowType) {
		this.processFlowType = processFlowType;
	}
	
}
