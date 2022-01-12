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

public class CreateFQCRule extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String ruleName = SMessageUtil.getBodyItemValue(doc, "RULENAME", true);
		String ruleType = SMessageUtil.getBodyItemValue(doc, "RULETYPE", true);
		String maxQuantity = SMessageUtil.getBodyItemValue(doc, "MAXQUANTITY", true);
		String minQuantity = SMessageUtil.getBodyItemValue(doc, "MINQUANTITY", true);
		String panelQuantity = SMessageUtil.getBodyItemValue(doc, "PANELQUANTITY", true);
		String allowNGQuantity = SMessageUtil.getBodyItemValue(doc, "ALLOWNGQUANTITY", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateFQCRule", getEventUser(), null, null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		String seq = ExtendedObjectProxy.getFQCRuleService().getFQCRuleSeq();

		FQCRule fqcRule = new FQCRule();
		fqcRule.setSeq(Long.parseLong(seq));
		fqcRule.setRuleName(ruleName);
		fqcRule.setRuleType(ruleType);
		fqcRule.setMaxQuantity(maxQuantity);
		fqcRule.setMinQuantity(minQuantity);
		fqcRule.setPanelQuantity(Integer.parseInt(panelQuantity));
		fqcRule.setAllowNGQuantity(Integer.parseInt(allowNGQuantity));
		fqcRule.setLastEventName(eventInfo.getEventName());
		fqcRule.setLastEventUser(eventInfo.getEventUser());
		fqcRule.setLastEventTime(eventInfo.getEventTime());
		fqcRule.setLastEventComment(eventInfo.getEventComment());
		fqcRule.setLastEventTimekey(eventInfo.getEventTimeKey());

		ExtendedObjectProxy.getFQCRuleService().create(eventInfo, fqcRule);

		return doc;
	}
}
