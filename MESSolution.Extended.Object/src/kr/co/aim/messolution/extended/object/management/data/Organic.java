package kr.co.aim.messolution.extended.object.management.data;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class Organic extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="consumableName", type="Key", dataType="String", initial="", history="")
	private String consumableName;
	
	@CTORMTemplate(seq = "2", name="crucibleLotName", type="Key", dataType="String", initial="", history="")
	private String crucibleLotName;

	@CTORMTemplate(seq = "3", name="assignQty", type="Column", dataType="Double", initial="", history="")
	private Double assignQty;

	@CTORMTemplate(seq = "4", name="inputQty", type="Column", dataType="Double", initial="", history="")
	private Double inputQty;

	public String getConsumableName() {
		return consumableName;
	}

	public void setConsumableName(String consumableName) {
		this.consumableName = consumableName;
	}

	public String getCrucibleLotName() {
		return crucibleLotName;
	}

	public void setCrucibleLotName(String crucibleLotName) {
		this.crucibleLotName = crucibleLotName;
	}

	public Double getAssignQty() {
		return assignQty;
	}

	public void setAssignQty(Double assignQty) {
		this.assignQty = assignQty;
	}

	public Double getInputQty() {
		return inputQty;
	}

	public void setInputQty(Double inputQty) {
		this.inputQty = inputQty;
	}

}