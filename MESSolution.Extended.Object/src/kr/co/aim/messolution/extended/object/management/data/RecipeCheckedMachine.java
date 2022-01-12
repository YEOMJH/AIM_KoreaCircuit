package kr.co.aim.messolution.extended.object.management.data;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class RecipeCheckedMachine extends UdfAccessor {

	@CTORMTemplate(seq = "1", name="checkedMachine", type="Key", dataType="String", initial="", history="")
	private String checkedMachine;
	
	@CTORMTemplate(seq = "2", name="portName", type="Key", dataType="String", initial="", history="")
	private String portName;
	
	@CTORMTemplate(seq = "3", name="recipeName", type="Key", dataType="String", initial="", history="")
	private String recipeName;
	
	@CTORMTemplate(seq = "4", name="mainMachineName", type="Key", dataType="String", initial="", history="")
	private String mainMachineName;

	@CTORMTemplate(seq = "5", name="carrierName", type="Column", dataType="String", initial="", history="")
	private String carrierName;
	
	@CTORMTemplate(seq = "6", name="timeKey", type="Column", dataType="String", initial="", history="")
	private String timeKey;

	public String getCheckedMachine()
	{
		return checkedMachine;
	}

	public void setCheckedMachine(String checkedMachine)
	{
		this.checkedMachine = checkedMachine;
	}

	public String getPortName()
	{
		return portName;
	}

	public void setPortName(String portName)
	{
		this.portName = portName;
	}

	public String getRecipeName()
	{
		return recipeName;
	}

	public void setRecipeName(String recipeName)
	{
		this.recipeName = recipeName;
	}

	public String getMainMachineName()
	{
		return mainMachineName;
	}

	public void setMainMachineName(String mainMachineName)
	{
		this.mainMachineName = mainMachineName;
	}

	public String getCarrierName()
	{
		return carrierName;
	}

	public void setCarrierName(String carrierName)
	{
		this.carrierName = carrierName;
	}

	public String getTimeKey()
	{
		return timeKey;
	}

	public void setTimeKey(String timeKey)
	{
		this.timeKey = timeKey;
	}
}