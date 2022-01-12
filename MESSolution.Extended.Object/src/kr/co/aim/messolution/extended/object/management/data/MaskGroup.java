package kr.co.aim.messolution.extended.object.management.data;


import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class MaskGroup extends UdfAccessor{

	@CTORMTemplate(seq = "1", name="MaskLotName", type="Key", dataType="String", initial="", history="")
	private String MaskLotName;	
	
	@CTORMTemplate(seq = "2", name="MaskGroupName", type="Column", dataType="String", initial="", history="")
	private String MaskGroupName;
	
	@CTORMTemplate(seq = "3", name="MachineName", type="Column", dataType="String", initial="", history="")
	private String MachineName;
	
	@CTORMTemplate(seq = "4", name="UnitName", type="Column", dataType="String", initial="", history="")
	private String UnitName;
	
	@CTORMTemplate(seq = "5", name="MSName", type="Column", dataType="String", initial="", history="")
	private String MSName;
	
	@CTORMTemplate(seq = "6", name="PortName", type="Column", dataType="String", initial="", history="")
	private String PortName;
	
	@CTORMTemplate(seq = "7", name="SubUnitName", type="Column", dataType="String", initial="", history="")
	private String SubUnitName;
	
	@CTORMTemplate(seq = "8", name="LineType", type="Column", dataType="String", initial="", history="")
	private String LineType;
	
	@CTORMTemplate(seq = "9", name="CSTSlot", type="Column", dataType="String", initial="", history="")
	private String CSTSlot;
	
	@CTORMTemplate(seq = "10", name="LastEventName", type="Column", dataType="String", initial="", history="N")
	private String LastEventName;
	
	@CTORMTemplate(seq = "11", name="lastEventTimekey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimekey;
	
	@CTORMTemplate(seq = "12", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "13", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "14", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;
	
	public String getMaskLotName() {
		return MaskLotName;
	}

	public void setMaskLotName(String maskLotName) {
		MaskLotName = maskLotName;
	}

	public String getMaskGroupName() {
		return MaskGroupName;
	}

	public void setMaskGroupName(String maskGroupName) {
		MaskGroupName = maskGroupName;
	}

	public String getMachineName() {
		return MachineName;
	}

	public void setMachineName(String machineName) {
		MachineName = machineName;
	}

	public String getUnitName() {
		return UnitName;
	}

	public void setUnitName(String unitName) {
		UnitName = unitName;
	}

	public String getMSName() {
		return MSName;
	}

	public void setMSName(String mSName) {
		MSName = mSName;
	}

	public String getPortName() {
		return PortName;
	}

	public void setPortName(String portName) {
		PortName = portName;
	}

	public String getSubUnitName() {
		return SubUnitName;
	}

	public void setSubUnitName(String subUnitName) {
		SubUnitName = subUnitName;
	}

	public String getLineType() {
		return LineType;
	}

	public void setLineType(String lineType) {
		LineType = lineType;
	}

	public String getCSTSlot() {
		return CSTSlot;
	}

	public void setCSTSlot(String cSTSlot) {
		CSTSlot = cSTSlot;
	}

	public String getLastEventName() {
		return LastEventName;
	}

	public void setLastEventName(String lastEventName) {
		LastEventName = lastEventName;
	}

	public String getLastEventTimekey() {
		return lastEventTimekey;
	}

	public void setLastEventTimekey(String lastEventTimekey) {
		this.lastEventTimekey = lastEventTimekey;
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
