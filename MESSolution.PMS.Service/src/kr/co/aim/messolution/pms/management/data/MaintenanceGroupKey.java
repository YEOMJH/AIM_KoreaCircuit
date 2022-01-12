package kr.co.aim.messolution.pms.management.data;

import kr.co.aim.greenframe.orm.info.KeyInfo;
import kr.co.aim.greenframe.orm.info.access.FieldAccessor;

public class MaintenanceGroupKey extends FieldAccessor implements KeyInfo {
	
	private String maintenanceGroupName;

	public String getMaintenanceGroupName() {
		return maintenanceGroupName;
	}

	public void setMaintenanceGroupName(String maintenanceGroupName) {
		this.maintenanceGroupName = maintenanceGroupName;
	}
	
	public MaintenanceGroupKey()
	{
		
	}
	
	public MaintenanceGroupKey(String maintGroupName)
	{
		this.maintenanceGroupName = maintGroupName;
	}
}
