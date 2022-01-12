package kr.co.aim.messolution.extended.object.management.data;

import java.util.Date;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class SVIPanelJudge extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="panelName", type="Key", dataType="String", initial="", history="N")
	private String panelName;
	
	@CTORMTemplate(seq = "2", name="seq", type="Key", dataType="Number", initial="", history="N")
	private long seq;

	@CTORMTemplate(seq = "3", name="beforeGrade", type="Column", dataType="String", initial="", history="N")
	private String beforeGrade;

	@CTORMTemplate(seq = "4", name="beforeJudge", type="Column", dataType="String", initial="", history="N")
	private String beforeJudge;

	@CTORMTemplate(seq = "5", name="SVIPanelGrade", type="Column", dataType="String", initial="", history="N")
	private String SVIPanelGrade;
	
	@CTORMTemplate(seq = "6", name="SVIPanelJudge", type="Column", dataType="String", initial="", history="N")
	private String SVIPanelJudge;
	
	@CTORMTemplate(seq = "7", name="eventUser", type="Column", dataType="String", initial="", history="N")
	private String eventUser;
	
	@CTORMTemplate(seq = "8", name="eventName", type="Column", dataType="String", initial="", history="N")
	private String eventName;
	
	@CTORMTemplate(seq = "9", name="eventTime", type="Column", dataType="Date", initial="", history="N")
	private Date eventTime;
	
	@CTORMTemplate(seq = "10", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimeKey;
	
	@CTORMTemplate(seq = "11", name="machineName", type="Column", dataType="String", initial="", history="N")
	private String machineName;

	public String getPanelName() {
		return panelName;
	}

	public void setPanelName(String panelName) {
		this.panelName = panelName;
	}

	public long getSeq() {
		return seq;
	}

	public void setSeq(long seq) {
		this.seq = seq;
	}

	public String getBeforeGrade() {
		return beforeGrade;
	}

	public void setBeforeGrade(String beforeGrade) {
		this.beforeGrade = beforeGrade;
	}

	public String getBeforeJudge() {
		return beforeJudge;
	}

	public void setBeforeJudge(String beforeJudge) {
		this.beforeJudge = beforeJudge;
	}

	public String getSVIPanelGrade() {
		return SVIPanelGrade;
	}

	public void setSVIPanelGrade(String sVIPanelGrade) {
		SVIPanelGrade = sVIPanelGrade;
	}

	public String getSVIPanelJudge() {
		return SVIPanelJudge;
	}

	public void setSVIPanelJudge(String sVIPanelJudge) {
		SVIPanelJudge = sVIPanelJudge;
	}

	public String getEventUser() {
		return eventUser;
	}

	public void setEventUser(String eventUser) {
		this.eventUser = eventUser;
	}

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public Date getEventTime() {
		return eventTime;
	}

	public void setEventTime(Date eventTime) {
		this.eventTime = eventTime;
	}

	public String getLastEventTimeKey() {
		return lastEventTimeKey;
	}

	public void setLastEventTimeKey(String lastEventTimeKey) {
		this.lastEventTimeKey = lastEventTimeKey;
	}

	public String getMachineName() {
		return machineName;
	}

	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}
	
	

}
