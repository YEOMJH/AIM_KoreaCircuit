package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class EnumDefValue extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="enumName", type="Key", dataType="String", initial="", history="")
	private String enumName;
	
	@CTORMTemplate(seq = "2", name="enumValue", type="Key", dataType="String", initial="", history="")
	private String enumValue;
	
	@CTORMTemplate(seq = "3", name="description", type="Key", dataType="String", initial="", history="")
	private String description;

	@CTORMTemplate(seq = "4", name="defaultFlag", type="Column", dataType="String", initial="", history="")
	private String defaultFlag;
	
	@CTORMTemplate(seq = "5", name="displayColor", type="Column", dataType="String", initial="", history="")
	private String displayColor;
	
	@CTORMTemplate(seq = "6", name="seq", type="Column", dataType="String", initial="", history="")
	private String seq;

	public String getEnumName() {
		return enumName;
	}

	public void setEnumName(String enumName) {
		this.enumName = enumName;
	}

	public String getEnumValue() {
		return enumValue;
	}

	public void setEnumValue(String enumValue) {
		this.enumValue = enumValue;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDefaultFlag() {
		return defaultFlag;
	}

	public void setDefaultFlag(String defaultFlag) {
		this.defaultFlag = defaultFlag;
	}

	public String getDisplayColor() {
		return displayColor;
	}

	public void setDisplayColor(String displayColor) {
		this.displayColor = displayColor;
	}

	public String getSeq() {
		return seq;
	}

	public void setSeq(String seq) {
		this.seq = seq;
	}
	



	
	
}