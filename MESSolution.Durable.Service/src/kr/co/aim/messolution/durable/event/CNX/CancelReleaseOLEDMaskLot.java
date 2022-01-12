package kr.co.aim.messolution.durable.event.CNX;

import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskFrame;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

public class CancelReleaseOLEDMaskLot extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		List<Element> MASKLIST = SMessageUtil.getBodySequenceItemList(doc, "MASKLIST", true);

		for (Element mask : MASKLIST)
		{
			String maskLotName = SMessageUtil.getChildText(mask, "MASKLOTNAME", true);

			EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelReleaseMask", this.getEventUser(), this.getEventComment(), "", "");

			eventInfo.setLastEventTimekey(TimeStampUtil.getCurrentEventTimeKey());
			MaskLot dataInfo = null;
			try
			{
				dataInfo = ExtendedObjectProxy.getMaskLotService().selectByKey(false, new Object[] { maskLotName });
			}
			catch (greenFrameDBErrorSignal nfds)
			{
				throw new CustomException("MASKLOT-0001", maskLotName);
			}

			dataInfo.setMaskLotState(constantMap.MaskLotState_Created);
			dataInfo.setMaskLotName(maskLotName);
			dataInfo.setMaskProcessOperationName("");
			dataInfo.setMaskProcessOperationVersion("");
			dataInfo.setNodeStack("");
			dataInfo.setReasonCode("");
			dataInfo.setReasonCodeType("");
			dataInfo.setLastEventComment(eventInfo.getEventComment());
			dataInfo.setLastEventName(eventInfo.getEventName());
			dataInfo.setLastEventTime(eventInfo.getEventTime());
			dataInfo.setLastEventTimeKey(eventInfo.getLastEventTimekey());
			dataInfo.setLastEventUser(eventInfo.getEventUser());
		}

		return doc;
	}

}
