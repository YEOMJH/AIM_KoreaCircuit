package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class ComponentPalletHistory extends UdfAccessor {
	@CTORMTemplate(seq = "1", name="timeKey", type="Key", dataType="String", initial="", history="")
	private String timeKey;
	@CTORMTemplate(seq = "2", name="palletName", type="Key", dataType="String", initial="", history="")
	private String palletName;
	@CTORMTemplate(seq = "3", name="eventName", type="Key", dataType="String", initial="", history="")
	private String eventName;
	@CTORMTemplate(seq = "4", name="eventTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp eventTime;
	@CTORMTemplate(seq = "5", name="eventUser", type="Column", dataType="String", initial="", history="")
	private String eventUser;
	@CTORMTemplate(seq = "6", name="factoryName", type="Column", dataType="String", initial="", history="")
	private String factoryName;
	@CTORMTemplate(seq = "7", name="machineName", type="Column", dataType="String", initial="", history="")
	private String machineName;
	@CTORMTemplate(seq = "8", name="unitName", type="Column", dataType="String", initial="", history="")
	private String unitName;
	@CTORMTemplate(seq = "9", name="subUnitName", type="Column", dataType="String", initial="", history="")
	private String subUnitName;
	
	public String getTimeKey() {
		return timeKey;
	}
	public void setTimeKey(String timeKey) {
		this.timeKey = timeKey;
	}
	public String getPalletName() {
		return palletName;
	}
	public void setPalletName(String palletName) {
		this.palletName = palletName;
	}
	public String getEventName() {
		return eventName;
	}
	public void setEventName(String eventName) {
		this.eventName = eventName;
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
	public String getSubUnitName() {
		return subUnitName;
	}
	public void setSubUnitName(String subUnitName) {
		this.subUnitName = subUnitName;
	}
	
}
