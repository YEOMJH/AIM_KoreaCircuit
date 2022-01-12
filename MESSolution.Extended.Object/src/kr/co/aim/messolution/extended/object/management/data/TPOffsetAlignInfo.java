package kr.co.aim.messolution.extended.object.management.data;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class TPOffsetAlignInfo extends UdfAccessor {
	@CTORMTemplate(seq = "1", name="offset", type="Key", dataType="Key", initial="", history="")
	private String offset;
	@CTORMTemplate(seq = "2", name="processOperationName", type="Key", dataType="String", initial="", history="")
	private String processOperationName;
	@CTORMTemplate(seq = "3", name="processOperationVersion", type="Key", dataType="String", initial="", history="")
	private String processOperationVersion;
	@CTORMTemplate(seq = "4", name="machineName", type="Key", dataType="Key", initial="", history="")
	private String machineName;
	@CTORMTemplate(seq = "5", name="recipeName", type="Column", dataType="String", initial="", history="")
	private String recipeName;
	@CTORMTemplate(seq = "6", name="RMSFlag", type="Column", dataType="String", initial="", history="")
	private String RMSFlag;
	
	public TPOffsetAlignInfo() {
		super();
	}

	public String getOffset() {
		return offset;
	}

	public void setOffset(String offset) {
		this.offset = offset;
	}

	public String getProcessOperationName() {
		return processOperationName;
	}

	public void setProcessOperationName(String processOperationName) {
		this.processOperationName = processOperationName;
	}

	public String getProcessOperationVersion() {
		return processOperationVersion;
	}

	public void setProcessOperationVersion(String processOperationVersion) {
		this.processOperationVersion = processOperationVersion;
	}

	public String getMachineName() {
		return machineName;
	}

	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}

	public String getRecipeName() {
		return recipeName;
	}

	public void setRecipeName(String recipeName) {
		this.recipeName = recipeName;
	}

	public String getRMSFlag() {
		return RMSFlag;
	}

	public void setRMSFlag(String rMSFlag) {
		RMSFlag = rMSFlag;
	}
}
