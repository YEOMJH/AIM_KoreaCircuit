package kr.co.aim.messolution.extended.object.management.data;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class MaskReserveSpec extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="machineName", type="Key", dataType="String", initial="", history="")
	private String machineName;
	
	@CTORMTemplate(seq = "2", name="unitName", type="Key", dataType="String", initial="", history="")
	private String unitName;
	
	@CTORMTemplate(seq = "3", name="subUnitName", type="Key", dataType="String", initial="", history="")
	private String subUnitName;
	
	@CTORMTemplate(seq = "4", name="durableSpecName", type="Key", dataType="String", initial="", history="")
	private String durableSpecName;
	
	@CTORMTemplate(seq = "5", name="maskUseCount", type="Column", dataType="String", initial="", history="")
	private long maskUseCount;
	
	@CTORMTemplate(seq = "6", name="scale", type="Column", dataType="String", initial="", history="")
	private long scale;
	
	
	public MaskReserveSpec()
	{
		
	}
	
	public MaskReserveSpec(String machineName, String unitName, String subUnitName, String durableSpecName)
	{
		setMachineName(machineName);
		setUnitName(unitName);
		setSubUnitName(subUnitName);
		setDurableSpecName(durableSpecName);
	}

	
	public String getMachineName() {
		return machineName;
	}

	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}

	public String getUnitName() {
		return unitName;
	}

	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}

	public String getSubUnitName() {
		return subUnitName;
	}

	public void setSubUnitName(String subUnitName) {
		this.subUnitName = subUnitName;
	}

	public String getDurableSpecName() {
		return durableSpecName;
	}

	public void setDurableSpecName(String durableSpecName) {
		this.durableSpecName = durableSpecName;
	}

	public long getMaskUseCount() {
		return maskUseCount;
	}

	public void setMaskUseCount(long maskUseCount) {
		this.maskUseCount = maskUseCount;
	}

	public long getScale() {
		return scale;
	}

	public void setScale(long scale) {
		this.scale = scale;
	}
}
