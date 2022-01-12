package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class DLAPacking extends UdfAccessor{
	@CTORMTemplate(seq = "1", name="deLamiGlassName", type="Key", dataType="String", initial="", history="N")
	private String deLamiGlassName;
	@CTORMTemplate(seq = "2", name="productName", type="Column", dataType="String", initial="", history="N")
	private String productName;
	@CTORMTemplate(seq = "3", name="boxName", type="Column", dataType="String", initial="", history="N")
	private String boxName;
	@CTORMTemplate(seq = "4", name="machineName", type="Column", dataType="String", initial="", history="N")
	private String machineName;
	@CTORMTemplate(seq = "5", name="productSpecName", type="Column", dataType="String", initial="", history="N")
	private String productSpecName;
	@CTORMTemplate(seq = "6", name="productSpecVersion", type="Column", dataType="String", initial="", history="N")
	private String productSpecVersion;
	@CTORMTemplate(seq = "7", name="processOperationName", type="Column", dataType="String", initial="", history="N")
	private String processOperationName;
	@CTORMTemplate(seq = "8", name="processOperationVersion", type="Column", dataType="String", initial="", history="N")
	private String processOperationVersion;
	@CTORMTemplate(seq = "9", name="slotNo", type="Column", dataType="String", initial="", history="N")
	private String slotNo;
	@CTORMTemplate(seq = "10", name="eventUser", type="Column", dataType="String", initial="", history="N")
	private String eventUser;
	@CTORMTemplate(seq = "11", name="eventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp eventTime;
	public String getDeLamiGlassName() {
		return deLamiGlassName;
	}
	public void setDeLamiGlassName(String deLamiGlassName) {
		this.deLamiGlassName = deLamiGlassName;
	}
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	public String getBoxName() {
		return boxName;
	}
	public void setBoxName(String boxName) {
		this.boxName = boxName;
	}
	public String getMachineName() {
		return machineName;
	}
	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}
	public String getProductSpecName() {
		return productSpecName;
	}
	public void setProductSpecName(String productSpecName) {
		this.productSpecName = productSpecName;
	}
	public String getProductSpecVersion() {
		return productSpecVersion;
	}
	public void setProductSpecVersion(String productSpecVersion) {
		this.productSpecVersion = productSpecVersion;
	}
	public String getProcessOperationName() {
		return processOperationName;
	}
	public void setProcessOperationName(String processOperationName) {
		this.processOperationName = processOperationName;
	}
	public String getProcessOperationVersion() {
		return processOperationVersion;
	}
	public void setProcessOperationVersion(String processOperationVersion) {
		this.processOperationVersion = processOperationVersion;
	}
	public String getSlotNo() {
		return slotNo;
	}
	public void setSlotNo(String slotNo) {
		this.slotNo = slotNo;
	}
	public String getEventUser() {
		return eventUser;
	}
	public void setEventUser(String eventUser) {
		this.eventUser = eventUser;
	}
	public Timestamp getEventTime() {
		return eventTime;
	}
	public void setEventTime(Timestamp eventTime) {
		this.eventTime = eventTime;
	}

}
