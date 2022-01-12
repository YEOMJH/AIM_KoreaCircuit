package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class EvaLineWo extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="unitName", type="Key", dataType="String", initial="", history="")
	private String unitName;

	public String getUnitName() {
		return unitName;
	}

	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}

	public String getLineType() {
		return lineType;
	}

	public void setLineType(String lineType) {
		this.lineType = lineType;
	}

	public long getSeq() {
		return seq;
	}

	public void setSeq(long seq) {
		this.seq = seq;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getMachineName() {
		return machineName;
	}

	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}

	public String getProductSpecName() {
		return productSpecName;
	}

	public void setProductSpecName(String productSpecName) {
		this.productSpecName = productSpecName;
	}

	public String getProductRequestName() {
		return productRequestName;
	}

	public void setProductRequestName(String productRequestName) {
		this.productRequestName = productRequestName;
	}

	public String getProcessQuantity() {
		return processQuantity;
	}

	public void setProcessQuantity(String processQuantity) {
		this.processQuantity = processQuantity;
	}

	public Timestamp getPlaneStartDate() {
		return planeStartDate;
	}

	public void setPlaneStartDate(Timestamp planeStartDate) {
		this.planeStartDate = planeStartDate;
	}

	public Timestamp getPlanEndDate() {
		return planEndDate;
	}

	public void setPlanEndDate(Timestamp planEndDate) {
		this.planEndDate = planEndDate;
	}

	public String getScheduleTimeKey() {
		return scheduleTimeKey;
	}

	public void setScheduleTimeKey(String scheduleTimeKey) {
		this.scheduleTimeKey = scheduleTimeKey;
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

	@CTORMTemplate(seq = "2", name="lineType", type="Key", dataType="String", initial="", history="")
	private String lineType;
	
	@CTORMTemplate(seq = "3", name="seq", type="Key", dataType="Number", initial="", history="")
	private long seq;
	
	@CTORMTemplate(seq = "4", name="state", type="Column", dataType="String", initial="", history="")
	private String state;
	
	@CTORMTemplate(seq = "5", name="machineName", type="Column", dataType="String", initial="", history="")
	private String machineName;
	
	@CTORMTemplate(seq = "6", name="productSpecName", type="Column", dataType="String", initial="", history="")
	private String productSpecName;
	
	@CTORMTemplate(seq = "7", name="productRequestName", type="Column", dataType="String", initial="", history="")
	private String productRequestName;
	
	@CTORMTemplate(seq = "8", name="processQuantity", type="Column", dataType="String", initial="", history="")
	private String processQuantity;
	
	@CTORMTemplate(seq = "9", name="planeStartDate", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp planeStartDate;
	
	@CTORMTemplate(seq = "10", name="planEndDate", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp planEndDate;
	
	@CTORMTemplate(seq = "11", name="scheduleTimeKey", type="Column", dataType="String", initial="", history="")
	private String scheduleTimeKey;
	
	@CTORMTemplate(seq = "12", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;

	@CTORMTemplate(seq = "13", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimeKey;

	@CTORMTemplate(seq = "14", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;

	@CTORMTemplate(seq = "15", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;

	@CTORMTemplate(seq = "16", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;
	
}
