package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class TPFOPolicy extends UdfAccessor
{
	@CTORMTemplate(seq = "1", name="FACTORYNAME", type="Key", dataType="String", initial="", history="")
	public String FACTORYNAME;
	@CTORMTemplate(seq = "2", name="PRODUCTSPECNAME", type="Key", dataType="String", initial="", history="")
	public String PRODUCTSPECNAME;
	@CTORMTemplate(seq = "3", name="PRODUCTSPECVERSION", type="Key", dataType="String", initial="", history="")
	public String PRODUCTSPECVERSION;
	@CTORMTemplate(seq = "4", name="PROCESSFLOWNAME", type="Key", dataType="String", initial="", history="")
	public String PROCESSFLOWNAME;
	@CTORMTemplate(seq = "5", name="PROCESSFLOWVERSION", type="Key", dataType="String", initial="", history="")
	public String PROCESSFLOWVERSION;
	@CTORMTemplate(seq = "6", name="PROCESSOPERATIONNAME", type="Key", dataType="String", initial="", history="")
	public String PROCESSOPERATIONNAME;
	@CTORMTemplate(seq = "7", name="PROCESSOPERATIONVERSION", type="Key", dataType="String", initial="", history="")
	public String PROCESSOPERATIONVERSION;
	@CTORMTemplate(seq = "8", name="CONDITIONID", type="Column", dataType="String", initial="", history="")
	public String CONDITIONID;
	@CTORMTemplate(seq = "9", name="MACHINEGROUPNAME", type="Column", dataType="String", initial="", history="")
	public String MACHINEGROUPNAME;
	@CTORMTemplate(seq = "10", name="MACHINERECIPENAME", type="Column", dataType="String", initial="", history="")
	public String MACHINERECIPENAME;
	@CTORMTemplate(seq = "11", name="DESCRIPTION", type="Column", dataType="String", initial="", history="")
	public String DESCRIPTION;
	@CTORMTemplate(seq = "12", name="DCSPECNAME", type="Column", dataType="String", initial="", history="")
	public String DCSPECNAME;
	@CTORMTemplate(seq = "13", name="DCSPECVERSION", type="Column", dataType="String", initial="", history="")
	public String DCSPECVERSION;
	
	public String getFACTORYNAME() {
		return FACTORYNAME;
	}
	public void setFACTORYNAME(String factoryname) {
		FACTORYNAME = factoryname;
	}
	public String getPRODUCTSPECNAME() {
		return PRODUCTSPECNAME;
	}
	public void setPRODUCTSPECNAME(String productspecname) {
		PRODUCTSPECNAME = productspecname;
	}
	public String getPRODUCTSPECVERSION() {
		return PRODUCTSPECVERSION;
	}
	public void setPRODUCTSPECVERSION(String productspecversion) {
		PRODUCTSPECVERSION = productspecversion;
	}
	public String getPROCESSFLOWNAME() {
		return PROCESSFLOWNAME;
	}
	public void setPROCESSFLOWNAME(String processflowname) {
		PROCESSFLOWNAME = processflowname;
	}
	public String getPROCESSFLOWVERSION() {
		return PROCESSFLOWVERSION;
	}
	public void setPROCESSFLOWVERSION(String processflowversion) {
		PROCESSFLOWVERSION = processflowversion;
	}
	public String getPROCESSOPERATIONNAME() {
		return PROCESSOPERATIONNAME;
	}
	public void setPROCESSOPERATIONNAME(String processoperationname) {
		PROCESSOPERATIONNAME = processoperationname;
	}
	public String getPROCESSOPERATIONVERSION() {
		return PROCESSOPERATIONVERSION;
	}
	public void setPROCESSOPERATIONVERSION(String processoperationversion) {
		PROCESSOPERATIONVERSION = processoperationversion;
	}
	public String getCONDITIONID() {
		return CONDITIONID;
	}
	public void setCONDITIONID(String conditionid) {
		CONDITIONID = conditionid;
	}
	public String getMACHINEGROUPNAME() {
		return MACHINEGROUPNAME;
	}
	public void setMACHINEGROUPNAME(String machinegroupname) {
		MACHINEGROUPNAME = machinegroupname;
	}
	public String getMACHINERECIPENAME() {
		return MACHINERECIPENAME;
	}
	public void setMACHINERECIPENAME(String machinerecipename) {
		MACHINERECIPENAME = machinerecipename;
	}
	public String getDESCRIPTION() {
		return DESCRIPTION;
	}
	public void setDESCRIPTION(String description) {
		DESCRIPTION = description;
	}
	public String getDCSPECNAME() {
		return DCSPECNAME;
	}
	public void setDCSPECNAME(String dcspecname) {
		DCSPECNAME = dcspecname;
	}
	public String getDCSPECVERSION() {
		return DCSPECVERSION;
	}
	public void setDCSPECVERSION(String dcspecversion) {
		DCSPECVERSION = dcspecversion;
	}
	
	
}
