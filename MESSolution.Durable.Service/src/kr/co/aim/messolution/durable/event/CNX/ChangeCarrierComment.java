package kr.co.aim.messolution.durable.event.CNX;

import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class ChangeCarrierComment extends SyncHandler {
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String durableName = SMessageUtil.getBodyItemValue(doc, "DURABLENAME", true);
		//String department = SMessageUtil.getBodyItemValue(doc, "DEPARTMENT", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeCSTComment", this.getEventUser(), this.getEventComment(), "", "");
		
		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableName);
		
		SetEventInfo setEventInfo = new SetEventInfo();
		//setEventInfo.getUdfs().put("DEPARTMENT", department);
		
		MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo , eventInfo);
		
		return doc;
	}
}
