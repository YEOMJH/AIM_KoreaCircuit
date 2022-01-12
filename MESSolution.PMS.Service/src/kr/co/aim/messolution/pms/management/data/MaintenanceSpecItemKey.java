package kr.co.aim.messolution.pms.management.data;

import kr.co.aim.greenframe.orm.info.KeyInfo;
import kr.co.aim.greenframe.orm.info.access.FieldAccessor;

public class MaintenanceSpecItemKey extends FieldAccessor implements KeyInfo {
	
	private String maintenanceGroupName;
	private String maintenanceSpecName;
	private long position;

	public String getMaintenanceGroupName() {
		return maintenanceGroupName;
	}

	public void setMaintenanceGroupName(String maintenanceGroupName) {
		this.maintenanceGroupName = maintenanceGroupName;
	}
	
	public String getMaintenanceSpecName() {
		return maintenanceSpecName;
	}

	public void setMaintenanceSpecName(String maintenanceSpecName) {
		this.maintenanceSpecName = maintenanceSpecName;
	}

	public long getPosition() {
		return position;
	}

	public void setPosition(long position) {
		this.position = position;
	}

	public MaintenanceSpecItemKey()
	{
		
	}
	
	public MaintenanceSpecItemKey(String maintGroupName, String maintSpecName, long pos)
	{
		this.maintenanceGroupName = maintGroupName;
		this.maintenanceSpecName = maintSpecName;
		this.position = pos;
	}
}
