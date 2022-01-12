package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class MaskMultiHold extends UdfAccessor {
	@CTORMTemplate(seq = "1", name = "maskLotName", type = "Key", dataType = "String", initial = "", history = "")
	private String maskLotName;

	@CTORMTemplate(seq = "2", name = "factoryName", type = "Key", dataType = "String", initial = "", history = "")
	private String factoryName;
	
	@CTORMTemplate(seq = "3", name = "reasonCode", type = "Key", dataType = "String", initial = "", history = "")
	private String reasonCode;

	@CTORMTemplate(seq = "4", name = "maskProcessOperationName", type = "String", dataType = "String", initial = "", history = "")
	private String maskProcessOperationName;

	@CTORMTemplate(seq = "5", name = "maskProcessOperationVersion", type = "String", dataType = "String", initial = "", history = "")
	private String maskProcessOperationVersion;

	@CTORMTemplate(seq = "6", name = "reasonCodeType", type = "String", dataType = "String", initial = "", history = "")
	private String reasonCodeType;


	@CTORMTemplate(seq = "7", name = "lastEventTime", type = "Column", dataType = "Timestamp", initial = "", history = "N")
	private Timestamp lastEventTime;

	@CTORMTemplate(seq = "8", name = "lastEventName", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventName;

	@CTORMTemplate(seq = "9", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventUser;

	@CTORMTemplate(seq = "10", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventComment;

	public MaskMultiHold()
	{
	}

	public MaskMultiHold(String maskLotName, String factoryName, String maskProcessOperationName,
			String maskProcessOperationVersion, String reasonCodeType, String reasonCode, Timestamp lastEventTime,
			String lastEventName, String lastEventUser, String lastEventComment)
	{
		super();
		this.maskLotName = maskLotName;
		this.factoryName = factoryName;
		this.maskProcessOperationName = maskProcessOperationName;
		this.maskProcessOperationVersion = maskProcessOperationVersion;
		this.reasonCodeType = reasonCodeType;
		this.reasonCode = reasonCode;
		this.lastEventTime = lastEventTime;
		this.lastEventName = lastEventName;
		this.lastEventUser = lastEventUser;
		this.lastEventComment = lastEventComment;
	}

	public String getMaskLotName()
	{
		return maskLotName;
	}

	public void setMaskLotName(String maskLotName)
	{
		this.maskLotName = maskLotName;
	}

	public String getFactoryName()
	{
		return factoryName;
	}

	public void setFactoryName(String factoryName)
	{
		this.factoryName = factoryName;
	}

	public String getMaskProcessOperationName()
	{
		return maskProcessOperationName;
	}

	public void setMaskProcessOperationName(String maskProcessOperationName)
	{
		this.maskProcessOperationName = maskProcessOperationName;
	}

	public String getMaskProcessOperationVersion()
	{
		return maskProcessOperationVersion;
	}

	public void setMaskProcessOperationVersion(String maskProcessOperationVersion)
	{
		this.maskProcessOperationVersion = maskProcessOperationVersion;
	}

	public String getReasonCodeType()
	{
		return reasonCodeType;
	}

	public void setReasonCodeType(String reasonCodeType)
	{
		this.reasonCodeType = reasonCodeType;
	}

	public String getReasonCode()
	{
		return reasonCode;
	}

	public void setReasonCode(String reasonCode)
	{
		this.reasonCode = reasonCode;
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
