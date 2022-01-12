package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class ProductIdleTime extends UdfAccessor 
{
	@CTORMTemplate(seq = "1", name="MACHINENAME", type="Key", dataType="String", initial="", history="")
	public String MACHINENAME;
	@CTORMTemplate(seq = "2", name="PRODUCTSPECNAME", type="Key", dataType="String", initial="", history="")
	public String PRODUCTSPECNAME;
	@CTORMTemplate(seq = "3", name="PROCESSOPERATIONNAME", type="Key", dataType="String", initial="", history="")
	public String PROCESSOPERATIONNAME;
	@CTORMTemplate(seq = "4", name="ENABLE", type="Column", dataType="String", initial="", history="")
	public String ENABLE;
	@CTORMTemplate(seq = "5", name="LASTPROCESSENDTIME", type="Column", dataType="Timestamp", initial="", history="")
	public Timestamp LASTPROCESSENDTIME;
	@CTORMTemplate(seq = "6", name="OVERTIME", type="Column", dataType="Number", initial="", history="")
	public Double OVERTIME;
	@CTORMTemplate(seq = "7", name="ALARMTIME", type="Column", dataType="Number", initial="", history="")
	public Double ALARMTIME;
	@CTORMTemplate(seq = "8", name="LOCKFLAG", type="Column", dataType="String", initial="", history="")
	public String LOCKFLAG;
	@CTORMTemplate(seq = "9", name="EVENTUSER", type="Column", dataType="String", initial="", history="N")
	public String EVENTUSER;
	@CTORMTemplate(seq = "10", name="EVENTCOMMENT", type="Column", dataType="String", initial="", history="N")
	public String EVENTCOMMENT;
	
	public String getMACHINENAME() {
		return MACHINENAME;
	}
	public void setMACHINENAME(String machinename) {
		MACHINENAME = machinename;
	}
	public String getPRODUCTSPECNAME() {
		return PRODUCTSPECNAME;
	}
	public void setPRODUCTSPECNAME(String productspecname) {
		PRODUCTSPECNAME = productspecname;
	}
	public String getPROCESSOPERATIONNAME() {
		return PROCESSOPERATIONNAME;
	}
	public void setPROCESSOPERATIONNAME(String processoperationname) {
		PROCESSOPERATIONNAME = processoperationname;
	}
	public String getENABLE() {
		return ENABLE;
	}
	public void setENABLE(String enable) {
		ENABLE = enable;
	}
	public Timestamp getLASTPROCESSENDTIME() {
		return LASTPROCESSENDTIME;
	}
	public void setLASTPROCESSENDTIME(Timestamp lastprocessendtime) {
		LASTPROCESSENDTIME = lastprocessendtime;
	}
	public Double getOVERTIME() {
		return OVERTIME;
	}
	public void setOVERTIME(Double overtime) {
		OVERTIME = overtime;
	}
	public Double getALARMTIME() {
		return ALARMTIME;
	}
	public void setALARMTIME(Double alarmtime) {
		ALARMTIME = alarmtime;
	}
	public String getLOCKFLAG() {
		return LOCKFLAG;
	}
	public void setLOCKFLAG(String lock) {
		LOCKFLAG = lock;
	}
	public String getEVENTUSER() {
		return EVENTUSER;
	}
	public void setEVENTUSER(String eventuser) {
		EVENTUSER = eventuser;
	}
	public String getEVENTCOMMENT() {
		return EVENTCOMMENT;
	}
	public void setEVENTCOMMENT(String eventcomment) {
		EVENTCOMMENT = eventcomment;
	}
	
}











