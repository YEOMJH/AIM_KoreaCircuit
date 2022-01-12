package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class STKConfig extends UdfAccessor 
{
	@CTORMTemplate(seq = "1", name="STKNAME", type="Key", dataType="String", initial="", history="")
	public String STKNAME;	
	
	@CTORMTemplate(seq = "2", name="STKMAXQTY", type="Column", dataType="Number", initial="", history="")
	public long STKMAXQTY;
	
	@CTORMTemplate(seq = "3", name="SCRAPPEDQTY", type="Column", dataType="Number", initial="", history="")
	public long SCRAPPEDQTY;
	
	@CTORMTemplate(seq = "4", name="SCRAPPEDTOSTK", type="Column", dataType="String", initial="", history="")
	public String SCRAPPEDTOSTK;
	
	@CTORMTemplate(seq = "5", name="CLEANAVAILABLEQTY", type="Column", dataType="Number", initial="", history="")
	public long CLEANAVAILABLEQTY;
	
	@CTORMTemplate(seq = "6", name="CLEANAVAILABLETOSTK", type="Column", dataType="String", initial="", history="")
	public String CLEANAVAILABLETOSTK;
	
	@CTORMTemplate(seq = "7", name="INUSEQTY", type="Column", dataType="Number", initial="", history="")
	public long INUSEQTY;
	
	@CTORMTemplate(seq = "8", name="INUSETOSTK", type="Column", dataType="String", initial="", history="")
	public String INUSETOSTK;
	
	@CTORMTemplate(seq = "9", name="EVENTCOMMENT", type="Column", dataType="String", initial="", history="")
	public String EVENTCOMMENT;
	
	@CTORMTemplate(seq = "10", name="DIRTYAVAILABLEQTY", type="Column", dataType="Number", initial="", history="")
	public long DIRTYAVAILABLEQTY;
	
	@CTORMTemplate(seq = "11", name="DIRTYAVAILABLETOSTK", type="Column", dataType="String", initial="", history="")
	public String DIRTYAVAILABLETOSTK;

	@CTORMTemplate(seq = "12", name="FACTORYNAME", type="Column", dataType="String", initial="", history="")
	public String FACTORYNAME;
	
	@CTORMTemplate(seq = "13", name="EMPTYLOWERLIMIT", type="Column", dataType="Number", initial="", history="")
	public long EMPTYLOWERLIMIT;
	
	@CTORMTemplate(seq = "14", name="EMPTYUPPERLIMIT", type="Column", dataType="Number", initial="", history="")
	public long EMPTYUPPERLIMIT;

	public String getSTKNAME() {
		return STKNAME;
	}

	public void setSTKNAME(String stkname) {
		STKNAME = stkname;
	}

	public long getSTKMAXQTY() {
		return STKMAXQTY;
	}

	public void setSTKMAXQTY(long stkmaxqty) {
		STKMAXQTY = stkmaxqty;
	}

	public long getSCRAPPEDQTY() {
		return SCRAPPEDQTY;
	}

	public void setSCRAPPEDQTY(long scrappedqty) {
		SCRAPPEDQTY = scrappedqty;
	}

	public String getSCRAPPEDTOSTK() {
		return SCRAPPEDTOSTK;
	}

	public void setSCRAPPEDTOSTK(String scrappedtostk) {
		SCRAPPEDTOSTK = scrappedtostk;
	}

	public long getDIRTYAVAILABLEQTY() {
		return DIRTYAVAILABLEQTY;
	}

	public void setDIRTYAVAILABLEQTY(long dirtyavailableqty) {
		DIRTYAVAILABLEQTY = dirtyavailableqty;
	}

	public String getDIRTYAVAILABLETOSTK() {
		return DIRTYAVAILABLETOSTK;
	}

	public void setDIRTYAVAILABLETOSTK(String dirtyavailabletostk) {
		DIRTYAVAILABLETOSTK = dirtyavailabletostk;
	}

	public long getCLEANAVAILABLEQTY() {
		return CLEANAVAILABLEQTY;
	}

	public void setCLEANAVAILABLEQTY(long cleanavailableqty) {
		CLEANAVAILABLEQTY = cleanavailableqty;
	}

	public String getCLEANAVAILABLETOSTK() {
		return CLEANAVAILABLETOSTK;
	}

	public void setCLEANAVAILABLETOSTK(String cleanavailabletostk) {
		CLEANAVAILABLETOSTK = cleanavailabletostk;
	}
	
	public long getINUSEQTY() {
		return INUSEQTY;
	}

	public void setINUSEQTY(long inuseqty) {
		INUSEQTY = inuseqty;
	}

	public String getINUSETOSTK() {
		return INUSETOSTK;
	}

	public void setINUSETOSTK(String inusetostk) {
		INUSETOSTK = inusetostk;
	}

	public String getEVENTCOMMENT() {
		return EVENTCOMMENT;
	}

	public void setEVENTCOMMENT(String eventcomment) {
		EVENTCOMMENT = eventcomment;
	}
	
	public String getFACTORYNAME() {
		return FACTORYNAME;
	}

	public void setFACTORYNAME(String fACTORYNAME) {
		FACTORYNAME = fACTORYNAME;
	}

	public long getEMPTYLOWERLIMIT() {
		return EMPTYLOWERLIMIT;
	}

	public void setEMPTYLOWERLIMIT(long eMPTYLOWERLIMIT) {
		EMPTYLOWERLIMIT = eMPTYLOWERLIMIT;
	}

	public long getEMPTYUPPERLIMIT() {
		return EMPTYUPPERLIMIT;
	}

	public void setEMPTYUPPERLIMIT(long eMPTYUPPERLIMIT) {
		EMPTYUPPERLIMIT = eMPTYUPPERLIMIT;
	}
}
