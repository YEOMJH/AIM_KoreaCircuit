package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import org.apache.commons.net.ntp.TimeStamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class MachineHistory extends UdfAccessor{
	@CTORMTemplate(seq = "1", name="MachineName", type="Key", dataType="String", initial="", history="")
	private String MachineName;
	
	@CTORMTemplate(seq = "2", name="TimeKey", type="Key", dataType="String", initial="", history="")
	private String TimeKey;
	
	@CTORMTemplate(seq = "3", name="EventTime", type="Column", dataType="TimeStamp", initial="", history="")
	private Timestamp EventTime;
	
	@CTORMTemplate(seq = "4", name="EventName", type="Column", dataType="String", initial="", history="")
	private String EventName;
	
	@CTORMTemplate(seq = "5", name="CommunicationState", type="Column", dataType="String", initial="", history="")
	private String CommunicationState;
	
	@CTORMTemplate(seq = "6", name="OldMachineStateName", type="Column", dataType="String", initial="", history="")
	private String OldMachineStateName;
	
	@CTORMTemplate(seq = "7", name="MachineStateName", type="Column", dataType="String", initial="", history="")
	private String MachineStateName;
	
	@CTORMTemplate(seq = "8", name="EventUser", type="Column", dataType="String", initial="", history="")
	private String EventUser;
	
	@CTORMTemplate(seq = "9", name="EventComment", type="Column", dataType="String", initial="", history="")
	private String EventComment;
	
	@CTORMTemplate(seq = "10", name="ReasonCodeType", type="Column", dataType="String", initial="", history="")
	private String ReasonCodeType;
	
	@CTORMTemplate(seq = "11", name="ReasonCode", type="Column", dataType="String", initial="", history="")
	private String ReasonCode;
	
	@CTORMTemplate(seq = "12", name="E10State", type="Column", dataType="String", initial="", history="")
	private String E10State;
	
	@CTORMTemplate(seq = "13", name="OperationMode", type="Column", dataType="String", initial="", history="")
	private String OperationMode;
	
	@CTORMTemplate(seq = "14", name="MachineEventName", type="Column", dataType="String", initial="", history="")
	private String MachineEventName;
	
	
	@CTORMTemplate(seq = "15", name="MACHINESUBSTATE", type="Column", dataType="String", initial="", history="")
	private String MACHINESUBSTATE;
	
	@CTORMTemplate(seq = "16", name="RADIO", type="Column", dataType="String", initial="", history="")
	private String RADIO;
	
	@CTORMTemplate(seq = "17", name="OLDE10STATE", type="Column", dataType="String", initial="", history="")
	private String OLDE10STATE;
	
	@CTORMTemplate(seq = "18", name="RESOURCESTATE", type="Column", dataType="String", initial="", history="")
	private String RESOURCESTATE;
	
	@CTORMTemplate(seq = "19", name="OLDRESOURCESTATE", type="Column", dataType="String", initial="", history="")
	private String OLDRESOURCESTATE;
	
	@CTORMTemplate(seq = "20", name="SYSTEMTIME", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp SYSTEMTIME;
	
	@CTORMTemplate(seq = "21", name="ProcessCount", type="Column", dataType="Number", initial="", history="")
	private double ProcessCount;		

	@CTORMTemplate(seq = "22", name="EventFlag", type="Column", dataType="String", initial="", history="")
	private String EventFlag;
	
	@CTORMTemplate(seq = "23", name="MCSUBJECTNAME", type="Column", dataType="String", initial="", history="")
	private String MCSUBJECTNAME;
	
	@CTORMTemplate(seq = "24", name="CancelFlag", type="Column", dataType="String", initial="", history="")
	private String CancelFlag;
	
	@CTORMTemplate(seq = "25", name="LastIdleTime", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp LastIdleTime;
	
	
	public String getMACHINESUBSTATE() {
		return MACHINESUBSTATE;
	}

	public void setMACHINESUBSTATE(String mACHINESUBSTATE) {
		MACHINESUBSTATE = mACHINESUBSTATE;
	}

	public String getRADIO() {
		return RADIO;
	}

	public void setRADIO(String rADIO) {
		RADIO = rADIO;
	}

	public String getOLDE10STATE() {
		return OLDE10STATE;
	}

	public void setOLDE10STATE(String oLDE10STATE) {
		OLDE10STATE = oLDE10STATE;
	}

	public String getRESOURCESTATE() {
		return RESOURCESTATE;
	}

	public void setRESOURCESTATE(String rESOURCESTATE) {
		RESOURCESTATE = rESOURCESTATE;
	}

	public String getOLDRESOURCESTATE() {
		return OLDRESOURCESTATE;
	}

	public void setOLDRESOURCESTATE(String oLDRESOURCESTATE) {
		OLDRESOURCESTATE = oLDRESOURCESTATE;
	}



	public Timestamp getSYSTEMTIME() {
		return SYSTEMTIME;
	}

	public void setSYSTEMTIME(Timestamp sYSTEMTIME) {
		SYSTEMTIME = sYSTEMTIME;
	}

	public String getMachineName() {
		return MachineName;
	}

	public void setMachineName(String machineName) {
		MachineName = machineName;
	}

	public String getTimeKey() {
		return TimeKey;
	}

	public void setTimeKey(String timeKey) {
		TimeKey = timeKey;
	}

	public Timestamp getEventTime() {
		return EventTime;
	}

	public void setEventTime(Timestamp eventTime) {
		EventTime = eventTime;
	}

	public String getEventName() {
		return EventName;
	}

	public void setEventName(String eventName) {
		EventName = eventName;
	}

	public String getCommunicationState() {
		return CommunicationState;
	}

	public void setCommunicationState(String communicationState) {
		CommunicationState = communicationState;
	}

	public String getOldMachineStateName() {
		return OldMachineStateName;
	}

	public void setOldMachineStateName(String oldMachineStateName) {
		OldMachineStateName = oldMachineStateName;
	}

	public String getMachineStateName() {
		return MachineStateName;
	}

	public void setMachineStateName(String machineStateName) {
		MachineStateName = machineStateName;
	}

	public String getEventUser() {
		return EventUser;
	}

	public void setEventUser(String eventUser) {
		EventUser = eventUser;
	}

	public String getEventComment() {
		return EventComment;
	}

	public void setEventComment(String eventComment) {
		EventComment = eventComment;
	}

	public String getReasonCodeType() {
		return ReasonCodeType;
	}

	public void setReasonCodeType(String reasonCodeType) {
		ReasonCodeType = reasonCodeType;
	}

	public String getReasonCode() {
		return ReasonCode;
	}

	public void setReasonCode(String reasonCode) {
		ReasonCode = reasonCode;
	}

	public String getE10State() {
		return E10State;
	}

	public void setE10State(String state) {
		E10State = state;
	}

	public String getOperationMode() {
		return OperationMode;
	}

	public void setOperationMode(String operationMode) {
		OperationMode = operationMode;
	}

	public String getMachineEventName() {
		return MachineEventName;
	}

	public void setMachineEventName(String machineEventName) {
		MachineEventName = machineEventName;
	}
	
	public double getProcessCount() {
		return ProcessCount;
	}

	public void setProcessCount(double processCount) {
		ProcessCount = processCount;
	}

	public String getEventFlag() {
		return EventFlag;
	}

	public void setEventFlag(String eventFlag) {
		EventFlag = eventFlag;
	}

	public String getMCSUBJECTNAME() {
		return MCSUBJECTNAME;
	}

	public void setMCSUBJECTNAME(String mCSUBJECTNAME) {
		MCSUBJECTNAME = mCSUBJECTNAME;
	}

	public String getCancelFlag() {
		return CancelFlag;
	}

	public void setCancelFlag(String cancelFlag) {
		CancelFlag = cancelFlag;
	}

	public Timestamp getLastIdleTime() {
		return LastIdleTime;
	}

	public void setLastIdleTime(Timestamp lastIdleTime) {
		LastIdleTime = lastIdleTime;
	}

}