package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;
import java.util.Date;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class ReviewTestImageJudge extends UdfAccessor{
	
	@CTORMTemplate(seq = "1", name = "ReviewTestName", type = "Key", dataType = "String", initial = "", history = "")
	private String ReviewTestName;
	@CTORMTemplate(seq = "2", name = "FactoryName", type = "Key", dataType = "String", initial = "", history = "")
	private String FactoryName;
	@CTORMTemplate(seq = "3", name = "MachineType", type = "Key", dataType = "String", initial = "", history = "")
	private String MachineType;
	@CTORMTemplate(seq = "4", name = "ReviewDefectCode", type = "Key", dataType = "String", initial = "", history = "")
	private String ReviewDefectCode;
	@CTORMTemplate(seq = "5", name = "ImageName", type = "Key", dataType = "String", initial = "", history = "")
	private String ImageName;
	@CTORMTemplate(seq = "6", name = "JudgeUser", type = "Key", dataType = "String", initial = "", history = "")
	private String JudgeUser;
	@CTORMTemplate(seq = "7", name = "JudgeCode", type = "Column", dataType = "String", initial = "", history = "")
	private String JudgeCode;
	@CTORMTemplate(seq = "8", name = "Currect", type = "Column", dataType = "String", initial = "", history = "")
	private String Currect;
	@CTORMTemplate(seq = "9", name = "StartTime", type = "Column", dataType = "Timestamp", initial = "", history = "")
	private Timestamp StartTime;
	@CTORMTemplate(seq = "10", name = "EndTime", type = "Column", dataType = "Timestamp", initial = "", history = "")
	private Timestamp EndTime;
	@CTORMTemplate(seq = "11", name = "LastEventTimeKey", type = "Column", dataType = "String", initial = "", history = "")
	private String LastEventTimeKey;
	@CTORMTemplate(seq = "12", name = "LastEventComment", type = "Column", dataType = "String", initial = "", history = "")
	private String LastEventComment;
	@CTORMTemplate(seq = "13", name = "LastEventName", type = "Column", dataType = "String", initial = "", history = "")
	private String LastEventName;
	@CTORMTemplate(seq = "14", name = "Seq", type = "Column", dataType = "String", initial = "", history = "")
	private String Seq;
	@CTORMTemplate(seq = "15", name = "DefectGrade", type = "Column", dataType = "String", initial = "", history = "")
	private String DefectGrade;
	public String getReviewTestName() {
		return ReviewTestName;
	}
	public void setReviewTestName(String reviewTestName) {
		ReviewTestName = reviewTestName;
	}
	public String getFactoryName() {
		return FactoryName;
	}
	public void setFactoryName(String factoryName) {
		FactoryName = factoryName;
	}
	public String getMachineType() {
		return MachineType;
	}
	public void setMachineType(String machineType) {
		MachineType = machineType;
	}
	public String getReviewDefectCode() {
		return ReviewDefectCode;
	}
	public void setReviewDefectCode(String reviewDefectCode) {
		ReviewDefectCode = reviewDefectCode;
	}
	public String getImageName() {
		return ImageName;
	}
	public void setImageName(String imageName) {
		ImageName = imageName;
	}
	public String getJudgeCode() {
		return JudgeCode;
	}
	public void setJudgeCode(String judgeCode) {
		JudgeCode = judgeCode;
	}
	public String getJudgeUser() {
		return JudgeUser;
	}
	public void setJudgeUser(String judgeUser) {
		JudgeUser = judgeUser;
	}
	public String getCurrect() {
		return Currect;
	}
	public void setCurrect(String currect) {
		Currect = currect;
	}
	public Timestamp getStartTime() {
		return StartTime;
	}
	public void setStartTime(Timestamp startTime) {
		StartTime = startTime;
	}
	public Timestamp getEndTime() {
		return EndTime;
	}
	public void setEndTime(Timestamp endTime) {
		EndTime = endTime;
	}
	public String getLastEventTimeKey() {
		return LastEventTimeKey;
	}
	public void setLastEventTimeKey(String lastEventTimeKey) {
		LastEventTimeKey = lastEventTimeKey;
	}
	public String getLastEventComment() {
		return LastEventComment;
	}
	public void setLastEventComment(String lastEventComment) {
		LastEventComment = lastEventComment;
	}
	public String getLastEventName() {
		return LastEventName;
	}
	public void setLastEventName(String lastEventName) {
		LastEventName = lastEventName;
	}
	public String getSeq() {
		return Seq;
	}
	public void setSeq(String seq) {
		Seq = seq;
	}
	public String getDefectGrade() {
		return DefectGrade;
	}
	public void setDefectGrade(String defectGrade) {
		DefectGrade = defectGrade;
	}
	

}
