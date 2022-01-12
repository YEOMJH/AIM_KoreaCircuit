package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;


public class SurfaceDefectCode extends UdfAccessor{
	
	@CTORMTemplate(seq = "1", name="productSpecName", type="Key", dataType="String", initial="", history="")
	private String productSpecName;
	@CTORMTemplate(seq = "2", name="operationName", type="Key", dataType="String", initial="", history="")
	private String operationName;	
	@CTORMTemplate(seq = "3", name="judge", type="Key", dataType="String", initial="", history="")
	private String judge;
	@CTORMTemplate(seq = "4", name="defectCode", type="Key", dataType="String", initial="", history="")
	private String defectCode;
	@CTORMTemplate(seq = "5", name="standard", type="Column", dataType="String", initial="", history="")
	private String standard;
	@CTORMTemplate(seq = "6", name="description", type="Column", dataType="String", initial="", history="")
	private String description;
	
	public String getProductSpecName() {
		return productSpecName;
	}
	public void setProductSpecName(String productSpecName) {
		this.productSpecName = productSpecName;
	}
	public String getOperationName() {
		return operationName;
	}
	public void setOperationName(String operationName) {
		this.operationName = operationName;
	}
	public String getJudge() {
		return judge;
	}
	public void setJudge(String judge) {
		this.judge = judge;
	}
	public String getDefectCode() {
		return defectCode;
	}
	public void setDefectCode(String defectCode) {
		this.defectCode = defectCode;
	}
	public String getStandard() {
		return standard;
	}
	public void setStandard(String standard) {
		this.standard = standard;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

}
