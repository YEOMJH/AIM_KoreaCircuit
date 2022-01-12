package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class GlassJudge extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="glassName", type="Key", dataType="String", initial="", history="")
	private String glassName;
	
	@CTORMTemplate(seq = "2", name="glassJudge", type="Column", dataType="String", initial="", history="")
	private String glassJudge;
	
	@CTORMTemplate(seq = "3", name="xAxis", type="Column", dataType="Number", initial="", history="")
	private String xAxis;
	
	@CTORMTemplate(seq = "4", name="yAxis", type="Column", dataType="Number", initial="", history="")
	private String yAxis;
	
	@CTORMTemplate(seq = "5", name="sheetName", type="Column", dataType="String", initial="", history="")
	private String sheetName;
	
	@CTORMTemplate(seq = "6", name="processFlowName", type="Column", dataType="String", initial="", history="")
	private String processFlowName;
	
	@CTORMTemplate(seq = "7", name="processFlowVersion", type="Column", dataType="String", initial="", history="")
	private String processFlowVersion;
	
	@CTORMTemplate(seq = "8", name="lastEventName", type="Column", dataType="String", initial="", history="N")
	private String lastEventName;
	
	@CTORMTemplate(seq = "9", name="lastEventUser", type="Column", dataType="String", initial="", history="N")
	private String lastEventUser;
	
	@CTORMTemplate(seq = "10", name="lastEventTime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp lastEventTime;
	
	@CTORMTemplate(seq = "11", name="lastEventComment", type="Column", dataType="String", initial="", history="N")
	private String lastEventComment;

	@CTORMTemplate(seq = "12", name="ProcessOperationName", type="Column", dataType="String", initial="", history="")
	private String ProcessOperationName;
	
	@CTORMTemplate(seq = "13", name="SCRAPFLAG", type="Column", dataType="String", initial="", history="")
	private String SCRAPFLAG; 
	
	@CTORMTemplate(seq = "14", name="ReuseFlag", type="Column", dataType="String", initial="", history="")
	private String ReuseFlag; 
	
	@CTORMTemplate(seq = "15", name="PanelGrades", type="Column", dataType="String", initial="", history="")
	private String PanelGrades; 
	
	@CTORMTemplate(seq = "16", name="LotName", type="Column", dataType="String", initial="", history="")
	private String LotName; 
	
	@CTORMTemplate(seq = "17", name="NGFlag", type="Column", dataType="String", initial="", history="")
	private String NGFlag; 

	public String getGlassName() {
		return glassName;
	}

	public void setGlassName(String glassName) {
		this.glassName = glassName;
	}

	public String getGlassJudge() {
		return glassJudge;
	}

	public void setGlassJudge(String glassJudge) {
		this.glassJudge = glassJudge;
	}

	public String getxAxis() {
		return xAxis;
	}

	public void setxAxis(String xAxis) {
		this.xAxis = xAxis;
	}

	public String getyAxis() {
		return yAxis;
	}

	public void setyAxis(String yAxis) {
		this.yAxis = yAxis;
	}

	public String getSheetName() {
		return sheetName;
	}

	public void setSheetName(String sheetName) {
		this.sheetName = sheetName;
	}

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

	public String getLastEventName() {
		return lastEventName;
	}

	public void setLastEventName(String lastEventName) {
		this.lastEventName = lastEventName;
	}

	public String getLastEventUser() {
		return lastEventUser;
	}

	public void setLastEventUser(String lastEventUser) {
		this.lastEventUser = lastEventUser;
	}

	public Timestamp getLastEventTime() {
		return lastEventTime;
	}

	public void setLastEventTime(Timestamp lastEventTime) {
		this.lastEventTime = lastEventTime;
	}

	public String getLastEventComment() {
		return lastEventComment;
	}

	public void setLastEventComment(String lastEventComment) {
		this.lastEventComment = lastEventComment;
	}
	
	public String getProcessOperationName() {
		return ProcessOperationName;
	}

	public void setProcessOperationName(String ProcessOperationName) {
		this.ProcessOperationName = ProcessOperationName;
	}

	//instantiation
	public GlassJudge()
	{
		
	}
	
	public GlassJudge(String glassName)
	{
		setGlassName(glassName);
	}

	public String getSCRAPFLAG()
	{
		return SCRAPFLAG;
	}
	public void setScrapJudge(String SCRAPFLAG) {
		this.SCRAPFLAG=SCRAPFLAG;
		// TODO Auto-generated method stub		
	}


	public void setReuseFlag(String reuseFlag) {
		this.ReuseFlag = reuseFlag;
	}

	public String getReuseFlag() {
		return ReuseFlag;
	}

	public void setPanelGrades(String panelGrades) {
		this.PanelGrades = panelGrades;
	}

	public String getPanelGrades() {
		return PanelGrades;
	}

	public String getLotName() {
		return LotName;
	}

	public void setLotName(String lotName) {
		LotName = lotName;
	}

	public String getNGFlag() {
		return NGFlag;
	}

	public void setNGFlag(String nGFlag) {
		NGFlag = nGFlag;
	}

	public void setSCRAPFLAG(String sCRAPFLAG) {
		SCRAPFLAG = sCRAPFLAG;
	}

}
