package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class TFEDownProduct extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name = "timeKey", type = "Key", dataType = "String", initial = "", history = "")
	private String timeKey;
	@CTORMTemplate(seq = "2", name = "productName", type = "Key", dataType = "String", initial = "", history = "")
	private String productName;
	@CTORMTemplate(seq = "3", name = "lotName", type = "Column", dataType = "String", initial = "", history = "")
	private String lotName;
	@CTORMTemplate(seq = "4", name = "factoryName", type = "Column", dataType = "String", initial = "", history = "")
	private String factoryName;
	@CTORMTemplate(seq = "5", name = "productRequestName", type = "Column", dataType = "String", initial = "", history = "")
	private String productRequestName;
	@CTORMTemplate(seq = "6", name = "productSpecName", type = "Column", dataType = "String", initial = "", history = "")
	private String productSpecName;
	@CTORMTemplate(seq = "7", name = "productSpecVersion", type = "Column", dataType = "String", initial = "", history = "")
	private String productSpecVersion;
	@CTORMTemplate(seq = "8", name = "processFlowName", type = "Column", dataType = "String", initial = "", history = "")
	private String processFlowName;
	@CTORMTemplate(seq = "9", name = "processFlowVersion", type = "Column", dataType = "String", initial = "", history = "")
	private String processFlowVersion;
	@CTORMTemplate(seq = "10", name = "processOperationName", type = "Column", dataType = "String", initial = "", history = "")
	private String processOperationName;
	@CTORMTemplate(seq = "11", name = "processOperationVersion", type = "Column", dataType = "String", initial = "", history = "")
	private String processOperationVersion;
	@CTORMTemplate(seq = "12", name = "productionType", type = "Column", dataType = "String", initial = "", history = "")
	private String productionType;
	@CTORMTemplate(seq = "13", name = "productType", type = "Column", dataType = "String", initial = "", history = "")
	private String productType;
	@CTORMTemplate(seq = "14", name = "machineName", type = "Column", dataType = "String", initial = "", history = "")
	private String machineName;
	@CTORMTemplate(seq = "15", name = "unitName", type = "Column", dataType = "String", initial = "", history = "")
	private String unitName;
	@CTORMTemplate(seq = "16", name = "subUnitName", type = "Column", dataType = "String", initial = "", history = "")
	private String subUnitName;
	@CTORMTemplate(seq = "17", name = "machineRecipeName", type = "Column", dataType = "String", initial = "", history = "")
	private String machineRecipeName;
	@CTORMTemplate(seq = "18", name = "trackInTime", type = "Column", dataType = "String", initial = "", history = "")
	private String trackInTime;
	@CTORMTemplate(seq = "19", name = "eventTime", type = "Column", dataType = "Timestamp", initial = "", history = "")
	private Timestamp eventTime;
	@CTORMTemplate(seq = "20", name = "eventName", type = "Column", dataType = "String", initial = "", history = "")
	private String eventName;
	@CTORMTemplate(seq = "21", name = "eventUser", type = "Column", dataType = "String", initial = "", history = "")
	private String eventUser;

	//instantiation
	public TFEDownProduct()
	{

	}
	
	public TFEDownProduct(String timeKey, String productName)
	{
	   setTimeKey(timeKey);
	   setProductName(productName);
	}

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

	public String getLotName()
	{
		return lotName;
	}

	public void setLotName(String lotName)
	{
		this.lotName = lotName;
	}

	public String getFactoryName()
	{
		return factoryName;
	}

	public void setFactoryName(String factoryName)
	{
		this.factoryName = factoryName;
	}

	public String getProductRequestName()
	{
		return productRequestName;
	}

	public void setProductRequestName(String productRequestName)
	{
		this.productRequestName = productRequestName;
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

	public Timestamp getEventTime()
	{
		return eventTime;
	}

	public void setEventTime(Timestamp eventTime)
	{
		this.eventTime = eventTime;
	}

	public String getEventName()
	{
		return eventName;
	}

	public void setEventName(String eventName)
	{
		this.eventName = eventName;
	}

	public String getEventUser()
	{
		return eventUser;
	}

	public void setEventUser(String eventUser)
	{
		this.eventUser = eventUser;
	}
	
}
