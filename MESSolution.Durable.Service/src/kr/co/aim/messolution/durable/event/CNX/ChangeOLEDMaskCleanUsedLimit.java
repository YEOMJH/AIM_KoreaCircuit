package kr.co.aim.messolution.durable.event.CNX;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class ChangeOLEDMaskCleanUsedLimit extends SyncHandler {
	@Override
	public Object doWorks(Document doc) throws CustomException

	{

		List<Element> MaskList = SMessageUtil.getBodySequenceItemList(doc, "MASKLIST", true);
		String cleanUsedLimit = SMessageUtil.getBodyItemValue(doc, "CLEANUSEDLIMIT", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeOLEDMaskCleanUsedLimit", this.getEventUser(), this.getEventUser(), "", "", "Y");
		eventInfo.setLastEventTimekey(TimeStampUtil.getCurrentEventTimeKey());

		for (Element Mask : MaskList)
		{
			String maskLotName = SMessageUtil.getChildText(Mask, "MASKLOTNAME", true);
			MaskLot maskLotInfo = ExtendedObjectProxy.getMaskLotService().selectByKey(true, new Object[] { maskLotName });
			
			CommonValidation.checkMaskLotHoldState(maskLotInfo);
			CommonValidation.checkMaskLotProcessStateRun(maskLotInfo);
			CommonValidation.checkMaskLotState(maskLotInfo);
			
			if(Float.parseFloat(cleanUsedLimit) < Float.parseFloat(maskLotInfo.getMaskCleanCount().toString())){
				throw new CustomException("MASK-0088",maskLotName);
			}
			
			if(Float.parseFloat(cleanUsedLimit) == Float.parseFloat(maskLotInfo.getCleanUsedLimit().toString())){
				throw new CustomException("MASK-0087",maskLotName);
			}
			
			maskLotInfo.setCleanUsedLimit(Float.parseFloat(cleanUsedLimit));
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
