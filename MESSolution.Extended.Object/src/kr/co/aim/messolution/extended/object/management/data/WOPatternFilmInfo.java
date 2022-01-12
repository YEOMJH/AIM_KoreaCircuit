package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class WOPatternFilmInfo extends UdfAccessor {
	@CTORMTemplate(seq = "1", name = "productSpecName", type = "Key", dataType = "String", initial = "", history = "")
	private String productSpecName;
	@CTORMTemplate(seq = "2", name = "productSpecVersion", type = "Key", dataType = "String", initial = "", history = "")
	private String productSpecVersion;
	@CTORMTemplate(seq = "3", name = "processFlowName", type = "key", dataType = "String", initial = "", history = "")
	private String processFlowName;
	@CTORMTemplate(seq = "4", name = "processFlowVersion", type = "Key", dataType = "String", initial = "", history = "")
	private String processFlowVersion;
	@CTORMTemplate(seq = "5", name = "processOperationName", type = "key", dataType = "String", initial = "", history = "")
	private String processOperationName;
	@CTORMTemplate(seq = "6", name = "processOperationVersion", type = "Key", dataType = "String", initial = "", history = "")
	private String processOperationVersion;
	@CTORMTemplate(seq = "7", name = "machineName", type = "key", dataType = "String", initial = "", history = "")
	private String machineName;
	@CTORMTemplate(seq = "8", name = "productRequestName", type = "key", dataType = "String", initial = "", history = "")
	private String productRequestName;
	@CTORMTemplate(seq = "9", name = "recipeName", type = "Column", dataType = "String", initial = "", history = "")
	private String recipeName;
	@CTORMTemplate(seq = "10", name = "factoryName", type = "Column", dataType = "String", initial = "", history = "")
	private String factoryName;
	@CTORMTemplate(seq = "11", name = "materialSpecName", type = "Column", dataType = "String", initial = "", history = "")
	private String materialSpecName;
	@CTORMTemplate(seq = "12", name = "materialSpecVersion", type = "Column", dataType = "String", initial = "", history = "")
	private String materialSpecVersion;
	@CTORMTemplate(seq = "13", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventUser;
	@CTORMTemplate(seq = "14", name = "lastEventName", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventName;
	@CTORMTemplate(seq = "15", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventComment;
	@CTORMTemplate(seq = "16", name = "lastEventTimekey", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventTimekey;
	@CTORMTemplate(seq = "17", name = "lastEventTime", type = "Column", dataType = "Timestamp", initial = "", history = "N")
	private Timestamp lastEventTime;
	@CTORMTemplate(seq = "18", name = "rmsFlag", type = "Column", dataType = "String", initial = "", history = "")
	private String rmsFlag;


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

	public String getProductRequestName()
	{
		return productRequestName;
	}

	public void setProductRequestName(String productRequestName)
	{
		this.productRequestName = productRequestName;
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

	public String getFactoryName()
	{
		return factoryName;
	}

	public void setFactoryName(String factoryName)
	{
		this.factoryName = factoryName;
	}

	public String getMaterialSpecName()
	{
		return materialSpecName;
	}

	public void setMaterialSpecName(String materialSpecName)
	{
		this.materialSpecName = materialSpecName;
	}

	public String getMaterialSpecVersion()
	{
		return materialSpecVersion;
	}

	public void setMaterialSpecVersion(String materialSpecVersion)
	{
		this.materialSpecVersion = materialSpecVersion;
	}

	public String getLastEventUser()
	{
		return lastEventUser;
	}

	public void setLastEventUser(String lastEventUser)
	{
		this.lastEventUser = lastEventUser;
	}

	public String getLastEventName()
	{
		return lastEventName;
	}

	public void setLastEventName(String lastEventName)
	{
		this.lastEventName = lastEventName;
	}

	public String getLastEventComment()
	{
		return lastEventComment;
	}

	public void setLastEventComment(String lastEventComment)
	{
		this.lastEventComment = lastEventComment;
	}

	public String getLastEventTimekey()
	{
		return lastEventTimekey;
	}

	public void setLastEventTimekey(String lastEventTimekey)
	{
		this.lastEventTimekey = lastEventTimekey;
	}

	public Timestamp getLastEventTime()
	{
		return lastEventTime;
	}

	public void setLastEventTime(Timestamp lastEventTime)
	{
		this.lastEventTime = lastEventTime;
	}

	
	public String getMachineName()
	{
		return machineName;
	}

	public void setMachineName(String machineName)
	{
		this.machineName = machineName;
	}
	
	
	public String getRecipeName()
	{
		return recipeName;
	}

	public void setRecipeName(String recipeName)
	{
		this.recipeName = recipeName;
	}
	
	public String getRmsFlag()
	{
		return rmsFlag;
	}

	public void setRmsFlag(String rmsFlag)
	{
		this.rmsFlag = rmsFlag;
	}

	public WOPatternFilmInfo(String productSpecName,String productSpecVersion,String processFlowName,String processFlowVersion,String processOperationName,String processOperationVersion,String machineName,String  productRequestName)
	{
		this.productSpecName = productSpecName;
		this.productSpecVersion=productSpecVersion;
		this.processFlowName=processFlowName;
		this.processFlowVersion=processFlowVersion;
		this.processOperationName=processOperationName;
		this.processOperationVersion=processOperationVersion;
		this.machineName=machineName;
		this.productRequestName=productRequestName;
	}

	public WOPatternFilmInfo()
	{
		
	}

}
