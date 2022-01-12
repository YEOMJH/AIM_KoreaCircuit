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
import kr.co.aim.greentrack.generic.util.StringUtil;

public class ChangeOLEDMaskTimeUsed extends SyncHandler {
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		Element eleBody = SMessageUtil.getBodyElement(doc);

		if (eleBody != null)
		{
			String maskLotName = SMessageUtil.getBodyItemValue(doc, "MASKLOTNAME", true);
			String timeUsed = SMessageUtil.getBodyItemValue(doc, "TIMEUSED", true);
			
			

			EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeTimeUsedMask", this.getEventUser(), this.getEventComment(), "", "", "Y");
			eventInfo.setLastEventTimekey(TimeStampUtil.getCurrentEventTimeKey());
			MaskLot maskLotInfo = ExtendedObjectProxy.getMaskLotService().selectByKey(true, new Object[] { maskLotName });
			
			if(Float.parseFloat(timeUsed)== maskLotInfo.getTimeUsed()){
				throw new CustomException("MASK-0094",maskLotInfo.getMaskLotName());
			}
			maskLotInfo.setTimeUsed(Float.parseFloat(timeUsed));
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
