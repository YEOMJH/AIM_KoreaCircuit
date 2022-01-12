package kr.co.aim.messolution.durable.event.CNX;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class ChangeOLEDMaskDurationUsedLimit extends SyncHandler {

	private static Log log = LogFactory.getLog(ChangeOLEDMaskDurationUsedLimit.class);

	public Object doWorks(Document doc) throws CustomException
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String maskSpecName = SMessageUtil.getBodyItemValue(doc, "MASKSPECNAME", false);
		String durationUsedLimit = SMessageUtil.getBodyItemValue(doc, "DURATIONUSEDLIMIT", false);
		String thickness = SMessageUtil.getBodyItemValue(doc, "MASKTHICKNESS", false);
		List<Element> MaskList = SMessageUtil.getBodySequenceItemList(doc, "MASKLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeMaskDurationUsedLimit&Thickness", this.getEventUser(), this.getEventComment(), "", "");
		String timeKey = TimeUtils.getCurrentEventTimeKey();
		eventInfo.setEventTimeKey(timeKey);
		eventInfo.setLastEventTimekey(timeKey);

		List<String> maskList = new ArrayList<String>();

		for (Element Mask : MaskList)
		{
			String maskLotName = SMessageUtil.getChildText(Mask, "MASKLOTNAME", true);
			MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().selectByKey(true, new Object[] { maskLotName });
            //houxk
			CommonValidation.checkMaskLotHoldState(maskLotData);
			CommonValidation.checkMaskLotProcessStateRun(maskLotData);
			CommonValidation.checkMaskLotState(maskLotData);
			
			// Set Mask Info
			if(StringUtils.isNotEmpty(durationUsedLimit))
			{
				maskLotData.setDurationUsedLimit(Float.parseFloat(durationUsedLimit));
			}
			if(StringUtils.isNotEmpty(thickness))
			{
				maskLotData.setMaskThickness(thickness);
			}
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
