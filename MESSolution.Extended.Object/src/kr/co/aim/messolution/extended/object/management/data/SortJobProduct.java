package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class SortJobProduct extends UdfAccessor {
	@CTORMTemplate(seq = "1", name = "jobName", type = "Key", dataType = "String", initial = "", history = "")
	private String jobName;

	@CTORMTemplate(seq = "2", name = "productName", type = "Key", dataType = "String", initial = "", history = "")
	private String productName;

	@CTORMTemplate(seq = "3", name = "machineName", type = "Column", dataType = "String", initial = "", history = "")
	private String machineName;

	@CTORMTemplate(seq = "4", name = "fromLotName", type = "Column", dataType = "String", initial = "", history = "")
	private String fromLotName;

	@CTORMTemplate(seq = "5", name = "fromCarrierName", type = "Column", dataType = "String", initial = "", history = "")
	private String fromCarrierName;

	@CTORMTemplate(seq = "6", name = "fromPortName", type = "Column", dataType = "String", initial = "", history = "")
	private String fromPortName;

	@CTORMTemplate(seq = "7", name = "fromPosition", type = "Column", dataType = "String", initial = "", history = "")
	private String fromPosition;

	@CTORMTemplate(seq = "8", name = "toLotName", type = "Column", dataType = "String", initial = "", history = "")
	private String toLotName;

	@CTORMTemplate(seq = "9", name = "toCarrierName", type = "Column", dataType = "String", initial = "", history = "")
	private String toCarrierName;

	@CTORMTemplate(seq = "10", name = "toPortName", type = "Column", dataType = "String", initial = "", history = "")
	private String toPortName;

	@CTORMTemplate(seq = "11", name = "toPosition", type = "Column", dataType = "String", initial = "", history = "")
	private String toPosition;

	@CTORMTemplate(seq = "12", name = "sortProductState", type = "Column", dataType = "String", initial = "", history = "")
	private String sortProductState;

	@CTORMTemplate(seq = "13", name = "turnFlag", type = "Column", dataType = "String", initial = "", history = "")
	private String turnFlag;

	@CTORMTemplate(seq = "14", name = "scrapFlag", type = "Column", dataType = "String", initial = "", history = "")
	private String scrapFlag;
	
	@CTORMTemplate(seq = "15", name = "cutFlag", type = "Column", dataType = "String", initial = "", history = "")
	private String cutFlag;

	@CTORMTemplate(seq = "16", name = "turnDegree", type = "Column", dataType = "String", initial = "", history = "")
	private String turnDegree;

	@CTORMTemplate(seq = "17", name = "slotPosition", type = "Column", dataType = "String", initial = "", history = "")
	private String slotPosition;

	@CTORMTemplate(seq = "18", name = "fromSlotPosition", type = "Column", dataType = "String", initial = "", history = "")
	private String fromSlotPosition;

	@CTORMTemplate(seq = "19", name = "toSlotPosition", type = "Column", dataType = "String", initial = "", history = "")
	private String toSlotPosition;

	@CTORMTemplate(seq = "20", name = "reasonType", type = "Column", dataType = "String", initial = "", history = "")
	private String reasonType;

	@CTORMTemplate(seq = "21", name = "reasonCode", type = "Column", dataType = "String", initial = "", history = "")
	private String reasonCode;

	@CTORMTemplate(seq = "22", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventComment;

	@CTORMTemplate(seq = "23", name = "lastEventName", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventName;

	@CTORMTemplate(seq = "24", name = "lastEventTime", type = "Column", dataType = "Timestamp", initial = "", history = "N")
	private Timestamp lastEventTime;

	@CTORMTemplate(seq = "25", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventUser;

	public String getJobName()
	{
		return jobName;
	}

	public void setJobName(String jobName)
	{
		this.jobName = jobName;
	}

	public String getProductName()
	{
		return productName;
	}

	public void setProductName(String productName)
	{
		this.productName = productName;
	}

	public String getMachineName()
	{
		return machineName;
	}

	public void setMachineName(String machineName)
	{
		this.machineName = machineName;
	}

	public String getFromLotName()
	{
		return fromLotName;
	}

	public void setFromLotName(String fromLotName)
	{
		this.fromLotName = fromLotName;
	}

	public String getFromCarrierName()
	{
		return fromCarrierName;
	}

	public void setFromCarrierName(String fromCarrierName)
	{
		this.fromCarrierName = fromCarrierName;
	}

	public String getFromPortName()
	{
		return fromPortName;
	}

	public void setFromPortName(String fromPortName)
	{
		this.fromPortName = fromPortName;
	}

	public String getFromPosition()
	{
		return fromPosition;
	}

	public void setFromPosition(String fromPosition)
	{
		this.fromPosition = fromPosition;
	}

	public String getToLotName()
	{
		return toLotName;
	}

	public void setToLotName(String toLotName)
	{
		this.toLotName = toLotName;
	}

	public String getToCarrierName()
	{
		return toCarrierName;
	}

	public void setToCarrierName(String toCarrierName)
	{
		this.toCarrierName = toCarrierName;
	}

	public String getToPortName()
	{
		return toPortName;
	}

	public void setToPortName(String toPortName)
	{
		this.toPortName = toPortName;
	}

	public String getToPosition()
	{
		return toPosition;
	}

	public void setToPosition(String toPosition)
	{
		this.toPosition = toPosition;
	}

	public String getSortProductState()
	{
		return sortProductState;
	}

	public void setSortProductState(String sortProductState)
	{
		this.sortProductState = sortProductState;
	}

	public String getTurnFlag()
	{
		return turnFlag;
	}

	public void setTurnFlag(String turnFlag)
	{
		this.turnFlag = turnFlag;
	}

	public String getScrapFlag()
	{
		return scrapFlag;
	}

	public void setScrapFlag(String scrapFlag)
	{
		this.scrapFlag = scrapFlag;
	}

	public String getCutFlag()
	{
		return cutFlag;
	}

	public void setCutFlag(String cutFlag)
	{
		this.cutFlag = cutFlag;
	}

	public String getTurnDegree()
	{
		return turnDegree;
	}

	public void setTurnDegree(String turnDegree)
	{
		this.turnDegree = turnDegree;
	}

	public String getSlotPosition()
	{
		return slotPosition;
	}

	public void setSlotPosition(String slotPosition)
	{
		this.slotPosition = slotPosition;
	}

	public String getFromSlotPosition()
	{
		return fromSlotPosition;
	}

	public void setFromSlotPosition(String fromSlotPosition)
	{
		this.fromSlotPosition = fromSlotPosition;
	}

	public String getToSlotPosition()
	{
		return toSlotPosition;
	}

	public void setToSlotPosition(String toSlotPosition)
	{
		this.toSlotPosition = toSlotPosition;
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

	public String getLastEventComment()
	{
		return lastEventComment;
	}

	public void setLastEventComment(String lastEventComment)
	{
		this.lastEventComment = lastEventComment;
	}

	public String getLastEventName()
	{
		return lastEventName;
	}

	public void setLastEventName(String lastEventName)
	{
		this.lastEventName = lastEventName;
	}

	public Timestamp getLastEventTime()
	{
		return lastEventTime;
	}

	public void setLastEventTime(Timestamp lastEventTime)
	{
		this.lastEventTime = lastEventTime;
	}

	public String getLastEventUser()
	{
		return lastEventUser;
	}

	public void setLastEventUser(String lastEventUser)
	{
		this.lastEventUser = lastEventUser;
	}

}
