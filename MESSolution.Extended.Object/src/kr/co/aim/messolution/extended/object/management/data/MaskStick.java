package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class MaskStick extends UdfAccessor{
	@CTORMTemplate(seq = "1", name="stickName", type="Key", dataType="String", initial="", history="")
	private String stickName;
	@CTORMTemplate(seq = "2", name="stickSpecName", type="Column", dataType="String", initial="", history="")
	private String stickSpecName;
	@CTORMTemplate(seq = "3", name="stickState", type="Column", dataType="String", initial="", history="")
	private String stickState;
	@CTORMTemplate(seq = "4", name="stickJudge", type="Column", dataType="String", initial="", history="")
	private String stickJudge;
	@CTORMTemplate(seq = "5", name="stickGrade", type="Column", dataType="String", initial="", history="")
	private String stickGrade;
	@CTORMTemplate(seq = "6", name="machineName", type="Column", dataType="String", initial="", history="")
	private String machineName;
	@CTORMTemplate(seq = "7", name="recipeID", type="Column", dataType="String", initial="", history="")
	private String recipeID;
	@CTORMTemplate(seq = "8", name="type", type="Column", dataType="String", initial="", history="")
	private String type;
	@CTORMTemplate(seq = "9", name="TP_X", type="Column", dataType="String", initial="", history="")
	private String TP_X;
	@CTORMTemplate(seq = "10", name="TP_X_JUDGE", type="Column", dataType="String", initial="", history="")
	private String TP_X_JUDGE;
	@CTORMTemplate(seq = "11", name="TP_Y", type="Column", dataType="String", initial="", history="")
	private String TP_Y;
	@CTORMTemplate(seq = "12", name="TP_Y_JUDGE", type="Column", dataType="String", initial="", history="")
	private String TP_Y_JUDGE;
	@CTORMTemplate(seq = "13", name="strightness", type="Column", dataType="String", initial="", history="")
	private String strightness;
	@CTORMTemplate(seq = "14", name="sharp", type="Column", dataType="String", initial="", history="")
	private String sharp;
	@CTORMTemplate(seq = "15", name="defect_No", type="Column", dataType="String", initial="", history="")
	private String defect_No;
	@CTORMTemplate(seq = "16", name="CD_X_MAX", type="Column", dataType="String", initial="", history="")
	private String CD_X_MAX;
	@CTORMTemplate(seq = "17", name="CD_X_MIN", type="Column", dataType="String", initial="", history="")
	private String CD_X_MIN;
	@CTORMTemplate(seq = "18", name="CD_X_AVE", type="Column", dataType="String", initial="", history="")
	private String CD_X_AVE;
	@CTORMTemplate(seq = "19", name="CD_X_CPK", type="Column", dataType="String", initial="", history="")
	private String CD_X_CPK;
	@CTORMTemplate(seq = "20", name="CD_Y_MAX", type="Column", dataType="String", initial="", history="")
	private String CD_Y_MAX;
	@CTORMTemplate(seq = "21", name="CD_Y_MIN", type="Column", dataType="String", initial="", history="")
	private String CD_Y_MIN;
	@CTORMTemplate(seq = "22", name="CD_Y_AVE", type="Column", dataType="String", initial="", history="")
	private String CD_Y_AVE;
	@CTORMTemplate(seq = "23", name="CD_Y_CPK", type="Column", dataType="String", initial="", history="")
	private String CD_Y_CPK;
	@CTORMTemplate(seq = "24", name="maskLotName", type="Column", dataType="String", initial="", history="")
	private String maskLotName;
	@CTORMTemplate(seq = "25", name="reasonCode", type="Column", dataType="String", initial="", history="")
	private String reasonCode;
	@CTORMTemplate(seq = "26", name="reasonCodeType", type="Column", dataType="String", initial="", history="")
	private String reasonCodeType;
	@CTORMTemplate(seq = "27", name="createUser", type="Column", dataType="String", initial="", history="")
	private String createUser;
	@CTORMTemplate(seq = "28", name="receiveTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp receiveTime;
	@CTORMTemplate(seq = "29", name="measurementTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp measurementTime;
	@CTORMTemplate(seq = "30", name="vendorName", type="Column", dataType="String", initial="", history="")
	private String vendorName;
	@CTORMTemplate(seq = "31", name="bankType", type="Column", dataType="String", initial="", history="")
	private String bankType;
	@CTORMTemplate(seq = "32", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;
	@CTORMTemplate(seq = "33", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;
	@CTORMTemplate(seq = "34", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;
	@CTORMTemplate(seq = "35", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimeKey;
	@CTORMTemplate(seq = "36", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;
	@CTORMTemplate(seq = "37", name="stickFilmLayer", type="Column", dataType="String", initial="", history="")
	private String stickFilmLayer;
	@CTORMTemplate(seq = "38", name="stickType", type="Column", dataType="String", initial="", history="")
	private String stickType;
	@CTORMTemplate(seq = "39", name="position", type="Column", dataType="Number", initial="", history="")
	private long position;
	@CTORMTemplate(seq = "40", name="TAPER_ANGLE_3D", type="Column", dataType="String", initial="", history="")
	private String TAPER_ANGLE_3D;
	@CTORMTemplate(seq = "41", name="RIB_THICKNESS_3D", type="Column", dataType="String", initial="", history="")
	private String RIB_THICKNESS_3D;
	@CTORMTemplate(seq = "42", name="STEP_HIGHT_3D", type="Column", dataType="String", initial="", history="")
	private String STEP_HIGHT_3D;
	@CTORMTemplate(seq = "43", name="STEP_WIDTH_3D", type="Column", dataType="String", initial="", history="")
	private String STEP_WIDTH_3D;
	@CTORMTemplate(seq = "44", name="texture", type="Column", dataType="String", initial="", history="")
	private String texture;	
	@CTORMTemplate(seq = "45", name="length", type="Column", dataType="String", initial="", history="")
	private String length;
	@CTORMTemplate(seq = "46", name="width", type="Column", dataType="String", initial="", history="")
	private String width;
	@CTORMTemplate(seq = "47", name="thickness", type="Column", dataType="String", initial="", history="")
	private String thickness;
	@CTORMTemplate(seq = "48", name="COA", type="Column", dataType="String", initial="", history="")
	private String COA;
	@CTORMTemplate(seq = "49", name="CPA", type="Column", dataType="String", initial="", history="")
	private String CPA;
	@CTORMTemplate(seq = "50", name="position1X", type="Column", dataType="String", initial="", history="")
	private String position1X;
	@CTORMTemplate(seq = "51", name="position1Y", type="Column", dataType="String", initial="", history="")
	private String position1Y;
	@CTORMTemplate(seq = "52", name="position2X", type="Column", dataType="String", initial="", history="")
	private String position2X;
	@CTORMTemplate(seq = "53", name="position2Y", type="Column", dataType="String", initial="", history="")
	private String position2Y;
	@CTORMTemplate(seq = "54", name="alignPositionVal", type="Column", dataType="String", initial="", history="")
	private String alignPositionVal;
	@CTORMTemplate(seq = "55", name="alignHoleRn", type="Column", dataType="String", initial="", history="")
	private String alignHoleRn;
	@CTORMTemplate(seq = "56", name="CD_X_COA_MAX", type="Column", dataType="String", initial="", history="")
	private String CD_X_COA_MAX;
	@CTORMTemplate(seq = "57", name="CD_X_COA_MIN", type="Column", dataType="String", initial="", history="")
	private String CD_X_COA_MIN;
	@CTORMTemplate(seq = "58", name="CD_X_COA_AVE", type="Column", dataType="String", initial="", history="")
	private String CD_X_COA_AVE;
	@CTORMTemplate(seq = "59", name="CD_X_COA_CPK", type="Column", dataType="String", initial="", history="")
	private String CD_X_COA_CPK;
	@CTORMTemplate(seq = "60", name="CD_Y_COA_MAX", type="Column", dataType="String", initial="", history="")
	private String CD_Y_COA_MAX;
	@CTORMTemplate(seq = "61", name="CD_Y_COA_MIN", type="Column", dataType="String", initial="", history="")
	private String CD_Y_COA_MIN;
	@CTORMTemplate(seq = "62", name="CD_Y_COA_AVE", type="Column", dataType="String", initial="", history="")
	private String CD_Y_COA_AVE;
	@CTORMTemplate(seq = "63", name="CD_Y_COA_CPK", type="Column", dataType="String", initial="", history="")
	private String CD_Y_COA_CPK;
	@CTORMTemplate(seq = "64", name="TP_X_COA", type="Column", dataType="String", initial="", history="")
	private String TP_X_COA;
	@CTORMTemplate(seq = "65", name="TP_Y_COA", type="Column", dataType="String", initial="", history="")
	private String TP_Y_COA;
	@CTORMTemplate(seq = "66", name="createTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp createTime;
	@CTORMTemplate(seq = "67", name="useTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp useTime;
		
	public String getStickName() {
		return stickName;
	}
	public void setStickName(String stickName) {
		this.stickName = stickName;
	}
	public String getStickSpecName() {
		return stickSpecName;
	}
	public void setStickSpecName(String stickSpecName) {
		this.stickSpecName = stickSpecName;
	}
	public String getStickState() {
		return stickState;
	}
	public void setStickState(String stickState) {
		this.stickState = stickState;
	}
	public String getStickJudge() {
		return stickJudge;
	}
	public void setStickJudge(String stickJudge) {
		this.stickJudge = stickJudge;
	}
	public String getStickGrade() {
		return stickGrade;
	}
	public void setStickGrade(String stickGrade) {
		this.stickGrade = stickGrade;
	}
	public String getMachineName() {
		return machineName;
	}
	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}
	public String getRecipeID() {
		return recipeID;
	}
	public void setRecipeID(String recipeID) {
		this.recipeID = recipeID;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getTP_X() {
		return TP_X;
	}
	public void setTP_X(String tP_X) {
		TP_X = tP_X;
	}
	public String getTP_X_JUDGE() {
		return TP_X_JUDGE;
	}
	public void setTP_X_JUDGE(String tP_X_JUDGE) {
		TP_X_JUDGE = tP_X_JUDGE;
	}
	public String getTP_Y() {
		return TP_Y;
	}
	public void setTP_Y(String tP_Y) {
		TP_Y = tP_Y;
	}
	public String getTP_Y_JUDGE() {
		return TP_Y_JUDGE;
	}
	public void setTP_Y_JUDGE(String tP_Y_JUDGE) {
		TP_Y_JUDGE = tP_Y_JUDGE;
	}
	public String getStrightness() {
		return strightness;
	}
	public void setStrightness(String strightness) {
		this.strightness = strightness;
	}
	public String getSharp() {
		return sharp;
	}
	public void setSharp(String sharp) {
		this.sharp = sharp;
	}
	public String getDefect_No() {
		return defect_No;
	}
	public void setDefect_No(String defect_No) {
		this.defect_No = defect_No;
	}
	public String getCD_X_MAX() {
		return CD_X_MAX;
	}
	public void setCD_X_MAX(String cD_X_MAX) {
		CD_X_MAX = cD_X_MAX;
	}
	public String getCD_X_MIN() {
		return CD_X_MIN;
	}
	public void setCD_X_MIN(String cD_X_MIN) {
		CD_X_MIN = cD_X_MIN;
	}
	public String getCD_X_AVE() {
		return CD_X_AVE;
	}
	public void setCD_X_AVE(String cD_X_AVE) {
		CD_X_AVE = cD_X_AVE;
	}
	public String getCD_X_CPK() {
		return CD_X_CPK;
	}
	public void setCD_X_CPK(String cD_X_CPK) {
		CD_X_CPK = cD_X_CPK;
	}
	public String getCD_Y_MAX() {
		return CD_Y_MAX;
	}
	public void setCD_Y_MAX(String cD_Y_MAX) {
		CD_Y_MAX = cD_Y_MAX;
	}
	public String getCD_Y_MIN() {
		return CD_Y_MIN;
	}
	public void setCD_Y_MIN(String cD_Y_MIN) {
		CD_Y_MIN = cD_Y_MIN;
	}
	public String getCD_Y_AVE() {
		return CD_Y_AVE;
	}
	public void setCD_Y_AVE(String cD_Y_AVE) {
		CD_Y_AVE = cD_Y_AVE;
	}
	public String getCD_Y_CPK() {
		return CD_Y_CPK;
	}
	public void setCD_Y_CPK(String cD_Y_CPK) {
		CD_Y_CPK = cD_Y_CPK;
	}
	public String getMaskLotName() {
		return maskLotName;
	}
	public void setMaskLotName(String maskLotName) {
		this.maskLotName = maskLotName;
	}
	public String getReasonCode() {
		return reasonCode;
	}
	public void setReasonCode(String reasonCode) {
		this.reasonCode = reasonCode;
	}
	public String getReasonCodeType() {
		return reasonCodeType;
	}
	public void setReasonCodeType(String reasonCodeType) {
		this.reasonCodeType = reasonCodeType;
	}
	public String getCreateUser() {
		return createUser;
	}
	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}
	public Timestamp getReceiveTime() {
		return receiveTime;
	}
	public void setReceiveTime(Timestamp receiveTime) {
		this.receiveTime = receiveTime;
	}
	public Timestamp getMeasurementTime() {
		return measurementTime;
	}
	public void setMeasurementTime(Timestamp measurementTime) {
		this.measurementTime = measurementTime;
	}
	public String getVendorName() {
		return vendorName;
	}
	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}
	public String getBankType() {
		return bankType;
	}
	public void setBankType(String bankType) {
		this.bankType = bankType;
	}
	public String getLastEventComment() {
		return lastEventComment;
	}
	public void setLastEventComment(String lastEventComment) {
		this.lastEventComment = lastEventComment;
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
	public String getLastEventTimeKey() {
		return lastEventTimeKey;
	}
	public void setLastEventTimeKey(String lastEventTimekey) {
		this.lastEventTimeKey = lastEventTimekey;
	}
	public String getLastEventUser() {
		return lastEventUser;
	}
	public void setLastEventUser(String lastEventUser) {
		this.lastEventUser = lastEventUser;
	}
	public String getStickFilmLayer() {
		return stickFilmLayer;
	}
	public void setStickFilmLayer(String stickFilmLayer) {
		this.stickFilmLayer = stickFilmLayer;
	}
	public String getStickType()
	{
		return stickType;
	}
	public void setStickType(String stickType)
	{
		this.stickType = stickType;
	}
	public long getPosition()
	{
		return position;
	}
	public void setPosition(long position)
	{
		this.position = position;
	}
	public String getTAPER_ANGLE_3D()
	{
		return TAPER_ANGLE_3D;
	}
	public void setTAPER_ANGLE_3D(String tAPER_ANGLE_3D)
	{
		TAPER_ANGLE_3D = tAPER_ANGLE_3D;
	}
	public String getRIB_THICKNESS_3D()
	{
		return RIB_THICKNESS_3D;
	}
	public void setRIB_THICKNESS_3D(String rIB_THICKNESS_3D)
	{
		RIB_THICKNESS_3D = rIB_THICKNESS_3D;
	}
	public String getSTEP_HIGHT_3D()
	{
		return STEP_HIGHT_3D;
	}
	public void setSTEP_HIGHT_3D(String sTEP_HIGHT_3D)
	{
		STEP_HIGHT_3D = sTEP_HIGHT_3D;
	}
	public String getSTEP_WIDTH_3D()
	{
		return STEP_WIDTH_3D;
	}
	public void setSTEP_WIDTH_3D(String sTEP_WIDTH_3D)
	{
		STEP_WIDTH_3D = sTEP_WIDTH_3D;
	}
	public String getTexture() {
		return texture;
	}
	public void setTexture(String texture) {
		this.texture = texture;
	}
	public String getLength() {
		return length;
	}
	public void setLength(String length) {
		this.length = length;
	}
	public String getWidth() {
		return width;
	}
	public void setWidth(String width) {
		this.width = width;
	}
	public String getThickness() {
		return thickness;
	}
	public void setThickness(String thickness) {
		this.thickness = thickness;
	}
	public String getCOA() {
		return COA;
	}
	public void setCOA(String cOA) {
		COA = cOA;
	}
	public String getCPA() {
		return CPA;
	}
	public void setCPA(String cPA) {
		CPA = cPA;
	}
	public String getPosition1X() {
		return position1X;
	}
	public void setPosition1X(String position1x) {
		position1X = position1x;
	}
	public String getPosition1Y() {
		return position1Y;
	}
	public void setPosition1Y(String position1y) {
		position1Y = position1y;
	}
	public String getPosition2X() {
		return position2X;
	}
	public void setPosition2X(String position2x) {
		position2X = position2x;
	}
	public String getPosition2Y() {
		return position2Y;
	}
	public void setPosition2Y(String position2y) {
		position2Y = position2y;
	}
	public String getAlignPositionVal() {
		return alignPositionVal;
	}
	public void setAlignPositionVal(String alignPositionVal) {
		this.alignPositionVal = alignPositionVal;
	}
	public String getAlignHoleRn() {
		return alignHoleRn;
	}
	public void setAlignHoleRn(String alignHoleRn) {
		this.alignHoleRn = alignHoleRn;
	}
	public String getCD_X_COA_MAX() {
		return CD_X_COA_MAX;
	}
	public void setCD_X_COA_MAX(String cD_X_COA_MAX) {
		CD_X_COA_MAX = cD_X_COA_MAX;
	}
	public String getCD_X_COA_MIN() {
		return CD_X_COA_MIN;
	}
	public void setCD_X_COA_MIN(String cD_X_COA_MIN) {
		CD_X_COA_MIN = cD_X_COA_MIN;
	}
	public String getCD_X_COA_AVE() {
		return CD_X_COA_AVE;
	}
	public void setCD_X_COA_AVE(String cD_X_COA_AVE) {
		CD_X_COA_AVE = cD_X_COA_AVE;
	}
	public String getCD_X_COA_CPK() {
		return CD_X_COA_CPK;
	}
	public void setCD_X_COA_CPK(String cD_X_COA_CPK) {
		CD_X_COA_CPK = cD_X_COA_CPK;
	}
	public String getCD_Y_COA_MAX() {
		return CD_Y_COA_MAX;
	}
	public void setCD_Y_COA_MAX(String cD_Y_COA_MAX) {
		CD_Y_COA_MAX = cD_Y_COA_MAX;
	}
	public String getCD_Y_COA_MIN() {
		return CD_Y_COA_MIN;
	}
	public void setCD_Y_COA_MIN(String cD_Y_COA_MIN) {
		CD_Y_COA_MIN = cD_Y_COA_MIN;
	}
	public String getCD_Y_COA_AVE() {
		return CD_Y_COA_AVE;
	}
	public void setCD_Y_COA_AVE(String cD_Y_COA_AVE) {
		CD_Y_COA_AVE = cD_Y_COA_AVE;
	}
	public String getCD_Y_COA_CPK() {
		return CD_Y_COA_CPK;
	}
	public void setCD_Y_COA_CPK(String cD_Y_COA_CPK) {
		CD_Y_COA_CPK = cD_Y_COA_CPK;
	}
	public String getTP_X_COA() {
		return TP_X_COA;
	}
	public void setTP_X_COA(String tP_X_COA) {
		TP_X_COA = tP_X_COA;
	}
	public String getTP_Y_COA() {
		return TP_Y_COA;
	}
	public void setTP_Y_COA(String tP_Y_COA) {
		TP_Y_COA = tP_Y_COA;
	}
	public Timestamp getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}
	public Timestamp getUseTime() {
		return useTime;
	}
	public void setUseTime(Timestamp useTime) {
		this.useTime = useTime;
	}
}
