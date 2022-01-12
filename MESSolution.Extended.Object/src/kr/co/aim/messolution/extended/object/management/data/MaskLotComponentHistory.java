package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class MaskLotComponentHistory extends UdfAccessor {
	@CTORMTemplate(seq = "1", name="timeKey", type="Key", dataType="String", initial="", history="")
	private String timeKey;
	@CTORMTemplate(seq = "2", name="maskLotName", type="Key", dataType="String", initial="", history="")
	private String maskLotName;
	@CTORMTemplate(seq = "3", name="eventName", type="Key", dataType="String", initial="", history="")
	private String eventName;
	@CTORMTemplate(seq = "4", name="maskGroupName", type="Column", dataType="String", initial="", history="")
	private String maskGroupName;
	@CTORMTemplate(seq = "5", name="toSlotId", type="Column", dataType="String", initial="", history="")
	private String toSlotId;
	@CTORMTemplate(seq = "6", name="fromSlotId", type="Column", dataType="String", initial="", history="")
	private String fromSlotId;
	@CTORMTemplate(seq = "7", name="eventTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp eventTime;
	@CTORMTemplate(seq = "8", name="eventUser", type="Column", dataType="String", initial="", history="")
	private String eventUser;
	@CTORMTemplate(seq = "9", name="factoryName", type="Column", dataType="String", initial="", history="")
	private String factoryName;
	@CTORMTemplate(seq = "10", name="vcrMaskLotName", type="Column", dataType="String", initial="", history="")
	private String vcrMaskLotName;
	@CTORMTemplate(seq = "11", name="maskLotGrade", type="Column", dataType="String", initial="", history="")
	private String maskLotGrade;
	@CTORMTemplate(seq = "12", name="maskLotJudge", type="Column", dataType="String", initial="", history="")
	private String maskLotJudge;
	@CTORMTemplate(seq = "13", name="carrierName", type="Column", dataType="String", initial="", history="")
	private String carrierName;
	@CTORMTemplate(seq = "14", name="maskProcessOperationName", type="Column", dataType="String", initial="", history="")
	private String maskProcessOperationName;
	@CTORMTemplate(seq = "15", name="machineName", type="Column", dataType="String", initial="", history="")
	private String machineName;
	@CTORMTemplate(seq = "16", name="materialLocationName", type="Column", dataType="String", initial="", history="")
	private String materialLocationName;
	@CTORMTemplate(seq = "17", name="productionType", type="Column", dataType="String", initial="", history="")
	private String productionType;
	@CTORMTemplate(seq = "18", name="maskSpecName", type="Column", dataType="String", initial="", history="")
	private String maskSpecName;
	@CTORMTemplate(seq = "19", name="maskProcessFlowName", type="Column", dataType="String", initial="", history="")
	private String maskProcessFlowName;
	@CTORMTemplate(seq = "20", name="frameName", type="Column", dataType="String", initial="", history="")
	private String frameName;
	
	public String getTimeKey() {
		return timeKey;
	}
	public void setTimeKey(String timeKey) {
		this.timeKey = timeKey;
	}
	public String getMaskLotName() {
		return maskLotName;
	}
	public void setMaskLotName(String maskLotName) {
		this.maskLotName = maskLotName;
	}
	public String getEventName() {
		return eventName;
	}
	public void setEventName(String eventName) {
		this.eventName = eventName;
	}
	public String getMaskGroupName() {
		return maskGroupName;
	}
	public void setMaskGroupName(String maskGroupName) {
		this.maskGroupName = maskGroupName;
	}
	public String getToSlotId() {
		return toSlotId;
	}
	public void setToSlotId(String toSlotId) {
		this.toSlotId = toSlotId;
	}
	public String getFromSlotId() {
		return fromSlotId;
	}
	public void setFromSlotId(String fromSlotId) {
		this.fromSlotId = fromSlotId;
	}
	public Timestamp getEventTime() {
		return eventTime;
	}
	public void setEventTime(Timestamp eventTime) {
		this.eventTime = eventTime;
	}
	public String getEventUser() {
		return eventUser;
	}
	public void setEventUser(String eventUser) {
		this.eventUser = eventUser;
	}
	public String getFactoryName() {
		return factoryName;
	}
	public void setFactoryName(String factoryName) {
		this.factoryName = factoryName;
	}
	public String getVcrMaskLotName() {
		return vcrMaskLotName;
	}
	public void setVcrMaskLotName(String vcrMaskLotName) {
		this.vcrMaskLotName = vcrMaskLotName;
	}
	public String getMaskLotGrade() {
		return maskLotGrade;
	}
	public void setMaskLotGrade(String maskLotGrade) {
		this.maskLotGrade = maskLotGrade;
	}
	public String getMaskLotJudge() {
		return maskLotJudge;
	}
	public void setMaskLotJudge(String maskLotJudge) {
		this.maskLotJudge = maskLotJudge;
	}
	public String getCarrierName() {
		return carrierName;
	}
	public void setCarrierName(String carrierName) {
		this.carrierName = carrierName;
	}
	public String getMaskProcessOperationName() {
		return maskProcessOperationName;
	}
	public void setMaskProcessOperationName(String maskProcessOperationName) {
		this.maskProcessOperationName = maskProcessOperationName;
	}
	public String getMachineName() {
		return machineName;
	}
	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}
	public String getMaterialLocationName() {
		return materialLocationName;
	}
	public void setMaterialLocationName(String materialLocationName) {
		this.materialLocationName = materialLocationName;
	}
	public String getProductionType() {
		return productionType;
	}
	public void setProductionType(String productionType) {
		this.productionType = productionType;
	}
	public String getMaskSpecName() {
		return maskSpecName;
	}
	public void setMaskSpecName(String maskSpecName) {
		this.maskSpecName = maskSpecName;
	}
	public String getMaskProcessFlowName() {
		return maskProcessFlowName;
	}
	public void setMaskProcessFlowName(String maskProcessFlowName) {
		this.maskProcessFlowName = maskProcessFlowName;
	}
	public String getFrameName() {
		return frameName;
	}
	public void setFrameName(String frameName) {
		this.frameName = frameName;
	}	
}
