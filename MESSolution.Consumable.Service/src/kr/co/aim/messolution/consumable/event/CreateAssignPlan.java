package kr.co.aim.messolution.consumable.event;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.CrucibleLot;
import kr.co.aim.messolution.extended.object.management.data.OrganicMapping;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;

public class CreateAssignPlan extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String crucibleName = SMessageUtil.getBodyItemValue(doc, "CRUCIBLENAME", true);
		String crucibleLotName = SMessageUtil.getBodyItemValue(doc, "CRUCIBLELOTNAME", true);
		String planWeight = SMessageUtil.getBodyItemValue(doc, "PLANWEIGHT", false);
		String planOrganicSpec = SMessageUtil.getBodyItemValue(doc, "PLANORGANICSPEC", false);
		String kitQtime = SMessageUtil.getBodyItemValue(doc, "KITQTIME", false);
		
		CrucibleLot crucibleLot = ExtendedObjectProxy.getCrucibleLotService().selectByKey(true, new Object[] { crucibleLotName });		
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ModifyAssignPlan", getEventUser(), getEventComment(), null, null);

		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		crucibleLot.setDurableName(crucibleName);
		crucibleLot.setOldDurableName(crucibleName);
		crucibleLot.setPlanWeight(planWeight.isEmpty()?0.0d:Double.valueOf(planWeight));
		crucibleLot.setPlanOrganicSpec(planOrganicSpec);
		crucibleLot.setKitQtime(kitQtime.isEmpty()?0.0d:Double.valueOf(kitQtime));
		crucibleLot.setLastEventComment(eventInfo.getEventComment());
		crucibleLot.setLastEventName(eventInfo.getEventName());
		crucibleLot.setLastEventTime(eventInfo.getEventTime());
		crucibleLot.setLastEventTimekey(eventInfo.getEventTimeKey());
		crucibleLot.setLastEventUser(eventInfo.getEventUser());
		ExtendedObjectProxy.getCrucibleLotService().modify(eventInfo, crucibleLot);		
		
		return doc;
	}
}
