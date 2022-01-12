package kr.co.aim.messolution.durable.event.CNX;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskSpec;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class ModifyMaskSpecOffsetLimit extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String maskSpecName = SMessageUtil.getBodyItemValue(doc, "MASKSPECNAME", true);
		String maskKind = SMessageUtil.getBodyItemValue(doc, "MASKKIND", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ModifyMaskSpecOffsetLimit", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		MaskSpec maskSpecData = ExtendedObjectProxy.getMaskSpecService().getMaskSpecData(factoryName, maskSpecName);
		
		if(StringUtil.isNotEmpty(maskKind) && StringUtil.equals(maskKind, "EVA"))
		{
			String OFFSET_X_UPPER_LIMIT = SMessageUtil.getBodyItemValue(doc, "OFFSET_X_UPPER_LIMIT", true);
			String OFFSET_X_LOWER_LIMIT = SMessageUtil.getBodyItemValue(doc, "OFFSET_X_LOWER_LIMIT", true);
			String OFFSET_Y_UPPER_LIMIT = SMessageUtil.getBodyItemValue(doc, "OFFSET_Y_UPPER_LIMIT", true);
			String OFFSET_Y_LOWER_LIMIT = SMessageUtil.getBodyItemValue(doc, "OFFSET_Y_LOWER_LIMIT", true);
			String OFFSET_THETA_UPPER_LIMIT = SMessageUtil.getBodyItemValue(doc, "OFFSET_THETA_UPPER_LIMIT", true);
			String OFFSET_THETA_LOWER_LIMIT = SMessageUtil.getBodyItemValue(doc, "OFFSET_THETA_LOWER_LIMIT", true);
			
			maskSpecData.setOFFSET_X_UPPER_LIMIT(Double.parseDouble(OFFSET_X_UPPER_LIMIT));
			maskSpecData.setOFFSET_X_LOWER_LIMIT(Double.parseDouble(OFFSET_X_LOWER_LIMIT));
			maskSpecData.setOFFSET_Y_UPPER_LIMIT(Double.parseDouble(OFFSET_Y_UPPER_LIMIT));
			maskSpecData.setOFFSET_Y_LOWER_LIMIT(Double.parseDouble(OFFSET_Y_LOWER_LIMIT));
			maskSpecData.setOFFSET_THETA_UPPER_LIMIT(Double.parseDouble(OFFSET_THETA_UPPER_LIMIT));
			maskSpecData.setOFFSET_THETA_LOWER_LIMIT(Double.parseDouble(OFFSET_THETA_LOWER_LIMIT));
		}
		else
		{
			String TFEOFFSET_X1_UPPER_LIMIT = SMessageUtil.getBodyItemValue(doc, "TFEOFFSET_X1_UPPER_LIMIT", true);
			String TFEOFFSET_X1_LOWER_LIMIT = SMessageUtil.getBodyItemValue(doc, "TFEOFFSET_X1_LOWER_LIMIT", true);
			String TFEOFFSET_X2_UPPER_LIMIT = SMessageUtil.getBodyItemValue(doc, "TFEOFFSET_X2_UPPER_LIMIT", true);
			String TFEOFFSET_X2_LOWER_LIMIT = SMessageUtil.getBodyItemValue(doc, "TFEOFFSET_X2_LOWER_LIMIT", true);
			String TFEOFFSET_Y1_UPPER_LIMIT = SMessageUtil.getBodyItemValue(doc, "TFEOFFSET_Y1_UPPER_LIMIT", true);
			String TFEOFFSET_Y1_LOWER_LIMIT = SMessageUtil.getBodyItemValue(doc, "TFEOFFSET_Y1_LOWER_LIMIT", true);
			String TFEOFFSET_Y2_UPPER_LIMIT = SMessageUtil.getBodyItemValue(doc, "TFEOFFSET_Y2_UPPER_LIMIT", true);
			String TFEOFFSET_Y2_LOWER_LIMIT = SMessageUtil.getBodyItemValue(doc, "TFEOFFSET_Y2_LOWER_LIMIT", true);
			
			maskSpecData.setTFEOFFSET_X1_UPPER_LIMIT(Double.parseDouble(TFEOFFSET_X1_UPPER_LIMIT));
			maskSpecData.setTFEOFFSET_X1_LOWER_LIMIT(Double.parseDouble(TFEOFFSET_X1_LOWER_LIMIT));
			maskSpecData.setTFEOFFSET_X2_UPPER_LIMIT(Double.parseDouble(TFEOFFSET_X2_UPPER_LIMIT));
			maskSpecData.setTFEOFFSET_X2_LOWER_LIMIT(Double.parseDouble(TFEOFFSET_X2_LOWER_LIMIT));
			maskSpecData.setTFEOFFSET_Y1_UPPER_LIMIT(Double.parseDouble(TFEOFFSET_Y1_UPPER_LIMIT));
			maskSpecData.setTFEOFFSET_Y1_LOWER_LIMIT(Double.parseDouble(TFEOFFSET_Y1_LOWER_LIMIT));
			maskSpecData.setTFEOFFSET_Y2_UPPER_LIMIT(Double.parseDouble(TFEOFFSET_Y2_UPPER_LIMIT));
			maskSpecData.setTFEOFFSET_Y2_LOWER_LIMIT(Double.parseDouble(TFEOFFSET_Y2_LOWER_LIMIT));
		}
		
		maskSpecData.setLastEventComment(eventInfo.getEventComment());
		maskSpecData.setLastEventName(eventInfo.getEventName());
		maskSpecData.setLastEventTime(eventInfo.getEventTime());
		maskSpecData.setLastEventTimeKey(eventInfo.getEventTimeKey());
		maskSpecData.setLastEventUser(eventInfo.getEventUser());

		ExtendedObjectProxy.getMaskSpecService().modify(eventInfo, maskSpecData);		

		return doc;
	}
}
