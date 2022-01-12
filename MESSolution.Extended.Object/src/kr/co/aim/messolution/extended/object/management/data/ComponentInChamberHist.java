package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class ComponentInChamberHist extends UdfAccessor {
	@CTORMTemplate(seq = "1", name="timeKey", type="Key", dataType="String", initial="", history="")
	private String timeKey;
	@CTORMTemplate(seq = "2", name="productName", type="Key", dataType="String", initial="", history="")
	private String productName;
	@CTORMTemplate(seq = "3", name="eventName", type="Key", dataType="String", initial="", history="")
	private String eventName;
	@CTORMTemplate(seq = "4", name="lotName", type="Column", dataType="String", initial="", history="")
	private String lotName;
	@CTORMTemplate(seq = "5", name="toSlotId", type="Column", dataType="Number", initial="", history="")
	private int toSlotId;
	@CTORMTemplate(seq = "6", name="fromSlotId", type="Column", dataType="Number", initial="", history="")
	private int fromSlotId;
	@CTORMTemplate(seq = "7", name="toSlotPosition", type="Column", dataType="String", initial="", history="")
	private String toSlotPosition;
	@CTORMTemplate(seq = "8", name="fromSlotPosition", type="Column", dataType="String", initial="", history="")
	private String fromSlotPosition;
	@CTORMTemplate(seq = "9", name="eventTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp eventTime;
	@CTORMTemplate(seq = "10", name="eventUser", type="Column", dataType="String", initial="", history="")
	private String eventUser;
	@CTORMTemplate(seq = "11", name="factoryName", type="Column", dataType="String", initial="", history="")
	private String factoryName;
	@CTORMTemplate(seq = "12", name="productSpecName", type="Column", dataType="String", initial="", history="")
	private String productSpecName;
	@CTORMTemplate(seq = "13", name="productSpecVersion", type="Column", dataType="String", initial="", history="")
	private String productSpecVersion;
	@CTORMTemplate(seq = "14", name="processFlowName", type="Column", dataType="String", initial="", history="")
	private String processFlowName;
	@CTORMTemplate(seq = "15", name="processFlowVersion", type="Column", dataType="String", initial="", history="")
	private String processFlowVersion;
	@CTORMTemplate(seq = "16", name="processOperationName", type="Column", dataType="String", initial="", history="")
	private String processOperationName;
	@CTORMTemplate(seq = "17", name="processOperationVersion", type="Column", dataType="String", initial="", history="")
	private String processOperationVersion;
	@CTORMTemplate(seq = "18", name="productionType", type="Column", dataType="String", initial="", history="")
	private String productionType;
	@CTORMTemplate(seq = "19", name="productType", type="Column", dataType="String", initial="", history="")
	private String productType;
	@CTORMTemplate(seq = "20", name="machineName", type="Column", dataType="String", initial="", history="")
	private String machineName;
	@CTORMTemplate(seq = "21", name="materialLocationName", type="Column", dataType="String", initial="", history="")
	private String materialLocationName;
	@CTORMTemplate(seq = "22", name="productGrade", type="Column", dataType="String", initial="", history="")
	private String productGrade;
	@CTORMTemplate(seq = "23", name="productJudge", type="Column", dataType="String", initial="", history="")
	private String productJudge;
	@CTORMTemplate(seq = "24", name="maskLotName", type="Column", dataType="String", initial="", history="")
	private String maskLotName;
	@CTORMTemplate(seq = "25", name="palletName", type="Column", dataType="String", initial="", history="")
	private String palletName;
	@CTORMTemplate(seq = "26", name="productRequestName", type="Column", dataType="String", initial="", history="")
	private String productRequestName;
	@CTORMTemplate(seq = "27", name="reasonCode", type="Column", dataType="String", initial="", history="")
	private String reasonCode;
	@CTORMTemplate(seq = "28", name="chamberYN", type="Column", dataType="String", initial="", history="")
	private String chamberYN;
	@CTORMTemplate(seq = "29", name="machineRecipeName", type="Column", dataType="String", initial="", history="")
	private String machineRecipeName;
	@CTORMTemplate(seq = "30", name="trackInTime", type="Column", dataType="String", initial="", history="")
	private String trackInTime;
	
	public String getTimeKey()
	{
		return timeKey;
	}
	public void setTimeKey(String timeKey)
	{
		this.timeKey = timeKey;
	}
	public String getProductName()
	{
		return productName;
	}
	public void setProductName(String productName)
	{
		this.productName = productName;
	}
	public String getEventName()
	{
		return eventName;
	}
	public void setEventName(String eventName)
	{
		this.eventName = eventName;
	}
	public String getLotName()
	{
		return lotName;
	}
	public void setLotName(String lotName)
	{
		this.lotName = lotName;
	}
	public int getToSlotId()
	{
		return toSlotId;
	}
	public void setToSlotId(int toSlotId)
	{
		this.toSlotId = toSlotId;
	}
	public int getFromSlotId()
	{
		return fromSlotId;
	}
	public void setFromSlotId(int fromSlotId)
	{
		this.fromSlotId = fromSlotId;
	}
	public String getToSlotPosition()
	{
		return toSlotPosition;
	}
	public void setToSlotPosition(String toSlotPosition)
	{
		this.toSlotPosition = toSlotPosition;
	}
	public String getFromSlotPosition()
	{
		return fromSlotPosition;
	}
	public void setFromSlotPosition(String fromSlotPosition)
	{
		this.fromSlotPosition = fromSlotPosition;
	}
	public Timestamp getEventTime()
	{
		return eventTime;
	}
	public void setEventTime(Timestamp eventTime)
	{
		this.eventTime = eventTime;
	}
	public String getEventUser()
	{
		return eventUser;
	}
	public void setEventUser(String eventUser)
	{
		this.eventUser = eventUser;
	}
	public String getFactoryName()
	{
		return factoryName;
	}
	public void setFactoryName(String factoryName)
	{
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
	public String getProductSpecVersion()
	{
		return productSpecVersion;
	}
	public void setProductSpecVersion(String productSpecVersion)
	{
		this.productSpecVersion = productSpecVersion;
	}
	public String getProcessFlowName()
	{
		return processFlowName;
	}
	public void setProcessFlowName(String processFlowName)
	{
		this.processFlowName = processFlowName;
	}
	public String getProcessFlowVersion()
	{
		return processFlowVersion;
	}
	public void setProcessFlowVersion(String processFlowVersion)
	{
		this.processFlowVersion = processFlowVersion;
	}
	public String getProcessOperationName()
	{
		return processOperationName;
	}
	public void setProcessOperationName(String processOperationName)
	{
		this.processOperationName = processOperationName;
	}
	public String getProcessOperationVersion()
	{
		return processOperationVersion;
	}
	public void setProcessOperationVersion(String processOperationVersion)
	{
		this.processOperationVersion = processOperationVersion;
	}
	public String getProductionType()
	{
		return productionType;
	}
	public void setProductionType(String productionType)
	{
		this.productionType = productionType;
	}
	public String getProductType()
	{
		return productType;
	}
	public void setProductType(String productType)
	{
		this.productType = productType;
	}
	public String getMachineName()
	{
		return machineName;
	}
	public void setMachineName(String machineName)
	{
		this.machineName = machineName;
	}
	public String getMaterialLocationName()
	{
		return materialLocationName;
	}
	public void setMaterialLocationName(String materialLocationName)
	{
		this.materialLocationName = materialLocationName;
	}
	public String getProductGrade()
	{
		return productGrade;
	}
	public void setProductGrade(String productGrade)
	{
		this.productGrade = productGrade;
	}
	public String getProductJudge()
	{
		return productJudge;
	}
	public void setProductJudge(String productJudge)
	{
		this.productJudge = productJudge;
	}
	public String getMaskLotName()
	{
		return maskLotName;
	}
	public void setMaskLotName(String maskLotName)
	{
		this.maskLotName = maskLotName;
	}
	public String getPalletName()
	{
		return palletName;
	}
	public void setPalletName(String palletName)
	{
		this.palletName = palletName;
	}
	public String getProductRequestName()
	{
		return productRequestName;
	}
	public void setProductRequestName(String productRequestName)
	{
		this.productRequestName = productRequestName;
	}
	public String getReasonCode()
	{
		return reasonCode;
	}
	public void setReasonCode(String reasonCode)
	{
		this.reasonCode = reasonCode;
	}
	public String getChamberYN()
	{
		return chamberYN;
	}
	public void setChamberYN(String chamberYN)
	{
		this.chamberYN = chamberYN;
	}
	public String getMachineRecipeName()
	{
		return machineRecipeName;
	}
	public void setMachineRecipeName(String machineRecipeName)
	{
		this.machineRecipeName = machineRecipeName;
	}
	public String getTrackInTime()
	{
		return trackInTime;
	}
	public void setTrackInTime(String trackInTime)
	{
		this.trackInTime = trackInTime;
	}
}
