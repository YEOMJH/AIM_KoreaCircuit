package kr.co.aim.messolution.durable.event.CNX;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskFrame;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;

public class ChangeFrame extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeFrame", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		String frameName = SMessageUtil.getBodyItemValue(doc, "FRAMENAME", true);
		String vendorName = SMessageUtil.getBodyItemValue(doc, "VENDORNAME", true);
		String thickNess = SMessageUtil.getBodyItemValue(doc, "THICKNESS", true);
		String flatNess = SMessageUtil.getBodyItemValue(doc, "FLATNESS", false);

		String outside1Length = SMessageUtil.getBodyItemValue(doc, "OUTSIDE1LENGTH", false);
		String outside1Width = SMessageUtil.getBodyItemValue(doc, "OUTSIDE1WIDTH", false);
		String internal1Length = SMessageUtil.getBodyItemValue(doc, "INTERNAL1LENGTH", false);
		String internal1Width = SMessageUtil.getBodyItemValue(doc, "INTERNAL1WIDTH", false);

		String outside2Length = SMessageUtil.getBodyItemValue(doc, "OUTSIDE2LENGTH", false);
		String outside2Width = SMessageUtil.getBodyItemValue(doc, "OUTSIDE2WIDTH", false);
		String internal2Length = SMessageUtil.getBodyItemValue(doc, "INTERNAL2LENGTH", false);
		String internal2Width = SMessageUtil.getBodyItemValue(doc, "INTERNAL2WIDTH", false);

		String alignPosition1DesignValX = SMessageUtil.getBodyItemValue(doc, "ALIGNPOSITION1DESIGNVALX", false);
		String alignPosition1DesignValY = SMessageUtil.getBodyItemValue(doc, "ALIGNPOSITION1DESIGNVALY", false);
		String alignPosition1MeasureValX = SMessageUtil.getBodyItemValue(doc, "ALIGNPOSITION1MEASUREVALX", false);
		String alignPosition1MeasureValY = SMessageUtil.getBodyItemValue(doc, "ALIGNPOSITION1MEASUREVALY", false);
		String alignPosition1DesignSize = SMessageUtil.getBodyItemValue(doc, "ALIGNPOSITION1DESIGNSIZE", false);
		String alignPosition1MeasureSize = SMessageUtil.getBodyItemValue(doc, "ALIGNPOSITION1MEASURESIZE", false);
		String alignHoleDesignRN1 = SMessageUtil.getBodyItemValue(doc, "COARN1", false);
		String alignHoleMeasurementRN1 = SMessageUtil.getBodyItemValue(doc, "RN1", false);

		String alignPosition2DesignValX = SMessageUtil.getBodyItemValue(doc, "ALIGNPOSITION2DESIGNVALX", false);
		String alignPosition2DesignValY = SMessageUtil.getBodyItemValue(doc, "ALIGNPOSITION2DESIGNVALY", false);
		String alignPosition2MeasureValX = SMessageUtil.getBodyItemValue(doc, "ALIGNPOSITION2MEASUREVALX", false);
		String alignPosition2MeasureValY = SMessageUtil.getBodyItemValue(doc, "ALIGNPOSITION2MEASUREVALY", false);
		String alignPosition2DesignSize = SMessageUtil.getBodyItemValue(doc, "ALIGNPOSITION2DESIGNSIZE", false);
		String alignPosition2MeasureSize = SMessageUtil.getBodyItemValue(doc, "ALIGNPOSITION2MEASURESIZE", false);
		String alignHoleDesignRN2 = SMessageUtil.getBodyItemValue(doc, "COARN2", false);
		String alignHoleMeasurementRN2 = SMessageUtil.getBodyItemValue(doc, "RN2", false);

		String alignPosition3DesignValX = SMessageUtil.getBodyItemValue(doc, "ALIGNPOSITION3DESIGNVALX", false);
		String alignPosition3DesignValY = SMessageUtil.getBodyItemValue(doc, "ALIGNPOSITION3DESIGNVALY", false);
		String alignPosition3MeasureValX = SMessageUtil.getBodyItemValue(doc, "ALIGNPOSITION3MEASUREVALX", false);
		String alignPosition3MeasureValY = SMessageUtil.getBodyItemValue(doc, "ALIGNPOSITION3MEASUREVALY", false);
		String alignPosition3DesignSize = SMessageUtil.getBodyItemValue(doc, "ALIGNPOSITION3DESIGNSIZE", false);
		String alignPosition3MeasureSize = SMessageUtil.getBodyItemValue(doc, "ALIGNPOSITION3MEASURESIZE", false);
		String alignHoleDesignRN3 = SMessageUtil.getBodyItemValue(doc, "COARN3", false);
		String alignHoleMeasurementRN3 = SMessageUtil.getBodyItemValue(doc, "RN3", false);

		String alignPosition4DesignValX = SMessageUtil.getBodyItemValue(doc, "ALIGNPOSITION4DESIGNVALX", false);
		String alignPosition4DesignValY = SMessageUtil.getBodyItemValue(doc, "ALIGNPOSITION4DESIGNVALY", false);
		String alignPosition4MeasureValX = SMessageUtil.getBodyItemValue(doc, "ALIGNPOSITION4MEASUREVALX", false);
		String alignPosition4MeasureValY = SMessageUtil.getBodyItemValue(doc, "ALIGNPOSITION4MEASUREVALY", false);
		String alignPosition4DesignSize = SMessageUtil.getBodyItemValue(doc, "ALIGNPOSITION4DESIGNSIZE", false);
		String alignPosition4MeasureSize= SMessageUtil.getBodyItemValue(doc, "ALIGNPOSITION4MEASURESIZE", false);
		String alignHoleDesignRN4 = SMessageUtil.getBodyItemValue(doc, "COARN4", false);
		String alignHoleMeasurementRN4 = SMessageUtil.getBodyItemValue(doc, "RN4", false);

		String coverDepthMax = SMessageUtil.getBodyItemValue(doc, "COVERDEPTHMAX", false);
		String coverDepthMin = SMessageUtil.getBodyItemValue(doc, "COVERDEPTHMIN", false);
		String coverWidthMax = SMessageUtil.getBodyItemValue(doc, "COVERWIDTHMAX", false);
		String coverWidthMin = SMessageUtil.getBodyItemValue(doc, "COVERWIDTHMIN", false);
		String haulingDepthMax = SMessageUtil.getBodyItemValue(doc, "HAULINGDEPTHMAX", false);
		String haulingDepthMin = SMessageUtil.getBodyItemValue(doc, "HAULINGDEPTHMIN", false);
		String haulingWidthMax = SMessageUtil.getBodyItemValue(doc, "HAULINGWIDTHMAX", false);
		String haulingWidthMin = SMessageUtil.getBodyItemValue(doc, "HAULINGWIDTHMIN", false);
		String texture = SMessageUtil.getBodyItemValue(doc, "TEXTURE", false);
		String flatness2 = SMessageUtil.getBodyItemValue(doc, "FLATNESS2", false);

		String receiveTime = SMessageUtil.getBodyItemValue(doc, "RECEIVETIME", true);
		String shippingDate = SMessageUtil.getBodyItemValue(doc, "SHIPPINGDATE", false);

		MaskFrame maskFrame = ExtendedObjectProxy.getMaskFrameService().selectByKey(false, new Object[] { frameName });

		maskFrame.setFrameName(frameName);
		maskFrame.setVendorName(vendorName);
		maskFrame.setThickNess(thickNess);
		maskFrame.setFlatNess(flatNess);
		maskFrame.setReceiveTime(TimeUtils.getTimestamp(receiveTime));
		maskFrame.setShippingDate(TimeUtils.getTimestamp(shippingDate));
		
		maskFrame.setCoverDepthMax(coverDepthMax);
		maskFrame.setCoverDepthMin(coverDepthMin);
		maskFrame.setCoverWidthMax(coverWidthMax);
		maskFrame.setCoverWidthMin(coverWidthMin);
		
		maskFrame.setHaulingDepthMax(haulingDepthMax);
		maskFrame.setHaulingDepthMin(haulingDepthMin);
		maskFrame.setHaulingWidthMax(haulingWidthMax);
		maskFrame.setHaulingWidthMin(haulingWidthMin);
		
		maskFrame.setOutside1Length(outside1Length);
		maskFrame.setOutside1Width(outside1Width);
		maskFrame.setInternal1Length(internal1Length);
		maskFrame.setInternal1Width(internal1Width);
		
		maskFrame.setOutside2Length(outside2Length);
		maskFrame.setOutside2Width(outside2Width);
		maskFrame.setInternal2Length(internal2Length);
		maskFrame.setInternal2Width(internal2Width);
		
		maskFrame.setAlignPosition1DesignValX(alignPosition1DesignValX);
		maskFrame.setAlignPosition1DesignValY(alignPosition1DesignValY);
		maskFrame.setAlignPosition1MeasureValX(alignPosition1MeasureValX);
		maskFrame.setAlignPosition1MeasureValY(alignPosition1MeasureValY);
		maskFrame.setAlignPosition1DesignSize(alignPosition1DesignSize);
		maskFrame.setAlignPosition1MeasureSize(alignPosition1MeasureSize);
		
		maskFrame.setAlignPosition2DesignValX(alignPosition2DesignValX);
		maskFrame.setAlignPosition2DesignValY(alignPosition2DesignValY);
		maskFrame.setAlignPosition2MeasureValX(alignPosition2MeasureValX);
		maskFrame.setAlignPosition2MeasureValY(alignPosition2MeasureValY);
		maskFrame.setAlignPosition2DesignSize(alignPosition2DesignSize);
		maskFrame.setAlignPosition2MeasureSize(alignPosition2MeasureSize);
		
		maskFrame.setAlignPosition3DesignValX(alignPosition3DesignValX);
		maskFrame.setAlignPosition3DesignValY(alignPosition3DesignValY);
		maskFrame.setAlignPosition3MeasureValX(alignPosition3MeasureValX);
		maskFrame.setAlignPosition3MeasureValY(alignPosition3MeasureValY);
		maskFrame.setAlignPosition3DesignSize(alignPosition3DesignSize);
		maskFrame.setAlignPosition3MeasureSize(alignPosition3MeasureSize);
		
		maskFrame.setAlignPosition4DesignValX(alignPosition4DesignValX);
		maskFrame.setAlignPosition4DesignValY(alignPosition4DesignValY);
		maskFrame.setAlignPosition4MeasureValX(alignPosition4MeasureValX);
		maskFrame.setAlignPosition4MeasureValY(alignPosition4MeasureValY);
		maskFrame.setAlignPosition4DesignSize(alignPosition4DesignSize);
		maskFrame.setAlignPosition4MeasureSize(alignPosition4MeasureSize);
		maskFrame.setTexture(texture);
		maskFrame.setFlatness2(flatness2);;
		
		maskFrame.setLastEventComment(eventInfo.getEventComment());
		maskFrame.setLastEventName(eventInfo.getEventName());
		maskFrame.setLastEventTime(eventInfo.getEventTime());
		maskFrame.setLastEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
		maskFrame.setLastEventUser(eventInfo.getEventUser());
		
		maskFrame.setAlignHoleDesignRN1(alignHoleDesignRN1);
		maskFrame.setAlignHoleMeasurementRN1(alignHoleMeasurementRN1);
		maskFrame.setAlignHoleDesignRN2(alignHoleDesignRN2);
		maskFrame.setAlignHoleMeasurementRN2(alignHoleMeasurementRN2);
		maskFrame.setAlignHoleDesignRN3(alignHoleDesignRN3);
		maskFrame.setAlignHoleMeasurementRN3(alignHoleMeasurementRN3);
		maskFrame.setAlignHoleDesignRN4(alignHoleDesignRN4);
		maskFrame.setAlignHoleMeasurementRN4(alignHoleMeasurementRN4);
			
		ExtendedObjectProxy.getMaskFrameService().modify(eventInfo, maskFrame);
		return doc;
	}

}
