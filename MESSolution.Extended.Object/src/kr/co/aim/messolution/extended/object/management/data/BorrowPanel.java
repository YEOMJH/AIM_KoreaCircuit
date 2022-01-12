package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class BorrowPanel extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="taskId", type="Key", dataType="String", initial="", history="")
	private String taskId;
	
	@CTORMTemplate(seq = "2", name="lotName", type="Key", dataType="String", initial="", history="")
	private String lotName;

	@CTORMTemplate(seq = "3", name="productRequestName", type="Column", dataType="String", initial="", history="")
	private String productRequestName;

	@CTORMTemplate(seq = "4", name="productSpecName", type="Column", dataType="String", initial="", history="")
	private String productSpecName;
	
	@CTORMTemplate(seq = "5", name="productSpecVersion", type="Column", dataType="String", initial="", history="")
	private String productSpecVersion;
	
	@CTORMTemplate(seq = "6", name="processFlowName", type="Column", dataType="String", initial="", history="")
	private String processFlowName;
	
	@CTORMTemplate(seq = "7", name="processFlowVersion", type="Column", dataType="String", initial="", history="")
	private String processFlowVersion;

	@CTORMTemplate(seq = "8", name="borrowUserName", type="Column", dataType="String", initial="", history="")
	private String borrowUserName;
	
	@CTORMTemplate(seq = "9", name="borrowCentrality", type="Column", dataType="String", initial="", history="")
	private String borrowCentrality;
	
	@CTORMTemplate(seq = "10", name="borrowDepartment", type="Column", dataType="String", initial="", history="")
	private String borrowDepartment;
	
	@CTORMTemplate(seq = "11", name="phone", type="Column", dataType="String", initial="", history="")
	private String phone;
	
	@CTORMTemplate(seq = "12", name="email", type="Column", dataType="String", initial="", history="")
	private String email;
	
	@CTORMTemplate(seq = "13", name="panelOutFlag", type="Column", dataType="String", initial="", history="")
	private String panelOutFlag;
	
	@CTORMTemplate(seq = "14", name="borrowDate", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp borrowDate;
	
	@CTORMTemplate(seq = "15", name="renewDate", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp renewDate;
	
	@CTORMTemplate(seq = "16", name="renewCount", type="Column", dataType="Number", initial="", history="")
	private Number renewCount;
	
	@CTORMTemplate(seq = "17", name="returnDate", type="Column", dataType="Timestamp", initial="", history="")
	private Timestamp returnDate;
	
	@CTORMTemplate(seq = "18", name="borrowState", type="Column", dataType="String", initial="", history="")
	private String borrowState;
	
	@CTORMTemplate(seq = "19", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "20", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;
	
	@CTORMTemplate(seq = "21", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "22", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;
	
	@CTORMTemplate(seq = "23", name="lastEventTimekey", type="Column", dataType="String", initial="", history="N")
	private String lastEventTimekey;

	public String getTaskId()
	{
		return taskId;
	}

	public void setTaskId(String taskId)
	{
		this.taskId = taskId;
	}

	public String getLotName()
	{
		return lotName;
	}

	public void setLotName(String lotName)
	{
		this.lotName = lotName;
	}

	public String getProductRequestName()
	{
		return productRequestName;
	}

	public void setProductRequestName(String productRequestName)
	{
		this.productRequestName = productRequestName;
	}

	public String getProductSpecName()
	{
		return productSpecName;
	}

	public void setProductSpecName(String productSpecName)
	{
		this.productSpecName = productSpecName;
	}

	public String getProductSpecVersion()
	{
		return productSpecVersion;
	}

	public void setProductSpecVersion(String productSpecVersion)
	{
		this.productSpecVersion = productSpecVersion;
	}

	public String getProcessFlowName()
	{
		return processFlowName;
	}

	public void setProcessFlowName(String processFlowName)
	{
		this.processFlowName = processFlowName;
	}

	public String getProcessFlowVersion()
	{
		return processFlowVersion;
	}

	public void setProcessFlowVersion(String processFlowVersion)
	{
		this.processFlowVersion = processFlowVersion;
	}

	public String getBorrowUserName()
	{
		return borrowUserName;
	}

	public void setBorrowUserName(String borrowUserName)
	{
		this.borrowUserName = borrowUserName;
	}

	public String getBorrowCentrality()
	{
		return borrowCentrality;
	}

	public void setBorrowCentrality(String borrowCentrality)
	{
		this.borrowCentrality = borrowCentrality;
	}

	public String getBorrowDepartment()
	{
		return borrowDepartment;
	}

	public void setBorrowDepartment(String borrowDepartment)
	{
		this.borrowDepartment = borrowDepartment;
	}

	public String getPhone()
	{
		return phone;
	}

	public void setPhone(String phone)
	{
		this.phone = phone;
	}

	public String getEmail()
	{
		return email;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	public String getPanelOutFlag()
	{
		return panelOutFlag;
	}

	public void setPanelOutFlag(String panelOutFlag)
	{
		this.panelOutFlag = panelOutFlag;
	}

	public Timestamp getBorrowDate()
	{
		return borrowDate;
	}

	public void setBorrowDate(Timestamp borrowDate)
	{
		this.borrowDate = borrowDate;
	}

	public Timestamp getRenewDate()
	{
		return renewDate;
	}

	public void setRenewDate(Timestamp renewDate)
	{
		this.renewDate = renewDate;
	}

	public Number getRenewCount()
	{
		return renewCount;
	}

	public void setRenewCount(Number renewCount)
	{
		this.renewCount = renewCount;
	}

	public Timestamp getReturnDate()
	{
		return returnDate;
	}

	public void setReturnDate(Timestamp returnDate)
	{
		this.returnDate = returnDate;
	}

	public String getBorrowState()
	{
		return borrowState;
	}

	public void setBorrowState(String borrowState)
	{
		this.borrowState = borrowState;
	}

	public Timestamp getLastEventTime()
	{
		return lastEventTime;
	}

	public void setLastEventTime(Timestamp lastEventTime)
	{
		this.lastEventTime = lastEventTime;
	}

	public String getLastEventName()
	{
		return lastEventName;
	}

	public void setLastEventName(String lastEventName)
	{
		this.lastEventName = lastEventName;
	}

	public String getLastEventUser()
	{
		return lastEventUser;
	}

	public void setLastEventUser(String lastEventUser)
	{
		this.lastEventUser = lastEventUser;
	}

	public String getLastEventComment()
	{
		return lastEventComment;
	}

	public void setLastEventComment(String lastEventComment)
	{
		this.lastEventComment = lastEventComment;
	}

	public String getLastEventTimekey()
	{
		return lastEventTimekey;
	}

	public void setLastEventTimekey(String lastEventTimekey)
	{
		this.lastEventTimekey = lastEventTimekey;
	}
}