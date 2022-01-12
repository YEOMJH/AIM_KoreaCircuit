package kr.co.aim.messolution.pms.management.data;

import kr.co.aim.greenframe.orm.info.DataInfo;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class MaintenanceOrderItem extends UdfAccessor implements
		DataInfo<MaintenanceOrderItemKey> {

	private MaintenanceOrderItemKey key;
	
	@Override
	public MaintenanceOrderItemKey getKey() {
		return this.key;
	}

	@Override
	public void setKey(MaintenanceOrderItemKey keyInfo) {
		this.key = keyInfo;
	}

	private String maintenanceOrderName;
	private String position;
	private String maintenanceItem;
	private String maintenanceFlag;
	private String maintenanceNote;

	public String getMaintenanceOrderName() {
		return maintenanceOrderName;
	}

	public void setMaintenanceOrderName(String maintenanceOrderName) {
		this.maintenanceOrderName = maintenanceOrderName;
	}
	
	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}
	
	public String getMaintenanceItem() {
		return maintenanceItem;
	}

	public void setMaintenanceItem(String maintenanceItem) {
		this.maintenanceItem = maintenanceItem;
	}

	public String getMaintenanceFlag() {
		return maintenanceFlag;
	}

	public void setMaintenanceFlag(String maintenanceFlag) {
		this.maintenanceFlag = maintenanceFlag;
	}

	public String getMaintenanceNote() {
		return maintenanceNote;
	}

	public void setMaintenanceNote(String maintenanceNote) {
		this.maintenanceNote = maintenanceNote;
	}
	
	public MaintenanceOrderItem()
	{
		
	}
	
	public MaintenanceOrderItem(MaintenanceOrderItemKey keyInfo)
	{
		this.key = keyInfo;
	}
}
