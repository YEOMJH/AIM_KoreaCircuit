package kr.co.aim.messolution.durable.event.CNX;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class ChangeOLEDMaskLotState extends SyncHandler {
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		Element eleBody = SMessageUtil.getBodyElement(doc);

		if (eleBody != null)
		{
			String maskLotName = SMessageUtil.getBodyItemValue(doc, "MASKLOTNAME", true);
			String maskLotState = SMessageUtil.getBodyItemValue(doc, "MASKLOTSTATE", true);

			EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeStateMask", this.getEventUser(), this.getEventComment(), "", "", "Y");
			eventInfo.setLastEventTimekey(TimeStampUtil.getCurrentEventTimeKey());
			MaskLot maskLotInfo = ExtendedObjectProxy.getMaskLotService().selectByKey(true, new Object[] { maskLotName });
			maskLotInfo.setMaskLotState(maskLotState);
			maskLotInfo.setReasonCode(null);
			maskLotInfo.setReasonCodeType(null);
			maskLotInfo.setLastEventName(eventInfo.getEventName());
			maskLotInfo.setLastEventTime(eventInfo.getEventTime());
			maskLotInfo.setLastEventTimeKey(eventInfo.getLastEventTimekey());
			maskLotInfo.setLastEventUser(eventInfo.getEventUser());
			maskLotInfo.setLastEventComment(eventInfo.getEventComment());
			ExtendedObjectProxy.getMaskLotService().modify(eventInfo, maskLotInfo);
		}
		return doc;
	}

}
