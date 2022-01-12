package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class AlterOperationByJudge extends UdfAccessor {
	@CTORMTemplate(seq = "1", name = "processFlowName", type = "Key", dataType = "String", initial = "", history = "")
	private String processFlowName;
	@CTORMTemplate(seq = "2", name = "processFlowVersion", type = "Key", dataType = "String", initial = "", history = "")
	private String processFlowVersion;
	@CTORMTemplate(seq = "3", name = "processOperationName", type = "Key", dataType = "String", initial = "", history = "")
	private String processOperationName;;
	@CTORMTemplate(seq = "4", name = "processOperationVersion", type = "Key", dataType = "String", initial = "", history = "")
	private String processOperationVersion;
	@CTORMTemplate(seq = "5", name = "judge", type = "Key", dataType = "String", initial = "", history = "")
	private String judge;
	@CTORMTemplate(seq = "6", name = "toProcessFlowName", type = "Column", dataType = "String", initial = "", history = "")
	private String toProcessFlowName;
	@CTORMTemplate(seq = "7", name = "toProcessFlowVersion", type = "Column", dataType = "String", initial = "", history = "")
	private String toProcessFlowVersion;
	@CTORMTemplate(seq = "8", name = "toProcessOperationName", type = "Column", dataType = "String", initial = "", history = "")
	private String toProcessOperationName;
	@CTORMTemplate(seq = "9", name = "toProcessOperationVersion", type = "Column", dataType = "String", initial = "", history = "")
	private String toProcessOperationVersion;
	public String getProcessFlowName() {
		return processFlowName;
	}
	public void setProcessFlowName(String processFlowName) {
		this.processFlowName = processFlowName;
	}
	public String getProcessFlowVersion() {
		return processFlowVersion;
	}
	public void setProcessFlowVersion(String processFlowVersion) {
		this.processFlowVersion = processFlowVersion;
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
	public String getJudge() {
		return judge;
	}
	public void setJudge(String judge) {
		this.judge = judge;
	}
	public String getToProcessFlowName() {
		return toProcessFlowName;
	}
	public void setToProcessFlowName(String toProcessFlowName) {
		this.toProcessFlowName = toProcessFlowName;
	}
	public String getToProcessFlowVersion() {
		return toProcessFlowVersion;
	}
	public void setToProcessFlowVersion(String toProcessFlowVersion) {
		this.toProcessFlowVersion = toProcessFlowVersion;
	}
	public String getToProcessOperationName() {
		return toProcessOperationName;
	}
	public void setToProcessOperationName(String toProcessOperationName) {
		this.toProcessOperationName = toProcessOperationName;
	}
	public String getToProcessOperationVersion() {
		return toProcessOperationVersion;
	}
	public void setToProcessOperationVersion(String toProcessOperationVersion) {
		this.toProcessOperationVersion = toProcessOperationVersion;
	}
	public AlterOperationByJudge() {
		super();
	}
}
