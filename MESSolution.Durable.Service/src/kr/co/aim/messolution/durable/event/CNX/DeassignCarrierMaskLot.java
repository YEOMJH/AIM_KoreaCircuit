package kr.co.aim.messolution.durable.event.CNX;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.IncrementTimeUsedInfo;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class DeassignCarrierMaskLot extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String durableName = SMessageUtil.getBodyItemValue(doc, "DURABLENAME", true);
		List<Element> maskLotList = SMessageUtil.getBodySequenceItemList(doc, "MASKLOTLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeassignCarrierMask", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableName);
		
		List<MaskLot> dataInfoList = new ArrayList<MaskLot>();

		for (Element mask : maskLotList)
		{
			String maskLotName = SMessageUtil.getChildText(mask, "MASKLOTNAME", true);

			MaskLot dataInfo = ExtendedObjectProxy.getMaskLotService().selectByKey(false, new Object[] { maskLotName });
			dataInfo.setCarrierName("");
			dataInfo.setPosition("");
			dataInfo.setReasonCode(dataInfo.getReasonCode());
			dataInfo.setReasonCodeType(dataInfo.getReasonCodeType());
			dataInfo.setLastEventComment(eventInfo.getEventComment());
			dataInfo.setLastEventName(eventInfo.getEventName());
			dataInfo.setLastEventTime(eventInfo.getEventTime());
			dataInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
			dataInfo.setLastEventUser(eventInfo.getEventUser());

			// execute batch
			dataInfoList.add(dataInfo);
		}
		ExtendedObjectProxy.getMaskLotService().modify(eventInfo, dataInfoList);

		// execute batch

		int lotQty = ExtendedObjectProxy.getMaskLotService().assignedMaskLotQtyByCarrier(durableName);

		// Set LotQuantity as MaskLotList & makeAvailable
		durableData.setLotQuantity(lotQty);

		if (lotQty == 0)
			durableData.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);

		DurableServiceProxy.getDurableService().update(durableData);

		// Durable - SetEvent
		SetEventInfo setEventInfo = new SetEventInfo();
		MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);

		// Mantis : 0000440
		// 所有Mask CST，当TimeUseCount>TimeUseCountLimit时，MaskCST变Dirty
		if (lotQty == 0)
		{
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			MESDurableServiceProxy.getDurableServiceImpl().incrementTimeUseCount(durableData, 1, eventInfo);
		}
		
		return doc;
	}

}
