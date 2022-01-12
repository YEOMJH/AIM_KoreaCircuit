package kr.co.aim.messolution.durable.event.CNX;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class ChangeOLEDMaskJudge extends SyncHandler {
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		Element eleBody = SMessageUtil.getBodyElement(doc);

		if (eleBody != null)
		{
			List<Element> eleMaskList = SMessageUtil.getBodySequenceItemList(doc, "MASKLIST", false);

			EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeJudgeMask", this.getEventUser(), this.getEventComment(), "", "", "Y");
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			List<MaskLot> maskLotInfoList = new ArrayList<MaskLot>();
			for (Element eleMask : eleMaskList)
			{
				String maskLotName = eleMask.getChildText("MASKLOTNAME");
				String maskLotJudge = eleMask.getChildText("MASKLOTJUDGE");
				String reasonCodeType = eleMask.getChildText("REASONCODETYPE");
				String reasonCode = eleMask.getChildText("REASONCODE");
				MaskLot maskLotInfo = ExtendedObjectProxy.getMaskLotService().selectByKey(true, new Object[] { maskLotName });
				if(!maskLotInfo.getMaskLotHoldState().equals("N"))//caixu
					throw new CustomException("MASK-0013", maskLotInfo.getMaskLotName());
				if(!maskLotInfo.getMaskLotProcessState().equals("WAIT"))
					throw new CustomException("MASK-0014");
				maskLotInfo.setMaskLotJudge(maskLotJudge);
				maskLotInfo.setReasonCodeType(reasonCodeType);
				maskLotInfo.setReasonCode(reasonCode);
				maskLotInfo.setLastEventName(eventInfo.getEventName());
				maskLotInfo.setLastEventTime(eventInfo.getEventTime());
				maskLotInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
				maskLotInfo.setLastEventUser(eventInfo.getEventUser());
				maskLotInfo.setLastEventComment(eventInfo.getEventComment());
				maskLotInfoList.add(maskLotInfo);
			   
			}

			ExtendedObjectProxy.getMaskLotService().modify(eventInfo, maskLotInfoList);
		}
		return doc;
	}
}
