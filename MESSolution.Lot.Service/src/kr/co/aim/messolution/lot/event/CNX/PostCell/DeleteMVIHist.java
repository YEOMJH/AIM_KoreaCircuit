package kr.co.aim.messolution.lot.event.CNX.PostCell;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class DeleteMVIHist extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String eventUser = SMessageUtil.getBodyItemValue(doc, "EVENTUSER", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeleteMVIUserHist", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(ConvertUtil.getCurrTimeKey());
		
		ExtendedObjectProxy.getMVIUserDefectService().deleteMVIUserDefectHistData(eventInfo, eventUser);
		
		return doc;
	}
	
}