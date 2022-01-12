package kr.co.aim.messolution.durable.event;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class UnScrapCarrier extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String messageName = SMessageUtil.getMessageName(doc);
		String sDurableName = SMessageUtil.getBodyItemValue(doc, "DURABLENAME", true);
		String sReasonCodeType = SMessageUtil.getBodyItemValue(doc, "REASONCODETYPE", false);
		String sReasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODE", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo(messageName, getEventUser(), getEventComment(), sReasonCodeType, sReasonCode);

		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sDurableName);
		MESDurableServiceProxy.getDurableServiceImpl().makeUnScrap(durableData, eventInfo);

		return doc;
	}

}
