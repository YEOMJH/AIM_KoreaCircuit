package kr.co.aim.messolution.pms.management.data;

import kr.co.aim.greenframe.orm.info.DataInfo;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class MaintenanceSpecItem extends UdfAccessor implements
		DataInfo<MaintenanceSpecItemKey> {
	
	private MaintenanceSpecItemKey key;
	
	@Override
	public MaintenanceSpecItemKey getKey() {
		return key;
	}

	@Override
	public void setKey(MaintenanceSpecItemKey keyInfo) {
		this.key = keyInfo;
	}
	
	private String maintenanceItem;
	
	public MaintenanceSpecItem()
	{
		
	}
	
	public MaintenanceSpecItem(MaintenanceSpecItemKey keyInfo)
	{
		this.key = keyInfo;
	}

	public String getMaintenanceItem() {
		return maintenanceItem;
	}

	public void setMaintenanceItem(String maintenanceItem) {
		this.maintenanceItem = maintenanceItem;
	}
}
