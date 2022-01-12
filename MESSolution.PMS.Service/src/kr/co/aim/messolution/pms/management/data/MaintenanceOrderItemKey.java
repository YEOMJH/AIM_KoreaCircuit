package kr.co.aim.messolution.pms.management.data;

import kr.co.aim.greenframe.orm.info.KeyInfo;
import kr.co.aim.greenframe.orm.info.access.FieldAccessor;

public class MaintenanceOrderItemKey extends FieldAccessor implements KeyInfo {
	
	private String maintenanceOrderName;
	private long position;
	
	public String getMaintenanceOrderName() {
		return maintenanceOrderName;
	}
	public void setMaintenanceOrderName(String maintenanceOrderName) {
		this.maintenanceOrderName = maintenanceOrderName;
	}
	public long getPosition() {
		return position;
	}
	public void setPosition(long position) {
		this.position = position;
	}
	
	public MaintenanceOrderItemKey()
	{
		
	}
	
	public MaintenanceOrderItemKey(String maintOrderName, long pos)
	{
		this.maintenanceOrderName = maintOrderName;
		this.position = pos;
	}
}
