package kr.co.aim.messolution.pms.management.data;

import kr.co.aim.greenframe.orm.info.DataInfo;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class MaintenanceSpec extends UdfAccessor implements
		DataInfo<MaintenanceSpecKey> {
	
	private MaintenanceSpecKey key;
	
	@Override
	public MaintenanceSpecKey getKey() {
		return key;
	}

	@Override
	public void setKey(MaintenanceSpecKey keyInfo) {
		this.key = keyInfo;
	}
	
	private String maintenanceType;
	private String cycleType;
	private long cycleDuration;
	private long exeDay;
	private long exeStartHour;
	private long exeDuration;
	
	public MaintenanceSpec()
	{
		
	}
	
	public MaintenanceSpec(MaintenanceSpecKey keyInfo)
	{
		this.key = keyInfo;
	}

	public String getMaintenanceType() {
		return maintenanceType;
	}

	public void setMaintenanceType(String maintenanceType) {
		this.maintenanceType = maintenanceType;
	}

	public String getCycleType() {
		return cycleType;
	}

	public void setCycleType(String cycleType) {
		this.cycleType = cycleType;
	}

	public long getCycleDuration() {
		return cycleDuration;
	}

	public void setCycleDuration(long cycleDuration) {
		this.cycleDuration = cycleDuration;
	}

	public long getExeDay() {
		return exeDay;
	}

	public void setExeDay(long exeDay) {
		this.exeDay = exeDay;
	}

	public long getExeStartHour() {
		return exeStartHour;
	}

	public void setExeStartHour(long exeStartHour) {
		this.exeStartHour = exeStartHour;
	}

	public long getExeDuration() {
		return exeDuration;
	}

	public void setExeDuration(long exeDuration) {
		this.exeDuration = exeDuration;
	}
}
