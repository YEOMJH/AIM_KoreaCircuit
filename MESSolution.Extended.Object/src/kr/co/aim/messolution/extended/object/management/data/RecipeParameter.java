package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class RecipeParameter extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="machineName", type="Key", dataType="String", initial="", history="")
	private String machineName;
	
	@CTORMTemplate(seq = "2", name="recipeName", type="Key", dataType="String", initial="", history="")
	private String recipeName;
	
	@CTORMTemplate(seq = "3", name="recipeParameterName", type="Key", dataType="String", initial="", history="")
	private String recipeParameterName;
	
	@CTORMTemplate(seq = "4", name="value", type="Key", dataType="String", initial="", history="")
	private String value;

	@CTORMTemplate(seq = "5", name="validationType", type="Column", dataType="String", initial="", history="")
	private String validationType;

	@CTORMTemplate(seq = "6", name="target", type="Column", dataType="String", initial="", history="")
	private String target;
	
	@CTORMTemplate(seq = "7", name="lowerLimit", type="Column", dataType="String", initial="", history="")
	private String lowerLimit;
	
	@CTORMTemplate(seq = "8", name="upperLimit", type="Column", dataType="String", initial="", history="")
	private String upperLimit;
	
	@CTORMTemplate(seq = "9", name="result", type="Column", dataType="String", initial="", history="")
	private String result;
     
	@CTORMTemplate(seq = "10", name="OLDVALUE", type="Column", dataType="String", initial="", history="")
	private String OLDVALUE;
	
	@CTORMTemplate(seq = "11", name="OLDVALIDATIONTYPE", type="Column", dataType="String", initial="", history="")
	private String OLDVALIDATIONTYPE;
	
	@CTORMTemplate(seq = "12", name="OLDTARGET", type="Column", dataType="String", initial="", history="")
	
	private String OLDTARGET;
	@CTORMTemplate(seq = "13", name="OLDLOWERLIMIT", type="Column", dataType="String", initial="", history="")
	
	private String OLDLOWERLIMIT;
	@CTORMTemplate(seq = "14", name="OLDUPPERLIMIT", type="Column", dataType="String", initial="", history="")
	private String OLDUPPERLIMIT;
	
	@CTORMTemplate(seq = "15", name="checkFlag", type="Column", dataType="String", initial="", history="")
	private String checkFlag;

	public String getOLDVALUE() {
		return OLDVALUE;
	}

	public void setOLDVALUE(String oldvalue) {
		OLDVALUE = oldvalue;
	}

	public String getOLDVALIDATIONTYPE() {
		return OLDVALIDATIONTYPE;
	}

	public void setOLDVALIDATIONTYPE(String oldvalidationtype) {
		OLDVALIDATIONTYPE = oldvalidationtype;
	}

	public String getOLDTARGET() {
		return OLDTARGET;
	}

	public void setOLDTARGET(String oldtarget) {
		OLDTARGET = oldtarget;
	}

	public String getOLDLOWERLIMIT() {
		return OLDLOWERLIMIT;
	}

	public void setOLDLOWERLIMIT(String oldlowerlimit) {
		OLDLOWERLIMIT = oldlowerlimit;
	}

	public String getOLDUPPERLIMIT() {
		return OLDUPPERLIMIT;
	}

	public void setOLDUPPERLIMIT(String oldupperlimit) {
		OLDUPPERLIMIT = oldupperlimit;
	}

	public RecipeParameter()
	{
		
	}
	
	public RecipeParameter(String machineName, String recipeName, String recipeParameterName)
	{
		setMachineName(machineName);
		setRecipeName(recipeName);
		setRecipeParameterName(recipeParameterName);
	}

	public String getMachineName() {
		return machineName;
	}

	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}

	public String getRecipeName() {
		return recipeName;
	}

	public void setRecipeName(String recipeName) {
		this.recipeName = recipeName;
	}

	public String getRecipeParameterName() {
		return recipeParameterName;
	}

	public void setRecipeParameterName(String recipeParameterName) {
		this.recipeParameterName = recipeParameterName;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getValidationType() {
		return validationType;
	}

	public void setValidationType(String validationType) {
		this.validationType = validationType;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getLowerLimit() {
		return lowerLimit;
	}

	public void setLowerLimit(String lowerLimit) {
		this.lowerLimit = lowerLimit;
	}

	public String getUpperLimit() {
		return upperLimit;
	}

	public void setUpperLimit(String upperLimit) {
		this.upperLimit = upperLimit;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getCheckFlag()
	{
		return checkFlag;
	}

	public void setCheckFlag(String checkFlag)
	{
		this.checkFlag = checkFlag;
	}

}
