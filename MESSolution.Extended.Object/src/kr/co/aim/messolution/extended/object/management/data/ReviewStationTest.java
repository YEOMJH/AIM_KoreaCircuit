package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;
import java.util.Date;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class ReviewStationTest extends UdfAccessor{
	@CTORMTemplate(seq = "1", name = "ReviewTestName", type = "Key", dataType = "String", initial = "", history = "")
	private String ReviewTestName;
	@CTORMTemplate(seq = "2", name = "FactoryName", type = "Key", dataType = "String", initial = "", history = "")
	private String FactoryName;
	@CTORMTemplate(seq = "3", name = "MachineType", type = "Key", dataType = "String", initial = "", history = "")
	private String MachineType;
	@CTORMTemplate(seq = "4", name = "ReviewDefectCode", type = "Key", dataType = "String", initial = "", history = "")
	private String ReviewDefectCode;
	@CTORMTemplate(seq = "5", name = "ImageCount", type = "Column", dataType = "Number", initial = "", history = "")
	private int ImageCount;
	@CTORMTemplate(seq = "6", name = "TestImageQty", type = "Column", dataType = "Number", initial = "", history = "")
	private int TestImageQty;
	@CTORMTemplate(seq = "7", name = "ReviewTestState", type = "Column", dataType = "String", initial = "", history = "")
	private String ReviewTestState;
	@CTORMTemplate(seq = "8", name = "TestUserCount", type = "Column", dataType = "Number", initial = "", history = "")
	private int TestUserCount;
	@CTORMTemplate(seq = "9", name = "CreateUser", type = "Column", dataType = "String", initial = "", history = "")
	private String CreateUser;
	@CTORMTemplate(seq = "10", name = "CreateTime", type = "Column", dataType = "Timestamp", initial = "", history = "")
	private Timestamp CreateTime;
	@CTORMTemplate(seq = "11", name = "LastEventTimeKey", type = "Column", dataType = "String", initial = "", history = "N")
	private String LastEventTimeKey;
	@CTORMTemplate(seq = "12", name = "LastEventUser", type = "Column", dataType = "String", initial = "", history = "N")
	private String LastEventUser;
	@CTORMTemplate(seq = "13", name = "LastEventComment", type = "Column", dataType = "String", initial = "", history = "N")
	private String LastEventComment;
	@CTORMTemplate(seq = "14", name = "LastEventName", type = "Column", dataType = "String", initial = "", history = "N")
	private String LastEventName;
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
	public int getImageCount() {
		return ImageCount;
	}
	public void setImageCount(int imageCount) {
		ImageCount = imageCount;
	}
	public int getTestImageQty() {
		return TestImageQty;
	}
	public void setTestImageQty(int testImageQty) {
		TestImageQty = testImageQty;
	}
	public String getReviewTestState() {
		return ReviewTestState;
	}
	public void setReviewTestState(String reviewTestState) {
		ReviewTestState = reviewTestState;
	}
	public int getTestUserCount() {
		return TestUserCount;
	}
	public void setTestUserCount(int testUserCount) {
		TestUserCount = testUserCount;
	}
	public String getCreateUser() {
		return CreateUser;
	}
	public void setCreateUser(String createUser) {
		CreateUser = createUser;
	}
	public Timestamp getCreateTime() {
		return CreateTime;
	}
	public void setCreateTime(Timestamp createTime) {
		CreateTime = createTime;
	}
	public String getLastEventTimeKey() {
		return LastEventTimeKey;
	}
	public void setLastEventTimeKey(String lastEventTimeKey) {
		LastEventTimeKey = lastEventTimeKey;
	}
	public String getLastEventUser() {
		return LastEventUser;
	}
	public void setLastEventUser(String lastEventUser) {
		LastEventUser = lastEventUser;
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
	

}
