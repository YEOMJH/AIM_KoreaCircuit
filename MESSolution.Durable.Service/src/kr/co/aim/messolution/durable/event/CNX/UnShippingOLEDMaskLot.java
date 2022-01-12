package kr.co.aim.messolution.durable.event.CNX;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskFrame;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class UnShippingOLEDMaskLot extends SyncHandler {

	public Object doWorks(Document doc) throws CustomException
	{
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		List<Element> eleMaskLotList = SMessageUtil.getBodySequenceItemList(doc, "MASKLOTLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("UnShipMask", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		List<MaskLot> MaskLotList = new ArrayList<MaskLot>();
		for (Element eledur : eleMaskLotList)
		{
			String factoryName = SMessageUtil.getChildText(eledur, "FACTORYNAME", true);
			String MaskLotName = SMessageUtil.getChildText(eledur, "MASKLOTNAME", true);
			String carrierName = SMessageUtil.getChildText(eledur, "CARRIERNAME", false);
			String position = SMessageUtil.getChildText(eledur, "POSITION", false);

			MaskLot dataInfo = ExtendedObjectProxy.getMaskLotService().selectByKey(false, new Object[] { MaskLotName });
			dataInfo.setFactoryName(factoryName);
			dataInfo.setMaskLotName(MaskLotName);
			dataInfo.setMaskLotProcessState(constantMap.MaskLotProcessState_Wait);
			dataInfo.setMaskLotState(constantMap.MaskLotState_Released);
			dataInfo.setReasonCodeType("");
			dataInfo.setReasonCode("");
			dataInfo.setLastEventComment(eventInfo.getEventComment());
			dataInfo.setLastEventName(eventInfo.getEventName());
			dataInfo.setLastEventTime(eventInfo.getEventTime());
			dataInfo.setLastEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
			dataInfo.setLastEventUser(eventInfo.getEventUser());
			MaskLotList.add(dataInfo);

			String maskKind = dataInfo.getMaskKind();
			if (!StringUtil.equals(maskKind, constantMap.OLEDMaskKind_TFE))
			{
				String frameName = dataInfo.getFrameName();

				MaskFrame frameData = ExtendedObjectProxy.getMaskFrameService().selectByKey(false, new Object[] { frameName });
				frameData.setFrameState("Released");
				frameData.setShippingDate(null);
				frameData.setLastEventComment(eventInfo.getEventComment());
				frameData.setLastEventName(eventInfo.getEventName());
				frameData.setLastEventTime(eventInfo.getEventTime());
				frameData.setLastEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
				frameData.setLastEventUser(eventInfo.getEventUser());

				ExtendedObjectProxy.getMaskFrameService().modify(eventInfo, frameData);
			}
			else
			{
				// Assign Carrier
				dataInfo.setCarrierName(carrierName);
				dataInfo.setPosition(position);
			}
		}

		ExtendedObjectProxy.getMaskLotService().modify(eventInfo, MaskLotList);

		eventInfo = EventInfoUtil.makeEventInfo("AssignCarrierMask", this.getEventUser(), this.getEventComment(), "", "");
		for (Element eledur : eleMaskLotList)
		{
			String MaskLotName = SMessageUtil.getChildText(eledur, "MASKLOTNAME", true);
			MaskLot dataInfo = ExtendedObjectProxy.getMaskLotService().selectByKey(false, new Object[] { MaskLotName });
			String maskKind = dataInfo.getMaskKind();

			if (StringUtil.equals(maskKind, constantMap.OLEDMaskKind_TFE))
			{
				String carrierName = SMessageUtil.getChildText(eledur, "CARRIERNAME", false);
				Durable carrierData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
				
				int lotQty = ExtendedObjectProxy.getMaskLotService().assignedMaskLotQtyByCarrier(carrierName);

				// Assign Carrier
				if (carrierData.getCapacity() < lotQty)
					throw new CustomException("MASKINSPECTION-0002", carrierName);

				carrierData.setLotQuantity(lotQty);
				carrierData.setDurableState(GenericServiceProxy.getConstantMap().Dur_InUse);

				DurableServiceProxy.getDurableService().update(carrierData);

				SetEventInfo setEventInfo1 = new SetEventInfo();
				MESDurableServiceProxy.getDurableServiceImpl().setEvent(carrierData, setEventInfo1, eventInfo);
			}
		}

		return doc;
	}
}
