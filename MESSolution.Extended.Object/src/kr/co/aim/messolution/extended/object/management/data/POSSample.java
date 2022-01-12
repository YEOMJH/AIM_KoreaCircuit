package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class POSSample extends UdfAccessor
{
	@CTORMTemplate(seq = "1", name="CONDITIONID", type="Key", dataType="String", initial="", history="")
	public String CONDITIONID;

	@CTORMTemplate(seq = "2", name="TOPROCESSFLOWNAME", type="Key", dataType="String", initial="", history="")
	public String TOPROCESSFLOWNAME;
	
	@CTORMTemplate(seq = "3", name="TOPROCESSFLOWVERSION", type="Key", dataType="String", initial="", history="")
	public String TOPROCESSFLOWVERSION;
	
	@CTORMTemplate(seq = "4", name="TOPROCESSOPERATIONNAME", type="Key", dataType="String", initial="", history="")
	public String TOPROCESSOPERATIONNAME;
	
	@CTORMTemplate(seq = "5", name="TOPROCESSOPERATIONVERSION", type="Key", dataType="String", initial="", history="")
	public String TOPROCESSOPERATIONVERSION;
	
	@CTORMTemplate(seq = "6", name="FLOWPRIORITY", type="Key", dataType="Number", initial="", history="")
	public Double FLOWPRIORITY;
	
	@CTORMTemplate(seq = "7", name="LOTSAMPLINGCOUNT", type="Column", dataType="String", initial="", history="")
	public String LOTSAMPLINGCOUNT;
	
	@CTORMTemplate(seq = "8", name="PRODUCTSAMPLINGCOUNT", type="Column", dataType="String", initial="", history="")
	public String PRODUCTSAMPLINGCOUNT;
	
	@CTORMTemplate(seq = "9", name="PRODUCTSAMPLINGPOSITION", type="Column", dataType="String", initial="", history="")
	public String PRODUCTSAMPLINGPOSITION;
	
	@CTORMTemplate(seq = "10", name="RETURNOPERATIONNAME", type="Column", dataType="String", initial="", history="")
	public String RETURNOPERATIONNAME;
	
	@CTORMTemplate(seq = "11", name="RETURNOPERATIONVER", type="Column", dataType="String", initial="", history="")
	public String RETURNOPERATIONVER;
	
	
	public String getCONDITIONID() {
		return CONDITIONID;
	}

	public void setCONDITIONID(String cONDITIONID) {
		CONDITIONID = cONDITIONID;
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

	public Double getFLOWPRIORITY() {
		return FLOWPRIORITY;
	}

	public void setFLOWPRIORITY(Double fLOWPRIORITY) {
		FLOWPRIORITY = fLOWPRIORITY;
	}

	public String getLOTSAMPLINGCOUNT() {
		return LOTSAMPLINGCOUNT;
	}

	public void setLOTSAMPLINGCOUNT(String lOTSAMPLINGCOUNT) {
		LOTSAMPLINGCOUNT = lOTSAMPLINGCOUNT;
	}

	public String getPRODUCTSAMPLINGCOUNT() {
		return PRODUCTSAMPLINGCOUNT;
	}

	public void setPRODUCTSAMPLINGCOUNT(String pRODUCTSAMPLINGCOUNT) {
		PRODUCTSAMPLINGCOUNT = pRODUCTSAMPLINGCOUNT;
	}

	public String getPRODUCTSAMPLINGPOSITION() {
		return PRODUCTSAMPLINGPOSITION;
	}

	public void setPRODUCTSAMPLINGPOSITION(String pRODUCTSAMPLINGPOSITION) {
		PRODUCTSAMPLINGPOSITION = pRODUCTSAMPLINGPOSITION;
	}

	public String getRETURNOPERATIONNAME() {
		return RETURNOPERATIONNAME;
	}

	public void setRETURNOPERATIONNAME(String rETURNOPERATIONNAME) {
		RETURNOPERATIONNAME = rETURNOPERATIONNAME;
	}

	public String getRETURNOPERATIONVER() {
		return RETURNOPERATIONVER;
	}

	public void setRETURNOPERATIONVER(String rETURNOPERATIONVER) {
		RETURNOPERATIONVER = rETURNOPERATIONVER;
	}
	
}
