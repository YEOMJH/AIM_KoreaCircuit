package kr.co.aim.messolution.durable.event.CNX;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class ChangeOLEDMaskCleanState extends SyncHandler {
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		Element eleBody = SMessageUtil.getBodyElement(doc);

		if (eleBody != null)
		{
			String maskLotName = SMessageUtil.getBodyItemValue(doc, "MASKLOTNAME", true);
			String cleanState = SMessageUtil.getBodyItemValue(doc, "CLEANSTATE", true);
			String cleanTime = SMessageUtil.getBodyItemValue(doc, "LASTCLEANTIME", false);

			EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeCleanStateMask", this.getEventUser(), this.getEventComment(), "", "", "Y");
			eventInfo.setLastEventTimekey(TimeStampUtil.getCurrentEventTimeKey());
			MaskLot maskLotInfo = ExtendedObjectProxy.getMaskLotService().selectByKey(true, new Object[] { maskLotName });
			ExtendedObjectProxy.getMaskLotService().checkMaskLot(maskLotInfo);//caixu 20200317 check 
			maskLotInfo.setCleanState(cleanState);
			String cleanFlag = null;
			Timestamp CleanTime = null;
			if (StringUtil.equals(cleanState, GenericServiceProxy.getConstantMap().Dur_Clean))
			{
				  cleanFlag = "Y";
				  CleanTime = TimeUtils.getTimestamp(cleanTime);
				//Validate cleantime is not null
				  if (cleanTime == null) {
					throw new CustomException("MASKCLEAN-0002");
				}
				 if(TimeStampUtil.getCurrentTimestamp().compareTo(CleanTime)>0)
				{
				  maskLotInfo.setCleanTime(CleanTime);
				  maskLotInfo.setLastCleanTimekey(eventInfo.getLastEventTimekey());
				}
				 

				 else
				 {
					 throw new CustomException("MASKCLEAN-0001");
				 }
			}
			else
			{
				cleanFlag = "N";
			}

			maskLotInfo.setCleanFlag(cleanFlag);
			//maskLotInfo.setReasonCode(null);
			//maskLotInfo.setReasonCodeType(null);
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
