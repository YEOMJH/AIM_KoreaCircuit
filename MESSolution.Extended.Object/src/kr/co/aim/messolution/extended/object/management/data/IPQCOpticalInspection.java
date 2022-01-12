package kr.co.aim.messolution.extended.object.management.data;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class IPQCOpticalInspection extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="ipqcLotName", type="Key", dataType="String", initial="", history="")
	private String ipqcLotName;
	
	@CTORMTemplate(seq = "2", name="seq", type="Key", dataType="Number", initial="", history="")
	private long seq;
	
	@CTORMTemplate(seq = "3", name="panelName", type="Key", dataType="String", initial="", history="")
	private String panelName;

	@CTORMTemplate(seq = "4", name="start_Time", type="Key", dataType="String", initial="", history="")
	private String start_Time;

	@CTORMTemplate(seq = "5", name="brightness", type="Key", dataType="String", initial="", history="")
	private String brightness;

	@CTORMTemplate(seq = "6", name="x", type="Key", dataType="String", initial="", history="")
	private String x;
	
	@CTORMTemplate(seq = "7", name="y", type="Key", dataType="String", initial="", history="")
	private String y;

	@CTORMTemplate(seq = "8", name="stop_Time", type="Column", dataType="String", initial="", history="")
	private String stop_Time;
	
	@CTORMTemplate(seq = "9", name="vr", type="Column", dataType="String", initial="", history="")
	private String vr;
	
	@CTORMTemplate(seq = "10", name="vg", type="Column", dataType="String", initial="", history="")
	private String vg;
	
	@CTORMTemplate(seq = "11", name="vb", type="Column", dataType="String", initial="", history="")
	private String vb;
	
	@CTORMTemplate(seq = "12", name="i", type="Column", dataType="String", initial="", history="")
	private String i;
	
	@CTORMTemplate(seq = "13", name="x_Aft", type="Column", dataType="String", initial="", history="")
	private String x_Aft;
	
	@CTORMTemplate(seq = "14", name="y_Aft", type="Column", dataType="String", initial="", history="")
	private String y_Aft;
	
	@CTORMTemplate(seq = "15", name="l_Aft", type="Column", dataType="String", initial="", history="")
	private String l_Aft;
	
	@CTORMTemplate(seq = "16", name="vr_Aft", type="Column", dataType="String", initial="", history="")
	private String vr_Aft;
	
	@CTORMTemplate(seq = "17", name="vg_Aft", type="Column", dataType="String", initial="", history="")
	private String vg_Aft;
	
	@CTORMTemplate(seq = "18", name="vb_Aft", type="Column", dataType="String", initial="", history="")
	private String vb_Aft;
	
	@CTORMTemplate(seq = "19", name="i_Aft", type="Column", dataType="String", initial="", history="")
	private String i_Aft;
	
	@CTORMTemplate(seq = "20", name="result", type="Column", dataType="String", initial="", history="")
	private String result;
	
	@CTORMTemplate(seq = "21", name="color", type="Column", dataType="String", initial="", history="")
	private String color;
	
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

	public String getStart_Time() {
		return start_Time;
	}

	public void setStart_Time(String start_Time) {
		this.start_Time = start_Time;
	}

	public String getBrightness() {
		return brightness;
	}

	public void setBrightness(String brightness) {
		this.brightness = brightness;
	}

	public String getX() {
		return x;
	}

	public void setX(String x) {
		this.x = x;
	}

	public String getY() {
		return y;
	}

	public void setY(String y) {
		this.y = y;
	}

	public String getStop_Time()
	{
		return stop_Time;
	}

	public void setStop_Time(String stop_Time)
	{
		this.stop_Time = stop_Time;
	}

	public String getVr()
	{
		return vr;
	}

	public void setVr(String vr)
	{
		this.vr = vr;
	}

	public String getVg()
	{
		return vg;
	}

	public void setVg(String vg)
	{
		this.vg = vg;
	}

	public String getVb()
	{
		return vb;
	}

	public void setVb(String vb)
	{
		this.vb = vb;
	}

	public String getI()
	{
		return i;
	}

	public void setI(String i)
	{
		this.i = i;
	}

	public String getX_Aft()
	{
		return x_Aft;
	}

	public void setX_Aft(String x_Aft)
	{
		this.x_Aft = x_Aft;
	}

	public String getY_Aft()
	{
		return y_Aft;
	}

	public void setY_Aft(String y_Aft)
	{
		this.y_Aft = y_Aft;
	}

	public String getL_Aft()
	{
		return l_Aft;
	}

	public void setL_Aft(String l_Aft)
	{
		this.l_Aft = l_Aft;
	}

	public String getVr_Aft()
	{
		return vr_Aft;
	}

	public void setVr_Aft(String vr_Aft)
	{
		this.vr_Aft = vr_Aft;
	}

	public String getVg_Aft()
	{
		return vg_Aft;
	}

	public void setVg_Aft(String vg_Aft)
	{
		this.vg_Aft = vg_Aft;
	}

	public String getVb_Aft()
	{
		return vb_Aft;
	}

	public void setVb_Aft(String vb_Aft)
	{
		this.vb_Aft = vb_Aft;
	}

	public String getI_Aft()
	{
		return i_Aft;
	}

	public void setI_Aft(String i_Aft)
	{
		this.i_Aft = i_Aft;
	}

	public String getResult()
	{
		return result;
	}

	public void setResult(String result)
	{
		this.result = result;
	}

	public String getColor()
	{
		return color;
	}

	public void setColor(String color)
	{
		this.color = color;
	}

}