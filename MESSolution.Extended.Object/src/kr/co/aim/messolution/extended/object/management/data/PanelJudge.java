package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class PanelJudge extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="PanelName", type="Key", dataType="String", initial="", history="")
	private String PanelName;
	
	@CTORMTemplate(seq = "2", name="Paneljudge", type="Column", dataType="String", initial="", history="")
	private String Paneljudge;
	
	@CTORMTemplate(seq = "3", name="Panelgrade", type="Column", dataType="String", initial="", history="")
	private String Panelgrade;
	
	@CTORMTemplate(seq = "4", name="Xaxis", type="Column", dataType="Number", initial="", history="")
	private String Xaxis;
	
	@CTORMTemplate(seq = "5", name="Yaxis", type="Column", dataType="Number", initial="", history="")
	private String Yaxis;
	
	@CTORMTemplate(seq = "6", name="Sheetname", type="Column", dataType="String", initial="", history="")
	private String Sheetname;
	
	@CTORMTemplate(seq = "7", name="Glassname", type="Column", dataType="String", initial="", history="")
	private String Glassname;
	
	@CTORMTemplate(seq = "8", name="Lasteventname", type="Column", dataType="String", initial="", history="N")
	private String Lasteventname;
	
	@CTORMTemplate(seq = "9", name="Lasteventuser", type="Column", dataType="String", initial="", history="N")
	private String Lasteventuser;
	
	@CTORMTemplate(seq = "10", name="Lasteventtime", type="Column", dataType="Timestamp", initial="", history="N")
	private Timestamp Lasteventtime;
	
	@CTORMTemplate(seq = "11", name="Lasteventcomment", type="Column", dataType="String", initial="", history="N")
	private String Lasteventcomment;
	
	@CTORMTemplate(seq = "12", name="ProcessOperationName", type="Column", dataType="String", initial="", history="")
	private String ProcessOperationName;
	
	@CTORMTemplate(seq = "13", name="LotName", type="Column", dataType="String", initial="", history="")
	private String LotName;
	

	public String getPanelName() {
		return PanelName;
	}
	public void setPanelName(String panelName) {
		this.PanelName = panelName;
	}
	
	public String getPaneljudge() {
		return Paneljudge;
	}
	public void setPaneljudge(String paneljudge) {
		this.Paneljudge = paneljudge;
	}
	
	public String getPanelgrade() {
		return Panelgrade;
	}
	public void setPanelgrade(String panelgrade) {
		this.Panelgrade = panelgrade;
	}
	
	public String getXaxis() {
		return Xaxis;
	}
	public void setXaxis(String xaxis) {
		this.Xaxis = xaxis;
	}
	
	public String getYaxis() {
		return Yaxis;
	}
	public void setYaxis(String yaxis) {
		this.Yaxis = yaxis;
	}
	
	public String getSheetname() {
		return Sheetname;
	}
	public void setSheetname(String sheetname) {
		this.Sheetname = sheetname;
	}
	
	public String getGlassname() {
		return Glassname;
	}
	public void setGlassname(String glassname) {
		this.Glassname = glassname;
	}
	
	public String getLasteventname() {
		return Lasteventname;
	}
	public void setLasteventname(String lasteventname) {
		this.Lasteventname = lasteventname;
	}
	
	public String getLasteventuser() {
		return Lasteventuser;
	}
	public void setLasteventuser(String lasteventuser) {
		this.Lasteventuser = lasteventuser;
	}
	
	public Timestamp getLasteventtime() {
		return Lasteventtime;
	}
	public void setLasteventtime(Timestamp lasteventtime) {
		this.Lasteventtime = lasteventtime;
	}
	
	public String getLasteventcomment() {
		return Lasteventcomment;
	}
	public void setLasteventcomment(String lasteventcomment) {
		this.Lasteventcomment = lasteventcomment;
	}
	
	public String getProcessOperationName() {
		return ProcessOperationName;
	}
	public void setProcessOperationName(String ProcessOperationName) {
		this.ProcessOperationName = ProcessOperationName;
	}
	
	public String getLotName() {
		return LotName;
	}
	public void setLotName(String LotName) {
		this.LotName = LotName;
	}

	public PanelJudge()
	{
		
	}

	public PanelJudge(String panelName)
	{
		setPanelName(panelName);
	}

}
