package kr.co.aim.messolution.extended.object.management.data;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class MachineGroup extends UdfAccessor{
	
	@CTORMTemplate(seq = "1", name="machineGroupName", type="Key", dataType="String", initial="", history="")
	private String machineGroupName;
	
	@CTORMTemplate(seq = "2", name="machineName", type="Key", dataType="String", initial="", history="")
	private String machineName;
	
	@CTORMTemplate(seq = "3", name="category", type="Column", dataType="String", initial="", history="")
	private String category;
	
	@CTORMTemplate(seq = "4", name="priority", type="Column", dataType="Long", initial="", history="")
	private long priority;
	
	@CTORMTemplate(seq = "5", name="description", type="Column", dataType="String", initial="", history="")
	private String description;
	
	public MachineGroup()
	{
		
	}
	
	public MachineGroup(String machineGroupName, String machineName)
	{
		this.machineGroupName = machineGroupName;
		this.machineName = machineName;
	}

	public String getMachineGroupName() {
		return machineGroupName;
	}

	public void setMachineGroupName(String machineGroupName) {
		this.machineGroupName = machineGroupName;
	}

	public String getMachineName() {
		return machineName;
	}

	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public long getPriority() {
		return priority;
	}

	public void setPriority(long priority) {
		this.priority = priority;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
