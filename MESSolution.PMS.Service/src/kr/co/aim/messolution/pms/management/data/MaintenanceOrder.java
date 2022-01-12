package kr.co.aim.messolution.pms.management.data;

import java.sql.Timestamp;

import kr.co.aim.greenframe.orm.info.DataInfo;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class MaintenanceOrder extends UdfAccessor implements
		DataInfo<MaintenanceOrderKey> {

	private MaintenanceOrderKey key;
	
	@Override
	public MaintenanceOrderKey getKey() {
		return this.key;
	}

	@Override
	public void setKey(MaintenanceOrderKey keyInfo) {
		this.key = keyInfo;
	}

	private String maintenanceOrderName;
	private String machineName;
	private String maintenanceGroupName;
	private String maintenanceSpecName;
	private String maintenanceType;
	private Timestamp planStartTime;
	private Timestamp planEndTime;
	private String maintenanceOrderState;
	private Timestamp createTime;
	private String createUser;
	private Timestamp orderTime;
	private String orderUser;
	private Timestamp startTime;
	private String startUser;
	private Timestamp endTime;
	private String endUser;

	public String getMaintenanceOrderName() {
		return maintenanceOrderName;
	}

	public void setMaintenanceOrderName(String maintenanceOrderName) {
		this.maintenanceOrderName = maintenanceOrderName;
	}
	
	public String getMachineName() {
		return machineName;
	}

	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}

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

	public String getMaintenanceType() {
		return maintenanceType;
	}

	public void setMaintenanceType(String maintenanceType) {
		this.maintenanceType = maintenanceType;
	}

	public Timestamp getPlanStartTime() {
		return planStartTime;
	}

	public void setPlanStartTime(Timestamp planStartTime) {
		this.planStartTime = planStartTime;
	}

	public Timestamp getPlanEndTime() {
		return planEndTime;
	}

	public void setPlanEndTime(Timestamp planEndTime) {
		this.planEndTime = planEndTime;
	}

	public String getMaintenanceOrderState() {
		return maintenanceOrderState;
	}

	public void setMaintenanceOrderState(String maintenanceOrderState) {
		this.maintenanceOrderState = maintenanceOrderState;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public Timestamp getOrderTime() {
		return orderTime;
	}

	public void setOrderTime(Timestamp orderTime) {
		this.orderTime = orderTime;
	}

	public String getOrderUser() {
		return orderUser;
	}

	public void setOrderUser(String orderUser) {
		this.orderUser = orderUser;
	}

	public Timestamp getStartTime() {
		return startTime;
	}

	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}

	public String getStartUser() {
		return startUser;
	}

	public void setStartUser(String startUser) {
		this.startUser = startUser;
	}

	public Timestamp getEndTime() {
		return endTime;
	}

	public void setEndTime(Timestamp endTime) {
		this.endTime = endTime;
	}

	public String getEndUser() {
		return endUser;
	}

	public void setEndUser(String endUser) {
		this.endUser = endUser;
	}

	public MaintenanceOrder()
	{
		
	}
	
	public MaintenanceOrder(MaintenanceOrderKey keyInfo)
	{
		this.key = keyInfo;
	}
}
