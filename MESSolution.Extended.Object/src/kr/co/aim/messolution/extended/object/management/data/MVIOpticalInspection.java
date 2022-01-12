package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class MVIOpticalInspection extends UdfAccessor {

	@CTORMTemplate(seq = "1", name = "panelName", type = "Key", dataType = "String", initial = "", history = "")
	private String panelName;
	@CTORMTemplate(seq = "2", name = "seq", type = "Key", dataType = "Number", initial = "", history = "")
	private long seq;
	@CTORMTemplate(seq = "3", name = "color", type = "Key", dataType = "String", initial = "", history = "")
	private String color;
	@CTORMTemplate(seq = "4", name = "start_Time", type = "Column", dataType = "Timestamp", initial = "", history = "")
	private Timestamp start_Time;
	@CTORMTemplate(seq = "5", name = "end_Time", type = "Column", dataType = "Timestamp", initial = "", history = "")
	private Timestamp end_Time;
	@CTORMTemplate(seq = "6", name = "luminance", type = "Column", dataType = "String", initial = "", history = "")
	private String luminance;
	@CTORMTemplate(seq = "7", name = "x", type = "Column", dataType = "String", initial = "", history = "")
	private String x;
	@CTORMTemplate(seq = "8", name = "y", type = "Column", dataType = "String", initial = "", history = "")
	private String y;
	@CTORMTemplate(seq = "9", name = "i", type = "Column", dataType = "String", initial = "", history = "")
	private String i;
	@CTORMTemplate(seq = "10", name = "efficiency", type = "Column", dataType = "String", initial = "", history = "")
	private String efficiency;
	@CTORMTemplate(seq = "11", name = "luminance_Aft", type = "Column", dataType = "String", initial = "", history = "")
	private String luminance_Aft;
	@CTORMTemplate(seq = "12", name = "x_Aft", type = "Column", dataType = "String", initial = "", history = "")
	private String x_Aft;
	@CTORMTemplate(seq = "13", name = "y_Aft", type = "Column", dataType = "String", initial = "", history = "")
	private String y_Aft;
	@CTORMTemplate(seq = "14", name = "i_Aft", type = "Column", dataType = "String", initial = "", history = "")
	private String i_Aft;
	@CTORMTemplate(seq = "15", name = "efficiency_Aft", type = "Column", dataType = "String", initial = "", history = "")
	private String efficiency_Aft;
	@CTORMTemplate(seq = "16", name = "result", type = "Column", dataType = "String", initial = "", history = "")
	private String result;
	@CTORMTemplate(seq = "17", name = "colorGamut", type = "Column", dataType = "String", initial = "", history = "")
	private String colorGamut;
	@CTORMTemplate(seq = "18", name = "lastEventName", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventName;
	@CTORMTemplate(seq = "19", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventUser;
	@CTORMTemplate(seq = "20", name = "lastEventTime", type = "Column", dataType = "Timestamp", initial = "", history = "N")
	private Timestamp lastEventTime;
	@CTORMTemplate(seq = "21", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventComment;
	@CTORMTemplate(seq = "22", name = "lastEventTimekey", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventTimekey;

	public String getPanelName()
	{
		return panelName;
	}

	public void setPanelName(String panelName)
	{
		this.panelName = panelName;
	}

	public long getSeq()
	{
		return seq;
	}

	public void setSeq(long seq)
	{
		this.seq = seq;
	}

	public String getColor()
	{
		return color;
	}

	public void setColor(String color)
	{
		this.color = color;
	}

	public Timestamp getStart_Time()
	{
		return start_Time;
	}

	public void setStart_Time(Timestamp start_Time)
	{
		this.start_Time = start_Time;
	}

	public Timestamp getEnd_Time()
	{
		return end_Time;
	}

	public void setEnd_Time(Timestamp end_Time)
	{
		this.end_Time = end_Time;
	}

	public String getLuminance()
	{
		return luminance;
	}

	public void setLuminance(String luminance)
	{
		this.luminance = luminance;
	}

	public String getX()
	{
		return x;
	}

	public void setX(String x)
	{
		this.x = x;
	}

	public String getY()
	{
		return y;
	}

	public void setY(String y)
	{
		this.y = y;
	}

	public String getI()
	{
		return i;
	}

	public void setI(String i)
	{
		this.i = i;
	}

	public String getEfficiency()
	{
		return efficiency;
	}

	public void setEfficiency(String efficiency)
	{
		this.efficiency = efficiency;
	}

	public String getLuminance_Aft()
	{
		return luminance_Aft;
	}

	public void setLuminance_Aft(String luminance_Aft)
	{
		this.luminance_Aft = luminance_Aft;
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

	public String getI_Aft()
	{
		return i_Aft;
	}

	public void setI_Aft(String i_Aft)
	{
		this.i_Aft = i_Aft;
	}

	public String getEfficiency_Aft()
	{
		return efficiency_Aft;
	}

	public void setEfficiency_Aft(String efficiency_Aft)
	{
		this.efficiency_Aft = efficiency_Aft;
	}

	public String getResult()
	{
		return result;
	}

	public void setResult(String result)
	{
		this.result = result;
	}

	public String getColorGamut()
	{
		return colorGamut;
	}

	public void setColorGamut(String colorGamut)
	{
		this.colorGamut = colorGamut;
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

	public Timestamp getLastEventTime()
	{
		return lastEventTime;
	}

	public void setLastEventTime(Timestamp lastEventTime)
	{
		this.lastEventTime = lastEventTime;
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