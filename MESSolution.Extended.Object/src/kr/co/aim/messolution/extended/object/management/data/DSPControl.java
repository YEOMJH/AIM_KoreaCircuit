package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class DSPControl extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name = "machineName", type = "Key", dataType = "String", initial = "", history = "")
	private String machineName;
	@CTORMTemplate(seq = "2", name = "processOperationName", type = "Key", dataType = "String", initial = "", history = "")
	private String processOperationName;
	@CTORMTemplate(seq = "3", name = "processOperationVersion", type = "Key", dataType = "String", initial = "", history = "")
	private String processOperationVersion;
	@CTORMTemplate(seq = "4", name = "factoryName", type = "Column", dataType = "String", initial = "", history = "")
	private String factoryName;
	@CTORMTemplate(seq = "5", name = "portName", type = "Column", dataType = "String", initial = "", history = "")
	private String portName;
	@CTORMTemplate(seq = "6", name = "dspFlag", type = "Column", dataType = "String", initial = "", history = "")
	private String dspFlag;
	@CTORMTemplate(seq = "7", name = "lastEventName", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventName;
	@CTORMTemplate(seq = "8", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventUser;
	@CTORMTemplate(seq = "9", name = "lastEventTime", type = "Column", dataType = "Timestamp", initial = "", history = "N")
	private Timestamp lastEventTime;
	@CTORMTemplate(seq = "10", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventComment;
	@CTORMTemplate(seq = "11", name = "lastEventTimekey", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventTimekey;
	
	
	public String getMachineName() {
		return machineName;
	}
	public void setMachineName(String machineName) {
		this.machineName = machineName;
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
	public String getFactoryName() {
		return factoryName;
	}
	public void setFactoryName(String factoryName) {
		this.factoryName = factoryName;
	}
	public String getPortName() {
		return portName;
	}
	public void setPortName(String portName) {
		this.portName = portName;
	}
	public String getDspFlag() {
		return dspFlag;
	}
	public void setDspFlag(String dspFlag) {
		this.dspFlag = dspFlag;
	}
	public String getLastEventName() {
		return lastEventName;
	}
	public void setLastEventName(String lastEventName) {
		this.lastEventName = lastEventName;
	}
	public String getLastEventUser() {
		return lastEventUser;
	}
	public void setLastEventUser(String lastEventUser) {
		this.lastEventUser = lastEventUser;
	}
	public Timestamp getLastEventTime() {
		return lastEventTime;
	}
	public void setLastEventTime(Timestamp lastEventTime) {
		this.lastEventTime = lastEventTime;
	}
	public String getLastEventComment() {
		return lastEventComment;
	}
	public void setLastEventComment(String lastEventComment) {
		this.lastEventComment = lastEventComment;
	}
	public String getLastEventTimekey() {
		return lastEventTimekey;
	}
	public void setLastEventTimekey(String lastEventTimekey) {
		this.lastEventTimekey = lastEventTimekey;
	}
}
