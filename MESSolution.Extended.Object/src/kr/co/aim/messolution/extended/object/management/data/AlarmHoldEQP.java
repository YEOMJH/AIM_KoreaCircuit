package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;
import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class AlarmHoldEQP extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="alarmCode", type="Key", dataType="String", initial="", history="")
	private String alarmCode;

	@CTORMTemplate(seq = "2", name="machineName", type="Key", dataType="String", initial="", history="")
	private String machineName;	
		
	@CTORMTemplate(seq = "3", name="actionName", type="Column", dataType="String", initial="", history="")
	private String actionName;

	//instantiation
	public AlarmHoldEQP()
	{

	}
	
	public AlarmHoldEQP(String alarmCode, String machineName)
	{
		setAlarmCode(alarmCode);
		setMachineName(machineName);
	}
	
	public String getAlarmCode() {
		return alarmCode;
	}

	public void setAlarmCode(String alarmCode) {
		this.alarmCode = alarmCode;
	}

	public String getMachineName()
	{
		return machineName;
	}

	public void setMachineName(String machineName)
	{
		this.machineName = machineName;
	}

	public String getActionName()
	{
		return actionName;
	}

	public void setActionName(String actionName)
	{
		this.actionName = actionName;
	}

}
