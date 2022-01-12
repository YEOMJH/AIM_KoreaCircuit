package kr.co.aim.messolution.extended.object.management.data;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class DefectCode extends UdfAccessor {
	@CTORMTemplate(seq = "1", name = "defectCode", type = "Key", dataType = "String", initial = "", history = "")
	private String defectCode;
	@CTORMTemplate(seq = "2", name = "superDefectCode", type = "Key", dataType = "String", initial = "", history = "")
	private String superDefectCode;
	@CTORMTemplate(seq = "3", name = "description", type = "Column", dataType = "String", initial = "", history = "")
	private String description;
	@CTORMTemplate(seq = "4", name = "levelNo", type = "Column", dataType = "Number", initial = "", history = "")
	private Number levelNo;

	public String getDefectCode()
	{
		return defectCode;
	}

	public void setDefectCode(String defectCode)
	{
		this.defectCode = defectCode;
	}

	public String getSuperDefectCode()
	{
		return superDefectCode;
	}

	public void setSuperDefectCode(String superDefectCode)
	{
		this.superDefectCode = superDefectCode;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public Number getLevelNo()
	{
		return levelNo;
	}

	public void setLevelNo(Number levelNo)
	{
		this.levelNo = levelNo;
	}
}
