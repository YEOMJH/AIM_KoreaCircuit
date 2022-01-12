package kr.co.aim.messolution.durable.event.CNX;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class ChangeOLEDMaskLotStockerName extends SyncHandler {

	public Object doWorks(Document doc) throws CustomException
	{
		String stockerName = SMessageUtil.getBodyItemValue(doc, "STOCKERNAME", true);
		List<Element> MaskList = SMessageUtil.getBodySequenceItemList(doc, "MASKLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeOLEDMaskLotStockerName", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setLastEventTimekey(TimeUtils.getCurrentTime());

		for (Element Mask : MaskList)
		{
			String maskLotName = SMessageUtil.getChildText(Mask, "MASKLOTNAME", true);
			MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().selectByKey(true, new Object[] { maskLotName });
			
			if(StringUtil.equals(maskLotData.getMaskLotProcessState(), "RUN"))
			{					
				throw new CustomException("MASK-0091", maskLotName);
			}
			
			if(StringUtil.equals(maskLotData.getMaskLotHoldState(), "Y"))
			{					
				throw new CustomException("MASK-0092", maskLotName);
			}
			
			// Set Mask Info
			maskLotData.setStockerName(stockerName);
			maskLotData.setLastEventName(eventInfo.getEventName());
			maskLotData.setLastEventTime(eventInfo.getEventTime());
			maskLotData.setLastEventTimeKey(eventInfo.getLastEventTimekey());
			maskLotData.setLastEventUser(eventInfo.getEventUser());
			maskLotData.setLastEventComment(eventInfo.getEventComment());

			// update mask info and history
			ExtendedObjectProxy.getMaskLotService().modify(eventInfo, maskLotData);

		}

		return doc;
	}
}
