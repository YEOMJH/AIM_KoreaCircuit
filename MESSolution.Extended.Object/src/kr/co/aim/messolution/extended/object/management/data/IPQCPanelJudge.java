package kr.co.aim.messolution.extended.object.management.data;

import java.util.Date;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class IPQCPanelJudge extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="ipqcLotName", type="Key", dataType="String", initial="", history="")
	private String ipqcLotName;
	
	@CTORMTemplate(seq = "2", name="seq", type="Key", dataType="Number", initial="", history="")
	private long seq;
	
	@CTORMTemplate(seq = "3", name="panelName", type="Key", dataType="String", initial="", history="")
	private String panelName;

	@CTORMTemplate(seq = "4", name="beforeGrade", type="Column", dataType="String", initial="", history="")
	private String beforeGrade;

	@CTORMTemplate(seq = "5", name="afterGrade", type="Column", dataType="String", initial="", history="")
	private String afterGrade;

	@CTORMTemplate(seq = "6", name="opticalJudge", type="Column", dataType="String", initial="", history="")
	private String opticalJudge;
	
	@CTORMTemplate(seq = "7", name="electricalJudge", type="Column", dataType="String", initial="", history="")
	private String electricalJudge;
	
	@CTORMTemplate(seq = "8", name="eventUser", type="Column", dataType="String", initial="", history="")
	private String eventUser;
	
	@CTORMTemplate(seq = "9", name="eventTime", type="Column", dataType="Date", initial="", history="")
	private Date eventTime;
	
	@CTORMTemplate(seq = "10", name="machineName", type="Column", dataType="String", initial="", history="")
	private String machineName;

	
	
	public String getMachineName() {
		return machineName;
	}

	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}

	public String getIpqcLotName() {
		return ipqcLotName;
	}

	public void setIpqcLotName(String ipqcLotName) {
		this.ipqcLotName = ipqcLotName;
	}

	public long getSeq() {
		return seq;
	}

	public void setSeq(long seq) {
		this.seq = seq;
	}

	public String getPanelName() {
		return panelName;
	}

	public void setPanelName(String panelName) {
		this.panelName = panelName;
	}

	public String getBeforeGrade() {
		return beforeGrade;
	}

	public void setBeforeGrade(String beforeGrade) {
		this.beforeGrade = beforeGrade;
	}

	public String getAfterGrade() {
		return afterGrade;
	}

	public void setAfterGrade(String afterGrade) {
		this.afterGrade = afterGrade;
	}

	public String getOpticalJudge() {
		return opticalJudge;
	}

	public void setOpticalJudge(String opticalJudge) {
		this.opticalJudge = opticalJudge;
	}

	public String getElectricalJudge() {
		return electricalJudge;
	}

	public void setElectricalJudge(String electricalJudge) {
		this.electricalJudge = electricalJudge;
	}

	public String getEventUser() {
		return eventUser;
	}

	public void setEventUser(String eventUser) {
		this.eventUser = eventUser;
	}

	public Date getEventTime() {
		return eventTime;
	}

	public void setEventTime(Date eventTime) {
		this.eventTime = eventTime;
	}

	
	

}