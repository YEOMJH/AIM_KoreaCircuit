package kr.co.aim.messolution.durable.event.CNX;

import org.jdom.Document; 
import org.jdom.Element;
import java.util.List;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.extended.object.management.data.MaskSpec;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class ChangeMaskLotPriority extends SyncHandler {
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		Element eleBody = SMessageUtil.getBodyElement(doc);
		List<Element> MaskPriorityList = SMessageUtil.getBodySequenceItemList(doc, "MaskPriorityLIST", true);
		String changMode = SMessageUtil.getBodyItemValue(doc, "ChangeMode", true);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeMaskLotPriority", this.getEventUser(), this.getEventComment(), "", "", "Y");
		eventInfo.setLastEventTimekey(TimeStampUtil.getCurrentEventTimeKey());
		
		for (Element Priority : MaskPriorityList)
		{
			String priority = SMessageUtil.getChildText(Priority, "PRIORITY", false);
			String factoryName = SMessageUtil.getChildText(Priority, "FACTORYNAME", false);
			if(changMode.equals("MaskSpec"))
			{
				String maskSpecName = SMessageUtil.getChildText(Priority, "MASKSPECNAME", false);
				MaskSpec maskSpecInfo = ExtendedObjectProxy.getMaskSpecService().selectByKey(true, new Object[] {factoryName, maskSpecName });
				maskSpecInfo.setPriority(Integer.valueOf(priority));
				maskSpecInfo.setLastEventName(eventInfo.getEventName());
				maskSpecInfo.setLastEventTime(eventInfo.getEventTime());
				maskSpecInfo.setLastEventTimeKey(eventInfo.getLastEventTimekey());
				maskSpecInfo.setLastEventUser(eventInfo.getEventUser());
				maskSpecInfo.setLastEventComment(eventInfo.getEventComment());
				ExtendedObjectProxy.getMaskSpecService().modify(eventInfo, maskSpecInfo);
			}
			else if(changMode.equals("Mask"))
			{
				String maskLotName = SMessageUtil.getChildText(Priority, "MASKID", false);
				MaskLot maskLotInfo = ExtendedObjectProxy.getMaskLotService().selectByKey(true, new Object[] { maskLotName });
				maskLotInfo.setPriority(Integer.valueOf(priority));;
				maskLotInfo.setReasonCode(null);
				maskLotInfo.setReasonCodeType(null);
				maskLotInfo.setLastEventName(eventInfo.getEventName());
				maskLotInfo.setLastEventTime(eventInfo.getEventTime());
				maskLotInfo.setLastEventTimeKey(eventInfo.getLastEventTimekey());
				maskLotInfo.setLastEventUser(eventInfo.getEventUser());
				maskLotInfo.setLastEventComment(eventInfo.getEventComment());
				ExtendedObjectProxy.getMaskLotService().modify(eventInfo, maskLotInfo);
			}
		}
		return doc;
	}

}
