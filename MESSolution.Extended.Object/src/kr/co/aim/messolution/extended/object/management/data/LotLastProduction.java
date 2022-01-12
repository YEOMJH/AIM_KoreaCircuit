package kr.co.aim.messolution.extended.object.management.data;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class LotLastProduction extends UdfAccessor
{
	@CTORMTemplate(seq = "1", name="lotName", type="Key", dataType="String", initial="", history="")
	private String lotName;
	
	@CTORMTemplate(seq = "2", name="processOperation", type="Key", dataType="String", initial="", history="")
	private String processOperation;
	
	@CTORMTemplate(seq = "3", name="processFlow", type="Column", dataType="String", initial="", history="")
	private String processFlow;
	
	@CTORMTemplate(seq = "4", name="machineName", type="Column", dataType="String", initial="", history="")
	private String machineName;
	
	@CTORMTemplate(seq = "5", name="lastEventName", type="Column", dataType="String", initial="", history="")
	private String lastEventName;
	
	@CTORMTemplate(seq = "6", name="lastEventUser", type="Column", dataType="String", initial="", history="")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "7", name="lastEventTimekey", type="Column", dataType="String", initial="", history="")
	private String lastEventTimekey;

	public LotLastProduction()
	{
		
	}
	
	public LotLastProduction(String lotName, String processOperation, String processFlow, String machineName, String lastEventName, String lastEventUser, String lastEventTimekey)
	{
		setLotName(lotName);
		setProcessOperation(processOperation);
		setProcessFlow(processFlow);
		setMachineName(machineName);
		setLastEventName(lastEventName);
		setLastEventUser(lastEventUser);
		setLastEventTimekey(lastEventTimekey);
	}
	
	public String getLotName() {
		return lotName;
	}

	public void setLotName(String lotName) {
		this.lotName = lotName;
	}

	public String getProcessOperation() {
		return processOperation;
	}

	public void setProcessOperation(String processOperation) {
		this.processOperation = processOperation;
	}

	public String getProcessFlow() {
		return processFlow;
	}

	public void setProcessFlow(String processFlow) {
		this.processFlow = processFlow;
	}

	public String getMachineName() {
		return machineName;
	}

	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}

	public String getLastEventName() {
		return lastEventName;
	}

	public void setLastEventName(String lastEventName) {
		this.lastEventName = lastEventName;
	}

	public String getLastEventUser() {
		return lastEventUser;
	}

	public void setLastEventUser(String lastEventUser) {
		this.lastEventUser = lastEventUser;
	}

	public String getLastEventTimekey() {
		return lastEventTimekey;
	}

	public void setLastEventTimekey(String lastEventTimekey) {
		this.lastEventTimekey = lastEventTimekey;
	}
	

}