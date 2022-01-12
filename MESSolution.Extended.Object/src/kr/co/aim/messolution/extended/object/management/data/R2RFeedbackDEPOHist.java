package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class R2RFeedbackDEPOHist extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="timeKey", type="Key", dataType="String", initial="", history="")
	private String timeKey;	
	
	@CTORMTemplate(seq = "2", name="machineRecipeName", type="Key", dataType="String", initial="", history="")
	private String machineRecipeName;	
	
	@CTORMTemplate(seq = "3", name="unitName", type="Key", dataType="String", initial="", history="")
	private String unitName;
	
	@CTORMTemplate(seq = "4", name="recipeName", type="Key", dataType="String", initial="", history="")
	private String recipeName;
	
	@CTORMTemplate(seq = "5", name="paraName", type="Key", dataType="String", initial="", history="")
	private String paraName;
	
	@CTORMTemplate(seq = "6", name="paraValue", type="Column", dataType="String", initial="", history="")
	private String paraValue;
	
	@CTORMTemplate(seq = "7", name="subUnitName", type="Column", dataType="String", initial="", history="")
	private String subUnitName;
	
	@CTORMTemplate(seq = "8", name="productSpecName", type="Column", dataType="String", initial="", history="")
	private String productSpecName;
	
	@CTORMTemplate(seq = "9", name="mode", type="Column", dataType="String", initial="", history="")
	private String mode;
	
	@CTORMTemplate(seq = "10", name="result", type="Column", dataType="String", initial="", history="")
	private String result;
	
	@CTORMTemplate(seq = "11", name="EventName", type="Column", dataType="String", initial="", history="N")
	private String EventName;
	
	@CTORMTemplate(seq = "12", name="EventUser", type="Column", dataType="String", initial="", history="N")
	private String EventUser;
	
	@CTORMTemplate(seq = "13", name="EventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp EventTime;
	
	@CTORMTemplate(seq = "14", name="EventComment", type="Column", dataType="String", initial="", history="N")
	private String EventComment;
	

	public String getTimeKey()
	{
		return timeKey;
	}

	public void setTimeKey(String timeKey)
	{
		this.timeKey = timeKey;
	}

	public String getMachineRecipeName()
	{
		return machineRecipeName;
	}

	public void setMachineRecipeName(String machineRecipeName)
	{
		this.machineRecipeName = machineRecipeName;
	}

	public String getUnitName()
	{
		return unitName;
	}

	public void setUnitName(String unitName)
	{
		this.unitName = unitName;
	}

	public String getRecipeName()
	{
		return recipeName;
	}

	public void setRecipeName(String recipeName)
	{
		this.recipeName = recipeName;
	}

	public String getParaName()
	{
		return paraName;
	}

	public void setParaName(String paraName)
	{
		this.paraName = paraName;
	}

	public String getParaValue()
	{
		return paraValue;
	}

	public void setParaValue(String paraValue)
	{
		this.paraValue = paraValue;
	}

	public String getSubUnitName()
	{
		return subUnitName;
	}

	public void setSubUnitName(String subUnitName)
	{
		this.subUnitName = subUnitName;
	}

	public String getProductSpecName()
	{
		return productSpecName;
	}

	public void setProductSpecName(String productSpecName)
	{
		this.productSpecName = productSpecName;
	}

	public String getMode()
	{
		return mode;
	}

	public void setMode(String mode)
	{
		this.mode = mode;
	}

	public String getResult()
	{
		return result;
	}

	public void setResult(String result)
	{
		this.result = result;
	}

	public String getEventName()
	{
		return EventName;
	}

	public void setEventName(String eventName)
	{
		EventName = eventName;
	}

	public String getEventUser()
	{
		return EventUser;
	}

	public void setEventUser(String eventUser)
	{
		EventUser = eventUser;
	}

	public Timestamp getEventTime()
	{
		return EventTime;
	}

	public void setEventTime(Timestamp eventTime)
	{
		EventTime = eventTime;
	}

	public String getEventComment()
	{
		return EventComment;
	}

	public void setEventComment(String eventComment)
	{
		EventComment = eventComment;
	}
}
