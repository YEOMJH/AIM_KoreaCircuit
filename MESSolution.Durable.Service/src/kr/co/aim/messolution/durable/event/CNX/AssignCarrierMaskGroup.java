package kr.co.aim.messolution.durable.event.CNX;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class AssignCarrierMaskGroup extends SyncHandler {

	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> eleMaskLotList = SMessageUtil.getBodySequenceItemList(doc, "MASKLOTLIST", true);
		String MaskCarrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		String Quantity = SMessageUtil.getBodyItemValue(doc, "QUANTITY", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("AssignCarrierGroupMask", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		List<MaskLot> MaskLotList = new ArrayList<MaskLot>();
		for (Element eledur : eleMaskLotList)
		{
			String factoryName = SMessageUtil.getChildText(eledur, "FACTORYNAME", true);
			String MaskLotName = SMessageUtil.getChildText(eledur, "MASKLOTNAME", true);
			String MaskType = SMessageUtil.getChildText(eledur, "MASKTYPE", true);
			String MaskSpecName = SMessageUtil.getChildText(eledur, "MASKSPECNAME", true);
			String Position = SMessageUtil.getChildText(eledur, "POSITION", true);

			MaskLot dataInfo = ExtendedObjectProxy.getMaskLotService().selectByKey(false, new Object[] { MaskLotName });
			dataInfo.setFactoryName(factoryName);
			dataInfo.setMaskLotName(MaskLotName);
			dataInfo.setMaskType(MaskType);
			dataInfo.setMaskSpecName(MaskSpecName);
			dataInfo.setCarrierName(MaskCarrierName);
			dataInfo.setReasonCodeType("");
			dataInfo.setReasonCode("");
			dataInfo.setPosition(Position);
			dataInfo.setLastEventComment(eventInfo.getEventComment());
			dataInfo.setLastEventName(eventInfo.getEventName());
			dataInfo.setLastEventTime(eventInfo.getEventTime());
			dataInfo.setLastEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
			dataInfo.setLastEventUser(eventInfo.getEventUser());
			MaskLotList.add(dataInfo);

		}

		ExtendedObjectProxy.getMaskLotService().modify(eventInfo, MaskLotList);

		// SetEvent Info for MaskCST
		Durable maskCSTData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(MaskCarrierName);

		maskCSTData.setDurableState("InUse");
		maskCSTData.setLotQuantity((long) Integer.parseInt(Quantity));

		DurableServiceProxy.getDurableService().update(maskCSTData);

		SetEventInfo setEventInfo = new SetEventInfo();
		DurableServiceProxy.getDurableService().setEvent(maskCSTData.getKey(), eventInfo, setEventInfo);

		return doc;
	}

}
