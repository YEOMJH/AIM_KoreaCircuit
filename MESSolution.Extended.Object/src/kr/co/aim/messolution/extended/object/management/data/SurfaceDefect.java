package kr.co.aim.messolution.extended.object.management.data;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class SurfaceDefect extends UdfAccessor {
	@CTORMTemplate(seq = "1", name="PanelName", type="Key", dataType="String", initial="", history="")
	private String PanelName;
	
	@CTORMTemplate(seq = "2", name="TimeKey", type="Key", dataType="String", initial="", history="")
	private String TimeKey;
	
	@CTORMTemplate(seq = "3", name="DefectCode", type="Column", dataType="String", initial="", history="")
	private String DefectCode;
	
	@CTORMTemplate(seq = "4", name="Count", type="Column", dataType="String", initial="", history="")
	private String Count;
	
	@CTORMTemplate(seq = "5", name="PanelGrade", type="Column", dataType="String", initial="", history="")
	private String PanelGrade;
	
	@CTORMTemplate(seq = "6", name="EventComment", type="Column", dataType="String", initial="", history="")
	private String EventComment;
	
	@CTORMTemplate(seq = "7", name="EventUser", type="Column", dataType="String", initial="", history="")
	private String EventUser;
			
	@CTORMTemplate(seq = "8", name="ProcessOperationname", type="Column", dataType="String", initial="", history="")
	private String ProcessOperationname;
	
	@CTORMTemplate(seq = "9", name="LotName", type="Column", dataType="String", initial="", history="")
	private String LotName;
	
	@CTORMTemplate(seq = "10", name="RepairCount", type="Column", dataType="String", initial="", history="")
	private String RepairCount;
	
	@CTORMTemplate(seq = "11", name="LoggedinTimeKey", type="Column", dataType="String", initial="", history="")
	private String LoggedinTimeKey;
	
	@CTORMTemplate(seq = "12", name="WorkOrder", type="Column", dataType="String", initial="", history="")
	private String WorkOrder;
	
	@CTORMTemplate(seq = "13", name="ProductSpecName", type="Column", dataType="String", initial="", history="")
	private String ProductSpecName;

	public String getPanelName() {
		return PanelName;
	}

	public void setPanelName(String panelName) {
		PanelName = panelName;
	}

	public String getTimeKey() {
		return TimeKey;
	}

	public void setTimeKey(String timeKey) {
		TimeKey = timeKey;
	}

	public String getDefectCode() {
		return DefectCode;
	}

	public void setDefectCode(String defectCode) {
		DefectCode = defectCode;
	}

	public String getCount() {
		return Count;
	}

	public void setCount(String count) {
		Count = count;
	}

	public String getPanelGrade() {
		return PanelGrade;
	}

	public void setPanelGrade(String panelGrade) {
		PanelGrade = panelGrade;
	}

	public String getEventComment() {
		return EventComment;
	}

	public void setEventComment(String eventComment) {
		EventComment = eventComment;
	}

	public String getEventUser() {
		return EventUser;
	}

	public void setEventUser(String eventUser) {
		EventUser = eventUser;
	}

	public String getProcessOperationname() {
		return ProcessOperationname;
	}

	public void setProcessOperationname(String processOperationname) {
		ProcessOperationname = processOperationname;
	}

	public String getLotName() {
		return LotName;
	}

	public void setLotName(String lotName) {
		LotName = lotName;
	}

	public String getRepairCount() {
		return RepairCount;
	}

	public void setRepairCount(String repairCount) {
		RepairCount = repairCount;
	}

	public String getLoggedinTimeKey() {
		return LoggedinTimeKey;
	}

	public void setLoggedinTimeKey(String loggedinTimeKey) {
		LoggedinTimeKey = loggedinTimeKey;
	}
	
	public String getWorkOrder() {
		return WorkOrder;
	}

	public void setWorkOrder(String workOrder) {
		WorkOrder = workOrder;
	}
	
	public String getProductSpecName() {
		return ProductSpecName;
	}

	public void setProductSpecName(String productSpecName) {
		ProductSpecName = productSpecName;
	}
	
	public SurfaceDefect()
	{
		
	}
	
	public SurfaceDefect(String panelName, String timeKey)
	{
		setPanelName(panelName);
		setTimeKey(timeKey);
	}
}
