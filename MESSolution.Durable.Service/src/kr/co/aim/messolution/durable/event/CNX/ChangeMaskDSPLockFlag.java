package kr.co.aim.messolution.durable.event.CNX;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

public class ChangeMaskDSPLockFlag extends SyncHandler {
	private static Log log = LogFactory.getLog(ChangeMaskDSPLockFlag.class);
	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String maskLotName = SMessageUtil.getBodyItemValue(doc, "MASKLOTNAME", true);
		String transportLockFlag = SMessageUtil.getBodyItemValue(doc, "TRANSPORTLOCKFLAG", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeMaskDSPLockFlag", getEventUser(), getEventComment(), "", "");

		if (StringUtil.isNotEmpty(maskLotName))
		{
			MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().selectByKey(false, new Object[] { maskLotName });

			maskLotData.setTransportLockFlag(transportLockFlag);
			maskLotData.setLastEventComment(eventInfo.getEventComment());
			maskLotData.setLastEventName(eventInfo.getEventName());
			maskLotData.setLastEventTime(eventInfo.getEventTime());
			maskLotData.setLastEventTimeKey(eventInfo.getLastEventTimekey());
			maskLotData.setLastEventUser(eventInfo.getEventUser());
			ExtendedObjectProxy.getMaskLotService().modify(eventInfo, maskLotData);
			
			log.info("Change Mask DSPLockFlag Complete ! ");
		}
		return doc;
	}
}
