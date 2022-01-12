package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class POSAlterProcessOperation extends UdfAccessor
{
	@CTORMTemplate(seq = "1", name="CONDITIONID", type="Key", dataType="String", initial="", history="")
	public String CONDITIONID;
	
	@CTORMTemplate(seq = "2", name="PRODUCTIONTYPE", type="Key", dataType="String", initial="", history="")
	public String PRODUCTIONTYPE;
	
	@CTORMTemplate(seq = "3", name="CONDITIONNAME", type="Key", dataType="String", initial="", history="")
	public String CONDITIONNAME;
	
	@CTORMTemplate(seq = "4", name="CONDITIONVALUE", type="Key", dataType="String", initial="", history="")
	public String CONDITIONVALUE;
	
	@CTORMTemplate(seq = "5", name="TOPROCESSFLOWNAME", type="Key", dataType="String", initial="", history="")
	public String TOPROCESSFLOWNAME;
	
	@CTORMTemplate(seq = "6", name="TOPROCESSFLOWVERSION", type="Column", dataType="String", initial="", history="")
	public String TOPROCESSFLOWVERSION;
	
	@CTORMTemplate(seq = "7", name="TOPROCESSOPERATIONNAME", type="Column", dataType="String", initial="", history="")
	public String TOPROCESSOPERATIONNAME;
	
	@CTORMTemplate(seq = "8", name="TOPROCESSOPERATIONVERSION", type="Column", dataType="String", initial="", history="")
	public String TOPROCESSOPERATIONVERSION;
	
	@CTORMTemplate(seq = "9", name="RETURNPROCESSFLOWNAME", type="Column", dataType="String", initial="", history="")
	public String RETURNPROCESSFLOWNAME;
	
	@CTORMTemplate(seq = "10", name="RETURNOPERATIONNAME", type="Column", dataType="String", initial="", history="")
	public String RETURNOPERATIONNAME;
	
	@CTORMTemplate(seq = "11", name="RETURNOPERATIONVERSION", type="Column", dataType="String", initial="", history="")
	public String RETURNOPERATIONVERSION;
	
	@CTORMTemplate(seq = "12", name="REWORKFLAG", type="Column", dataType="String", initial="", history="")
	public String REWORKFLAG;
	
	@CTORMTemplate(seq = "13", name="REWORKCOUNTLIMIT", type="Column", dataType="String", initial="", history="")
	public String REWORKCOUNTLIMIT;
	
	@CTORMTemplate(seq = "14", name="RETURNPROCESSFLOWVERSION", type="Column", dataType="String", initial="", history="")
	public String RETURNPROCESSFLOWVERSION;
	
	@CTORMTemplate(seq = "15", name="REWORKTYPE", type="Column", dataType="String", initial="", history="")
	public String REWORKTYPE;
	
	@CTORMTemplate(seq = "16", name="REWORKCOUNTLIMITBYTYPE", type="Column", dataType="String", initial="", history="")
	public String REWORKCOUNTLIMITBYTYPE;
	
	
	public String getCONDITIONID() {
		return CONDITIONID;
	}

	public void setCONDITIONID(String cONDITIONID) {
		CONDITIONID = cONDITIONID;
	}

	public String getPRODUCTIONTYPE() {
		return PRODUCTIONTYPE;
	}

	public void setPRODUCTIONTYPE(String pRODUCTIONTYPE) {
		PRODUCTIONTYPE = pRODUCTIONTYPE;
	}

	public String getCONDITIONNAME() {
		return CONDITIONNAME;
	}

	public void setCONDITIONNAME(String cONDITIONNAME) {
		CONDITIONNAME = cONDITIONNAME;
	}

	public String getCONDITIONVALUE() {
		return CONDITIONVALUE;
	}

	public void setCONDITIONVALUE(String cONDITIONVALUE) {
		CONDITIONVALUE = cONDITIONVALUE;
	}

	public String getTOPROCESSFLOWNAME() {
		return TOPROCESSFLOWNAME;
	}

	public void setTOPROCESSFLOWNAME(String tOPROCESSFLOWNAME) {
		TOPROCESSFLOWNAME = tOPROCESSFLOWNAME;
	}

	public String getTOPROCESSFLOWVERSION() {
		return TOPROCESSFLOWVERSION;
	}

	public void setTOPROCESSFLOWVERSION(String tOPROCESSFLOWVERSION) {
		TOPROCESSFLOWVERSION = tOPROCESSFLOWVERSION;
	}

	public String getTOPROCESSOPERATIONNAME() {
		return TOPROCESSOPERATIONNAME;
	}

	public void setTOPROCESSOPERATIONNAME(String tOPROCESSOPERATIONNAME) {
		TOPROCESSOPERATIONNAME = tOPROCESSOPERATIONNAME;
	}

	public String getTOPROCESSOPERATIONVERSION() {
		return TOPROCESSOPERATIONVERSION;
	}

	public void setTOPROCESSOPERATIONVERSION(String tOPROCESSOPERATIONVERSION) {
		TOPROCESSOPERATIONVERSION = tOPROCESSOPERATIONVERSION;
	}

	public String getRETURNPROCESSFLOWNAME() {
		return RETURNPROCESSFLOWNAME;
	}

	public void setRETURNPROCESSFLOWNAME(String rETURNPROCESSFLOWNAME) {
		RETURNPROCESSFLOWNAME = rETURNPROCESSFLOWNAME;
	}

	public String getRETURNOPERATIONNAME() {
		return RETURNOPERATIONNAME;
	}

	public void setRETURNOPERATIONNAME(String rETURNOPERATIONNAME) {
		RETURNOPERATIONNAME = rETURNOPERATIONNAME;
	}

	public String getRETURNOPERATIONVERSION() {
		return RETURNOPERATIONVERSION;
	}

	public void setRETURNOPERATIONVERSION(String rETURNOPERATIONVERSION) {
		RETURNOPERATIONVERSION = rETURNOPERATIONVERSION;
	}

	public String getREWORKFLAG() {
		return REWORKFLAG;
	}

	public void setREWORKFLAG(String rEWORKFLAG) {
		REWORKFLAG = rEWORKFLAG;
	}

	public String getREWORKCOUNTLIMIT() {
		return REWORKCOUNTLIMIT;
	}

	public void setREWORKCOUNTLIMIT(String rEWORKCOUNTLIMIT) {
		REWORKCOUNTLIMIT = rEWORKCOUNTLIMIT;
	}

	public String getRETURNPROCESSFLOWVERSION() {
		return RETURNPROCESSFLOWVERSION;
	}

	public void setRETURNPROCESSFLOWVERSION(String rETURNPROCESSFLOWVERSION) {
		RETURNPROCESSFLOWVERSION = rETURNPROCESSFLOWVERSION;
	}

	public String getREWORKTYPE() {
		return REWORKTYPE;
	}

	public void setREWORKTYPE(String rEWORKTYPE) {
		REWORKTYPE = rEWORKTYPE;
	}

	public String getREWORKCOUNTLIMITBYTYPE() {
		return REWORKCOUNTLIMITBYTYPE;
	}

	public void setREWORKCOUNTLIMITBYTYPE(String rEWORKCOUNTLIMITBYTYPE) {
		REWORKCOUNTLIMITBYTYPE = rEWORKCOUNTLIMITBYTYPE;
	}

	
	
}
