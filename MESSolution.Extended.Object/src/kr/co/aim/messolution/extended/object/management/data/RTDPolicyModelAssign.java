package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class RTDPolicyModelAssign 
{
	@CTORMTemplate(seq = "1", name="RULEGROUP", type="Key", dataType="String", initial="", history="")
	public String RULEGROUP;
	@CTORMTemplate(seq = "2", name="CONDITIONNAME", type="Key", dataType="String", initial="", history="")
	public String CONDITIONNAME;
	@CTORMTemplate(seq = "3", name="RULEOBJECT", type="Key", dataType="String", initial="", history="")
	public String RULEOBJECT;
	@CTORMTemplate(seq = "4", name="RULENAME", type="Column", dataType="String", initial="", history="")
	public String RULENAME;
	@CTORMTemplate(seq = "5", name="ENABLE", type="Column", dataType="String", initial="", history="")
	public String ENABLE;
	@CTORMTemplate(seq = "6", name="DESCRIPTION", type="Column", dataType="String", initial="", history="")
	public String DESCRIPTION;
	
	public String getRULEGROUP() {
		return RULEGROUP;
	}
	public void setRULEGROUP(String rulegroup) {
		RULEGROUP = rulegroup;
	}
	public String getCONDITIONNAME() {
		return CONDITIONNAME;
	}
	public void setCONDITIONNAME(String conditionname) {
		CONDITIONNAME = conditionname;
	}
	public String getRULEOBJECT() {
		return RULEOBJECT;
	}
	public void setRULEOBJECT(String ruleobject) {
		RULEOBJECT = ruleobject;
	}
	public String getRULENAME() {
		return RULENAME;
	}
	public void setRULENAME(String rulename) {
		RULENAME = rulename;
	}
	public String getENABLE() {
		return ENABLE;
	}
	public void setENABLE(String enable) {
		ENABLE = enable;
	}
	public String getDESCRIPTION() {
		return DESCRIPTION;
	}
	public void setDESCRIPTION(String description) {
		DESCRIPTION = description;
	}
}
