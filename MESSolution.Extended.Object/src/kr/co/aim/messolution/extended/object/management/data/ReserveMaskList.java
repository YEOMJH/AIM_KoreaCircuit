package kr.co.aim.messolution.extended.object.management.data;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class ReserveMaskList extends UdfAccessor {
	
	@CTORMTemplate(seq = "1", name="machineName", type="Key", dataType="String", initial="", history="")
	private String machineName;
	
	@CTORMTemplate(seq = "2", name="portName", type="Key", dataType="String", initial="", history="")
	private String portName;
	
	@CTORMTemplate(seq = "3", name="maskName", type="Key", dataType="String", initial="", history="")
	private String maskName;
	
	@CTORMTemplate(seq = "4", name="carrierName", type="Column", dataType="String", initial="", history="")
	private String carrierName;
	
	@CTORMTemplate(seq = "5", name="position", type="Column", dataType="String", initial="", history="")
	private String position;
	
	@CTORMTemplate(seq = "6", name="maskCleanRecipe", type="Column", dataType="String", initial="", history="")
	private String maskCleanRecipe;
	
	@CTORMTemplate(seq = "7", name="carrierType", type="Column", dataType="String", initial="", history="")
	private String carrierType;
	
	@CTORMTemplate(seq = "8", name="subUnitName", type="Column", dataType="String", initial="", history="")
	private String subUnitName;
	
	@CTORMTemplate(seq = "9", name="sslotNo", type="Column", dataType="String", initial="", history="")
	private String sslotNo;
	
	@CTORMTemplate(seq = "10", name="maskThickNess", type="Column", dataType="String", initial="", history="")
	private String maskThickNess;
	
	@CTORMTemplate(seq = "11", name="maskMagnet", type="Column", dataType="String", initial="", history="")
	private String maskMagnet;
	
	@CTORMTemplate(seq = "11", name="OffSet_X", type="Column", dataType="String", initial="", history="")
	private String OffSet_X;
	
	@CTORMTemplate(seq = "12", name="OffSet_Y", type="Column", dataType="String", initial="", history="")
	private String OffSet_Y;
	
	@CTORMTemplate(seq = "13", name="OffSet_T", type="Column", dataType="String", initial="", history="")
	private String OffSet_T;
	@CTORMTemplate(seq = "14", name="MASKSPEC", type="Column", dataType="String", initial="", history="")
	private String MASKSPEC;
	
	public String getMachineName() {
		return machineName;
	}
	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}
	public String getPortName() {
		return portName;
	}
	public void setPortname(String portname) {
		this.portName = portname;
	}
	
	public String getMaskName() {
		return maskName;
	}
	public void setMaskName(String maskName) {
		this.maskName = maskName;
	}
	
	public String getCarrierName() {
		return carrierName;
	}
	public void setCarrierName(String carrierName) {
		this.carrierName = carrierName;
	}
	public String getPosition() {
		return position;
	}
	public void setPosition(String position) {
		this.position = position;
	}

	public String getMaskCleanRecipe() {
		return maskCleanRecipe;
	}
	public void setMaskCleanRecipe(String maskCleanRecipe) {
		this.maskCleanRecipe = maskCleanRecipe;
	}
	
	public String getCarrierType() {
		return carrierType;
	}
	public String getMASKSPEC() {
		return MASKSPEC;
	}
	public void setMASKSPEC(String maskspec) {
		MASKSPEC = maskspec;
	}
	public void setCarrierType(String carrierType) {
		this.carrierType = carrierType;
	}
	
	public String getSubUnitName() {
		return subUnitName;
	}
	public void setSubUnitName(String subUnitName) {
		this.subUnitName = subUnitName;
	}

	public String getSSlotNo() {
		return sslotNo;
	}
	public void setSSlotNo(String sslotNo) {
		this.sslotNo = sslotNo;
	}
	
	public String getMaskThickNess() {
		return maskThickNess;
	}
	public void setMaskThickNess(String maskThickNess) {
		this.maskThickNess = maskThickNess;
	}
	
	public String getMaskMagnet() {
		return maskMagnet;
	}
	public void setMaskMagnet(String maskMagnet) {
		this.maskMagnet = maskMagnet;
	}
	
	public String getOffSet_X() {
		return OffSet_X;
	}
	public void setOffSet_X(String OffSet_X) {
		this.OffSet_X = OffSet_X;
	}
	
	public String getOffSet_Y() {
		return OffSet_Y;
	}
	public void setOffSet_Y(String OffSet_Y) {
		this.OffSet_Y = OffSet_Y;
	}
	
	public String getOffSet_T() {
		return OffSet_T;
	}
	public void setOffSet_T(String OffSet_T) {
		this.OffSet_T = OffSet_T;
	}
	
	//instantiation
	public ReserveMaskList()
	{
		
	}
	
	public ReserveMaskList(String machineName, String portName,String maskName)
	{
		setMachineName(machineName);
		setPortname(portName);
		setMaskName(maskName);

	}
	public ReserveMaskList(String maskName)
	{
		setMaskName(maskName);
	}
}
