package kr.co.aim.messolution.durable.event.CNX;

import org.jdom.Document;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import org.apache.commons.lang.StringUtils;
public class ChangeOLEDMaskCode extends SyncHandler {
	@Override
	public Object doWorks(Document doc) throws CustomException

	{
		String maskLotName = SMessageUtil.getBodyItemValue(doc, "MASKLOTNAME", true);
		String maskAOICode = SMessageUtil.getBodyItemValue(doc, "AOICODE", true);
		String maskRepairCode = SMessageUtil.getBodyItemValue(doc, "REPAIRCODE", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeOLEDMaskCode", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setLastEventTimekey(TimeStampUtil.getCurrentEventTimeKey());
		MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().selectByKey(true, new Object[] { maskLotName });
		if (StringUtils.equals(maskLotData.getAoiCode(),maskAOICode) && StringUtils.equals(maskLotData.getRepairCode(),maskRepairCode))//houxk 20200416
		{
			throw new CustomException("DURABLE-0100"); 
		}
		ExtendedObjectProxy.getMaskLotService().checkMaskLot(maskLotData);//houxk 20200416
		maskLotData.setAoiCode(maskAOICode);
		maskLotData.setRepairCode(maskRepairCode);
		maskLotData.setLastEventComment(eventInfo.getEventComment());
		maskLotData.setLastEventName(eventInfo.getEventName());
		maskLotData.setLastEventTime(eventInfo.getEventTime());
		maskLotData.setLastEventTimeKey(eventInfo.getLastEventTimekey());
		maskLotData.setLastEventUser(eventInfo.getEventUser());

		// update mask info and history
		ExtendedObjectProxy.getMaskLotService().modify(eventInfo, maskLotData);
	
		return doc;
	}

}
