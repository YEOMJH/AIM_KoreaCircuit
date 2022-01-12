package kr.co.aim.messolution.durable.event;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.MakeScrappedInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class ScrapCarrier extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String messageName = SMessageUtil.getMessageName(doc);

		String sDurableName = SMessageUtil.getBodyItemValue(doc, "DURABLENAME", true);
		String sReasonCodeType = SMessageUtil.getBodyItemValue(doc, "REASONCODETYPE", true);
		String sReasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODE", false);
		
		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sDurableName);
		CommonValidation.CheckDurableHoldState(durableData);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo(messageName, getEventUser(), getEventComment(), sReasonCodeType, sReasonCode);
		
		MakeScrappedInfo scrapInfo = MESDurableServiceProxy.getDurableInfoUtil().makeScrappedInfo(durableData);
		MESDurableServiceProxy.getDurableServiceImpl().makeScrapped(durableData, scrapInfo, eventInfo);

		return doc;
	}

}
