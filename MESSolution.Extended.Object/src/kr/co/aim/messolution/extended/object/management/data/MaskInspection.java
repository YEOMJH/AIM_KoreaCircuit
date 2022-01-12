package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class MaskInspection extends UdfAccessor {


	@CTORMTemplate(seq = "1", name="machineName", type="Column", dataType="String", initial="", history="")
	private String machineName;
	@CTORMTemplate(seq = "2", name="unitName", type="Column", dataType="String", initial="", history="")
	private String unitName;
	@CTORMTemplate(seq = "3", name="subUnitName", type="Column", dataType="String", initial="", history="")
	private String subUnitName;
	@CTORMTemplate(seq = "4", name="maskLotName", type="Column", dataType="String", initial="", history="")
	private String maskLotName;
	@CTORMTemplate(seq = "5", name="frameName", type="Column", dataType="String", initial="", history="")
	private String frameName;
	@CTORMTemplate(seq = "6", name="recipeID", type="Column", dataType="String", initial="", history="")
	private String recipeID;
	@CTORMTemplate(seq = "7", name="maskType", type="Column", dataType="String", initial="", history="")
	private String maskType;
	@CTORMTemplate(seq = "8", name="maskStartTime", type="Column", dataType="String", initial="", history="")
	private String maskStartTime;
	@CTORMTemplate(seq = "9", name="maskEndTime", type="Column", dataType="String", initial="", history="")
	private String maskEndTime;
	@CTORMTemplate(seq = "10", name="maskJudge", type="Column", dataType="String", initial="", history="")
	private String maskJudge;
	@CTORMTemplate(seq = "11", name="ALIGN_HOLE_DESIGN_POSITIONX1", type="Column", dataType="String", initial="", history="")
	private String ALIGN_HOLE_DESIGN_POSITIONX1;
	@CTORMTemplate(seq = "12", name="ALIGN_HOLE_DESIGN_POSITIONX2", type="Column", dataType="String", initial="", history="")
	private String ALIGN_HOLE_DESIGN_POSITIONX2;
	@CTORMTemplate(seq = "13", name="ALIGN_HOLE_DESIGN_POSITIONX3", type="Column", dataType="String", initial="", history="")
	private String ALIGN_HOLE_DESIGN_POSITIONX3;
	@CTORMTemplate(seq = "14", name="ALIGN_HOLE_DESIGN_POSITIONX4", type="Column", dataType="String", initial="", history="")
	private String ALIGN_HOLE_DESIGN_POSITIONX4;
	@CTORMTemplate(seq = "15", name="ALIGN_HOLE_DESIGN_POSITIONY1", type="Column", dataType="String", initial="", history="")
	private String ALIGN_HOLE_DESIGN_POSITIONY1;
	@CTORMTemplate(seq = "16", name="ALIGN_HOLE_DESIGN_POSITIONY2", type="Column", dataType="String", initial="", history="")
	private String ALIGN_HOLE_DESIGN_POSITIONY2;
	@CTORMTemplate(seq = "17", name="ALIGN_HOLE_DESIGN_POSITIONY3", type="Column", dataType="String", initial="", history="")
	private String ALIGN_HOLE_DESIGN_POSITIONY3;
	@CTORMTemplate(seq = "18", name="ALIGN_HOLE_DESIGN_POSITIONY4", type="Column", dataType="String", initial="", history="")
	private String ALIGN_HOLE_DESIGN_POSITIONY4;
	@CTORMTemplate(seq = "19", name="ALIGN_HOLE_MEASURE_POSITIONX1", type="Column", dataType="String", initial="", history="")
	private String ALIGN_HOLE_MEASURE_POSITIONX1;
	@CTORMTemplate(seq = "20", name="ALIGN_HOLE_MEASURE_POSITIONX2", type="Column", dataType="String", initial="", history="")
	private String ALIGN_HOLE_MEASURE_POSITIONX2;
	@CTORMTemplate(seq = "21", name="ALIGN_HOLE_MEASURE_POSITIONX3", type="Column", dataType="String", initial="", history="")
	private String ALIGN_HOLE_MEASURE_POSITIONX3;
	@CTORMTemplate(seq = "22", name="ALIGN_HOLE_MEASURE_POSITIONX4", type="Column", dataType="String", initial="", history="")
	private String ALIGN_HOLE_MEASURE_POSITIONX4;
	@CTORMTemplate(seq = "23", name="ALIGN_HOLE_MEASURE_POSITIONY1", type="Column", dataType="String", initial="", history="")
	private String ALIGN_HOLE_MEASURE_POSITIONY1;
	@CTORMTemplate(seq = "24", name="ALIGN_HOLE_MEASURE_POSITIONY2", type="Column", dataType="String", initial="", history="")
	private String ALIGN_HOLE_MEASURE_POSITIONY2;
	@CTORMTemplate(seq = "25", name="ALIGN_HOLE_MEASURE_POSITIONY3", type="Column", dataType="String", initial="", history="")
	private String ALIGN_HOLE_MEASURE_POSITIONY3;
	@CTORMTemplate(seq = "26", name="ALIGN_HOLE_MEASURE_POSITIONY4", type="Column", dataType="String", initial="", history="")
	private String ALIGN_HOLE_MEASURE_POSITIONY4;
	@CTORMTemplate(seq = "27", name="ALIGN_HOLE_DESIGN_SIZE1", type="Column", dataType="String", initial="", history="")
	private String ALIGN_HOLE_DESIGN_SIZE1;
	@CTORMTemplate(seq = "28", name="ALIGN_HOLE_DESIGN_SIZE2", type="Column", dataType="String", initial="", history="")
	private String ALIGN_HOLE_DESIGN_SIZE2;
	@CTORMTemplate(seq = "29", name="ALIGN_HOLE_DESIGN_SIZE3", type="Column", dataType="String", initial="", history="")
	private String ALIGN_HOLE_DESIGN_SIZE3;
	@CTORMTemplate(seq = "30", name="ALIGN_HOLE_DESIGN_SIZE4", type="Column", dataType="String", initial="", history="")
	private String ALIGN_HOLE_DESIGN_SIZE4;
	@CTORMTemplate(seq = "31", name="ALIGN_HOLE_MEASURE_SIZE1", type="Column", dataType="String", initial="", history="")
	private String ALIGN_HOLE_MEASURE_SIZE1;
	@CTORMTemplate(seq = "32", name="ALIGN_HOLE_MEASURE_SIZE2", type="Column", dataType="String", initial="", history="")
	private String ALIGN_HOLE_MEASURE_SIZE2;
	@CTORMTemplate(seq = "33", name="ALIGN_HOLE_MEASURE_SIZE3", type="Column", dataType="String", initial="", history="")
	private String ALIGN_HOLE_MEASURE_SIZE3;
	@CTORMTemplate(seq = "34", name="ALIGN_HOLE_MEASURE_SIZE4", type="Column", dataType="String", initial="", history="")
	private String ALIGN_HOLE_MEASURE_SIZE4;
	@CTORMTemplate(seq = "35", name="Mask_CD_X_max", type="Column", dataType="String", initial="", history="")
	private String Mask_CD_X_max;
	@CTORMTemplate(seq = "36", name="Mask_CD_X_min", type="Column", dataType="String", initial="", history="")
	private String Mask_CD_X_min;
	@CTORMTemplate(seq = "37", name="Mask_CD_X_CPK", type="Column", dataType="String", initial="", history="")
	private String Mask_CD_X_CPK;
	@CTORMTemplate(seq = "38", name="Mask_CD_Y_max", type="Column", dataType="String", initial="", history="")
	private String Mask_CD_Y_max;
	@CTORMTemplate(seq = "39", name="Mask_CD_Y_min", type="Column", dataType="String", initial="", history="")
	private String Mask_CD_Y_min;
	@CTORMTemplate(seq = "40", name="Mask_CD_Y_CPK", type="Column", dataType="String", initial="", history="")
	private String Mask_CD_Y_CPK;
	@CTORMTemplate(seq = "41", name="PPA_X_JUDGE", type="Column", dataType="String", initial="", history="")
	private String PPA_X_JUDGE;
	@CTORMTemplate(seq = "42", name="PPA_X_MAX", type="Column", dataType="String", initial="", history="")
	private String PPA_X_MAX;
	@CTORMTemplate(seq = "43", name="PPA_X_MIN", type="Column", dataType="String", initial="", history="")
	private String PPA_X_MIN;
	@CTORMTemplate(seq = "44", name="PPA_X_OK_RATE", type="Column", dataType="String", initial="", history="")
	private String PPA_X_OK_RATE;
	@CTORMTemplate(seq = "45", name="PPA_Y_JUDGE", type="Column", dataType="String", initial="", history="")
	private String PPA_Y_JUDGE;
	@CTORMTemplate(seq = "46", name="PPA_Y_MAX", type="Column", dataType="String", initial="", history="")
	private String PPA_Y_MAX;
	@CTORMTemplate(seq = "47", name="PPA_Y_MIN", type="Column", dataType="String", initial="", history="")
	private String PPA_Y_MIN;
	@CTORMTemplate(seq = "48", name="PPA_Y_OK_RATE", type="Column", dataType="String", initial="", history="")
	private String PPA_Y_OK_RATE;
	@CTORMTemplate(seq = "49", name="flatNess", type="Column", dataType="String", initial="", history="")
	private String flatNess;
	@CTORMTemplate(seq = "50", name="STRAIGHTNESS_X", type="Column", dataType="String", initial="", history="")
	private String STRAIGHTNESS_X;
	@CTORMTemplate(seq = "51", name="STRAIGHTNESS_Y", type="Column", dataType="String", initial="", history="")
	private String STRAIGHTNESS_Y;
	@CTORMTemplate(seq = "52", name="lastEventName", type="Column", dataType="String", initial="", history="")
	private String lastEventName;
	@CTORMTemplate(seq = "53", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;
	@CTORMTemplate(seq = "54", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;
	@CTORMTemplate(seq = "55", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;
	@CTORMTemplate(seq = "56", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimeKey;
	public String getMachineName() {
		return machineName;
	}
	public String getUnitName() {
		return unitName;
	}
	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}
	public String getSubUnitName() {
		return subUnitName;
	}
	public void setSubUnitName(String subUnitName) {
		this.subUnitName = subUnitName;
	}
	public String getMaskLotName() {
		return maskLotName;
	}
	public void setMaskLotName(String maskLotName) {
		this.maskLotName = maskLotName;
	}
	public String getFrameName() {
		return frameName;
	}
	public void setFrameName(String frameName) {
		this.frameName = frameName;
	}
	public String getRecipeID() {
		return recipeID;
	}
	public void setRecipeID(String recipeID) {
		this.recipeID = recipeID;
	}
	public String getMaskType() {
		return maskType;
	}
	public void setMaskType(String maskType) {
		this.maskType = maskType;
	}
	public String getMaskStartTime() {
		return maskStartTime;
	}
	public void setMaskStartTime(String maskStartTime) {
		this.maskStartTime = maskStartTime;
	}
	public String getMaskEndTime() {
		return maskEndTime;
	}
	public void setMaskEndTime(String maskEndTime) {
		this.maskEndTime = maskEndTime;
	}
	public String getMaskJudge() {
		return maskJudge;
	}
	public void setMaskJudge(String maskJudge) {
		this.maskJudge = maskJudge;
	}
	public String getALIGN_HOLE_DESIGN_POSITIONX1() {
		return ALIGN_HOLE_DESIGN_POSITIONX1;
	}
	public void setALIGN_HOLE_DESIGN_POSITIONX1(String aLIGN_HOLE_DESIGN_POSITIONX1) {
		ALIGN_HOLE_DESIGN_POSITIONX1 = aLIGN_HOLE_DESIGN_POSITIONX1;
	}
	public String getALIGN_HOLE_DESIGN_POSITIONX2() {
		return ALIGN_HOLE_DESIGN_POSITIONX2;
	}
	public void setALIGN_HOLE_DESIGN_POSITIONX2(String aLIGN_HOLE_DESIGN_POSITIONX2) {
		ALIGN_HOLE_DESIGN_POSITIONX2 = aLIGN_HOLE_DESIGN_POSITIONX2;
	}
	public String getALIGN_HOLE_DESIGN_POSITIONX3() {
		return ALIGN_HOLE_DESIGN_POSITIONX3;
	}
	public void setALIGN_HOLE_DESIGN_POSITIONX3(String aLIGN_HOLE_DESIGN_POSITIONX3) {
		ALIGN_HOLE_DESIGN_POSITIONX3 = aLIGN_HOLE_DESIGN_POSITIONX3;
	}
	public String getALIGN_HOLE_DESIGN_POSITIONX4() {
		return ALIGN_HOLE_DESIGN_POSITIONX4;
	}
	public void setALIGN_HOLE_DESIGN_POSITIONX4(String aLIGN_HOLE_DESIGN_POSITIONX4) {
		ALIGN_HOLE_DESIGN_POSITIONX4 = aLIGN_HOLE_DESIGN_POSITIONX4;
	}
	public String getALIGN_HOLE_DESIGN_POSITIONY1() {
		return ALIGN_HOLE_DESIGN_POSITIONY1;
	}
	public void setALIGN_HOLE_DESIGN_POSITIONY1(String aLIGN_HOLE_DESIGN_POSITIONY1) {
		ALIGN_HOLE_DESIGN_POSITIONY1 = aLIGN_HOLE_DESIGN_POSITIONY1;
	}
	public String getALIGN_HOLE_DESIGN_POSITIONY2() {
		return ALIGN_HOLE_DESIGN_POSITIONY2;
	}
	public void setALIGN_HOLE_DESIGN_POSITIONY2(String aLIGN_HOLE_DESIGN_POSITIONY2) {
		ALIGN_HOLE_DESIGN_POSITIONY2 = aLIGN_HOLE_DESIGN_POSITIONY2;
	}
	public String getALIGN_HOLE_DESIGN_POSITIONY3() {
		return ALIGN_HOLE_DESIGN_POSITIONY3;
	}
	public void setALIGN_HOLE_DESIGN_POSITIONY3(String aLIGN_HOLE_DESIGN_POSITIONY3) {
		ALIGN_HOLE_DESIGN_POSITIONY3 = aLIGN_HOLE_DESIGN_POSITIONY3;
	}
	public String getALIGN_HOLE_DESIGN_POSITIONY4() {
		return ALIGN_HOLE_DESIGN_POSITIONY4;
	}
	public void setALIGN_HOLE_DESIGN_POSITIONY4(String aLIGN_HOLE_DESIGN_POSITIONY4) {
		ALIGN_HOLE_DESIGN_POSITIONY4 = aLIGN_HOLE_DESIGN_POSITIONY4;
	}
	public String getALIGN_HOLE_MEASURE_POSITIONX1() {
		return ALIGN_HOLE_MEASURE_POSITIONX1;
	}
	public void setALIGN_HOLE_MEASURE_POSITIONX1(
			String aLIGN_HOLE_MEASURE_POSITIONX1) {
		ALIGN_HOLE_MEASURE_POSITIONX1 = aLIGN_HOLE_MEASURE_POSITIONX1;
	}
	public String getALIGN_HOLE_MEASURE_POSITIONX2() {
		return ALIGN_HOLE_MEASURE_POSITIONX2;
	}
	public void setALIGN_HOLE_MEASURE_POSITIONX2(
			String aLIGN_HOLE_MEASURE_POSITIONX2) {
		ALIGN_HOLE_MEASURE_POSITIONX2 = aLIGN_HOLE_MEASURE_POSITIONX2;
	}
	public String getALIGN_HOLE_MEASURE_POSITIONX3() {
		return ALIGN_HOLE_MEASURE_POSITIONX3;
	}
	public void setALIGN_HOLE_MEASURE_POSITIONX3(
			String aLIGN_HOLE_MEASURE_POSITIONX3) {
		ALIGN_HOLE_MEASURE_POSITIONX3 = aLIGN_HOLE_MEASURE_POSITIONX3;
	}
	public String getALIGN_HOLE_MEASURE_POSITIONX4() {
		return ALIGN_HOLE_MEASURE_POSITIONX4;
	}
	public void setALIGN_HOLE_MEASURE_POSITIONX4(
			String aLIGN_HOLE_MEASURE_POSITIONX4) {
		ALIGN_HOLE_MEASURE_POSITIONX4 = aLIGN_HOLE_MEASURE_POSITIONX4;
	}
	public String getALIGN_HOLE_MEASURE_POSITIONY1() {
		return ALIGN_HOLE_MEASURE_POSITIONY1;
	}
	public void setALIGN_HOLE_MEASURE_POSITIONY1(
			String aLIGN_HOLE_MEASURE_POSITIONY1) {
		ALIGN_HOLE_MEASURE_POSITIONY1 = aLIGN_HOLE_MEASURE_POSITIONY1;
	}
	public String getALIGN_HOLE_MEASURE_POSITIONY2() {
		return ALIGN_HOLE_MEASURE_POSITIONY2;
	}
	public void setALIGN_HOLE_MEASURE_POSITIONY2(
			String aLIGN_HOLE_MEASURE_POSITIONY2) {
		ALIGN_HOLE_MEASURE_POSITIONY2 = aLIGN_HOLE_MEASURE_POSITIONY2;
	}
	public String getALIGN_HOLE_MEASURE_POSITIONY3() {
		return ALIGN_HOLE_MEASURE_POSITIONY3;
	}
	public void setALIGN_HOLE_MEASURE_POSITIONY3(
			String aLIGN_HOLE_MEASURE_POSITIONY3) {
		ALIGN_HOLE_MEASURE_POSITIONY3 = aLIGN_HOLE_MEASURE_POSITIONY3;
	}
	public String getALIGN_HOLE_MEASURE_POSITIONY4() {
		return ALIGN_HOLE_MEASURE_POSITIONY4;
	}
	public void setALIGN_HOLE_MEASURE_POSITIONY4(
			String aLIGN_HOLE_MEASURE_POSITIONY4) {
		ALIGN_HOLE_MEASURE_POSITIONY4 = aLIGN_HOLE_MEASURE_POSITIONY4;
	}
	public String getALIGN_HOLE_DESIGN_SIZE1() {
		return ALIGN_HOLE_DESIGN_SIZE1;
	}
	public void setALIGN_HOLE_DESIGN_SIZE1(String aLIGN_HOLE_DESIGN_SIZE1) {
		ALIGN_HOLE_DESIGN_SIZE1 = aLIGN_HOLE_DESIGN_SIZE1;
	}
	public String getALIGN_HOLE_DESIGN_SIZE2() {
		return ALIGN_HOLE_DESIGN_SIZE2;
	}
	public void setALIGN_HOLE_DESIGN_SIZE2(String aLIGN_HOLE_DESIGN_SIZE2) {
		ALIGN_HOLE_DESIGN_SIZE2 = aLIGN_HOLE_DESIGN_SIZE2;
	}
	public String getALIGN_HOLE_DESIGN_SIZE3() {
		return ALIGN_HOLE_DESIGN_SIZE3;
	}
	public void setALIGN_HOLE_DESIGN_SIZE3(String aLIGN_HOLE_DESIGN_SIZE3) {
		ALIGN_HOLE_DESIGN_SIZE3 = aLIGN_HOLE_DESIGN_SIZE3;
	}
	public String getALIGN_HOLE_DESIGN_SIZE4() {
		return ALIGN_HOLE_DESIGN_SIZE4;
	}
	public void setALIGN_HOLE_DESIGN_SIZE4(String aLIGN_HOLE_DESIGN_SIZE4) {
		ALIGN_HOLE_DESIGN_SIZE4 = aLIGN_HOLE_DESIGN_SIZE4;
	}
	public String getALIGN_HOLE_MEASURE_SIZE1() {
		return ALIGN_HOLE_MEASURE_SIZE1;
	}
	public void setALIGN_HOLE_MEASURE_SIZE1(String aLIGN_HOLE_MEASURE_SIZE1) {
		ALIGN_HOLE_MEASURE_SIZE1 = aLIGN_HOLE_MEASURE_SIZE1;
	}
	public String getALIGN_HOLE_MEASURE_SIZE2() {
		return ALIGN_HOLE_MEASURE_SIZE2;
	}
	public void setALIGN_HOLE_MEASURE_SIZE2(String aLIGN_HOLE_MEASURE_SIZE2) {
		ALIGN_HOLE_MEASURE_SIZE2 = aLIGN_HOLE_MEASURE_SIZE2;
	}
	public String getALIGN_HOLE_MEASURE_SIZE3() {
		return ALIGN_HOLE_MEASURE_SIZE3;
	}
	public void setALIGN_HOLE_MEASURE_SIZE3(String aLIGN_HOLE_MEASURE_SIZE3) {
		ALIGN_HOLE_MEASURE_SIZE3 = aLIGN_HOLE_MEASURE_SIZE3;
	}
	public String getALIGN_HOLE_MEASURE_SIZE4() {
		return ALIGN_HOLE_MEASURE_SIZE4;
	}
	public void setALIGN_HOLE_MEASURE_SIZE4(String aLIGN_HOLE_MEASURE_SIZE4) {
		ALIGN_HOLE_MEASURE_SIZE4 = aLIGN_HOLE_MEASURE_SIZE4;
	}
	public String getMask_CD_X_max() {
		return Mask_CD_X_max;
	}
	public void setMask_CD_X_max(String mask_CD_X_max) {
		Mask_CD_X_max = mask_CD_X_max;
	}
	public String getMask_CD_X_min() {
		return Mask_CD_X_min;
	}
	public void setMask_CD_X_min(String mask_CD_X_min) {
		Mask_CD_X_min = mask_CD_X_min;
	}
	public String getMask_CD_X_CPK() {
		return Mask_CD_X_CPK;
	}
	public void setMask_CD_X_CPK(String mask_CD_X_CPK) {
		Mask_CD_X_CPK = mask_CD_X_CPK;
	}
	public String getMask_CD_Y_max() {
		return Mask_CD_Y_max;
	}
	public void setMask_CD_Y_max(String mask_CD_Y_max) {
		Mask_CD_Y_max = mask_CD_Y_max;
	}
	public String getMask_CD_Y_min() {
		return Mask_CD_Y_min;
	}
	public void setMask_CD_Y_min(String mask_CD_Y_min) {
		Mask_CD_Y_min = mask_CD_Y_min;
	}
	public String getMask_CD_Y_CPK() {
		return Mask_CD_Y_CPK;
	}
	public void setMask_CD_Y_CPK(String mask_CD_Y_CPK) {
		Mask_CD_Y_CPK = mask_CD_Y_CPK;
	}
	public String getPPA_X_JUDGE() {
		return PPA_X_JUDGE;
	}
	public void setPPA_X_JUDGE(String pPA_X_JUDGE) {
		PPA_X_JUDGE = pPA_X_JUDGE;
	}
	public String getPPA_X_MAX() {
		return PPA_X_MAX;
	}
	public void setPPA_X_MAX(String pPA_X_MAX) {
		PPA_X_MAX = pPA_X_MAX;
	}
	public String getPPA_X_MIN() {
		return PPA_X_MIN;
	}
	public void setPPA_X_MIN(String pPA_X_MIN) {
		PPA_X_MIN = pPA_X_MIN;
	}
	public String getPPA_X_OK_RATE() {
		return PPA_X_OK_RATE;
	}
	public void setPPA_X_OK_RATE(String pPA_X_OK_RATE) {
		PPA_X_OK_RATE = pPA_X_OK_RATE;
	}
	public String getPPA_Y_JUDGE() {
		return PPA_Y_JUDGE;
	}
	public void setPPA_Y_JUDGE(String pPA_Y_JUDGE) {
		PPA_Y_JUDGE = pPA_Y_JUDGE;
	}
	public String getPPA_Y_MAX() {
		return PPA_Y_MAX;
	}
	public void setPPA_Y_MAX(String pPA_Y_MAX) {
		PPA_Y_MAX = pPA_Y_MAX;
	}
	public String getPPA_Y_MIN() {
		return PPA_Y_MIN;
	}
	public void setPPA_Y_MIN(String pPA_Y_MIN) {
		PPA_Y_MIN = pPA_Y_MIN;
	}
	public String getPPA_Y_OK_RATE() {
		return PPA_Y_OK_RATE;
	}
	public void setPPA_Y_OK_RATE(String pPA_Y_OK_RATE) {
		PPA_Y_OK_RATE = pPA_Y_OK_RATE;
	}
	public String getFlatNess() {
		return flatNess;
	}
	public void setFlatNess(String flatNess) {
		this.flatNess = flatNess;
	}
	public String getSTRAIGHTNESS_X() {
		return STRAIGHTNESS_X;
	}
	public void setSTRAIGHTNESS_X(String sTRAIGHTNESS_X) {
		STRAIGHTNESS_X = sTRAIGHTNESS_X;
	}
	public String getSTRAIGHTNESS_Y() {
		return STRAIGHTNESS_Y;
	}
	public void setSTRAIGHTNESS_Y(String sTRAIGHTNESS_Y) {
		STRAIGHTNESS_Y = sTRAIGHTNESS_Y;
	}
	public String getLastEventName() {
		return lastEventName;
	}
	public void setLastEventName(String lastEventName) {
		this.lastEventName = lastEventName;
	}
	public Timestamp getLastEventTime() {
		return lastEventTime;
	}
	public void setLastEventTime(Timestamp lastEventTime) {
		this.lastEventTime = lastEventTime;
	}
	public String getLastEventUser() {
		return lastEventUser;
	}
	public void setLastEventUser(String lastEventUser) {
		this.lastEventUser = lastEventUser;
	}
	public String getLastEventComment() {
		return lastEventComment;
	}
	public void setLastEventComment(String lastEventComment) {
		this.lastEventComment = lastEventComment;
	}
	public String getLastEventTimeKey() {
		return lastEventTimeKey;
	}
	public void setLastEventTimeKey(String lastEventTimeKey) {
		this.lastEventTimeKey = lastEventTimeKey;
	}
	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}

	
}
