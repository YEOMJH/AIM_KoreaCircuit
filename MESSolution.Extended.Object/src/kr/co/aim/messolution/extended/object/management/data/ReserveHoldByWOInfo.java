package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import org.apache.commons.net.ntp.TimeStamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class ReserveHoldByWOInfo extends UdfAccessor  {
	
	@CTORMTemplate(seq = "1", name = "productRequestName", type = "Key", dataType = "String", initial = "", history = "")
	private String productRequestName;
	@CTORMTemplate(seq = "2", name = "processOperation", type = "Key", dataType = "String", initial = "", history = "")
	private String processOperation;
	@CTORMTemplate(seq = "3", name = "panelGrade", type = "Key", dataType = "String", initial = "", history = "")
	private String panelGrade;
	@CTORMTemplate(seq = "4", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventUser;
	@CTORMTemplate(seq = "5", name = "lastEventTime", type = "Column", dataType = "Timestamp", initial = "", history = "N")
	private Timestamp lastEventTime;
	@CTORMTemplate(seq = "6", name = "lastEventTimekey", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventTimekey;
	@CTORMTemplate(seq = "7", name = "lastEventName", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventName;
	@CTORMTemplate(seq = "7", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventComment;
	public String getProductRequestName() {
		return productRequestName;
	}
	public void setProductRequestName(String productRequestName) {
		this.productRequestName = productRequestName;
	}
	public Timestamp getLastEventTime() {
		return lastEventTime;
	}
	public void setLastEventTime(Timestamp timestamp) {
		this.lastEventTime = timestamp;
	}
	public String getProcessOperation() {
		return processOperation;
	}
	public void setProcessOperation(String processOperation) {
		this.processOperation = processOperation;
	}
	public String getPanelGrade() {
		return panelGrade;
	}
	public void setPanelGrade(String panelGrade) {
		this.panelGrade = panelGrade;
	}
	public String getLastEventUser() {
		return lastEventUser;
	}
	public void setLastEventUser(String lastEventUser) {
		this.lastEventUser = lastEventUser;
	}

	public String getLastEventTimekey() {
		return lastEventTimekey;
	}
	public void setLastEventTimekey(String lastEventTimekey) {
		this.lastEventTimekey = lastEventTimekey;
	}
	public String getLastEventName() {
		return lastEventName;
	}
	public void setLastEventName(String lastEventName) {
		this.lastEventName = lastEventName;
	}
	public String getLastEventComment() {
		return lastEventComment;
	}
	public void setLastEventComment(String lastEventComment) {
		this.lastEventComment = lastEventComment;
	}

	public ReserveHoldByWOInfo(String productRequestName, String processOperation,
			String panelGrade) {
		super();
		this.productRequestName = productRequestName;
		this.processOperation = processOperation;
		this.panelGrade = panelGrade;
	}
	public ReserveHoldByWOInfo() {
		
	}

}
