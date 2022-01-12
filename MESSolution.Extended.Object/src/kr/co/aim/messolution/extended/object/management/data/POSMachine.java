package kr.co.aim.messolution.extended.object.management.data;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class POSMachine extends UdfAccessor {
	@CTORMTemplate(seq = "1", name = "conditionID", type = "Key", dataType = "String", initial = "", history = "")
	public String conditionID;
	
	@CTORMTemplate(seq = "2", name = "machineName", type = "Key", dataType = "String", initial = "", history = "")
	public String machineName;
	
	@CTORMTemplate(seq = "3", name = "rollType", type = "Column", dataType = "String", initial = "", history = "")
	public String rollType;
	
	@CTORMTemplate(seq = "4", name = "machineRecipeName", type = "Column", dataType = "String", initial = "", history = "")
	public String machineRecipeName;
	
	@CTORMTemplate(seq = "5", name = "INT", type = "Column", dataType = "String", initial = "", history = "")
	public String INT;
	
	@CTORMTemplate(seq = "6", name = "MFG", type = "Column", dataType = "String", initial = "", history = "")
	public String MFG;
	
	@CTORMTemplate(seq = "7", name = "autoChangeFlag", type = "Column", dataType = "String", initial = "", history = "")
	public String autoChangeFlag;
	
	@CTORMTemplate(seq = "8", name = "autoChangeTime", type = "Column", dataType = "Number", initial = "", history = "")
	public long autoChangeTime;
	
	@CTORMTemplate(seq = "9", name = "autoChangeLotQuantity", type = "Column", dataType = "Number", initial = "", history = "")
	public long autoChangeLotQuantity;
	
	@CTORMTemplate(seq = "10", name = "checkLevel", type = "Column", dataType = "String", initial = "", history = "")
	public String checkLevel;
	
	@CTORMTemplate(seq = "11", name = "disPatchState", type = "Column", dataType = "String", initial = "", history = "")
	public String disPatchState;
	
	@CTORMTemplate(seq = "12", name = "disPatchPriority", type = "Column", dataType = "String", initial = "", history = "")
	public String disPatchPriority;
	
	@CTORMTemplate(seq = "13", name = "rmsFlag", type = "Column", dataType = "String", initial = "", history = "")
	public String rmsFlag;

	@CTORMTemplate(seq = "14", name = "ecRecipeFlag", type = "Column", dataType = "String", initial = "", history = "")
	public String ecRecipeFlag;
	
	@CTORMTemplate(seq = "15", name = "ecRecipeName", type = "Column", dataType = "String", initial = "", history = "")
	public String ecRecipeName;
	
	@CTORMTemplate(seq = "16", name = "maskCycleTarget", type = "Column", dataType = "String", initial = "", history = "")
	public String maskCycleTarget;

	public String getConditionID()
	{
		return conditionID;
	}

	public void setConditionID(String conditionID)
	{
		this.conditionID = conditionID;
	}

	public String getMachineName()
	{
		return machineName;
	}

	public void setMachineName(String machineName)
	{
		this.machineName = machineName;
	}

	public String getRollType()
	{
		return rollType;
	}

	public void setRollType(String rollType)
	{
		this.rollType = rollType;
	}

	public String getMachineRecipeName()
	{
		return machineRecipeName;
	}

	public void setMachineRecipeName(String machineRecipeName)
	{
		this.machineRecipeName = machineRecipeName;
	}

	public String getINT()
	{
		return INT;
	}

	public void setINT(String iNT)
	{
		INT = iNT;
	}

	public String getMFG()
	{
		return MFG;
	}

	public void setMFG(String mFG)
	{
		MFG = mFG;
	}

	public String getAutoChangeFlag()
	{
		return autoChangeFlag;
	}

	public void setAutoChangeFlag(String autoChangeFlag)
	{
		this.autoChangeFlag = autoChangeFlag;
	}

	public long getAutoChangeTime()
	{
		return autoChangeTime;
	}

	public void setAutoChangeTime(long autoChangeTime)
	{
		this.autoChangeTime = autoChangeTime;
	}

	public long getAutoChangeLotQuantity()
	{
		return autoChangeLotQuantity;
	}

	public void setAutoChangeLotQuantity(long autoChangeLotQuantity)
	{
		this.autoChangeLotQuantity = autoChangeLotQuantity;
	}

	public String getCheckLevel()
	{
		return checkLevel;
	}

	public void setCheckLevel(String checkLevel)
	{
		this.checkLevel = checkLevel;
	}

	public String getDisPatchState()
	{
		return disPatchState;
	}

	public void setDisPatchState(String disPatchState)
	{
		this.disPatchState = disPatchState;
	}

	public String getDisPatchPriority()
	{
		return disPatchPriority;
	}

	public void setDisPatchPriority(String disPatchPriority)
	{
		this.disPatchPriority = disPatchPriority;
	}

	public String getRmsFlag()
	{
		return rmsFlag;
	}

	public void setRmsFlag(String rmsFlag)
	{
		this.rmsFlag = rmsFlag;
	}
	
	public String getEcRecipeFlag() {
		return ecRecipeFlag;
	}

	public void setEcRecipeFlag(String ecRecipeFlag) {
		this.ecRecipeFlag = ecRecipeFlag;
	}

	public String getEcRecipeName() {
		return ecRecipeName;
	}

	public void setEcRecipeName(String ecRecipeName) {
		this.ecRecipeName = ecRecipeName;
	}

	public String getMaskCycleTarget() {
		return maskCycleTarget;
	}

	public void setMaskCycleTarget(String maskCycleTarget) {
		this.maskCycleTarget = maskCycleTarget;
	}

}
