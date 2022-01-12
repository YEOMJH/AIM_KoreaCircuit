package kr.co.aim.messolution.extended.object.management.data;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;

public class MachineAlarmProductList extends UdfAccessor{

	@CTORMTemplate(seq = "1", name="alarmTimeKey", type="Key", dataType="String", initial="", history="")
	private String alarmTimeKey;
	
	@CTORMTemplate(seq = "2", name="productName", type="Key", dataType="String", initial="", history="")
	private String productName;
	
	@CTORMTemplate(seq = "3", name="maskName", type="Column", dataType="String", initial="", history="")
	private String maskName;
	@CTORMTemplate(seq = "4", name="ProcessOperationName", type="Column", dataType="String", initial="", history="")
	private String ProcessOperationName;
	
	public String getProcessOperationName() {
		return ProcessOperationName;
	}

	public void setProcessOperationName(String processOperationName) {
		ProcessOperationName = processOperationName;
	}

	//instantiation
	public MachineAlarmProductList()
	{
		
	}
	
	//instantiation
	public MachineAlarmProductList(String alarmTimeKey, String productName)
	{
         setAlarmTimeKey(alarmTimeKey);
         setProductName(productName);
	}

	public String getAlarmTimeKey() {
		return alarmTimeKey;
	}

	public void setAlarmTimeKey(String alarmTimeKey) {
		this.alarmTimeKey = alarmTimeKey;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getMaskName() {
		return maskName;
	}

	public void setMaskName(String maskName) {
		this.maskName = maskName;
	}
	
}
