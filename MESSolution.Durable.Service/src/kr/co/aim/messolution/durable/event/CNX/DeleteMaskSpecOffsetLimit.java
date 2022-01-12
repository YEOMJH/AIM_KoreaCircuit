package kr.co.aim.messolution.durable.event.CNX;

import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskSpec;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class DeleteMaskSpecOffsetLimit extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		List<Element> maskSpecList = SMessageUtil.getBodySequenceItemList(doc, "MASKSPECLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeleteMaskSpecOffsetLimit", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		for (Element maskSpec : maskSpecList)
		{
			String maskSpecName = SMessageUtil.getChildText(maskSpec, "MASKSPECNAME", true);
			
			MaskSpec maskSpecData = ExtendedObjectProxy.getMaskSpecService().getMaskSpecData(factoryName, maskSpecName);

			maskSpecData.setOFFSET_X_UPPER_LIMIT(null);
			maskSpecData.setOFFSET_X_LOWER_LIMIT(null);
			maskSpecData.setOFFSET_Y_UPPER_LIMIT(null);
			maskSpecData.setOFFSET_Y_LOWER_LIMIT(null);
			maskSpecData.setOFFSET_THETA_UPPER_LIMIT(null);
			maskSpecData.setOFFSET_THETA_LOWER_LIMIT(null);
			
			maskSpecData.setTFEOFFSET_X1_UPPER_LIMIT(null);
			maskSpecData.setTFEOFFSET_X1_LOWER_LIMIT(null);
			maskSpecData.setTFEOFFSET_X2_UPPER_LIMIT(null);
			maskSpecData.setTFEOFFSET_X2_LOWER_LIMIT(null);
			maskSpecData.setTFEOFFSET_Y1_UPPER_LIMIT(null);
			maskSpecData.setTFEOFFSET_Y1_LOWER_LIMIT(null);
			maskSpecData.setTFEOFFSET_Y2_UPPER_LIMIT(null);
			maskSpecData.setTFEOFFSET_Y2_LOWER_LIMIT(null);
			
			maskSpecData.setLastEventComment(eventInfo.getEventComment());
			maskSpecData.setLastEventName(eventInfo.getEventName());
			maskSpecData.setLastEventTime(eventInfo.getEventTime());
			maskSpecData.setLastEventTimeKey(eventInfo.getEventTimeKey());
			maskSpecData.setLastEventUser(eventInfo.getEventUser());

			ExtendedObjectProxy.getMaskSpecService().modify(eventInfo, maskSpecData);
		}

		return doc;
	}

}
