package kr.co.aim.messolution.extended.object.management.data;

import kr.co.aim.greenframe.orm.info.KeyInfo;
import kr.co.aim.greenframe.orm.info.access.FieldAccessor;

public class TestDataKey  extends FieldAccessor implements KeyInfo  {
	
	private String maskLotName;
	
	public TestDataKey(){};
	public TestDataKey(String maskLotName)
	{
		
		this.maskLotName = maskLotName;
	}
	public String getMaskLotName()
	{
		return maskLotName;
	}
	public void setMaskLotName(String maskLotName)
	{
		this.maskLotName = maskLotName;
	}
	
	
}