package kr.co.aim.messolution.pms.management.data;

import kr.co.aim.greenframe.orm.info.KeyInfo;
import kr.co.aim.greenframe.orm.info.access.FieldAccessor;

public class MaintenanceOrderKey extends FieldAccessor implements KeyInfo {
	
	private String maintenanceOrderName;

	public String getMaintenanceOrderName() {
		return maintenanceOrderName;
	}

	public void setMaintenanceOrderName(String maintenanceOrderName) {
		this.maintenanceOrderName = maintenanceOrderName;
	}
	
	public MaintenanceOrderKey()
	{
		
	}
	
	public MaintenanceOrderKey(String maintOrderName)
	{
		this.maintenanceOrderName = maintOrderName;
	}
}
