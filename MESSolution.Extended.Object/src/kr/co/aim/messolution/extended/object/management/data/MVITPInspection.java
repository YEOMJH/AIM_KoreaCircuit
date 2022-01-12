package kr.co.aim.messolution.extended.object.management.data;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class MVITPInspection extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="seq", type="Key", dataType="Number", initial="", history="")
	private long seq;
	
	@CTORMTemplate(seq = "2", name="panelName", type="Key", dataType="String", initial="", history="")
	private String panelName;

	@CTORMTemplate(seq = "3", name="testName", type="Key", dataType="String", initial="", history="")
	private String testName;

	@CTORMTemplate(seq = "4", name="testResult", type="Column", dataType="String", initial="", history="")
	private String testResult;

	@CTORMTemplate(seq = "7", name="lastEventTimeKey", type="Column", dataType="String", initial="", history="")
	private String lastEventTimeKey;

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

	public String getTestName() {
		return testName;
	}

	public void setTestName(String testName) {
		this.testName = testName;
	}

	public String getTestResult() {
		return testResult;
	}

	public void setTestResult(String testResult) {
		this.testResult = testResult;
	}

	public String getLastEventTimeKey() {
		return lastEventTimeKey;
	}

	public void setLastEventTimeKey(String lastEventTimeKey) {
		this.lastEventTimeKey = lastEventTimeKey;
	}
	
	
}