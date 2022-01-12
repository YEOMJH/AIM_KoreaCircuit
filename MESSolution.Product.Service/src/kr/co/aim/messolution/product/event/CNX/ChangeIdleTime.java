package kr.co.aim.messolution.product.event.CNX;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ProductIdleTime;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import org.jdom.Document;
public class ChangeIdleTime extends SyncHandler{

	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		String Machinename = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String Productspecname  = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String Processoperationname = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);

		String Enable = SMessageUtil.getBodyItemValue(doc, "ENABLE", true);
		String AlarmTime = SMessageUtil.getBodyItemValue(doc, "ALARMTIME",false );
		String Lockflag = SMessageUtil.getBodyItemValue(doc, "LOCKFLAG", true);
		
		ProductIdleTime dataInfo = new ProductIdleTime();
		try
		{
			dataInfo = ExtendedObjectProxy.getProductIdleTimeService().selectByKey(false, new Object[]{Machinename,Productspecname,Processoperationname});
		}
		catch(Exception e)
		{
			throw new CustomException("SYS-8001", e.getMessage());
		}

		dataInfo.setENABLE(Enable);
		
		dataInfo.setALARMTIME(Double.parseDouble(AlarmTime));
		dataInfo.setLOCKFLAG(Lockflag);
		dataInfo.setEVENTUSER(getEventUser());
		dataInfo.setEVENTCOMMENT(getEventComment());
	
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Modify", getEventUser(), getEventComment(), "", "");

		
		try
		{
        	ExtendedObjectProxy.getProductIdleTimeService().modify(eventInfo,dataInfo);
		}
		catch(Exception e)
		{
			throw new CustomException("SYS-8001", e.getMessage());
		}
	
		return doc;
	}

}
