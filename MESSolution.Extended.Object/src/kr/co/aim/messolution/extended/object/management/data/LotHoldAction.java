package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class LotHoldAction extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="factoryName", type="Key", dataType="String", initial="", history="")
	private String factoryName;	
	
	@CTORMTemplate(seq = "2", name="processFlowType", type="Key", dataType="String", initial="", history="")
	private String processFlowType;	
	
	@CTORMTemplate(seq = "3", name="detailProcessOperationType", type="Key", dataType="String", initial="", history="")
	private String detailProcessOperationType;	
	
	@CTORMTemplate(seq = "4", name="beforeHold", type="Column", dataType="String", initial="", history="")
	private String beforeHold;
	
	@CTORMTemplate(seq = "5", name="afterHold", type="Column", dataType="String", initial="", history="")
	private String afterHold;
	
	@CTORMTemplate(seq = "6", name="lastEventName", type="Column", dataType="String", initial="", history="")
	private String lastEventName;
	
	@CTORMTemplate(seq = "7", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="")
	private String lastEventTimeKey;
	
	@CTORMTemplate(seq = "8", name="lastEventUser", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "9", name="lastEventUser", type="Column", dataType="String", initial="", history="")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "10", name="lastEventComment", type="Column", dataType="String", initial="", history="")
	private String lastEventComment;
	
	//instantiation
	public LotHoldAction()
	{
		
	}

	public String getFactoryName() {
		return factoryName;
	}

	public void setFactoryName(String factoryName) {
		this.factoryName = factoryName;
	}

	public String getProcessFlowType() {
		return processFlowType;
	}

	public void setProcessFlowType(String processFlowType) {
		this.processFlowType = processFlowType;
	}

	public String getDetailProcessOperationType() {
		return detailProcessOperationType;
	}

	public void setDetailProcessOperationType(String detailProcessOperationType) {
		this.detailProcessOperationType = detailProcessOperationType;
	}

	public String getBeforeHold() {
		return beforeHold;
	}

	public void setBeforeHold(String beforeHold) {
		this.beforeHold = beforeHold;
	}

	public String getAfterHold() {
		return afterHold;
	}

	public void setAfterHold(String afterHold) {
		this.afterHold = afterHold;
	}

	public String getLastEventName() {
		return lastEventName;
	}

	public void setLastEventName(String lastEventName) {
		this.lastEventName = lastEventName;
	}

	public String getLastEventTimeKey() {
		return lastEventTimeKey;
	}

	public void setLastEventTimeKey(String lastEventTimeKey) {
		this.lastEventTimeKey = lastEventTimeKey;
	}

	public Timestamp getLastEventTime() {
		return lastEventTime;
	}

	public void setLastEventTime(Timestamp lastEventTime) {
		this.lastEventTime = lastEventTime;
	}

	public String getLastEventUser() {
		return lastEventUser;
	}

	public void setLastEventUser(String lastEventUser) {
		this.lastEventUser = lastEventUser;
	}

	public String getLastEventComment() {
		return lastEventComment;
	}

	public void setLastEventComment(String lastEventComment) {
		this.lastEventComment = lastEventComment;
	}
}
