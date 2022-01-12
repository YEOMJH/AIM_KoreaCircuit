package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class ReserveMaskToEQP extends UdfAccessor {
	@CTORMTemplate(seq = "1", name = "machineName", type = "Key", dataType = "String", initial = "", history = "")
	private String machineName;

	@CTORMTemplate(seq = "2", name = "unitName", type = "Key", dataType = "String", initial = "", history = "")
	private String unitName;

	@CTORMTemplate(seq = "3", name = "subUnitName", type = "Key", dataType = "String", initial = "", history = "")
	private String subUnitName;

	@CTORMTemplate(seq = "4", name = "carrierName", type = "Key", dataType = "String", initial = "", history = "")
	private String carrierName;

	@CTORMTemplate(seq = "5", name = "position", type = "Key", dataType = "String", initial = "", history = "")
	private String position;

	@CTORMTemplate(seq = "6", name = "maskLotName", type = "Column", dataType = "String", initial = "", history = "")
	private String maskLotName;

	@CTORMTemplate(seq = "7", name = "lastEventName", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventName;

	@CTORMTemplate(seq = "8", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventUser;

	@CTORMTemplate(seq = "9", name = "lastEventTime", type = "Column", dataType = "Timestamp", initial = "", history = "N")
	private Timestamp lastEventTime;

	@CTORMTemplate(seq = "10", name = "lastEventTimekey", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventTimekey;

	@CTORMTemplate(seq = "11", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventComment;
	
	@CTORMTemplate(seq = "12", name = "msName", type = "Column", dataType = "String", initial = "", history = "")
	private String msName;

	@CTORMTemplate(seq = "13", name = "portName", type = "Column", dataType = "String", initial = "", history = "")
	private String portName;
	
	@CTORMTemplate(seq = "14", name = "lineType", type = "Column", dataType = "String", initial = "", history = "")
	private String lineType;
	
	@CTORMTemplate(seq = "15", name = "maskGroupName", type = "Column", dataType = "String", initial = "", history = "")
	private String maskGroupName;
	
	public String getMachineName()
	{
		return machineName;
	}

	public void setMachineName(String machineName)
	{
		this.machineName = machineName;
	}

	public String getUnitName()
	{
		return unitName;
	}

	public void setUnitName(String unitName)
	{
		this.unitName = unitName;
	}

	public String getSubUnitName()
	{
		return subUnitName;
	}

	public void setSubUnitName(String subUnitName)
	{
		this.subUnitName = subUnitName;
	}

	public String getCarrierName()
	{
		return carrierName;
	}

	public void setCarrierName(String carrierName)
	{
		this.carrierName = carrierName;
	}

	public String getPosition()
	{
		return position;
	}

	public void setPosition(String position)
	{
		this.position = position;
	}

	public String getMaskLotName()
	{
		return maskLotName;
	}

	public void setMaskLotName(String maskLotName)
	{
		this.maskLotName = maskLotName;
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

	public Timestamp getLastEventTime()
	{
		return lastEventTime;
	}

	public void setLastEventTime(Timestamp lastEventTime)
	{
		this.lastEventTime = lastEventTime;
	}

	public String getLastEventTimekey()
	{
		return lastEventTimekey;
	}

	public void setLastEventTimekey(String lastEventTimekey)
	{
		this.lastEventTimekey = lastEventTimekey;
	}

	public String getLastEventComment()
	{
		return lastEventComment;
	}

	public void setLastEventComment(String lastEventComment)
	{
		this.lastEventComment = lastEventComment;
	}
	
	public String getMsName() {
		return msName;
	}

	public void setMsName(String msName) {
		this.msName = msName;
	}

	public String getPortName() {
		return portName;
	}

	public void setPortName(String portName) {
		this.portName = portName;
	}

	public String getLineType() {
		return lineType;
	}

	public void setLineType(String lineType) {
		this.lineType = lineType;
	}

	public String getMaskGroupName() {
		return maskGroupName;
	}

	public void setMaskGroupName(String maskGroupName) {
		this.maskGroupName = maskGroupName;
	}
}
