package kr.co.aim.messolution.durable.event.OledMask;

import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskOffsetSpec;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class ModifyMaskOffsetSpec extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {

		String maskKind = SMessageUtil.getBodyItemValue(doc, "MASKKIND", true);
		List<Element> MaskOffsetList = SMessageUtil.getBodySequenceItemList(doc, "MASKLOTLIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ModifyMaskOffsetSpec", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		if (StringUtil.isNotEmpty(maskKind) && StringUtil.equals(maskKind, "EVA"))
		{
			for(Element MaskOffset:MaskOffsetList){
				
				String maskLotName = SMessageUtil.getChildText(MaskOffset, "MASKLOTNAME", true);
				String strXUpper = SMessageUtil.getChildText(MaskOffset, "OFFSET_X_UPPER_LIMIT", true);
				String strXLower = SMessageUtil.getChildText(MaskOffset, "OFFSET_X_LOWER_LIMIT", true);
				String strYUpper = SMessageUtil.getChildText(MaskOffset, "OFFSET_Y_UPPER_LIMIT", true);
				String strYLower = SMessageUtil.getChildText(MaskOffset, "OFFSET_Y_LOWER_LIMIT", true);
				String strThetaUpper = SMessageUtil.getChildText(MaskOffset, "OFFSET_THETA_UPPER_LIMIT", true);
				String strThetaLower = SMessageUtil.getChildText(MaskOffset, "OFFSET_THETA_LOWER_LIMIT", true);
				double xUpper = (!StringUtil.equals(strXUpper, "")) ? Double.parseDouble(strXUpper) : 0;
				double xLower = (!StringUtil.equals(strXLower, "")) ? Double.parseDouble(strXLower) : 0;
				double yUpper = (!StringUtil.equals(strYUpper, "")) ? Double.parseDouble(strYUpper) : 0;
				double yLower = (!StringUtil.equals(strYLower, "")) ? Double.parseDouble(strYLower) : 0;
				double thetaUpper = (!StringUtil.equals(strThetaUpper, "")) ? Double.parseDouble(strThetaUpper) : 0;
				double thetaLower = (!StringUtil.equals(strThetaLower, "")) ? Double.parseDouble(strThetaLower) : 0;				
				
				MaskOffsetSpec dataInfo =ExtendedObjectProxy.getMaskOffsetSpecService().selectByKey(false, new Object[]{maskLotName});
				
				try{
					dataInfo.setMasklotname(maskLotName);
					dataInfo.setOffset_x_lower_limit(xLower);
					dataInfo.setOffset_x_upper_limit(xUpper);
					dataInfo.setOffset_y_lower_limit(yLower);
					dataInfo.setOffset_y_upper_limit(yUpper);
					dataInfo.setOffset_theta_lower_limit(thetaLower);
					dataInfo.setOffset_theta_upper_limit(thetaUpper);	
					dataInfo.setLastEventName(eventInfo.getEventName());
					dataInfo.setLastEventUser(eventInfo.getEventUser());
					dataInfo.setLastEventTime(eventInfo.getEventTime());
					dataInfo.setLastEventComment(eventInfo.getEventComment());
					dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
					ExtendedObjectProxy.getMaskOffsetSpecService().modify(eventInfo, dataInfo);
				}catch(Exception ex)
				{
				    throw new CustomException(ex.getCause());	
				}			
			}
		}
		else
		{
			for(Element MaskOffset:MaskOffsetList){
				
				String maskLotName = SMessageUtil.getChildText(MaskOffset, "MASKLOTNAME", true);
				String strX1Upper = SMessageUtil.getChildText(MaskOffset, "TFEOFFSET_X1_UPPER_LIMIT", true);
				String strX1Lower = SMessageUtil.getChildText(MaskOffset, "TFEOFFSET_X1_LOWER_LIMIT", true);
				String strX2Upper = SMessageUtil.getChildText(MaskOffset, "TFEOFFSET_X2_UPPER_LIMIT", true);
				String strX2Lower = SMessageUtil.getChildText(MaskOffset, "TFEOFFSET_X2_LOWER_LIMIT", true);
				String strY1Upper = SMessageUtil.getChildText(MaskOffset, "TFEOFFSET_Y1_UPPER_LIMIT", true);
				String strY1Lower = SMessageUtil.getChildText(MaskOffset, "TFEOFFSET_Y1_LOWER_LIMIT", true);
				String strY2Upper = SMessageUtil.getChildText(MaskOffset, "TFEOFFSET_Y2_UPPER_LIMIT", true);
				String strY2Lower = SMessageUtil.getChildText(MaskOffset, "TFEOFFSET_Y2_LOWER_LIMIT", true);
				double X1Upper = (!StringUtil.equals(strX1Upper, "")) ? Double.parseDouble(strX1Upper) : 0;
				double X1Lower = (!StringUtil.equals(strX1Upper, "")) ? Double.parseDouble(strX1Lower) : 0;
				double X2Upper = (!StringUtil.equals(strX2Upper, "")) ? Double.parseDouble(strX2Upper) : 0;
				double X2Lower = (!StringUtil.equals(strX2Upper, "")) ? Double.parseDouble(strX2Lower) : 0;
				double Y1Upper = (!StringUtil.equals(strY1Upper, "")) ? Double.parseDouble(strY1Upper) : 0;
				double Y1Lower = (!StringUtil.equals(strY1Upper, "")) ? Double.parseDouble(strY1Lower) : 0;
				double Y2Upper = (!StringUtil.equals(strY2Upper, "")) ? Double.parseDouble(strY2Upper) : 0;
				double Y2Lower = (!StringUtil.equals(strY2Upper, "")) ? Double.parseDouble(strY2Lower) : 0;
				
				MaskOffsetSpec dataInfo =ExtendedObjectProxy.getMaskOffsetSpecService().selectByKey(false, new Object[]{maskLotName});
				
				try{
					dataInfo.setMasklotname(maskLotName);
					dataInfo.setTFEOFFSET_X1_UPPER_LIMIT(X1Upper);
					dataInfo.setTFEOFFSET_X1_LOWER_LIMIT(X1Lower);
					dataInfo.setTFEOFFSET_X2_UPPER_LIMIT(X2Upper);
					dataInfo.setTFEOFFSET_X2_LOWER_LIMIT(X2Lower);
					dataInfo.setTFEOFFSET_Y1_UPPER_LIMIT(Y1Upper);
					dataInfo.setTFEOFFSET_Y1_LOWER_LIMIT(Y1Lower);
					dataInfo.setTFEOFFSET_Y2_UPPER_LIMIT(Y2Upper);
					dataInfo.setTFEOFFSET_Y2_LOWER_LIMIT(Y2Lower);	
					dataInfo.setLastEventName(eventInfo.getEventName());
					dataInfo.setLastEventUser(eventInfo.getEventUser());
					dataInfo.setLastEventTime(eventInfo.getEventTime());
					dataInfo.setLastEventComment(eventInfo.getEventComment());
					dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
					ExtendedObjectProxy.getMaskOffsetSpecService().modify(eventInfo, dataInfo);
				}catch(Exception ex)
				{
				    throw new CustomException(ex.getCause());	
				}			
			}
		}		
	
		return doc;
	}

}
