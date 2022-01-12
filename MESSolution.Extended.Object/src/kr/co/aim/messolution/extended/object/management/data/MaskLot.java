package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class MaskLot extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="maskLotName", type="Key", dataType="String", initial="", history="")
	private String maskLotName;
	
	@CTORMTemplate(seq = "2", name="maskKind", type="Column", dataType="String", initial="", history="")
	private String maskKind;

	@CTORMTemplate(seq = "3", name="maskType", type="Column", dataType="String", initial="", history="")
	private String maskType;

	@CTORMTemplate(seq = "4", name="maskSpecName", type="Column", dataType="String", initial="", history="")
	private String maskSpecName;

	@CTORMTemplate(seq = "5", name="frameName", type="Column", dataType="String", initial="", history="")
	private String frameName;

	@CTORMTemplate(seq = "6", name="carrierName", type="Column", dataType="String", initial="", history="")
	private String carrierName;

	@CTORMTemplate(seq = "7", name="cleanState", type="Column", dataType="String", initial="", history="")
	private String cleanState;

	@CTORMTemplate(seq = "8", name="factoryName", type="Column", dataType="String", initial="", history="")
	private String factoryName;

	@CTORMTemplate(seq = "9", name="areaName", type="Column", dataType="String", initial="", history="")
	private String areaName;

	@CTORMTemplate(seq = "10", name="maskLotState", type="Column", dataType="String", initial="", history="")
	private String maskLotState;

	@CTORMTemplate(seq = "11", name="maskLotProcessState", type="Column", dataType="String", initial="", history="")
	private String maskLotProcessState;

	@CTORMTemplate(seq = "12", name="maskLotJudge", type="Column", dataType="String", initial="", history="")
	private String maskLotJudge;

	@CTORMTemplate(seq = "13", name="maskLotHoldState", type="Column", dataType="String", initial="", history="")
	private String maskLotHoldState;

	@CTORMTemplate(seq = "14", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;

	@CTORMTemplate(seq = "15", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimeKey;

	@CTORMTemplate(seq = "16", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;

	@CTORMTemplate(seq = "17", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;

	@CTORMTemplate(seq = "18", name="maskGroupName", type="Column", dataType="String", initial="", history="")
	private String maskGroupName;

	@CTORMTemplate(seq = "19", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;

	@CTORMTemplate(seq = "20", name="reasonCodeType", type="Column", dataType="String", initial="", history="")
	private String reasonCodeType;

	@CTORMTemplate(seq = "21", name="reasonCode", type="Column", dataType="String", initial="", history="")
	private String reasonCode;

	@CTORMTemplate(seq = "22", name="maskProcessFlowName", type="Column", dataType="String", initial="", history="")
	private String maskProcessFlowName;
	
	@CTORMTemplate(seq = "23", name="maskProcessFlowVersion", type="Column", dataType="String", initial="", history="")
	private String maskProcessFlowVersion;

	@CTORMTemplate(seq = "24", name="maskProcessOperationName", type="Column", dataType="String", initial="", history="")
	private String maskProcessOperationName;

	@CTORMTemplate(seq = "25", name="maskProcessOperationVersion", type="Column", dataType="String", initial="", history="")
	private String maskProcessOperationVersion;	
	
	@CTORMTemplate(seq = "26", name="maskRepairState", type="Column", dataType="String", initial="", history="")
	private String maskRepairState;

	@CTORMTemplate(seq = "27", name="maskRepairCount", type="Column", dataType="Number", initial="", history="")
	private Number maskRepairCount;

	@CTORMTemplate(seq = "28", name="machineName", type="Column", dataType="String", initial="", history="")
	private String machineName;

	@CTORMTemplate(seq = "29", name="machineRecipeName", type="Column", dataType="String", initial="", history="")
	private String machineRecipeName;

	@CTORMTemplate(seq = "30", name="fmmPatternJudge", type="Column", dataType="String", initial="", history="")
	private String fmmPatternJudge;

	@CTORMTemplate(seq = "31", name="fmmDefectJudge", type="Column", dataType="String", initial="", history="")
	private String fmmDefectJudge;

	@CTORMTemplate(seq = "32", name="fmmLaserRepairJudge", type="Column", dataType="String", initial="", history="")
	private String fmmLaserRepairJudge;

	@CTORMTemplate(seq = "33", name="tfePatternJudge", type="Column", dataType="String", initial="", history="")
	private String tfePatternJudge;

	@CTORMTemplate(seq = "34", name="tfeDefectJudge", type="Column", dataType="String", initial="", history="")
	private String tfeDefectJudge;

	@CTORMTemplate(seq = "35", name="tensionJudge", type="Column", dataType="String", initial="", history="")
	private String tensionJudge;

	@CTORMTemplate(seq = "36", name="defectCount", type="Column", dataType="Number", initial="", history="")
	private Number defectCount;

	@CTORMTemplate(seq = "37", name="maskModelName", type="Column", dataType="String", initial="", history="")
	private String maskModelName;

	@CTORMTemplate(seq = "38", name="maskThickness", type="Column", dataType="String", initial="", history="")
	private String maskThickness;

	@CTORMTemplate(seq = "39", name="maskCleanCount", type="Column", dataType="Number", initial="", history="")
	private Number maskCleanCount;

	@CTORMTemplate(seq = "40", name="nodeStack", type="Column", dataType="String", initial="", history="")
	private String nodeStack;

	@CTORMTemplate(seq = "41", name="returnSequenceId", type="Column", dataType="String", initial="", history="")
	private String returnSequenceId;

	@CTORMTemplate(seq = "42", name="reworkState", type="Column", dataType="String", initial="", history="")
	private String reworkState;

	@CTORMTemplate(seq = "43", name="reworkCount", type="Column", dataType="Number", initial="", history="")
	private Number reworkCount;

	@CTORMTemplate(seq = "44", name="maskBoxName", type="Column", dataType="String", initial="", history="")
	private String maskBoxName;

	@CTORMTemplate(seq = "45", name="maskQuantity", type="Column", dataType="String", initial="", history="")
	private String maskQuantity;

	@CTORMTemplate(seq = "46", name="vendor", type="Column", dataType="String", initial="", history="")
	private String vendor;

	@CTORMTemplate(seq = "47", name="shippingDate", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp shippingDate;

	@CTORMTemplate(seq = "48", name="position", type="Column", dataType="String", initial="", history="")
	private String position;

	@CTORMTemplate(seq = "49", name="lastCleanTimekey", type="Column", dataType="String", initial="", history="")
	private String lastCleanTimekey;

	@CTORMTemplate(seq = "50", name="cleanTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp cleanTime;

	@CTORMTemplate(seq = "51", name="timeUsed", type="Column", dataType="Float", initial="", history="")
	private Float timeUsed;

	@CTORMTemplate(seq = "52", name="cleanFlag", type="Column", dataType="String", initial="", history="")
	private String cleanFlag;

	@CTORMTemplate(seq = "53", name="cleanStartTimekey", type="Column", dataType="String", initial="", history="")
	private String cleanStartTimekey;

	@CTORMTemplate(seq = "54", name="cleanEndTimekey", type="Column", dataType="String", initial="", history="")
	private String cleanEndTimekey;

	@CTORMTemplate(seq = "55", name="lastLoggedInTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp lastLoggedInTime;

	@CTORMTemplate(seq = "56", name="lastLoggedInUser", type="Column", dataType="String", initial="", history="")
	private String lastLoggedInUser;

	@CTORMTemplate(seq = "57", name="lastLoggedOutTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp lastLoggedOutTime;

	@CTORMTemplate(seq = "58", name="lastLoggedOutUser", type="Column", dataType="String", initial="", history="")
	private String lastLoggedOutUser;

	@CTORMTemplate(seq = "59", name="maskLotNote", type="Column", dataType="String", initial="", history="")
	private String maskLotNote;

	@CTORMTemplate(seq = "60", name="portName", type="Column", dataType="String", initial="", history="")
	private String portName;

	@CTORMTemplate(seq = "61", name="portType", type="Column", dataType="String", initial="", history="")
	private String portType;
	
	@CTORMTemplate(seq = "62", name="patternCount", type="Column", dataType="Number", initial="", history="")
	private Number patternCount;

	@CTORMTemplate(seq = "63", name="laserRepairCount", type="Column", dataType="Number", initial="", history="")
	private Number laserRepairCount;

	@CTORMTemplate(seq = "64", name="materialLocationName", type="Column", dataType="String", initial="", history="")
	private String materialLocationName;

	@CTORMTemplate(seq = "65", name="reticlesLot", type="Column", dataType="String", initial="", history="")
	private String reticlesLot;

//	@CTORMTemplate(seq = "66", name="tensionOffSetX", type="Column", dataType="String", initial="", history="")
//	private String tensionOffSetX;
//
//	@CTORMTemplate(seq = "67", name="tensionOffSetY", type="Column", dataType="String", initial="", history="")
//	private String tensionOffSetY;
//
//	@CTORMTemplate(seq = "68", name="tensionOffSetTheta", type="Column", dataType="String", initial="", history="")
//	private String tensionOffSetTheta;
//
//	@CTORMTemplate(seq = "69", name="patternOffSetX", type="Column", dataType="String", initial="", history="")
//	private String patternOffSetX;
//
//	@CTORMTemplate(seq = "70", name="patternOffSetY", type="Column", dataType="String", initial="", history="")
//	private String patternOffSetY;
//
//	@CTORMTemplate(seq = "71", name="patternOffSetTheta", type="Column", dataType="String", initial="", history="")
//	private String patternOffSetTheta;

	@CTORMTemplate(seq = "72", name="initialOffSetX", type="Column", dataType="String", initial="", history="")
	private String initialOffSetX;

	@CTORMTemplate(seq = "73", name="initialOffSetY", type="Column", dataType="String", initial="", history="")
	private String initialOffSetY;

	@CTORMTemplate(seq = "74", name="initialOffSetTheta", type="Column", dataType="String", initial="", history="")
	private String initialOffSetTheta;

	@CTORMTemplate(seq = "75", name="chamberName", type="Column", dataType="String", initial="", history="")
	private String chamberName;

	@CTORMTemplate(seq = "76", name="stageName", type="Column", dataType="String", initial="", history="")
	private String stageName;

	@CTORMTemplate(seq = "77", name="productionType", type="Column", dataType="String", initial="", history="")
	private String productionType;

	@CTORMTemplate(seq = "78", name="transferState", type="Column", dataType="String", initial="", history="")
	private String transferState;

	@CTORMTemplate(seq = "79", name="transferType", type="Column", dataType="String", initial="", history="")
	private String transferType;

	@CTORMTemplate(seq = "80", name="zoneName", type="Column", dataType="String", initial="", history="")
	private String zoneName;

	@CTORMTemplate(seq = "81", name="frameSpecName", type="Column", dataType="String", initial="", history="")
	private String frameSpecName;

	@CTORMTemplate(seq = "82", name="coverBoxName", type="Column", dataType="String", initial="", history="")
	private String coverBoxName;

	@CTORMTemplate(seq = "83", name="haulBoxName", type="Column", dataType="String", initial="", history="")
	private String haulBoxName;

	@CTORMTemplate(seq = "84", name="evaOffSetX", type="Column", dataType="String", initial="", history="")
	private String evaOffSetX;

	@CTORMTemplate(seq = "85", name="evaOffSetY", type="Column", dataType="String", initial="", history="")
	private String evaOffSetY;

	@CTORMTemplate(seq = "86", name="evaOffSetTheta", type="Column", dataType="String", initial="", history="")
	private String evaOffSetTheta;

	@CTORMTemplate(seq = "87", name="coverQuantity", type="Column", dataType="Number", initial="", history="")
	private Number coverQuantity;

	@CTORMTemplate(seq = "88", name="haulQuantity", type="Column", dataType="Number", initial="", history="")
	private Number haulQuantity;
	
	@CTORMTemplate(seq = "89", name="cleanUsedLimit", type="Column", dataType="Float", initial="", history="")
	private Float cleanUsedLimit;

	@CTORMTemplate(seq = "90", name="durationUsedLimit", type="Column", dataType="Float", initial="", history="")
	private Float durationUsedLimit;

	@CTORMTemplate(seq = "91", name="magnet", type="Column", dataType="Float", initial="", history="")
	private Float magnet;

	@CTORMTemplate(seq = "92", name="timeUsedLimit", type="Column", dataType="Float", initial="", history="")
	private Float timeUsedLimit;

	@CTORMTemplate(seq = "93", name="priority", type="Column", dataType="Number", initial="", history="")
	private Number priority;

	@CTORMTemplate(seq = "94", name="reserveStage", type="Column", dataType="String", initial="", history="")
	private String reserveStage;
	
	@CTORMTemplate(seq = "95", name="jobDownFlag", type="Column", dataType="String", initial="", history="")
	private String jobDownFlag;
	
	@CTORMTemplate(seq = "96", name="maskStock", type="Column", dataType="String", initial="", history="")
	private String maskStock;
	
	@CTORMTemplate(seq = "97", name="stockerName", type="Column", dataType="String", initial="", history="")	
	private String stockerName;

	@CTORMTemplate(seq = "98", name="reserveLimit", type="Column", dataType="String", initial="", history="")	
	private String reserveLimit;
	
	@CTORMTemplate(seq = "99", name="aoiCount", type="Column", dataType="Number", initial="", history="")	
	private Number aoiCount;
	
	@CTORMTemplate(seq = "100", name="aoiCode", type="Column", dataType="String", initial="", history="")	
	private String aoiCode;
	
	@CTORMTemplate(seq = "101", name="repairCode", type="Column", dataType="String", initial="", history="")	
	private String repairCode;
	
	@CTORMTemplate(seq = "102", name="maskSubSpecName", type="Column", dataType="String", initial="", history="")	
	private String maskSubSpecName;
	
	@CTORMTemplate(seq = "103", name="maskFlowState", type="Column", dataType="String", initial="", history="")	
	private String maskFlowState;
	
	@CTORMTemplate(seq = "104", name="maskCycleCount", type="Column", dataType="Number", initial="", history="")	
	private Number maskCycleCount;
	
	@CTORMTemplate(seq = "105", name="processIngInfo", type="Column", dataType="String", initial="", history="")	
	private String processIngInfo;
	
	@CTORMTemplate(seq = "106", name="cool_Z_Position", type="Column", dataType="String", initial="", history="")	
	private String cool_Z_Position;
	
	@CTORMTemplate(seq = "107", name="alignRecipe", type="Column", dataType="String", initial="", history="")	
	private String alignRecipe;
	
	@CTORMTemplate(seq = "108", name="detachStickType", type="Column", dataType="String", initial="", history="")	
	private String detachStickType;
	
	@CTORMTemplate(seq = "109", name="detachPosition", type="Column", dataType="String", initial="", history="")	
	private String detachPosition;
	
	@CTORMTemplate(seq = "110", name="maskFilmLayer", type="Column", dataType="String", initial="", history="")	
	private String maskFilmLayer;
	
	@CTORMTemplate(seq = "111", name="boxName", type="Column", dataType="String", initial="", history="")	
	private String boxName;
	
	@CTORMTemplate(seq = "112", name="r2rOffSetX", type="Column", dataType="String", initial="", history="")
	private String r2rOffSetX;

	@CTORMTemplate(seq = "113", name="r2rOffSetY", type="Column", dataType="String", initial="", history="")
	private String r2rOffSetY;

	@CTORMTemplate(seq = "114", name="r2rOffSetTheta", type="Column", dataType="String", initial="", history="")
	private String r2rOffSetTheta;
	
	@CTORMTemplate(seq = "115", name="vcrMaskName", type="Column", dataType="String", initial="", history="")
	private String vcrMaskName;
	
	@CTORMTemplate(seq = "116", name="TFEOFFSETX1", type="Column", dataType="String", initial="", history="")
	private String TFEOFFSETX1 ;
	
	@CTORMTemplate(seq = "117", name="TFEOFFSETY1", type="Column", dataType="String", initial="", history="")
	private String TFEOFFSETY1 ;
	
	@CTORMTemplate(seq = "118", name="TFEOFFSETX2", type="Column", dataType="String", initial="", history="")
	private String TFEOFFSETX2 ;
	
	@CTORMTemplate(seq = "119", name="TFEOFFSETY2", type="Column", dataType="String", initial="", history="")
	private String TFEOFFSETY2 ;

	@CTORMTemplate(seq = "120", name="transportLockFlag", type="Column", dataType="String", initial="", history="")
	private String transportLockFlag ;
	
	@CTORMTemplate(seq = "121", name="spcReserveHoldFlag", type="Column", dataType="String", initial="", history="")
	private String spcReserveHoldFlag ;
	
	private String branchEndNodeId;

	public String getBranchEndNodeId()
	{
		return branchEndNodeId;
	}

	public void setBranchEndNodeId(String branchEndNodeId)
	{
		this.branchEndNodeId = branchEndNodeId;
	}

	public String getMaskLotName() {
		return maskLotName;
	}

	public void setMaskLotName(String maskLotName) {
		this.maskLotName = maskLotName;
	}

	public String getMaskKind() {
		return maskKind;
	}

	public void setMaskKind(String maskKind) {
		this.maskKind = maskKind;
	}

	public String getMaskType() {
		return maskType;
	}

	public void setMaskType(String maskType) {
		this.maskType = maskType;
	}

	public String getMaskSpecName() {
		return maskSpecName;
	}

	public void setMaskSpecName(String maskSpecName) {
		this.maskSpecName = maskSpecName;
	}

	public String getFrameName() {
		return frameName;
	}

	public void setFrameName(String frameName) {
		this.frameName = frameName;
	}

	public String getCarrierName() {
		return carrierName;
	}

	public void setCarrierName(String carrierName) {
		this.carrierName = carrierName;
	}

	public String getCleanState() {
		return cleanState;
	}

	public void setCleanState(String cleanState) {
		this.cleanState = cleanState;
	}

	public String getFactoryName() {
		return factoryName;
	}

	public void setFactoryName(String factoryName) {
		this.factoryName = factoryName;
	}

	public String getAreaName() {
		return areaName;
	}

	public void setAreaName(String areaName) {
		this.areaName = areaName;
	}

	public String getMaskLotState() {
		return maskLotState;
	}

	public void setMaskLotState(String maskLotState) {
		this.maskLotState = maskLotState;
	}

	public String getMaskLotProcessState() {
		return maskLotProcessState;
	}

	public void setMaskLotProcessState(String maskLotProcessState) {
		this.maskLotProcessState = maskLotProcessState;
	}

	public String getMaskLotJudge() {
		return maskLotJudge;
	}

	public void setMaskLotJudge(String maskLotJudge) {
		this.maskLotJudge = maskLotJudge;
	}

	public String getMaskLotHoldState() {
		return maskLotHoldState;
	}

	public void setMaskLotHoldState(String maskLotHoldState) {
		this.maskLotHoldState = maskLotHoldState;
	}

	public String getLastEventName() {
		return lastEventName;
	}

	public void setLastEventName(String lastEventName) {
		this.lastEventName = lastEventName;
	}

	public String getLastEventTimeKey() {
		return lastEventTimeKey;
	}

	public void setLastEventTimeKey(String lastEventTimeKey) {
		this.lastEventTimeKey = lastEventTimeKey;
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

	public String getMaskGroupName() {
		return maskGroupName;
	}

	public void setMaskGroupName(String maskGroupName) {
		this.maskGroupName = maskGroupName;
	}

	public String getLastEventComment() {
		return lastEventComment;
	}

	public void setLastEventComment(String lastEventComment) {
		this.lastEventComment = lastEventComment;
	}

	public String getReasonCodeType() {
		return reasonCodeType;
	}

	public void setReasonCodeType(String reasonCodeType) {
		this.reasonCodeType = reasonCodeType;
	}

	public String getReasonCode() {
		return reasonCode;
	}

	public void setReasonCode(String reasonCode) {
		this.reasonCode = reasonCode;
	}

	public String getMaskProcessFlowName() {
		return maskProcessFlowName;
	}

	public void setMaskProcessFlowName(String maskProcessFlowName) {
		this.maskProcessFlowName = maskProcessFlowName;
	}

	public String getMaskProcessFlowVersion() {
		return maskProcessFlowVersion;
	}

	public void setMaskProcessFlowVersion(String maskProcessFlowVersion) {
		this.maskProcessFlowVersion = maskProcessFlowVersion;
	}

	public String getMaskProcessOperationName() {
		return maskProcessOperationName;
	}

	public void setMaskProcessOperationName(String maskProcessOperationName) {
		this.maskProcessOperationName = maskProcessOperationName;
	}

	public String getMaskProcessOperationVersion() {
		return maskProcessOperationVersion;
	}

	public void setMaskProcessOperationVersion(String maskProcessOperationVersion) {
		this.maskProcessOperationVersion = maskProcessOperationVersion;
	}

	public String getMaskRepairState() {
		return maskRepairState;
	}

	public void setMaskRepairState(String maskRepairState) {
		this.maskRepairState = maskRepairState;
	}

	public Number getMaskRepairCount() {
		return maskRepairCount;
	}

	public void setMaskRepairCount(Number maskRepairCount) {
		this.maskRepairCount = maskRepairCount;
	}

	public String getMachineName() {
		return machineName;
	}

	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}

	public String getMachineRecipeName() {
		return machineRecipeName;
	}

	public void setMachineRecipeName(String machineRecipeName) {
		this.machineRecipeName = machineRecipeName;
	}

	public String getFmmPatternJudge() {
		return fmmPatternJudge;
	}

	public void setFmmPatternJudge(String fmmPatternJudge) {
		this.fmmPatternJudge = fmmPatternJudge;
	}

	public String getFmmDefectJudge() {
		return fmmDefectJudge;
	}

	public void setFmmDefectJudge(String fmmDefectJudge) {
		this.fmmDefectJudge = fmmDefectJudge;
	}

	public String getFmmLaserRepairJudge() {
		return fmmLaserRepairJudge;
	}

	public void setFmmLaserRepairJudge(String fmmLaserRepairJudge) {
		this.fmmLaserRepairJudge = fmmLaserRepairJudge;
	}

	public String getTfePatternJudge() {
		return tfePatternJudge;
	}

	public void setTfePatternJudge(String tfePatternJudge) {
		this.tfePatternJudge = tfePatternJudge;
	}

	public String getTfeDefectJudge() {
		return tfeDefectJudge;
	}

	public void setTfeDefectJudge(String tfeDefectJudge) {
		this.tfeDefectJudge = tfeDefectJudge;
	}

	public String getTensionJudge() {
		return tensionJudge;
	}

	public void setTensionJudge(String tensionJudge) {
		this.tensionJudge = tensionJudge;
	}

	public Number getDefectCount() {
		return defectCount;
	}

	public void setDefectCount(Number defectCount) {
		this.defectCount = defectCount;
	}

	public String getMaskModelName() {
		return maskModelName;
	}

	public void setMaskModelName(String maskModelName) {
		this.maskModelName = maskModelName;
	}

	public String getMaskThickness() {
		return maskThickness;
	}

	public void setMaskThickness(String maskThickness) {
		this.maskThickness = maskThickness;
	}

	public Number getMaskCleanCount() {
		return maskCleanCount;
	}

	public void setMaskCleanCount(Number maskCleanCount) {
		this.maskCleanCount = maskCleanCount;
	}

	public String getNodeStack() {
		return nodeStack;
	}

	public void setNodeStack(String nodeStack) {
		this.nodeStack = nodeStack;
	}

	public String getReturnSequenceId() {
		return returnSequenceId;
	}

	public void setReturnSequenceId(String returnSequenceId) {
		this.returnSequenceId = returnSequenceId;
	}

	public String getReworkState() {
		return reworkState;
	}

	public void setReworkState(String reworkState) {
		this.reworkState = reworkState;
	}

	public Number getReworkCount() {
		return reworkCount;
	}

	public void setReworkCount(Number reworkCount) {
		this.reworkCount = reworkCount;
	}

	public String getMaskBoxName() {
		return maskBoxName;
	}

	public void setMaskBoxName(String maskBoxName) {
		this.maskBoxName = maskBoxName;
	}

	public String getMaskQuantity() {
		return maskQuantity;
	}

	public void setMaskQuantity(String maskQuantity) {
		this.maskQuantity = maskQuantity;
	}

	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public Timestamp getShippingDate() {
		return shippingDate;
	}

	public void setShippingDate(Timestamp shippingDate) {
		this.shippingDate = shippingDate;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public String getLastCleanTimekey() {
		return lastCleanTimekey;
	}

	public void setLastCleanTimekey(String lastCleanTimekey) {
		this.lastCleanTimekey = lastCleanTimekey;
	}

	public Timestamp getCleanTime() {
		return cleanTime;
	}

	public void setCleanTime(Timestamp cleanTime) {
		this.cleanTime = cleanTime;
	}

	public Float getTimeUsed() {
		return timeUsed;
	}

	public void setTimeUsed(Float timeUsed) {
		this.timeUsed = timeUsed;
	}

	public String getCleanFlag() {
		return cleanFlag;
	}

	public void setCleanFlag(String cleanFlag) {
		this.cleanFlag = cleanFlag;
	}

	public String getCleanStartTimekey() {
		return cleanStartTimekey;
	}

	public void setCleanStartTimekey(String cleanStartTimekey) {
		this.cleanStartTimekey = cleanStartTimekey;
	}

	public String getCleanEndTimekey() {
		return cleanEndTimekey;
	}

	public void setCleanEndTimekey(String cleanEndTimekey) {
		this.cleanEndTimekey = cleanEndTimekey;
	}

	public Timestamp getLastLoggedInTime() {
		return lastLoggedInTime;
	}

	public void setLastLoggedInTime(Timestamp lastLoggedInTime) {
		this.lastLoggedInTime = lastLoggedInTime;
	}

	public String getLastLoggedInUser() {
		return lastLoggedInUser;
	}

	public void setLastLoggedInUser(String lastLoggedInUser) {
		this.lastLoggedInUser = lastLoggedInUser;
	}

	public Timestamp getLastLoggedOutTime() {
		return lastLoggedOutTime;
	}

	public void setLastLoggedOutTime(Timestamp lastLoggedOutTime) {
		this.lastLoggedOutTime = lastLoggedOutTime;
	}

	public String getLastLoggedOutUser() {
		return lastLoggedOutUser;
	}

	public void setLastLoggedOutUser(String lastLoggedOutUser) {
		this.lastLoggedOutUser = lastLoggedOutUser;
	}

	public String getMaskLotNote() {
		return maskLotNote;
	}

	public void setMaskLotNote(String maskLotNote) {
		this.maskLotNote = maskLotNote;
	}

	public String getPortName() {
		return portName;
	}

	public void setPortName(String portName) {
		this.portName = portName;
	}

	public String getPortType() {
		return portType;
	}

	public void setPortType(String portType) {
		this.portType = portType;
	}

	public Number getPatternCount() {
		return patternCount;
	}

	public void setPatternCount(Number patternCount) {
		this.patternCount = patternCount;
	}

	public Number getLaserRepairCount() {
		return laserRepairCount;
	}

	public void setLaserRepairCount(Number laserRepairCount) {
		this.laserRepairCount = laserRepairCount;
	}

	public String getMaterialLocationName() {
		return materialLocationName;
	}

	public void setMaterialLocationName(String materialLocationName) {
		this.materialLocationName = materialLocationName;
	}

	public String getReticlesLot() {
		return reticlesLot;
	}

	public void setReticlesLot(String reticlesLot) {
		this.reticlesLot = reticlesLot;
	}

//	public String getTensionOffSetX() {
//		return tensionOffSetX;
//	}
//
//	public void setTensionOffSetX(String tensionOffSetX) {
//		this.tensionOffSetX = tensionOffSetX;
//	}
//
//	public String getTensionOffSetY() {
//		return tensionOffSetY;
//	}
//
//	public void setTensionOffSetY(String tensionOffSetY) {
//		this.tensionOffSetY = tensionOffSetY;
//	}
//
//	public String getTensionOffSetTheta() {
//		return tensionOffSetTheta;
//	}
//
//	public void setTensionOffSetTheta(String tensionOffSetTheta) {
//		this.tensionOffSetTheta = tensionOffSetTheta;
//	}
//
//	public String getPatternOffSetX() {
//		return patternOffSetX;
//	}
//
//	public void setPatternOffSetX(String patternOffSetX) {
//		this.patternOffSetX = patternOffSetX;
//	}
//
//	public String getPatternOffSetY() {
//		return patternOffSetY;
//	}
//
//	public void setPatternOffSetY(String patternOffSetY) {
//		this.patternOffSetY = patternOffSetY;
//	}
//
//	public String getPatternOffSetTheta() {
//		return patternOffSetTheta;
//	}
//
//	public void setPatternOffSetTheta(String patternOffSetTheta) {
//		this.patternOffSetTheta = patternOffSetTheta;
//	}

	public String getInitialOffSetX() {
		return initialOffSetX;
	}

	public void setInitialOffSetX(String initialOffSetX) {
		this.initialOffSetX = initialOffSetX;
	}

	public String getInitialOffSetY() {
		return initialOffSetY;
	}

	public void setInitialOffSetY(String initialOffSetY) {
		this.initialOffSetY = initialOffSetY;
	}

	public String getInitialOffSetTheta() {
		return initialOffSetTheta;
	}

	public void setInitialOffSetTheta(String initialOffSetTheta) {
		this.initialOffSetTheta = initialOffSetTheta;
	}

	public String getChamberName() {
		return chamberName;
	}

	public void setChamberName(String chamberName) {
		this.chamberName = chamberName;
	}

	public String getStageName() {
		return stageName;
	}

	public void setStageName(String stageName) {
		this.stageName = stageName;
	}

	public String getProductionType() {
		return productionType;
	}

	public void setProductionType(String productionType) {
		this.productionType = productionType;
	}

	public String getTransferState() {
		return transferState;
	}

	public void setTransferState(String transferState) {
		this.transferState = transferState;
	}

	public String getTransferType() {
		return transferType;
	}

	public void setTransferType(String transferType) {
		this.transferType = transferType;
	}

	public String getZoneName() {
		return zoneName;
	}

	public void setZoneName(String zoneName) {
		this.zoneName = zoneName;
	}

	public String getFrameSpecName() {
		return frameSpecName;
	}

	public void setFrameSpecName(String frameSpecName) {
		this.frameSpecName = frameSpecName;
	}

	public String getCoverBoxName() {
		return coverBoxName;
	}

	public void setCoverBoxName(String coverBoxName) {
		this.coverBoxName = coverBoxName;
	}

	public String getHaulBoxName() {
		return haulBoxName;
	}

	public void setHaulBoxName(String haulBoxName) {
		this.haulBoxName = haulBoxName;
	}

	public String getEvaOffSetX() {
		return evaOffSetX;
	}

	public void setEvaOffSetX(String evaOffSetX) {
		this.evaOffSetX = evaOffSetX;
	}

	public String getEvaOffSetY() {
		return evaOffSetY;
	}

	public void setEvaOffSetY(String evaOffSetY) {
		this.evaOffSetY = evaOffSetY;
	}

	public String getEvaOffSetTheta() {
		return evaOffSetTheta;
	}

	public void setEvaOffSetTheta(String evaOffSetTheta) {
		this.evaOffSetTheta = evaOffSetTheta;
	}

	public Number getCoverQuantity() {
		return coverQuantity;
	}

	public void setCoverQuantity(Number coverQuantity) {
		this.coverQuantity = coverQuantity;
	}

	public Number getHaulQuantity() {
		return haulQuantity;
	}

	public void setHaulQuantity(Number haulQuantity) {
		this.haulQuantity = haulQuantity;
	}

	public Float getCleanUsedLimit() {
		return cleanUsedLimit;
	}

	public void setCleanUsedLimit(Float cleanUsedLimit) {
		this.cleanUsedLimit = cleanUsedLimit;
	}

	public Float getDurationUsedLimit() {
		return durationUsedLimit;
	}

	public void setDurationUsedLimit(Float durationUsedLimit) {
		this.durationUsedLimit = durationUsedLimit;
	}

	public Float getMagnet() {
		return magnet;
	}

	public void setMagnet(Float magnet) {
		this.magnet = magnet;
	}

	public Float getTimeUsedLimit() {
		return timeUsedLimit;
	}

	public void setTimeUsedLimit(Float timeUsedLimit) {
		this.timeUsedLimit = timeUsedLimit;
	}

	public Number getPriority() {
		return priority;
	}

	public void setPriority(Number priority) {
		this.priority = priority;
	}

	public String getReserveStage() {
		return reserveStage;
	}

	public void setReserveStage(String reserveStage) {
		this.reserveStage = reserveStage;
	}
	
	public String getJobDownFlag() {
		return jobDownFlag;
	}

	public void setJobDownFlag(String jobDownFlag) {
		this.jobDownFlag = jobDownFlag;
	}

	public String getMaskStock()
	{
		return maskStock;
	}

	public void setMaskStock(String maskStock)
	{
		this.maskStock = maskStock;
	}
	
	public String getStockerName() {
		return stockerName;
	}

	public void setStockerName(String stockerName) {
		this.stockerName = stockerName;
	}

	public String getReserveLimit() {
		return reserveLimit;
	}

	public void setReserveLimit(String reserveLimit) {
		this.reserveLimit = reserveLimit;
	}
	
	public Number getAoiCount() {
		return aoiCount;
	}

	public void setAoiCount(Number aoiCount) {
		this.aoiCount = aoiCount;
	}
	
	public String getAoiCode() {
		return aoiCode;
	}

	public void setAoiCode(String aoiCode) {
		this.aoiCode = aoiCode;
	}
	
	public String getRepairCode() {
		return repairCode;
	}

	public void setRepairCode(String repairCode) {
		this.repairCode = repairCode;
	}
	
	
	public String getMaskSubSpecName() {
		return maskSubSpecName;
	}

	public void setMaskSubSpecName(String maskSubSpecName) {
		this.maskSubSpecName = maskSubSpecName;
	}
	
	public String getMaskFlowState() {
		return maskFlowState;
	}

	public void setMaskFlowState(String maskFlowState) {
		this.maskFlowState = maskFlowState;
	}
	
	public Number  getMaskCycleCount() {
		return maskCycleCount;
	}

	public void setMaskCycleCount(Number maskCycleCount) {
		this.maskCycleCount = maskCycleCount;
	}
	
	public String getProcessIngInfo() {
		return processIngInfo;
	}

	public void setProcessIngInfo(String processIngInfo) {
		this.processIngInfo = processIngInfo;
	}
	
	public String getCool_Z_Position() {
		return cool_Z_Position;
	}

	public void setCool_Z_Position(String cool_Z_Position) {
		this.cool_Z_Position = cool_Z_Position;
	}
	
	public String getAlignRecipe() {
		return alignRecipe;
	}

	public void setAlignRecipe(String alignRecipe) {
		this.alignRecipe = alignRecipe;
	}

	public String getDetachStickType() {
		return detachStickType;
	}

	public void setDetachStickType(String detachStickType) {
		this.detachStickType = detachStickType;
	}
	
	public String getDetachPosition() {
		return detachPosition;
	}

	public void setDetachPosition(String detachPosition) {
		this.detachPosition = detachPosition;
	}
	
	public String getMaskFilmLayer()
	{
		return maskFilmLayer;
	}

	public void setMaskFilmLayer(String maskFilmLayer)
	{
		this.maskFilmLayer = maskFilmLayer;
	}

	public String getBoxName() {
		return boxName;
	}

	public void setBoxName(String boxName) {
		this.boxName = boxName;
	}
	
	public String getR2rOffSetX() {
		return r2rOffSetX;
	}

	public void setR2rOffSetX(String r2rOffSetX) {
		this.r2rOffSetX = r2rOffSetX;
	}

	public String getR2rOffSetY() {
		return r2rOffSetY;
	}

	public void setR2rOffSetY(String r2rOffSetY) {
		this.r2rOffSetY = r2rOffSetY;
	}

	public String getR2rOffSetTheta() {
		return r2rOffSetTheta;
	}

	public void setR2rOffSetTheta(String r2rOffSetTheta) {
		this.r2rOffSetTheta = r2rOffSetTheta;
	}

	public String getVcrMaskName()
	{
		return vcrMaskName;
	}

	public void setVcrMaskName(String vcrMaskName)
	{
		this.vcrMaskName = vcrMaskName;
	}

	public String getTFEOFFSETX1()
	{
		return TFEOFFSETX1;
	}

	public void setTFEOFFSETX1(String tFEOFFSETX1)
	{
		TFEOFFSETX1 = tFEOFFSETX1;
	}

	public String getTFEOFFSETY1()
	{
		return TFEOFFSETY1;
	}

	public void setTFEOFFSETY1(String tFEOFFSETY1)
	{
		TFEOFFSETY1 = tFEOFFSETY1;
	}

	public String getTFEOFFSETX2()
	{
		return TFEOFFSETX2;
	}

	public void setTFEOFFSETX2(String tFEOFFSETX2)
	{
		TFEOFFSETX2 = tFEOFFSETX2;
	}

	public String getTFEOFFSETY2()
	{
		return TFEOFFSETY2;
	}

	public void setTFEOFFSETY2(String tFEOFFSETY2)
	{
		TFEOFFSETY2 = tFEOFFSETY2;
	}

	public String getTransportLockFlag() {
		return transportLockFlag;
	}

	public void setTransportLockFlag(String transportLockFlag) {
		this.transportLockFlag = transportLockFlag;
	}

	public String getSpcReserveHoldFlag() {
		return spcReserveHoldFlag;
	}

	public void setSpcReserveHoldFlag(String spcReserveHoldFlag) {
		this.spcReserveHoldFlag = spcReserveHoldFlag;
	}
	
}