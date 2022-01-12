package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class MaskFrame extends UdfAccessor {


	@CTORMTemplate(seq = "1", name="frameName", type="Key", dataType="String", initial="", history="")
	private String frameName;
	@CTORMTemplate(seq = "2", name="shippingDate", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp shippingDate;
	@CTORMTemplate(seq = "3", name="frameState", type="Column", dataType="String", initial="", history="")
	private String frameState;
	@CTORMTemplate(seq = "4", name="maskLotName", type="Column", dataType="String", initial="", history="")
	private String maskLotName;
	@CTORMTemplate(seq = "5", name="vendorName", type="Column", dataType="String", initial="", history="")
	private String vendorName;
	@CTORMTemplate(seq = "6", name="thickNess", type="Column", dataType="String", initial="", history="")
	private String thickNess;
	@CTORMTemplate(seq = "7", name="flatNess", type="Column", dataType="String", initial="", history="")
	private String flatNess;
	@CTORMTemplate(seq = "8", name="outside1Length", type="Column", dataType="String", initial="", history="")
	private String outside1Length;
	@CTORMTemplate(seq = "9", name="outside1Width", type="Column", dataType="String", initial="", history="")
	private String outside1Width;
	@CTORMTemplate(seq = "10", name="internal1Length", type="Column", dataType="String", initial="", history="")
	private String internal1Length;
	@CTORMTemplate(seq = "11", name="internal1Width", type="Column", dataType="String", initial="", history="")
	private String internal1Width;
	@CTORMTemplate(seq = "12", name="outside2Length", type="Column", dataType="String", initial="", history="")
	private String outside2Length;
	@CTORMTemplate(seq = "13", name="outside2Width", type="Column", dataType="String", initial="", history="")
	private String outside2Width;
	@CTORMTemplate(seq = "14", name="internal2Length", type="Column", dataType="String", initial="", history="")
	private String internal2Length;
	@CTORMTemplate(seq = "15", name="internal2Width", type="Column", dataType="String", initial="", history="")
	private String internal2Width;
	@CTORMTemplate(seq = "16", name="alignPosition1DesignValX", type="Column", dataType="String", initial="", history="")
	private String alignPosition1DesignValX;
	@CTORMTemplate(seq = "17", name="alignPosition1DesignValY", type="Column", dataType="String", initial="", history="")
	private String alignPosition1DesignValY;
	@CTORMTemplate(seq = "18", name="alignPosition1MeasureValX", type="Column", dataType="String", initial="", history="")
	private String alignPosition1MeasureValX;
	@CTORMTemplate(seq = "19", name="alignPosition1MeasureValY", type="Column", dataType="String", initial="", history="")
	private String alignPosition1MeasureValY;
	@CTORMTemplate(seq = "20", name="alignPosition1DesignSize", type="Column", dataType="String", initial="", history="")
	private String alignPosition1DesignSize;
	@CTORMTemplate(seq = "21", name="alignPosition1MeasureSize", type="Column", dataType="String", initial="", history="")
	private String alignPosition1MeasureSize;
	@CTORMTemplate(seq = "22", name="alignPosition2DesignValX", type="Column", dataType="String", initial="", history="")
	private String alignPosition2DesignValX;
	@CTORMTemplate(seq = "23", name="alignPosition2DesignValY", type="Column", dataType="String", initial="", history="")
	private String alignPosition2DesignValY;
	@CTORMTemplate(seq = "24", name="alignPosition2MeasureValX", type="Column", dataType="String", initial="", history="")
	private String alignPosition2MeasureValX;
	@CTORMTemplate(seq = "25", name="alignPosition2MeasureValY", type="Column", dataType="String", initial="", history="")
	private String alignPosition2MeasureValY;
	@CTORMTemplate(seq = "26", name="alignPosition2DesignSize", type="Column", dataType="String", initial="", history="")
	private String alignPosition2DesignSize;
	@CTORMTemplate(seq = "27", name="alignPosition2MeasureSize", type="Column", dataType="String", initial="", history="")
	private String alignPosition2MeasureSize;
	@CTORMTemplate(seq = "28", name="alignPosition3DesignValX", type="Column", dataType="String", initial="", history="")
	private String alignPosition3DesignValX;
	@CTORMTemplate(seq = "29", name="alignPosition3DesignValY", type="Column", dataType="String", initial="", history="")
	private String alignPosition3DesignValY;
	@CTORMTemplate(seq = "30", name="alignPosition3MeasureValX", type="Column", dataType="String", initial="", history="")
	private String alignPosition3MeasureValX;
	@CTORMTemplate(seq = "31", name="alignPosition3MeasureValY", type="Column", dataType="String", initial="", history="")
	private String alignPosition3MeasureValY;
	@CTORMTemplate(seq = "32", name="alignPosition3DesignSize", type="Column", dataType="String", initial="", history="")
	private String alignPosition3DesignSize;
	@CTORMTemplate(seq = "33", name="alignPosition3MeasureSize", type="Column", dataType="String", initial="", history="")
	private String alignPosition3MeasureSize;
	@CTORMTemplate(seq = "34", name="alignPosition4DesignValX", type="Column", dataType="String", initial="", history="")
	private String alignPosition4DesignValX;
	@CTORMTemplate(seq = "35", name="alignPosition4DesignValY", type="Column", dataType="String", initial="", history="")
	private String alignPosition4DesignValY;
	@CTORMTemplate(seq = "36", name="alignPosition4MeasureValX", type="Column", dataType="String", initial="", history="")
	private String alignPosition4MeasureValX;
	@CTORMTemplate(seq = "37", name="alignPosition4MeasureValY", type="Column", dataType="String", initial="", history="")
	private String alignPosition4MeasureValY;
	@CTORMTemplate(seq = "38", name="alignPosition4DesignSize", type="Column", dataType="String", initial="", history="")
	private String alignPosition4DesignSize;
	@CTORMTemplate(seq = "39", name="alignPosition4MeasureSize", type="Column", dataType="String", initial="", history="")
	private String alignPosition4MeasureSize;
	@CTORMTemplate(seq = "40", name="coverDepthMax", type="Column", dataType="String", initial="", history="")
	private String coverDepthMax;
	@CTORMTemplate(seq = "41", name="coverDepthMin", type="Column", dataType="String", initial="", history="")
	private String coverDepthMin;
	@CTORMTemplate(seq = "42", name="coverWidthMax", type="Column", dataType="String", initial="", history="")
	private String coverWidthMax;
	@CTORMTemplate(seq = "43", name="coverWidthMin", type="Column", dataType="String", initial="", history="")
	private String coverWidthMin;
	@CTORMTemplate(seq = "44", name="haulingDepthMax", type="Column", dataType="String", initial="", history="")
	private String haulingDepthMax;
	@CTORMTemplate(seq = "45", name="haulingDepthMin", type="Column", dataType="String", initial="", history="")
	private String haulingDepthMin;
	@CTORMTemplate(seq = "46", name="haulingWidthMax", type="Column", dataType="String", initial="", history="")
	private String haulingWidthMax;
	@CTORMTemplate(seq = "47", name="haulingWidthMin", type="Column", dataType="String", initial="", history="")
	private String haulingWidthMin;
	@CTORMTemplate(seq = "48", name="maskStartTime", type="Column", dataType="TimeStamp", initial="", history="")
	private Timestamp maskStartTime;
	@CTORMTemplate(seq = "49", name="maskEndTime", type="Column", dataType="TimeStamp", initial="", history="")
	private Timestamp maskEndTime;
	@CTORMTemplate(seq = "50", name="receiveTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp receiveTime;
	@CTORMTemplate(seq = "51", name="reasonCode", type="Column", dataType="String", initial="", history="")
	private String reasonCode;
	@CTORMTemplate(seq = "52", name="reasonCodeType", type="Column", dataType="String", initial="", history="")
	private String reasonCodeType;
	@CTORMTemplate(seq = "53", name="machineName", type="Column", dataType="String", initial="", history="")
	private String machineName;
	@CTORMTemplate(seq = "54", name="unitName", type="Column", dataType="String", initial="", history="")
	private String unitName;
	@CTORMTemplate(seq = "55", name="recipeID", type="Column", dataType="String", initial="", history="")
	private String recipeID;
	@CTORMTemplate(seq = "56", name="maskType", type="Column", dataType="String", initial="", history="")
	private String maskType;
	@CTORMTemplate(seq = "57", name="maskFlowState", type="Column", dataType="String", initial="", history="")
	private String maskFlowState;
	@CTORMTemplate(seq = "58", name="boxName", type="Column", dataType="String", initial="", history="")
	private String boxName;
	@CTORMTemplate(seq = "59", name="subUnitName", type="Column", dataType="String", initial="", history="N")
	private String subUnitName;
	@CTORMTemplate(seq = "60", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;
	@CTORMTemplate(seq = "61", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;
	@CTORMTemplate(seq = "62", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;
	@CTORMTemplate(seq = "63", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimeKey;
	@CTORMTemplate(seq = "64", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;
	@CTORMTemplate(seq = "65", name="alignHoleDesignRN1", type="Column", dataType="String", initial="", history="")
	private String alignHoleDesignRN1;
	@CTORMTemplate(seq = "66", name="alignHoleMeasurementRN1", type="Column", dataType="String", initial="", history="")
	private String alignHoleMeasurementRN1;
	@CTORMTemplate(seq = "67", name="alignHoleDesignRN2", type="Column", dataType="String", initial="", history="")
	private String alignHoleDesignRN2;
	@CTORMTemplate(seq = "68", name="alignHoleMeasurementRN2", type="Column", dataType="String", initial="", history="")
	private String alignHoleMeasurementRN2;
	@CTORMTemplate(seq = "69", name="alignHoleDesignRN3", type="Column", dataType="String", initial="", history="")
	private String alignHoleDesignRN3;
	@CTORMTemplate(seq = "70", name="alignHoleMeasurementRN3", type="Column", dataType="String", initial="", history="")
	private String alignHoleMeasurementRN3;
	@CTORMTemplate(seq = "71", name="alignHoleDesignRN4", type="Column", dataType="String", initial="", history="")
	private String alignHoleDesignRN4;
	@CTORMTemplate(seq = "72", name="alignHoleMeasurementRN4", type="Column", dataType="String", initial="", history="")
	private String alignHoleMeasurementRN4;
	
	@CTORMTemplate(seq = "73", name="texture", type="Column", dataType="String", initial="", history="")
	private String texture;
	
	@CTORMTemplate(seq = "74", name="flatness2", type="Column", dataType="String", initial="", history="")
	private String flatness2;
		
	@CTORMTemplate(seq = "75", name="maskTestTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp maskTestTime;
	
	public String getFrameName() {
		return frameName;
	}
	public void setFrameName(String frameName) {
		this.frameName = frameName;
	}
	public Timestamp getShippingDate() {
		return shippingDate;
	}
	public void setShippingDate(Timestamp shippingDate) {
		this.shippingDate = shippingDate;
	}
	public String getFrameState() {
		return frameState;
	}
	public void setFrameState(String frameState) {
		this.frameState = frameState;
	}
	public String getMaskLotName() {
		return maskLotName;
	}
	public void setMaskLotName(String maskLotName) {
		this.maskLotName = maskLotName;
	}
	public String getVendorName() {
		return vendorName;
	}
	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}
	public String getThickNess() {
		return thickNess;
	}
	public void setThickNess(String thickNess) {
		this.thickNess = thickNess;
	}
	public String getFlatNess() {
		return flatNess;
	}
	public void setFlatNess(String flatNess) {
		this.flatNess = flatNess;
	}
	public String getOutside1Length() {
		return outside1Length;
	}
	public void setOutside1Length(String outside1Length) {
		this.outside1Length = outside1Length;
	}
	public String getOutside1Width() {
		return outside1Width;
	}
	public void setOutside1Width(String outside1Width) {
		this.outside1Width = outside1Width;
	}
	public String getInternal1Length() {
		return internal1Length;
	}
	public void setInternal1Length(String internal1Length) {
		this.internal1Length = internal1Length;
	}
	public String getInternal1Width() {
		return internal1Width;
	}
	public void setInternal1Width(String internal1Width) {
		this.internal1Width = internal1Width;
	}
	public String getOutside2Length() {
		return outside2Length;
	}
	public void setOutside2Length(String outside2Length) {
		this.outside2Length = outside2Length;
	}
	public String getOutside2Width() {
		return outside2Width;
	}
	public void setOutside2Width(String outside2Width) {
		this.outside2Width = outside2Width;
	}
	public String getInternal2Length() {
		return internal2Length;
	}
	public void setInternal2Length(String internal2Length) {
		this.internal2Length = internal2Length;
	}
	public String getInternal2Width() {
		return internal2Width;
	}
	public void setInternal2Width(String internal2Width) {
		this.internal2Width = internal2Width;
	}
	public String getAlignPosition1DesignValX() {
		return alignPosition1DesignValX;
	}
	public void setAlignPosition1DesignValX(String alignPosition1DesignValX) {
		this.alignPosition1DesignValX = alignPosition1DesignValX;
	}
	public String getAlignPosition1DesignValY() {
		return alignPosition1DesignValY;
	}
	public void setAlignPosition1DesignValY(String alignPosition1DesignValY) {
		this.alignPosition1DesignValY = alignPosition1DesignValY;
	}
	public String getAlignPosition1MeasureValX() {
		return alignPosition1MeasureValX;
	}
	public void setAlignPosition1MeasureValX(String alignPosition1MeasureValX) {
		this.alignPosition1MeasureValX = alignPosition1MeasureValX;
	}
	public String getAlignPosition1MeasureValY() {
		return alignPosition1MeasureValY;
	}
	public void setAlignPosition1MeasureValY(String alignPosition1MeasureValY) {
		this.alignPosition1MeasureValY = alignPosition1MeasureValY;
	}
	public String getAlignPosition1DesignSize() {
		return alignPosition1DesignSize;
	}
	public void setAlignPosition1DesignSize(String alignPosition1DesignSize) {
		this.alignPosition1DesignSize = alignPosition1DesignSize;
	}
	public String getAlignPosition1MeasureSize() {
		return alignPosition1MeasureSize;
	}
	public void setAlignPosition1MeasureSize(String alignPosition1MeasureSize) {
		this.alignPosition1MeasureSize = alignPosition1MeasureSize;
	}
	public String getAlignPosition2DesignValX() {
		return alignPosition2DesignValX;
	}
	public void setAlignPosition2DesignValX(String alignPosition2DesignValX) {
		this.alignPosition2DesignValX = alignPosition2DesignValX;
	}
	public String getAlignPosition2DesignValY() {
		return alignPosition2DesignValY;
	}
	public void setAlignPosition2DesignValY(String alignPosition2DesignValY) {
		this.alignPosition2DesignValY = alignPosition2DesignValY;
	}
	public String getAlignPosition2MeasureValX() {
		return alignPosition2MeasureValX;
	}
	public void setAlignPosition2MeasureValX(String alignPosition2MeasureValX) {
		this.alignPosition2MeasureValX = alignPosition2MeasureValX;
	}
	public String getAlignPosition2MeasureValY() {
		return alignPosition2MeasureValY;
	}
	public void setAlignPosition2MeasureValY(String alignPosition2MeasureValY) {
		this.alignPosition2MeasureValY = alignPosition2MeasureValY;
	}
	public String getAlignPosition2DesignSize() {
		return alignPosition2DesignSize;
	}
	public void setAlignPosition2DesignSize(String alignPosition2DesignSize) {
		this.alignPosition2DesignSize = alignPosition2DesignSize;
	}
	public String getAlignPosition2MeasureSize() {
		return alignPosition2MeasureSize;
	}
	public void setAlignPosition2MeasureSize(String alignPosition2MeasureSize) {
		this.alignPosition2MeasureSize = alignPosition2MeasureSize;
	}
	public String getAlignPosition3DesignValX() {
		return alignPosition3DesignValX;
	}
	public void setAlignPosition3DesignValX(String alignPosition3DesignValX) {
		this.alignPosition3DesignValX = alignPosition3DesignValX;
	}
	public String getAlignPosition3DesignValY() {
		return alignPosition3DesignValY;
	}
	public void setAlignPosition3DesignValY(String alignPosition3DesignValY) {
		this.alignPosition3DesignValY = alignPosition3DesignValY;
	}
	public String getAlignPosition3MeasureValX() {
		return alignPosition3MeasureValX;
	}
	public void setAlignPosition3MeasureValX(String alignPosition3MeasureValX) {
		this.alignPosition3MeasureValX = alignPosition3MeasureValX;
	}
	public String getAlignPosition3MeasureValY() {
		return alignPosition3MeasureValY;
	}
	public void setAlignPosition3MeasureValY(String alignPosition3MeasureValY) {
		this.alignPosition3MeasureValY = alignPosition3MeasureValY;
	}
	public String getAlignPosition3DesignSize() {
		return alignPosition3DesignSize;
	}
	public void setAlignPosition3DesignSize(String alignPosition3DesignSize) {
		this.alignPosition3DesignSize = alignPosition3DesignSize;
	}
	public String getAlignPosition3MeasureSize() {
		return alignPosition3MeasureSize;
	}
	public void setAlignPosition3MeasureSize(String alignPosition3MeasureSize) {
		this.alignPosition3MeasureSize = alignPosition3MeasureSize;
	}
	public String getAlignPosition4DesignValX() {
		return alignPosition4DesignValX;
	}
	public void setAlignPosition4DesignValX(String alignPosition4DesignValX) {
		this.alignPosition4DesignValX = alignPosition4DesignValX;
	}
	public String getAlignPosition4DesignValY() {
		return alignPosition4DesignValY;
	}
	public void setAlignPosition4DesignValY(String alignPosition4DesignValY) {
		this.alignPosition4DesignValY = alignPosition4DesignValY;
	}
	public String getAlignPosition4MeasureValX() {
		return alignPosition4MeasureValX;
	}
	public void setAlignPosition4MeasureValX(String alignPosition4MeasureValX) {
		this.alignPosition4MeasureValX = alignPosition4MeasureValX;
	}
	public String getAlignPosition4MeasureValY() {
		return alignPosition4MeasureValY;
	}
	public void setAlignPosition4MeasureValY(String alignPosition4MeasureValY) {
		this.alignPosition4MeasureValY = alignPosition4MeasureValY;
	}
	public String getAlignPosition4DesignSize() {
		return alignPosition4DesignSize;
	}
	public void setAlignPosition4DesignSize(String alignPosition4DesignSize) {
		this.alignPosition4DesignSize = alignPosition4DesignSize;
	}
	public String getAlignPosition4MeasureSize() {
		return alignPosition4MeasureSize;
	}
	public void setAlignPosition4MeasureSize(String alignPosition4MeasureSize) {
		this.alignPosition4MeasureSize = alignPosition4MeasureSize;
	}
	public String getCoverDepthMax() {
		return coverDepthMax;
	}
	public void setCoverDepthMax(String coverDepthMax) {
		this.coverDepthMax = coverDepthMax;
	}
	public String getCoverDepthMin() {
		return coverDepthMin;
	}
	public void setCoverDepthMin(String coverDepthMin) {
		this.coverDepthMin = coverDepthMin;
	}
	public String getCoverWidthMax() {
		return coverWidthMax;
	}
	public void setCoverWidthMax(String coverWidthMax) {
		this.coverWidthMax = coverWidthMax;
	}
	public String getCoverWidthMin() {
		return coverWidthMin;
	}
	public void setCoverWidthMin(String coverWidthMin) {
		this.coverWidthMin = coverWidthMin;
	}
	public String getHaulingDepthMax() {
		return haulingDepthMax;
	}
	public void setHaulingDepthMax(String haulingDepthMax) {
		this.haulingDepthMax = haulingDepthMax;
	}
	public String getHaulingDepthMin() {
		return haulingDepthMin;
	}
	public void setHaulingDepthMin(String haulingDepthMin) {
		this.haulingDepthMin = haulingDepthMin;
	}
	public String getHaulingWidthMax() {
		return haulingWidthMax;
	}
	public void setHaulingWidthMax(String haulingWidthMax) {
		this.haulingWidthMax = haulingWidthMax;
	}
	public String getHaulingWidthMin() {
		return haulingWidthMin;
	}
	public void setHaulingWidthMin(String haulingWidthMin) {
		this.haulingWidthMin = haulingWidthMin;
	}

	public Timestamp getMaskStartTime() {
		return maskStartTime;
	}
	public void setMaskStartTime(Timestamp maskStartTime) {
		this.maskStartTime = maskStartTime;
	}
	public Timestamp getMaskEndTime() {
		return maskEndTime;
	}
	public void setMaskEndTime(Timestamp maskEndTime) {
		this.maskEndTime = maskEndTime;
	}
	public Timestamp getReceiveTime() {
		return receiveTime;
	}
	public void setReceiveTime(Timestamp receiveTime) {
		this.receiveTime = receiveTime;
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
	public String getMachineName() {
		return machineName;
	}
	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}
	public String getUnitName() {
		return unitName;
	}
	public void setUnitName(String unitName) {
		this.unitName = unitName;
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
	public String getMaskFlowState() {
		return maskFlowState;
	}
	public void setMaskFlowState(String maskFlowState) {
		this.maskFlowState = maskFlowState;
	}
	public String getBoxName() {
		return boxName;
	}
	public void setBoxName(String boxName) {
		this.boxName = boxName;
	}
	public String getSubUnitName() {
		return subUnitName;
	}
	public void setSubUnitName(String subUnitName) {
		this.subUnitName = subUnitName;
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
	public void setLastEventTimeKey(String lastEventTimeKey) {
		this.lastEventTimeKey = lastEventTimeKey;
	}
	public String getLastEventUser() {
		return lastEventUser;
	}
	public void setLastEventUser(String lastEventUser) {
		this.lastEventUser = lastEventUser;
	}
	public String getAlignHoleDesignRN1()
	{
		return alignHoleDesignRN1;
	}
	public void setAlignHoleDesignRN1(String alignHoleDesignRN1)
	{
		this.alignHoleDesignRN1 = alignHoleDesignRN1;
	}
	public String getAlignHoleMeasurementRN1()
	{
		return alignHoleMeasurementRN1;
	}
	public void setAlignHoleMeasurementRN1(String alignHoleMeasurementRN1)
	{
		this.alignHoleMeasurementRN1 = alignHoleMeasurementRN1;
	}
	public String getAlignHoleDesignRN2()
	{
		return alignHoleDesignRN2;
	}
	public void setAlignHoleDesignRN2(String alignHoleDesignRN2)
	{
		this.alignHoleDesignRN2 = alignHoleDesignRN2;
	}
	public String getAlignHoleMeasurementRN2()
	{
		return alignHoleMeasurementRN2;
	}
	public void setAlignHoleMeasurementRN2(String alignHoleMeasurementRN2)
	{
		this.alignHoleMeasurementRN2 = alignHoleMeasurementRN2;
	}
	public String getAlignHoleDesignRN3()
	{
		return alignHoleDesignRN3;
	}
	public void setAlignHoleDesignRN3(String alignHoleDesignRN3)
	{
		this.alignHoleDesignRN3 = alignHoleDesignRN3;
	}
	public String getAlignHoleMeasurementRN3()
	{
		return alignHoleMeasurementRN3;
	}
	public void setAlignHoleMeasurementRN3(String alignHoleMeasurementRN3)
	{
		this.alignHoleMeasurementRN3 = alignHoleMeasurementRN3;
	}
	public String getAlignHoleDesignRN4()
	{
		return alignHoleDesignRN4;
	}
	public void setAlignHoleDesignRN4(String alignHoleDesignRN4)
	{
		this.alignHoleDesignRN4 = alignHoleDesignRN4;
	}
	public String getAlignHoleMeasurementRN4()
	{
		return alignHoleMeasurementRN4;
	}
	public void setAlignHoleMeasurementRN4(String alignHoleMeasurementRN4)
	{
		this.alignHoleMeasurementRN4 = alignHoleMeasurementRN4;
	}
	public String getTexture() {
		return texture;
	}
	public void setTexture(String texture) {
		this.texture = texture;
	}
	public String getFlatness2() {
		return flatness2;
	}
	public void setFlatness2(String flatness2) {
		this.flatness2 = flatness2;
	}
	
	public Timestamp getMaskTestTime() {
		return maskTestTime;
	}
	public void setMaskTestTime(Timestamp maskTestTime) {
		this.maskTestTime = maskTestTime;
	}

}
