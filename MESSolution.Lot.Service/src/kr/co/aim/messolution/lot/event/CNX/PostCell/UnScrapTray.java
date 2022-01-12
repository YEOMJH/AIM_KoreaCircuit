package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableHistory;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class UnScrapTray extends SyncHandler {

	@Override
    public Object doWorks(Document doc) throws CustomException
	{
		// Get Doc
		List<Element> trayList = SMessageUtil.getBodySequenceItemList(doc, "TRAYLIST", false);
		String reasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODE", true);
		String reasonCodeType = SMessageUtil.getBodyItemValue(doc, "REASONCODETYPE", true);

		// EventInfo
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("UnScrapTray", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
        
		for(Element element : trayList) 
		{
			String trayName = element.getChildText("DURABLENAME");
			Durable durableInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayName);
			Durable olddurableInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayName);
			
			CommonValidation.CheckScrapedDurableState(durableInfo);
						
			durableInfo.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);
			durableInfo.setReasonCode(reasonCode); 
			durableInfo.setReasonCodeType(reasonCodeType); 
			durableInfo.setLastEventName(eventInfo.getEventName());
			durableInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
			durableInfo.setLastEventTime(eventInfo.getEventTime());
			durableInfo.setLastEventUser(eventInfo.getEventUser());
			durableInfo.setLastEventComment(eventInfo.getEventComment());
						
			DurableHistory durHistory = new DurableHistory();
			durHistory = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(olddurableInfo, durableInfo, durHistory);
			
//			DurableServiceProxy.getDurableService().delete(olddurableInfo.getKey());
			
			DurableServiceProxy.getDurableService().update(durableInfo);
			
			DurableServiceProxy.getDurableHistoryService().insert(durHistory);
		}
		return doc;
	}
	
}
