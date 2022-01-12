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
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class AssignMaskLotCarrier extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String durableName = SMessageUtil.getBodyItemValue(doc, "DURABLENAME", true);
		List<Element> maskLotList = SMessageUtil.getBodySequenceItemList(doc, "MASKLOTLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("AssignCarrierMask", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableName);

		List<MaskLot> dataInfoList = new ArrayList<MaskLot>();

		boolean dirtyFlag = false;

		for (Element mask : maskLotList)
		{
			String maskLotName = SMessageUtil.getChildText(mask, "MASKLOTNAME", true);
			String position = SMessageUtil.getChildText(mask, "POSITION", true);

			MaskLot dataInfo = ExtendedObjectProxy.getMaskLotService().selectByKey(false, new Object[] { maskLotName });
			if (StringUtil.equals(dataInfo.getCleanState(), GenericServiceProxy.getConstantMap().Dur_Dirty))
				dirtyFlag = true;

			dataInfo.setCarrierName(durableName);
			dataInfo.setPosition(position);
			dataInfo.setReasonCode("");
			dataInfo.setReasonCodeType("");
			dataInfo.setLastEventComment(eventInfo.getEventComment());
			dataInfo.setLastEventName(eventInfo.getEventName());
			dataInfo.setLastEventTime(eventInfo.getEventTime());
			dataInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
			dataInfo.setLastEventUser(eventInfo.getEventUser());

			// execute batch
			dataInfoList.add(dataInfo);
		}
		ExtendedObjectProxy.getMaskLotService().modify(eventInfo, dataInfoList);

		int lotQty = ExtendedObjectProxy.getMaskLotService().assignedMaskLotQtyByCarrier(durableName);

		// Set LotQuantity as MaskLotList & makeInUse
		durableData.setLotQuantity(lotQty);
		durableData.setDurableState(GenericServiceProxy.getConstantMap().Dur_InUse);
		if (dirtyFlag)
			durableData.setDurableCleanState(GenericServiceProxy.getConstantMap().Dur_Dirty);

		DurableServiceProxy.getDurableService().update(durableData);

		// Durable - SetEvent
		SetEventInfo setEventInfo = new SetEventInfo();
		MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);

		return doc;
	}

}
