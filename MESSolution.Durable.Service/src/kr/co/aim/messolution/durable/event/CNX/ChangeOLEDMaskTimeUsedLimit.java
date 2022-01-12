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

public class ChangeOLEDMaskTimeUsedLimit extends SyncHandler {
	@Override
	public Object doWorks(Document doc) throws CustomException

	{

		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String maskSpecName = SMessageUtil.getBodyItemValue(doc, "MASKSPECNAME", true);
		List<Element> MaskList = SMessageUtil.getBodySequenceItemList(doc, "MASKLIST", true);
		String timeUsedLimit = SMessageUtil.getBodyItemValue(doc, "TIMEUSEDLIMIT", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeOLEDMaskTimeUsedLimit", this.getEventUser(), this.getEventComment(), "", "", "Y");
		eventInfo.setLastEventTimekey(TimeStampUtil.getCurrentEventTimeKey());

		List<String> maskList = new ArrayList<String>();
		for (Element Mask : MaskList)
		{
			String maskLotName = SMessageUtil.getChildText(Mask, "MASKLOTNAME", true);
			MaskLot maskLotInfo = ExtendedObjectProxy.getMaskLotService().selectByKey(true, new Object[] { maskLotName });
			
			CommonValidation.checkMaskLotHoldState(maskLotInfo);
			CommonValidation.checkMaskLotProcessStateRun(maskLotInfo);
			CommonValidation.checkMaskLotState(maskLotInfo);
			
			if(Float.parseFloat(timeUsedLimit)<maskLotInfo.getTimeUsed()){
				throw new CustomException("MASK-0088",maskLotName);
			}
			
			if(Float.parseFloat(timeUsedLimit) == maskLotInfo.getTimeUsedLimit()){
				throw new CustomException("MASK-0087",maskLotName);
			}

			maskLotInfo.setTimeUsedLimit(Float.parseFloat(timeUsedLimit));
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
