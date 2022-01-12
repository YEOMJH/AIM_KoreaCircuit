package kr.co.aim.messolution.extended.object.management.data;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class CheckOffset extends UdfAccessor {
	@CTORMTemplate(seq = "1", name="maskLotName", type="Key", dataType="String", initial="", history="")
	private String maskLotName;
	@CTORMTemplate(seq = "2", name="maskSpecName", type="Key", dataType="String", initial="", history="")
	private String maskSpecName;
	@CTORMTemplate(seq = "3", name="maskProcessFlowName", type="Key", dataType="String", initial="", history="")
	private String maskProcessFlowName;
	@CTORMTemplate(seq = "4", name="maskProcessOperationName", type="Key", dataType="String", initial="", history="")
	private String maskProcessOperationName;
	@CTORMTemplate(seq = "5", name="checkFlag", type="Column", dataType="String", initial="", history="")
	private String checkFlag;
	@CTORMTemplate(seq = "6", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;
	public String getMaskLotName() {
		return maskLotName;
	}
	public void setMaskLotName(String maskLotName) {
		this.maskLotName = maskLotName;
	}
	public String getMaskSpecName() {
		return maskSpecName;
	}
	public void setMaskSpecName(String maskSpecName) {
		this.maskSpecName = maskSpecName;
	}
	public String getMaskProcessFlowName() {
		return maskProcessFlowName;
	}
	public void setMaskProcessFlowName(String maskProcessFlowName) {
		this.maskProcessFlowName = maskProcessFlowName;
	}
	public String getMaskProcessOperationName() {
		return maskProcessOperationName;
	}
	public void setMaskProcessOperationName(String maskProcessOperationName) {
		this.maskProcessOperationName = maskProcessOperationName;
	}
	public String getCheckFlag() {
		return checkFlag;
	}
	public void setCheckFlag(String checkFlag) {
		this.checkFlag = checkFlag;
	}
	public String getLastEventUser() {
		return lastEventUser;
	}
	public void setLastEventUser(String lastEventUser) {
		this.lastEventUser = lastEventUser;
	}
}
