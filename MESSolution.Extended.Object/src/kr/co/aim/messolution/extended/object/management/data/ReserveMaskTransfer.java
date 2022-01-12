package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class ReserveMaskTransfer extends UdfAccessor {
	@CTORMTemplate(seq = "1", name = "carrierName", type = "Key", dataType = "String", initial = "", history = "")
	private String carrierName;

	@CTORMTemplate(seq = "2", name = "position", type = "Key", dataType = "String", initial = "", history = "")
	private String position;

	@CTORMTemplate(seq = "3", name = "maskLotName", type = "Column", dataType = "String", initial = "", history = "")
	private String maskLotName;

	@CTORMTemplate(seq = "4", name = "machineName", type = "Column", dataType = "String", initial = "", history = "")
	private String machineName;

	@CTORMTemplate(seq = "5", name = "lastEventName", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventName;

	@CTORMTemplate(seq = "6", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventUser;

	@CTORMTemplate(seq = "7", name = "lastEventTime", type = "Column", dataType = "Timestamp", initial = "", history = "N")
	private Timestamp lastEventTime;

	@CTORMTemplate(seq = "8", name = "lastEventTimeKey", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventTimeKey;

	@CTORMTemplate(seq = "9", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventComment;
	
	@CTORMTemplate(seq = "10", name = "autoFlag", type = "Column", dataType = "String", initial = "", history = "")
	private String autoFlag;
	
	@CTORMTemplate(seq = "11", name = "maskGroupName", type = "Column", dataType = "String", initial = "", history = "")
	private String maskGroupName;
	
	@CTORMTemplate(seq = "12", name = "dspFlag", type = "Column", dataType = "String", initial = "", history = "")
	private String dspFlag;
	
	@CTORMTemplate(seq = "13", name = "productSpecName", type = "Column", dataType = "String", initial = "", history = "")
	private String productSpecName;
	
	@CTORMTemplate(seq = "14", name = "productRequestName", type = "Column", dataType = "String", initial = "", history = "")
	private String productRequestName;
	
	@CTORMTemplate(seq = "15", name = "seq", type = "Column", dataType = "Number", initial = "", history = "")
	private long seq;
	
	@CTORMTemplate(seq = "16", name = "evaMachineName", type = "Column", dataType = "String", initial = "", history = "")
	private String evaMachineName;
	
	@CTORMTemplate(seq = "14", name = "stage", type = "Column", dataType = "String", initial = "", history = "")
	private String stage;
	
	@CTORMTemplate(seq = "15", name = "evaUnitName", type = "Column", dataType = "String", initial = "", history = "")
	private String evaUnitName;
	
	@CTORMTemplate(seq = "16", name = "evaSubUnitName", type = "Column", dataType = "String", initial = "", history = "")
	private String evaSubUnitName;
	
	



	public String getCarrierName()
	{
		return carrierName;
	}

	public void setCarrierName(String carrierName)
	{
		this.carrierName = carrierName;
	}

	public String getPosition()
	{
		return position;
	}

	public void setPosition(String position)
	{
		this.position = position;
	}

	public String getMaskLotName()
	{
		return maskLotName;
	}

	public void setMaskLotName(String maskLotName)
	{
		this.maskLotName = maskLotName;
	}

	public String getMachineName()
	{
		return machineName;
	}

	public void setMachineName(String machineName)
	{
		this.machineName = machineName;
	}

	public String getLastEventName()
	{
		return lastEventName;
	}

	public void setLastEventName(String lastEventName)
	{
		this.lastEventName = lastEventName;
	}

	public String getLastEventUser()
	{
		return lastEventUser;
	}

	public void setLastEventUser(String lastEventUser)
	{
		this.lastEventUser = lastEventUser;
	}

	public Timestamp getLastEventTime()
	{
		return lastEventTime;
	}

	public void setLastEventTime(Timestamp lastEventTime)
	{
		this.lastEventTime = lastEventTime;
	}

	public String getLastEventTimeKey()
	{
		return lastEventTimeKey;
	}

	public void setLastEventTimeKey(String lastEventTimeKey)
	{
		this.lastEventTimeKey = lastEventTimeKey;
	}

	public String getLastEventComment()
	{
		return lastEventComment;
	}

	public void setLastEventComment(String lastEventComment)
	{
		this.lastEventComment = lastEventComment;
	}
	
	public String getAutoFlag() {
		return autoFlag;
	}

	public void setAutoFlag(String autoFlag) {
		this.autoFlag = autoFlag;
	}

	public String getMaskGroupName() {
		return maskGroupName;
	}

	public void setMaskGroupName(String maskGroupName) {
		this.maskGroupName = maskGroupName;
	}

	public String getDspFlag() {
		return dspFlag;
	}

	public void setDspFlag(String dspFlag) {
		this.dspFlag = dspFlag;
	}

	public String getProductSpecName() {
		return productSpecName;
	}

	public void setProductSpecName(String productSpecName) {
		this.productSpecName = productSpecName;
	}

	public String getProductRequestName() {
		return productRequestName;
	}

	public void setProductRequestName(String productRequestName) {
		this.productRequestName = productRequestName;
	}

	public long getSeq() {
		return seq;
	}

	public void setSeq(long seq) {
		this.seq = seq;
	}

	public String getEvaMachineName() {
		return evaMachineName;
	}

	public void setEvaMachineName(String evaMachineName) {
		this.evaMachineName = evaMachineName;
	}

	public String getStage() {
		return stage;
	}

	public void setStage(String stage) {
		this.stage = stage;
	}

	public String getEvaUnitName() {
		return evaUnitName;
	}

	public void setEvaUnitName(String evaUnitName) {
		this.evaUnitName = evaUnitName;
	}

	public String getEvaSubUnitName() {
		return evaSubUnitName;
	}

	public void setEvaSubUnitName(String evaSubUnitName) {
		this.evaSubUnitName = evaSubUnitName;
	}

	public ReserveMaskTransfer(){}
/*
	public ReserveMaskTransfer(String carrierName, String position, String maskLotName, String machineName) {
	
		setCarrierName(carrierName);
		setPosition(position);
		setMaskLotName(maskLotName);
		setMachineName(machineName);
	}*/

	public ReserveMaskTransfer(String carrierName, String position, String maskLotName, String machineName,
			String autoFlag, String maskGroupName, String dspFlag, String productSpecName, String productRequestName,
			long seq, String evaMachineName, String stage, String evaUnitName, String evaSubUnitName,String lastEventName,String lastEventTimeKey,String lastEventComment,Timestamp lastEventTime,String lastEventUser) //
	{
		setCarrierName(carrierName);
		setPosition(position);
		setMaskLotName(maskLotName);
		setMachineName(machineName);
		setAutoFlag(autoFlag);
		setMaskGroupName(maskGroupName);
		setDspFlag(dspFlag);
		setProductSpecName(productSpecName);
		setProductRequestName(productRequestName);
		setSeq(seq);
		setEvaMachineName(evaMachineName);
		setStage(stage);
		setEvaUnitName(evaUnitName);
		setEvaSubUnitName(evaSubUnitName);
		setLastEventName(lastEventName);
		setLastEventTimeKey(lastEventTimeKey);
		setLastEventComment(lastEventComment);
		setLastEventTime(lastEventTime);
		setLastEventUser(lastEventUser);
		
	}
	
	
  
}
