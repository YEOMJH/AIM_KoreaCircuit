package kr.co.aim.messolution.pms.management.data;

import kr.co.aim.greenframe.orm.info.DataInfo;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class MaintenanceGroup extends UdfAccessor implements
		DataInfo<MaintenanceGroupKey> {
	
	private MaintenanceGroupKey key;
	
	@Override
	public MaintenanceGroupKey getKey() {
		return key;
	}

	@Override
	public void setKey(MaintenanceGroupKey keyInfo) {
		this.key = keyInfo;
	}
	
	private String description;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public MaintenanceGroup()
	{
		
	}
	
	public MaintenanceGroup(MaintenanceGroupKey keyInfo)
	{
		this.key = keyInfo;
	}
}
