package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class ELACondition extends UdfAccessor {

	@CTORMTemplate(seq = "1", name="machineName", type="Key", dataType="String", initial="", history="")
	private String machineName ;
	@CTORMTemplate(seq = "2", name="productSpecName", type="Key", dataType="String", initial="", history="")
	private String productSpecName;
	@CTORMTemplate(seq = "3", name="uc1", type="Column", dataType="Number", initial="", history="")
	private Number uc1;
	@CTORMTemplate(seq = "4", name="uc2", type="Column", dataType="Number", initial="", history="")
	private Number uc2 ;
	@CTORMTemplate(seq = "5", name="uc3", type="Column", dataType="Number", initial="", history="")
	private Number uc3;
	@CTORMTemplate(seq = "6", name="uc4", type="Column", dataType="Number", initial="", history="")
	private Number uc4;
	@CTORMTemplate(seq = "7", name="uc5", type="Column", dataType="Number", initial="", history="")
	private Number uc5;
	@CTORMTemplate(seq = "8", name="uc6", type="Column", dataType="Number", initial="", history="")
	private Number uc6;
	@CTORMTemplate(seq = "9", name="uc1Value", type="Column", dataType="Number", initial="", history="")
	private Number uc1Value;
	@CTORMTemplate(seq = "10", name="uc2Value", type="Column", dataType="Number", initial="", history="")
	private Number uc2Value;
	@CTORMTemplate(seq = "11", name="uc3Value", type="Column", dataType="Number", initial="", history="")
	private Number uc3Value;
	@CTORMTemplate(seq = "12", name="uc4Value", type="Column", dataType="Number", initial="", history="")
	private Number uc4Value;
	@CTORMTemplate(seq = "13", name="uc5Value", type="Column", dataType="Number", initial="", history="")
	private Number uc5Value;
	@CTORMTemplate(seq = "14", name="uc6UpperValue", type="Column", dataType="Number", initial="", history="")
	private Number uc6UpperValue;
	@CTORMTemplate(seq = "15", name="uc6LowerValue", type="Column", dataType="Number", initial="", history="")
	private Number uc6LowerValue;
	@CTORMTemplate(seq = "16", name="lastEventName", type="Column", dataType="String", initial="", history="")
	private String lastEventName;
	@CTORMTemplate(seq = "17", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="")
	private String lastEventTimeKey;
	@CTORMTemplate(seq = "18", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp lastEventTime;
	@CTORMTemplate(seq = "19", name="lastEventUser", type="Column", dataType="String", initial="", history="")
	private String lastEventUser;
	@CTORMTemplate(seq = "20", name="lastEventComment", type="Column", dataType="String", initial="", history="")
	private String lastEventComment;
	@CTORMTemplate(seq = "21", name="nfcEarly", type="Column", dataType="Number", initial="", history="")
	private Number nfcEarly;
	@CTORMTemplate(seq = "22", name="nfcLate", type="Column", dataType="Number", initial="", history="")
	private Number nfcLate;
	@CTORMTemplate(seq = "23", name="nfcMidLower", type="Column", dataType="Number", initial="", history="")
	private Number nfcMidLower;
	@CTORMTemplate(seq = "24", name="nfcMidUpper", type="Column", dataType="Number", initial="", history="")
	private Number nfcMidUpper;
	@CTORMTemplate(seq = "25", name="earlyFlag", type="Column", dataType="String", initial="", history="")
	private String earlyFlag;
	@CTORMTemplate(seq = "26", name="lateFlag", type="Column", dataType="String", initial="", history="")
	private String lateFlag;
	@CTORMTemplate(seq = "27", name="midFlag", type="Column", dataType="String", initial="", history="")
	private String midFlag;
	
	public ELACondition()
	{
		
	}
	
	public ELACondition(String productSpecName,String machineName )
	{
		setProductSpecName(productSpecName);
		setMachineName(machineName);
	}

	public String getMachineName()
	{
		return machineName;
	}

	public void setMachineName(String machineName)
	{
		this.machineName = machineName;
	}

	public String getProductSpecName()
	{
		return productSpecName;
	}

	public void setProductSpecName(String productSpecName)
	{
		this.productSpecName = productSpecName;
	}

	public Number getUc1()
	{
		return uc1;
	}

	public void setUc1(Number uc1)
	{
		this.uc1 = uc1;
	}

	public Number getUc2()
	{
		return uc2;
	}

	public void setUc2(Number uc2)
	{
		this.uc2 = uc2;
	}

	public Number getUc3()
	{
		return uc3;
	}

	public void setUc3(Number uc3)
	{
		this.uc3 = uc3;
	}

	public Number getUc4()
	{
		return uc4;
	}

	public void setUc4(Number uc4)
	{
		this.uc4 = uc4;
	}

	public Number getUc5()
	{
		return uc5;
	}

	public void setUc5(Number uc5)
	{
		this.uc5 = uc5;
	}

	public Number getUc6()
	{
		return uc6;
	}

	public void setUc6(Number uc6)
	{
		this.uc6 = uc6;
	}

	public Number getUc1Value()
	{
		return uc1Value;
	}

	public void setUc1Value(Number uc1Value)
	{
		this.uc1Value = uc1Value;
	}

	public Number getUc2Value()
	{
		return uc2Value;
	}

	public void setUc2Value(Number uc2Value)
	{
		this.uc2Value = uc2Value;
	}

	public Number getUc3Value()
	{
		return uc3Value;
	}

	public void setUc3Value(Number uc3Value)
	{
		this.uc3Value = uc3Value;
	}

	public Number getUc4Value()
	{
		return uc4Value;
	}

	public void setUc4Value(Number uc4Value)
	{
		this.uc4Value = uc4Value;
	}

	public Number getUc5Value()
	{
		return uc5Value;
	}

	public void setUc5Value(Number uc5Value)
	{
		this.uc5Value = uc5Value;
	}

	public Number getUc6UpperValue()
	{
		return uc6UpperValue;
	}

	public void setUc6UpperValue(Number uc6UpperValue)
	{
		this.uc6UpperValue = uc6UpperValue;
	}

	public Number getUc6LowerValue()
	{
		return uc6LowerValue;
	}

	public void setUc6LowerValue(Number uc6LowerValue)
	{
		this.uc6LowerValue = uc6LowerValue;
	}

	public String getLastEventName()
	{
		return lastEventName;
	}

	public void setLastEventName(String lastEventName)
	{
		this.lastEventName = lastEventName;
	}

	public String getLastEventTimeKey()
	{
		return lastEventTimeKey;
	}

	public void setLastEventTimeKey(String lastEventTimeKey)
	{
		this.lastEventTimeKey = lastEventTimeKey;
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

	public Number getNfcEarly() {
		return nfcEarly;
	}

	public void setNfcEarly(Number nfcEarly) {
		this.nfcEarly = nfcEarly;
	}

	public Number getNfcLate() {
		return nfcLate;
	}

	public void setNfcLate(Number nfcLate) {
		this.nfcLate = nfcLate;
	}

	public Number getNfcMidLower() {
		return nfcMidLower;
	}

	public void setNfcMidLower(Number nfcMidLower) {
		this.nfcMidLower = nfcMidLower;
	}

	public Number getNfcMidUpper() {
		return nfcMidUpper;
	}

	public void setNfcMidUpper(Number nfcMidUpper) {
		this.nfcMidUpper = nfcMidUpper;
	}

	public String getEarlyFlag() {
		return earlyFlag;
	}

	public void setEarlyFlag(String earlyFlag) {
		this.earlyFlag = earlyFlag;
	}

	public String getLateFlag() {
		return lateFlag;
	}

	public void setLateFlag(String lateFlag) {
		this.lateFlag = lateFlag;
	}

	public String getMidFlag() {
		return midFlag;
	}

	public void setMidFlag(String midFlag) {
		this.midFlag = midFlag;
	}
	
}
