package kr.co.aim.messolution.extended.object.management.data;
import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class EvaLineSchedule extends UdfAccessor{
	
	@CTORMTemplate(seq = "1", name="machineName", type="Key", dataType="String", initial="", history="")
	private String machineName;
	
	@CTORMTemplate(seq = "2", name="lineType", type="Key", dataType="String", initial="", history="")
	private String lineType;

	@CTORMTemplate(seq = "3", name="productSpecName", type="Key", dataType="String", initial="", history="")
	private String productSpecName;

	@CTORMTemplate(seq = "4", name="seq", type="Key", dataType="Number", initial="", history="")
	private long seq;

	@CTORMTemplate(seq = "5", name="productRequestName", type="Key", dataType="String", initial="", history="")
	private String productRequestName;

	@CTORMTemplate(seq = "6", name="state", type="Column", dataType="String", initial="", history="")
	private String state;

	@CTORMTemplate(seq = "7", name="planQuantity", type="Column", dataType="Number", initial="", history="")
	private long planQuantity;

	@CTORMTemplate(seq = "8", name="planStartDate", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp planStartDate;

	@CTORMTemplate(seq = "9", name="planEndDate", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp planEndDate;

	@CTORMTemplate(seq = "10", name="useFlag", type="Column", dataType="String", initial="", history="")
	private String useFlag;
	
	@CTORMTemplate(seq = "11", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;

	@CTORMTemplate(seq = "12", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimeKey;

	@CTORMTemplate(seq = "13", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;

	@CTORMTemplate(seq = "14", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;

	@CTORMTemplate(seq = "15", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;
	
	@CTORMTemplate(seq = "16", name="scheduleTimeKey", type="Column", dataType="String", initial="", history="")
	private String scheduleTimeKey;

	public String getMachineName() {
		return machineName;
	}

	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}

	public String getLineType() {
		return lineType;
	}

	public void setLineType(String lineType) {
		this.lineType = lineType;
	}

	public String getProductSpecName() {
		return productSpecName;
	}

	public void setProductSpecName(String productSpecName) {
		this.productSpecName = productSpecName;
	}

	public long getSeq() {
		return seq;
	}

	public void setSeq(long seq) {
		this.seq = seq;
	}

	public String getProductRequestName() {
		return productRequestName;
	}

	public void setProductRequestName(String productRequestName) {
		this.productRequestName = productRequestName;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public long getPlanQuantity() {
		return planQuantity;
	}

	public void setPlanQuantity(long planQuantity) {
		this.planQuantity = planQuantity;
	}

	public Timestamp getPlanStartDate() {
		return planStartDate;
	}

	public void setPlanStartDate(Timestamp planStartDate) {
		this.planStartDate = planStartDate;
	}

	public Timestamp getPlanEndDate() {
		return planEndDate;
	}

	public void setPlanEndDate(Timestamp planEndDate) {
		this.planEndDate = planEndDate;
	}

	public String getUseFlag() {
		return useFlag;
	}

	public void setUseFlag(String useFlag) {
		this.useFlag = useFlag;
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

	public String getScheduleTimeKey() {
		return scheduleTimeKey;
	}

	public void setScheduleTimeKey(String scheduleTimeKey) {
		this.scheduleTimeKey = scheduleTimeKey;
	}

	
}
