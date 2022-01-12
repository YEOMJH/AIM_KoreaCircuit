package kr.co.aim.messolution.lot.event.CNX.PostCell;

import org.jdom.Document;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.IPQCRule;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class DeleteIPQCRule extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String seq = SMessageUtil.getBodyItemValue(doc, "SEQ", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Delete", getEventUser(), null, null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
		
		IPQCRule rule = ExtendedObjectProxy.getIPQCRuleService().selectByKey(false, new Object[] {Long.parseLong(seq)});
		
		ExtendedObjectProxy.getIPQCRuleService().remove(eventInfo, rule);
		
		return doc;
	}
}
