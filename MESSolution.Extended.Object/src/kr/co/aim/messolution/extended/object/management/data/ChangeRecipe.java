package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class ChangeRecipe extends UdfAccessor{
	@CTORMTemplate(seq = "1", name="factoryName", type="Key", dataType="String", initial="", history="")
	private String factoryName;
	@CTORMTemplate(seq = "2", name="productSpecName", type="Key", dataType="String", initial="", history="")
	private String productSpecName;
	@CTORMTemplate(seq = "3", name="processFlowName", type="Key", dataType="String", initial="", history="")
	private String processFlowName;
	@CTORMTemplate(seq = "4", name="processOperationName", type="Key", dataType="String", initial="", history="")
	private String processOperationName;
	@CTORMTemplate(seq = "5", name="machineName", type="Key", dataType="String", initial="", history="")
	private String machineName;
	@CTORMTemplate(seq = "6", name="afterProcessOperName", type="Key", dataType="String", initial="", history="")
	private String afterProcessOperName;
	@CTORMTemplate(seq = "7", name="afterRecipeName", type="Column", dataType="String", initial="", history="")
	private String afterRecipeName;
	@CTORMTemplate(seq = "8", name="changedTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp changedTime;
	@CTORMTemplate(seq = "9", name="lastEventUser", type="Column", dataType="String", initial="", history="")
	private String lastEventUser;
	@CTORMTemplate(seq = "10", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp lastEventTime;
	@CTORMTemplate(seq = "11", name="lasteventTimeKey", type="Column", dataType="String", initial="", history="")
	private String lasteventTimeKey;
	@CTORMTemplate(seq = "12", name="lastEventName", type="Column", dataType="String", initial="", history="")
	private String lastEventName;
	@CTORMTemplate(seq = "13", name="lastEventComment", type="Column", dataType="String", initial="", history="")
	private String lastEventComment;
	
	//instantiation
	public ChangeRecipe()
	{
			
	}
		
	public String getFactoryName() {
		return factoryName;
	}
	public void setFactoryName(String factoryName) {
		this.factoryName = factoryName;
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
	public void setProcessFlowName(String processFlowName) {
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

	public String getMachineName() {
		return machineName;
	}
	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}
	public String getAfterProcessOperName() {
		return afterProcessOperName;
	}
	public void setAfterProcessOperName(String afterProcessOperName) {
		this.afterProcessOperName = afterProcessOperName;
	}
	public String getAfterRecipeName() {
		return afterRecipeName;
	}
	public void setAfterRecipeName(String afterRecipeName) {
		this.afterRecipeName = afterRecipeName;
	}
	public Timestamp getChangedTime() {
		return changedTime;
	}
	public void setChangedTime(Timestamp changedTime) {
		this.changedTime = changedTime;
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
	
}
