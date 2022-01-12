package kr.co.aim.messolution.lot.event.CNX.PostCell;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.FQCRule;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;

public class DeleteFQCRule extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String seq = SMessageUtil.getBodyItemValue(doc, "SEQ", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeleteFQCRule", getEventUser(), null, null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		FQCRule fqcRule = ExtendedObjectProxy.getFQCRuleService().getFQCRuleData(Long.parseLong(seq));
		fqcRule.setLastEventName(eventInfo.getEventName());
		fqcRule.setLastEventUser(eventInfo.getEventUser());
		fqcRule.setLastEventTime(eventInfo.getEventTime());
		fqcRule.setLastEventComment(eventInfo.getEventComment());
		fqcRule.setLastEventTimekey(eventInfo.getEventTimeKey());
		
		ExtendedObjectProxy.getFQCRuleService().remove(eventInfo, fqcRule);

		return doc;
	}
}
