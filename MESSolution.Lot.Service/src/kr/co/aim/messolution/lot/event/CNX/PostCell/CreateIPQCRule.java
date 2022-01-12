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

public class CreateIPQCRule extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String ruleName = SMessageUtil.getBodyItemValue(doc, "RULENAME", true);
		String ruleType = SMessageUtil.getBodyItemValue(doc, "RULETYPE", true);
		String maxQuantity = SMessageUtil.getBodyItemValue(doc, "MAXQUANTITY", true);
		String minQuantity = SMessageUtil.getBodyItemValue(doc, "MINQUANTITY", true);
		String panelQuantity = SMessageUtil.getBodyItemValue(doc, "PANELQUANTITY", true);
		String allowNGQuantity = SMessageUtil.getBodyItemValue(doc, "ALLOWNGQUANTITY", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", getEventUser(), null, null, null);
		String timeKey = TimeUtils.getCurrentEventTimeKey();
		eventInfo.setEventTimeKey(timeKey);
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
		
		String seq = ExtendedObjectProxy.getIPQCRuleService().getIPQCRuleSeq();
		
		IPQCRule ipqcRule = new IPQCRule();	
		ipqcRule.setSeq(Long.parseLong(seq));
		ipqcRule.setRuleName(ruleName);
		ipqcRule.setRuleType(ruleType);
		ipqcRule.setMaxQuantity(maxQuantity);
		ipqcRule.setMinQuantity(minQuantity);
		ipqcRule.setPanelQuantity(Integer.parseInt(panelQuantity));
		ipqcRule.setAllowNGQuantity(Integer.parseInt(allowNGQuantity));
		
		ExtendedObjectProxy.getIPQCRuleService().create(eventInfo, ipqcRule);
		
		return doc;
	}
}
