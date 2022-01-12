package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class ReviewOperationInfo extends UdfAccessor {
	@CTORMTemplate(seq = "1", name = "processOperationName", type = "Key", dataType = "String", initial = "", history = "")
	private String processOperationName;
	@CTORMTemplate(seq = "2", name = "processOperationVersion", type = "Key", dataType = "String", initial = "", history = "")
	private String processOperationVersion;
	@CTORMTemplate(seq = "3", name = "offlineFlag", type = "Column", dataType = "String", initial = "", history = "")
	private String offlineFlag;
	@CTORMTemplate(seq = "4", name = "QTime", type = "Column", dataType = "Number", initial = "", history = "")
	private double QTime;
	@CTORMTemplate(seq = "5", name = "QTimeFlag", type = "Column", dataType = "String", initial = "", history = "")
	private String QTimeFlag;
	
	public ReviewOperationInfo() {
		super();
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

	public String getOfflineFlag() {
		return offlineFlag;
	}

	public void setOfflineFlag(String offlineFlag) {
		this.offlineFlag = offlineFlag;
	}

	public double getQTime() {
		return QTime;
	}
	public void setQTime(double qTime) {
		QTime = qTime;
	}
	public String getQTimeFlag() {
		return QTimeFlag;
	}
	public void setQTimeFlag(String qTimeFlag) {
		QTimeFlag = qTimeFlag;
	}
}
