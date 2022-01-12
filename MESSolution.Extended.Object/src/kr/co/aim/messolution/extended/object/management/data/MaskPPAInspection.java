package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class MaskPPAInspection extends UdfAccessor 
{
	@CTORMTemplate(seq = "1", name = "maskLotName", type = "Key", dataType = "String", initial = "", history = "")
	private String maskLotName;
	@CTORMTemplate(seq = "2", name = "machineName", type = "Column", dataType = "String", initial = "", history = "")
	private String machineName;
	@CTORMTemplate(seq = "3", name = "unitName", type = "Column", dataType = "String", initial = "", history = "")
	private String unitName;
	@CTORMTemplate(seq = "4", name = "subUnitName", type = "Column", dataType = "String", initial = "", history = "")
	private String subUnitName;
	@CTORMTemplate(seq = "5", name = "frameName", type = "Column", dataType = "String", initial = "", history = "")
	private String frameName;
	@CTORMTemplate(seq = "6", name = "recipeID", type = "Column", dataType = "String", initial = "", history = "")
	private String recipeID;
	@CTORMTemplate(seq = "7", name = "maskType", type = "Column", dataType = "String", initial = "", history = "")
	private String maskType;
	@CTORMTemplate(seq = "8", name = "maskStartTime", type = "Column", dataType = "String", initial = "", history = "")
	private String maskStartTime;
	@CTORMTemplate(seq = "9", name = "maskEndTime", type = "Column", dataType = "String", initial = "", history = "")
	private String maskEndTime;
	@CTORMTemplate(seq = "10", name = "maskJudge", type = "Column", dataType = "String", initial = "", history = "")
	private String maskJudge;
	@CTORMTemplate(seq = "11", name = "thickNess", type = "Column", dataType = "String", initial = "", history = "")
	private String thickNess;
	@CTORMTemplate(seq = "12", name = "flatNess", type = "Column", dataType = "String", initial = "", history = "")
	private String flatNess;
	@CTORMTemplate(seq = "13", name = "CENTER_PPA_X_JUDGE", type = "Column", dataType = "String", initial = "", history = "")
	private String CENTER_PPA_X_JUDGE;
	@CTORMTemplate(seq = "14", name = "CENTER_PPA_X_MAX", type = "Column", dataType = "String", initial = "", history = "")
	private String CENTER_PPA_X_MAX;
	@CTORMTemplate(seq = "15", name = "CENTER_PPA_X_MIN", type = "Column", dataType = "String", initial = "", history = "")
	private String CENTER_PPA_X_MIN;
	@CTORMTemplate(seq = "16", name = "PPA_X_OK_RATE", type = "Column", dataType = "String", initial = "", history = "")
	private String PPA_X_OK_RATE;
	@CTORMTemplate(seq = "17", name = "CENTER_PPA_Y_JUDGE", type = "Column", dataType = "String", initial = "", history = "")
	private String CENTER_PPA_Y_JUDGE;
	@CTORMTemplate(seq = "18", name = "CENTER_PPA_Y_MAX", type = "Column", dataType = "String", initial = "", history = "")
	private String CENTER_PPA_Y_MAX;
	@CTORMTemplate(seq = "19", name = "CENTER_PPA_Y_MIN", type = "Column", dataType = "String", initial = "", history = "")
	private String CENTER_PPA_Y_MIN;
	@CTORMTemplate(seq = "20", name = "PPA_Y_OK_RATE", type = "Column", dataType = "String", initial = "", history = "")
	private String PPA_Y_OK_RATE;
	@CTORMTemplate(seq = "21", name = "STRAIGHTNESS_X_MAX", type = "Column", dataType = "String", initial = "", history = "")
	private String STRAIGHTNESS_X_MAX;
	@CTORMTemplate(seq = "22", name = "STRAIGHTNESS_Y_MAX", type = "Column", dataType = "String", initial = "", history = "")
	private String STRAIGHTNESS_Y_MAX;
	@CTORMTemplate(seq = "23", name = "MASK_CD_X_MAX", type = "Column", dataType = "String", initial = "", history = "")
	private String MASK_CD_X_MAX;
	@CTORMTemplate(seq = "24", name = "MASK_CD_X_MIN", type = "Column", dataType = "String", initial = "", history = "")
	private String MASK_CD_X_MIN;
	@CTORMTemplate(seq = "25", name = "MASK_CD_X_CPK", type = "Column", dataType = "String", initial = "", history = "")
	private String MASK_CD_X_CPK;
	@CTORMTemplate(seq = "26", name = "MASK_CD_Y_MAX", type = "Column", dataType = "String", initial = "", history = "")
	private String MASK_CD_Y_MAX;
	@CTORMTemplate(seq = "27", name = "MASK_CD_Y_MIN", type = "Column", dataType = "String", initial = "", history = "")
	private String MASK_CD_Y_MIN;
	@CTORMTemplate(seq = "28", name = "MASK_CD_Y_CPK", type = "Column", dataType = "String", initial = "", history = "")
	private String MASK_CD_Y_CPK;
	@CTORMTemplate(seq = "29", name = "maskCycleCount", type = "Column", dataType = "Number", initial = "", history = "")
	private long maskCycleCount;
	
	@CTORMTemplate(seq = "30", name="lastEventName", type="Column", dataType="String", initial="", history="")
	private String lastEventName;
	@CTORMTemplate(seq = "31", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;
	@CTORMTemplate(seq = "32", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;
	@CTORMTemplate(seq = "33", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;
	@CTORMTemplate(seq = "34", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimeKey;
	
	public MaskPPAInspection(){}
	
	public MaskPPAInspection(String maskLotName)
	{
		setMaskLotName(maskLotName);
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
	public String getMaskLotName()
	{
		return maskLotName;
	}
	public void setMaskLotName(String maskLotName)
	{
		this.maskLotName = maskLotName;
	}
	public String getFrameName()
	{
		return frameName;
	}
	public void setFrameName(String frameName)
	{
		this.frameName = frameName;
	}
	public String getRecipeID()
	{
		return recipeID;
	}
	public void setRecipeID(String recipeID)
	{
		this.recipeID = recipeID;
	}
	public String getMaskType()
	{
		return maskType;
	}
	public void setMaskType(String maskType)
	{
		this.maskType = maskType;
	}
	public String getMaskStartTime()
	{
		return maskStartTime;
	}
	public void setMaskStartTime(String maskStartTime)
	{
		this.maskStartTime = maskStartTime;
	}
	public String getMaskEndTime()
	{
		return maskEndTime;
	}
	public void setMaskEndTime(String maskEndTime)
	{
		this.maskEndTime = maskEndTime;
	}
	public String getMaskJudge()
	{
		return maskJudge;
	}
	public void setMaskJudge(String maskJudge)
	{
		this.maskJudge = maskJudge;
	}
	public String getThickNess()
	{
		return thickNess;
	}
	public void setThickNess(String thickNess)
	{
		this.thickNess = thickNess;
	}
	public String getFlatNess()
	{
		return flatNess;
	}
	public void setFlatNess(String flatNess)
	{
		this.flatNess = flatNess;
	}
	public String getCENTER_PPA_X_JUDGE()
	{
		return CENTER_PPA_X_JUDGE;
	}
	public void setCENTER_PPA_X_JUDGE(String cENTER_PPA_X_JUDGE)
	{
		CENTER_PPA_X_JUDGE = cENTER_PPA_X_JUDGE;
	}
	public String getCENTER_PPA_X_MAX()
	{
		return CENTER_PPA_X_MAX;
	}
	public void setCENTER_PPA_X_MAX(String cENTER_PPA_X_MAX)
	{
		CENTER_PPA_X_MAX = cENTER_PPA_X_MAX;
	}
	public String getCENTER_PPA_X_MIN()
	{
		return CENTER_PPA_X_MIN;
	}
	public void setCENTER_PPA_X_MIN(String cENTER_PPA_X_MIN)
	{
		CENTER_PPA_X_MIN = cENTER_PPA_X_MIN;
	}
	public String getPPA_X_OK_RATE()
	{
		return PPA_X_OK_RATE;
	}
	public void setPPA_X_OK_RATE(String pPA_X_OK_RATE)
	{
		PPA_X_OK_RATE = pPA_X_OK_RATE;
	}
	public String getCENTER_PPA_Y_JUDGE()
	{
		return CENTER_PPA_Y_JUDGE;
	}
	public void setCENTER_PPA_Y_JUDGE(String cENTER_PPA_Y_JUDGE)
	{
		CENTER_PPA_Y_JUDGE = cENTER_PPA_Y_JUDGE;
	}
	public String getCENTER_PPA_Y_MAX()
	{
		return CENTER_PPA_Y_MAX;
	}
	public void setCENTER_PPA_Y_MAX(String cENTER_PPA_Y_MAX)
	{
		CENTER_PPA_Y_MAX = cENTER_PPA_Y_MAX;
	}
	public String getCENTER_PPA_Y_MIN()
	{
		return CENTER_PPA_Y_MIN;
	}
	public void setCENTER_PPA_Y_MIN(String cENTER_PPA_Y_MIN)
	{
		CENTER_PPA_Y_MIN = cENTER_PPA_Y_MIN;
	}
	public String getPPA_Y_OK_RATE()
	{
		return PPA_Y_OK_RATE;
	}
	public void setPPA_Y_OK_RATE(String pPA_Y_OK_RATE)
	{
		PPA_Y_OK_RATE = pPA_Y_OK_RATE;
	}
	public String getSTRAIGHTNESS_X_MAX()
	{
		return STRAIGHTNESS_X_MAX;
	}
	public void setSTRAIGHTNESS_X_MAX(String sTRAIGHTNESS_X_MAX)
	{
		STRAIGHTNESS_X_MAX = sTRAIGHTNESS_X_MAX;
	}
	public String getSTRAIGHTNESS_Y_MAX()
	{
		return STRAIGHTNESS_Y_MAX;
	}
	public void setSTRAIGHTNESS_Y_MAX(String sTRAIGHTNESS_Y_MAX)
	{
		STRAIGHTNESS_Y_MAX = sTRAIGHTNESS_Y_MAX;
	}
	
	public String getMASK_CD_X_MAX()
	{
		return MASK_CD_X_MAX;
	}

	public void setMASK_CD_X_MAX(String mASK_CD_X_MAX)
	{
		MASK_CD_X_MAX = mASK_CD_X_MAX;
	}

	public String getMASK_CD_X_MIN()
	{
		return MASK_CD_X_MIN;
	}

	public void setMASK_CD_X_MIN(String mASK_CD_X_MIN)
	{
		MASK_CD_X_MIN = mASK_CD_X_MIN;
	}

	public String getMASK_CD_X_CPK()
	{
		return MASK_CD_X_CPK;
	}

	public void setMASK_CD_X_CPK(String mASK_CD_X_CPK)
	{
		MASK_CD_X_CPK = mASK_CD_X_CPK;
	}

	public String getMASK_CD_Y_MAX()
	{
		return MASK_CD_Y_MAX;
	}

	public void setMASK_CD_Y_MAX(String mASK_CD_Y_MAX)
	{
		MASK_CD_Y_MAX = mASK_CD_Y_MAX;
	}

	public String getMASK_CD_Y_MIN()
	{
		return MASK_CD_Y_MIN;
	}

	public void setMASK_CD_Y_MIN(String mASK_CD_Y_MIN)
	{
		MASK_CD_Y_MIN = mASK_CD_Y_MIN;
	}

	public String getMASK_CD_Y_CPK()
	{
		return MASK_CD_Y_CPK;
	}

	public void setMASK_CD_Y_CPK(String mASK_CD_Y_CPK)
	{
		MASK_CD_Y_CPK = mASK_CD_Y_CPK;
	}

	public long getMaskCycleCount()
	{
		return maskCycleCount;
	}
	public void setMaskCycleCount(long maskCycleCount)
	{
		this.maskCycleCount = maskCycleCount;
	}
	public String getLastEventName()
	{
		return lastEventName;
	}
	public void setLastEventName(String lastEventName)
	{
		this.lastEventName = lastEventName;
	}
	public Timestamp getLastEventTime()
	{
		return lastEventTime;
	}
	public void setLastEventTime(Timestamp lastEventTime)
	{
		this.lastEventTime = lastEventTime;
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
	public String getLastEventTimeKey()
	{
		return lastEventTimeKey;
	}
	public void setLastEventTimeKey(String lastEventTimeKey)
	{
		this.lastEventTimeKey = lastEventTimeKey;
	}
	
}
