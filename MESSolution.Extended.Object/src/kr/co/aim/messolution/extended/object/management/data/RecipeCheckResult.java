package kr.co.aim.messolution.extended.object.management.data;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class RecipeCheckResult extends UdfAccessor {

	@CTORMTemplate(seq = "1", name="machineName", type="Key", dataType="String", initial="", history="")
	private String machineName;
	
	@CTORMTemplate(seq = "2", name="portName", type="Key", dataType="String", initial="", history="")
	private String portName;

	@CTORMTemplate(seq = "3", name="carrierName", type="Key", dataType="String", initial="", history="")
	private String carrierName;
	
	@CTORMTemplate(seq = "4", name="recipeName", type="Key", dataType="String", initial="", history="")
	private String recipeName;
	
	@CTORMTemplate(seq = "5", name="checkLevel", type="Column", dataType="String", initial="", history="")
	private String checkLevel;

	@CTORMTemplate(seq = "6", name="originalSubjectName", type="Column", dataType="String", initial="", history="")
	private String originalSubjectName;

	@CTORMTemplate(seq = "7", name="unitQty", type="Column", dataType="Number", initial="", history="")
	private long unitQty;

	@CTORMTemplate(seq = "8", name="checkUnitQty", type="Column", dataType="Number", initial="", history="")
	private long checkUnitQty;

	@CTORMTemplate(seq = "9", name="subUnitQty", type="Column", dataType="Number", initial="", history="")
	private long subUnitQty;

	@CTORMTemplate(seq = "10", name="checkSubUnitQty", type="Column", dataType="Number", initial="", history="")
	private long checkSubUnitQty;

	@CTORMTemplate(seq = "11", name="result", type="Column", dataType="String", initial="", history="")
	private String result;
	
	@CTORMTemplate(seq = "12", name="resultComment", type="Column", dataType="String", initial="", history="")
	private String resultComment;

	@CTORMTemplate(seq = "13", name="createTimeKey", type="Column", dataType="String", initial="", history="")
	private String createTimeKey;

	@CTORMTemplate(seq = "14", name="updateTimeKey", type="Column", dataType="String", initial="", history="")
	private String updateTimeKey;

	@CTORMTemplate(seq = "14", name="currentCheckComment", type="Column", dataType="String", initial="", history="")
	private String currentCheckComment;
	
	public String getMachineName()
	{
		return machineName;
	}

	public void setMachineName(String machineName)
	{
		this.machineName = machineName;
	}

	public String getPortName()
	{
		return portName;
	}

	public void setPortName(String portName)
	{
		this.portName = portName;
	}

	public String getCarrierName()
	{
		return carrierName;
	}

	public void setCarrierName(String carrierName)
	{
		this.carrierName = carrierName;
	}

	public String getRecipeName()
	{
		return recipeName;
	}

	public void setRecipeName(String recipeName)
	{
		this.recipeName = recipeName;
	}

	public String getOriginalSubjectName()
	{
		return originalSubjectName;
	}

	public void setOriginalSubjectName(String originalSubjectName)
	{
		this.originalSubjectName = originalSubjectName;
	}

	public long getUnitQty()
	{
		return unitQty;
	}

	public void setUnitQty(long unitQty)
	{
		this.unitQty = unitQty;
	}

	public long getCheckUnitQty()
	{
		return checkUnitQty;
	}

	public void setCheckUnitQty(long checkUnitQty)
	{
		this.checkUnitQty = checkUnitQty;
	}

	public long getSubUnitQty()
	{
		return subUnitQty;
	}

	public void setSubUnitQty(long subUnitQty)
	{
		this.subUnitQty = subUnitQty;
	}

	public long getCheckSubUnitQty()
	{
		return checkSubUnitQty;
	}

	public void setCheckSubUnitQty(long checkSubUnitQty)
	{
		this.checkSubUnitQty = checkSubUnitQty;
	}

	public String getResult()
	{
		return result;
	}

	public void setResult(String result)
	{
		this.result = result;
	}

	public String getResultComment()
	{
		return resultComment;
	}

	public void setResultComment(String resultComment)
	{
		this.resultComment = resultComment;
	}

	public String getCreateTimeKey()
	{
		return createTimeKey;
	}

	public void setCreateTimeKey(String createTimeKey)
	{
		this.createTimeKey = createTimeKey;
	}

	public String getUpdateTimeKey()
	{
		return updateTimeKey;
	}

	public void setUpdateTimeKey(String updateTimeKey)
	{
		this.updateTimeKey = updateTimeKey;
	}

	public String getCheckLevel()
	{
		return checkLevel;
	}

	public void setCheckLevel(String checkLevel)
	{
		this.checkLevel = checkLevel;
	}
	
	public String getCurrentCheckComment()
	{
		return currentCheckComment;
	}

	public void setCurrentCheckComment(String currentCheckComment)
	{
		this.currentCheckComment = currentCheckComment;
	}
	
}