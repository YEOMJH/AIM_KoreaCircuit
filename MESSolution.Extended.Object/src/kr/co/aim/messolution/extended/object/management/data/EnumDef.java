package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class EnumDef extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="enumName", type="Key", dataType="String", initial="", history="")
	private String enumName;
	
	@CTORMTemplate(seq = "2", name="description", type="Column", dataType="String", initial="", history="")
	private String description;

	@CTORMTemplate(seq = "3", name="accessType", type="Column", dataType="String", initial="", history="")
	private String accessType;

	@CTORMTemplate(seq = "4", name="usage", type="Column", dataType="String", initial="", history="")
	private String usage;
	
	@CTORMTemplate(seq = "5", name="constantFlag", type="Column", dataType="String", initial="", history="")
	private String constantFlag;

	public String getEnumName() {
		return enumName;
	}

	public void setEnumName(String enumName) {
		this.enumName = enumName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAccessType() {
		return accessType;
	}

	public void setAccessType(String accessType) {
		this.accessType = accessType;
	}

	public String getUsage() {
		return usage;
	}

	public void setUsage(String usage) {
		this.usage = usage;
	}

	public String getConstantFlag() {
		return constantFlag;
	}

	public void setConstantFlag(String constantFlag) {
		this.constantFlag = constantFlag;
	}


	
	
}