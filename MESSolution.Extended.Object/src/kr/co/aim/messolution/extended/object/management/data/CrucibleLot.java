package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class CrucibleLot extends UdfAccessor {
	@CTORMTemplate(seq = "1", name = "crucibleLotName", type = "Key", dataType = "String", initial = "", history = "")
	private String crucibleLotName;
	@CTORMTemplate(seq = "2", name = "durableName", type = "Column", dataType = "String", initial = "", history = "")
	private String durableName;
	@CTORMTemplate(seq = "3", name = "crucibleLotState", type = "Column", dataType = "String", initial = "", history = "")
	private String crucibleLotState;
	@CTORMTemplate(seq = "4", name = "weight", type = "Column", dataType = "Number", initial = "", history = "")
	private double weight;
	@CTORMTemplate(seq = "5", name = "createTime", type = "Column", dataType = "Timestamp", initial = "", history = "")
	private Timestamp createTime;
	@CTORMTemplate(seq = "6", name = "kitTime", type = "Column", dataType = "Timestamp", initial = "", history = "")
	private Timestamp kitTime;
	@CTORMTemplate(seq = "7", name = "unkitTime", type = "Column", dataType = "Timestamp", initial = "", history = "")
	private Timestamp unkitTime;
	@CTORMTemplate(seq = "8", name = "factoryName", type = "Column", dataType = "String", initial = "", history = "")
	private String factoryName;
	@CTORMTemplate(seq = "9", name = "machineName", type = "Column", dataType = "String", initial = "", history = "")
	private String machineName;
	@CTORMTemplate(seq = "10", name = "materialLocationName", type = "Column", dataType = "String", initial = "", history = "")
	private String materialLocationName;
	@CTORMTemplate(seq = "11", name = "lastEventName", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventName;
	@CTORMTemplate(seq = "12", name = "lastEventUser", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventUser;
	@CTORMTemplate(seq = "13", name = "lastEventTime", type = "Column", dataType = "Timestamp", initial = "", history = "N")
	private Timestamp lastEventTime;
	@CTORMTemplate(seq = "14", name = "lastEventTimekey", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventTimekey;
	@CTORMTemplate(seq = "15", name = "lastEventComment", type = "Column", dataType = "String", initial = "", history = "N")
	private String lastEventComment;
	@CTORMTemplate(seq = "16", name = "planWeight", type = "Column", dataType = "Number", initial = "", history = "")
	private double planWeight;
	@CTORMTemplate(seq = "17", name = "oldDurableName", type = "Column", dataType = "String", initial = "", history = "")
	private String oldDurableName;
	@CTORMTemplate(seq = "18", name = "planOrganicSpec", type = "Column", dataType = "String", initial = "", history = "")
	private String planOrganicSpec;
	@CTORMTemplate(seq = "19", name = "crucibleWeight", type = "Column", dataType = "Number", initial = "", history = "")
	private double crucibleWeight;
	@CTORMTemplate(seq = "20", name = "assignTime", type = "Column", dataType = "Timestamp", initial = "", history = "")
	private Timestamp assignTime;	
	@CTORMTemplate(seq = "21", name = "kitQtime", type = "Column", dataType = "Number", initial = "", history = "")
	private double kitQtime;
	@CTORMTemplate(seq = "22", name = "consumeRatio", type = "Column", dataType = "String", initial = "", history = "")
	private String consumeRatio;
	
	public double getCrucibleWeight() {
		return crucibleWeight;
	}

	public void setCrucibleWeight(double crucibleWeight) {
		this.crucibleWeight = crucibleWeight;
	}

	public String getPlanOrganicSpec() {
		return planOrganicSpec;
	}

	public void setPlanOrganicSpec(String planOrganicSpec) {
		this.planOrganicSpec = planOrganicSpec;
	}

	public String getCrucibleLotName()
	{
		return crucibleLotName;
	}

	public void setCrucibleLotName(String crucibleLotName)
	{
		this.crucibleLotName = crucibleLotName;
	}

	public String getDurableName()
	{
		return durableName;
	}

	public void setDurableName(String durableName)
	{
		this.durableName = durableName;
	}

	public String getCrucibleLotState()
	{
		return crucibleLotState;
	}

	public void setCrucibleLotState(String crucibleLotState)
	{
		this.crucibleLotState = crucibleLotState;
	}

	public double getWeight()
	{
		return weight;
	}

	public void setWeight(double weight)
	{
		this.weight = weight;
	}

	public Timestamp getCreateTime()
	{
		return createTime;
	}

	public void setCreateTime(Timestamp createTime)
	{
		this.createTime = createTime;
	}

	public Timestamp getKitTime()
	{
		return kitTime;
	}

	public void setKitTime(Timestamp kitTime)
	{
		this.kitTime = kitTime;
	}

	public Timestamp getUnkitTime()
	{
		return unkitTime;
	}

	public void setUnkitTime(Timestamp unkitTime)
	{
		this.unkitTime = unkitTime;
	}

	public String getFactoryName()
	{
		return factoryName;
	}

	public void setFactoryName(String factoryName)
	{
		this.factoryName = factoryName;
	}

	public String getMachineName()
	{
		return machineName;
	}

	public void setMachineName(String machineName)
	{
		this.machineName = machineName;
	}

	public String getMaterialLocationName()
	{
		return materialLocationName;
	}

	public void setMaterialLocationName(String materialLocationName)
	{
		this.materialLocationName = materialLocationName;
	}

	public String getLastEventName()
	{
		return lastEventName;
	}

	public void setLastEventName(String lastEventName)
	{
		this.lastEventName = lastEventName;
	}

	public String getLastEventUser()
	{
		return lastEventUser;
	}

	public void setLastEventUser(String lastEventUser)
	{
		this.lastEventUser = lastEventUser;
	}

	public Timestamp getLastEventTime()
	{
		return lastEventTime;
	}

	public void setLastEventTime(Timestamp lastEventTime)
	{
		this.lastEventTime = lastEventTime;
	}

	public String getLastEventTimekey()
	{
		return lastEventTimekey;
	}

	public void setLastEventTimekey(String lastEventTimekey)
	{
		this.lastEventTimekey = lastEventTimekey;
	}

	public String getLastEventComment()
	{
		return lastEventComment;
	}

	public void setLastEventComment(String lastEventComment)
	{
		this.lastEventComment = lastEventComment;
	}

	public double getPlanWeight()
	{
		return planWeight;
	}

	public void setPlanWeight(double planWeight)
	{
		this.planWeight = planWeight;
	}

	public String getOldDurableName()
	{
		return oldDurableName;
	}

	public void setOldDurableName(String oldDurableName)
	{
		this.oldDurableName = oldDurableName;
	}
	public Timestamp getAssignTime() {
		return assignTime;
	}

	public void setAssignTime(Timestamp assignTime) {
		this.assignTime = assignTime;
	}

	public double getKitQtime() {
		return kitQtime;
	}

	public void setKitQtime(double kitQtime) {
		this.kitQtime = kitQtime;
	}

	public String getConsumeRatio() {
		return consumeRatio;
	}

	public void setConsumeRatio(String consumeRatio) {
		this.consumeRatio = consumeRatio;
	}

}
