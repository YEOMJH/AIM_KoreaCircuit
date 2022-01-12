package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class POSQueueTime 
{
	@CTORMTemplate(seq = "1", name="CONDITIONID", type="Key", dataType="String", initial="", history="")
	public String CONDITIONID;
	
	@CTORMTemplate(seq = "2", name="TOFACTORYNAME", type="Key", dataType="String", initial="", history="")
	public String TOFACTORYNAME;
	
	@CTORMTemplate(seq = "3", name="TOPROCESSFLOWNAME", type="Key", dataType="String", initial="", history="")
	public String TOPROCESSFLOWNAME;
	
	@CTORMTemplate(seq = "4", name="TOPROCESSOPERATIONNAME", type="Key", dataType="String", initial="", history="")
	public String TOPROCESSOPERATIONNAME;
	
	@CTORMTemplate(seq = "5", name="WARNINGDURATIONLIMIT", type="Column", dataType="String", initial="", history="")
	public String WARNINGDURATIONLIMIT;
	
	@CTORMTemplate(seq = "6", name="INTERLOCKDURATIONLIMIT", type="Column", dataType="String", initial="", history="")
	public String INTERLOCKDURATIONLIMIT;
	
	@CTORMTemplate(seq = "7", name="REVIEWSTATIONQTIMELIMIT", type="Column", dataType="Number", initial="", history="")
	public Double REVIEWSTATIONQTIMELIMIT;
	
	@CTORMTemplate(seq = "8", name="MANAGEMENTDESC", type="Column", dataType="String", initial="", history="")
	public String MANAGEMENTDESC;
	
	public Double getREVIEWSTATIONQTIMELIMIT() {
		return REVIEWSTATIONQTIMELIMIT;
	}

	public void setREVIEWSTATIONQTIMELIMIT(Double rEVIEWSTATIONQTIMELIMIT) {
		REVIEWSTATIONQTIMELIMIT = rEVIEWSTATIONQTIMELIMIT;
	}

	public String getCONDITIONID() {
		return CONDITIONID;
	}
	
	public void setCONDITIONID(String conditionid) {
		CONDITIONID = conditionid;
	}
	
	public String getTOFACTORYNAME() {
		return TOFACTORYNAME;
	}
	
	public void setTOFACTORYNAME(String tOFACTORYNAME) {
		TOFACTORYNAME = tOFACTORYNAME;
	}
	
	public String getTOPROCESSFLOWNAME() {
		return TOPROCESSFLOWNAME;
	}
	
	public void setTOPROCESSFLOWNAME(String tOPROCESSFLOWNAME) {
		TOPROCESSFLOWNAME = tOPROCESSFLOWNAME;
	}
	
	public String getTOPROCESSOPERATIONNAME() {
		return TOPROCESSOPERATIONNAME;
	}
	
	public void setTOPROCESSOPERATIONNAME(String toprocessoperationname) {
		TOPROCESSOPERATIONNAME = toprocessoperationname;
	}
	
	public String getWARNINGDURATIONLIMIT() {
		return WARNINGDURATIONLIMIT;
	}
	
	public void setWARNINGDURATIONLIMIT(String warningdurationlimit) {
		WARNINGDURATIONLIMIT = warningdurationlimit;
	}
	
	public String getINTERLOCKDURATIONLIMIT() {
		return INTERLOCKDURATIONLIMIT;
	}
	
	public void setINTERLOCKDURATIONLIMIT(String interlockdurationlimit) {
		INTERLOCKDURATIONLIMIT = interlockdurationlimit;
	}

	public String getMANAGEMENTDESC() {
		return MANAGEMENTDESC;
	}

	public void setMANAGEMENTDESC(String mANAGEMENTDESC) {
		MANAGEMENTDESC = mANAGEMENTDESC;
	}
	

}
