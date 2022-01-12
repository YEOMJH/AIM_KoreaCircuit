package kr.co.aim.messolution.pms.management.data;

import kr.co.aim.greenframe.orm.info.KeyInfo;
import kr.co.aim.greenframe.orm.info.access.FieldAccessor;

public class MaintenanceSpecKey extends FieldAccessor implements KeyInfo {
	
	private String maintenanceGroupName;
	private String maintenanceSpecName;

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

	public MaintenanceSpecKey()
	{
		
	}
	
	public MaintenanceSpecKey(String maintGroupName, String maintSpecName)
	{
		this.maintenanceGroupName = maintGroupName;
		this.maintenanceSpecName = maintSpecName;
	}
}
