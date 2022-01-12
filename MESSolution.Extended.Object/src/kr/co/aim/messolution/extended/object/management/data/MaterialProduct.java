package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class MaterialProduct extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="timeKey", type="Key", dataType="String", initial="", history="")
	private String timeKey;
	@CTORMTemplate(seq = "2", name="productName", type="Key", dataType="String", initial="", history="")
	private String productName;
	@CTORMTemplate(seq = "3", name="materialName", type="Key", dataType="String", initial="", history="")
	private String materialName;
	@CTORMTemplate(seq = "4", name="lotName", type="Column", dataType="String", initial="", history="")
	private String lotName;
	@CTORMTemplate(seq = "5", name="materialKind", type="Column", dataType="String", initial="", history="")
	private String materialKind;
	@CTORMTemplate(seq = "6", name="materialType", type="Column", dataType="String", initial="", history="")
	private String materialType;
	@CTORMTemplate(seq = "7", name="quantity", type="Column", dataType="Number", initial="", history="")
	private double quantity;
	@CTORMTemplate(seq = "8", name="eventName", type="Column", dataType="String", initial="", history="")
	private String eventName;
	@CTORMTemplate(seq = "9", name="eventTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp eventTime;
	@CTORMTemplate(seq = "10", name="factoryName", type="Column", dataType="String", initial="", history="")
	private String factoryName;
	@CTORMTemplate(seq = "11", name="productSpecName", type="Column", dataType="String", initial="", history="")
	private String productSpecName;
	@CTORMTemplate(seq = "12", name="productSpecVersion", type="Column", dataType="String", initial="", history="")
	private String productSpecVersion;
	@CTORMTemplate(seq = "13", name="processFlowName", type="Column", dataType="String", initial="", history="")
	private String processFlowName;
	@CTORMTemplate(seq = "14", name="processFlowVersion", type="Column", dataType="String", initial="", history="")
	private String processFlowVersion;
	@CTORMTemplate(seq = "15", name="processOperationName", type="Column", dataType="String", initial="", history="")
	private String processOperationName;
	@CTORMTemplate(seq = "16", name="processOperationVersion", type="Column", dataType="String", initial="", history="")
	private String processOperationVersion;
	@CTORMTemplate(seq = "17", name="machineName", type="Column", dataType="String", initial="", history="")
	private String machineName;
	@CTORMTemplate(seq = "18", name="materialLocationName", type="Column", dataType="String", initial="", history="")
	private String materialLocationName;
	@CTORMTemplate(seq = "19", name="maskCycleCount", type="Column", dataType="Number", initial="", history="")
	private String maskCycleCount;
	@CTORMTemplate(seq = "20", name="offsetX", type="Column", dataType="Number", initial="", history="")
	private String offsetX;
	@CTORMTemplate(seq = "21", name="offsetY", type="Column", dataType="Number", initial="", history="")
	private String offsetY;
	@CTORMTemplate(seq = "22", name="offsetT", type="Column", dataType="Number", initial="", history="")
	private String offsetT;

	
	public String getProductName()
	{
		return productName;
	}
	public void setProductName(String productName)
	{
		this.productName = productName;
	}
	public String getMaterialName()
	{
		return materialName;
	}
	public void setMaterialName(String materialName)
	{
		this.materialName = materialName;
	}
	public String getTimeKey()
	{
		return timeKey;
	}
	public void setTimeKey(String timeKey)
	{
		this.timeKey = timeKey;
	}
	public String getLotName()
	{
		return lotName;
	}
	public void setLotName(String lotName)
	{
		this.lotName = lotName;
	}
	public String getMaterialKind()
	{
		return materialKind;
	}
	public void setMaterialKind(String materialKind)
	{
		this.materialKind = materialKind;
	}
	public String getMaterialType()
	{
		return materialType;
	}
	public void setMaterialType(String materialType)
	{
		this.materialType = materialType;
	}
	public double getQuantity()
	{
		return quantity;
	}
	public void setQuantity(double quantity)
	{
		this.quantity = quantity;
	}
	public String getEventName()
	{
		return eventName;
	}
	public void setEventName(String eventName)
	{
		this.eventName = eventName;
	}
	public Timestamp getEventTime()
	{
		return eventTime;
	}
	public void setEventTime(Timestamp eventTime)
	{
		this.eventTime = eventTime;
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
	public String getMaskCycleCount() 
	{
		return maskCycleCount;
	}
	public void setMaskCycleCount(String maskCycleCount) 
	{
		this.maskCycleCount = maskCycleCount;
	}
	public String getOffsetX() 
	{
		return offsetX;
	}
	public void setOffsetX(String offsetX) 
	{
		this.offsetX = offsetX;
	}
	public String getOffsetY() 
	{
		return offsetY;
	}
	public void setOffsetY(String offsetY) 
	{
		this.offsetY = offsetY;
	}
	public String getOffsetT() 
	{
		return offsetT;
	}
	public void setOffsetT(String offsetT) 
	{
		this.offsetT = offsetT;
	}	
}
