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

public class ShippingOLEDMaskLot extends SyncHandler {

	public Object doWorks(Document doc) throws CustomException
	{
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		List<Element> eleMaskLotList = SMessageUtil.getBodySequenceItemList(doc, "MASKLOTLIST", true);

		// DeAssign Start
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeassignCarrierMask", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		for (Element eleMask : eleMaskLotList)
		{
			String maskLotName = SMessageUtil.getChildText(eleMask, "MASKLOTNAME", true);
			MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().selectByKey(false, new Object[] { maskLotName });
			
			if (!StringUtil.equals(maskLotData.getMaskKind(), constantMap.OLEDMaskKind_TFE))
				continue;
			
			String durableName = maskLotData.getCarrierName();
			if (StringUtil.isEmpty(durableName))
				continue;
			
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableName);

			int lotQty = ExtendedObjectProxy.getMaskLotService().assignedMaskLotQtyByCarrier(durableName);

			// Set LotQuantity as MaskLotList & makeAvailable
			durableData.setLotQuantity(lotQty);

			if (lotQty == 0)
				durableData.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);

			DurableServiceProxy.getDurableService().update(durableData);

			// Durable - SetEvent
			SetEventInfo setEventInfo = new SetEventInfo();
			MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
		}

		List<MaskLot> dataInfoList = new ArrayList<MaskLot>();

		for (Element eleMask : eleMaskLotList)
		{
			String maskLotName = SMessageUtil.getChildText(eleMask, "MASKLOTNAME", true);
			MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().selectByKey(false, new Object[] { maskLotName });
			if (!StringUtil.equals(maskLotData.getMaskKind(), constantMap.OLEDMaskKind_TFE))
				continue;
			
			String durableName = maskLotData.getCarrierName();
			if (StringUtil.isEmpty(durableName))
				continue;

			maskLotData.setCarrierName("");
			maskLotData.setPosition("");
			maskLotData.setReasonCode("");
			maskLotData.setReasonCodeType("");
			maskLotData.setLastEventComment(eventInfo.getEventComment());
			maskLotData.setLastEventName(eventInfo.getEventName());
			maskLotData.setLastEventTime(eventInfo.getEventTime());
			maskLotData.setLastEventTimeKey(eventInfo.getEventTimeKey());
			maskLotData.setLastEventUser(eventInfo.getEventUser());

			// execute batch
			dataInfoList.add(maskLotData);
		}

		if (dataInfoList.size() > 0)
			ExtendedObjectProxy.getMaskLotService().modify(eventInfo, dataInfoList);
		// DeAssign End

		// Ship Start
		eventInfo = EventInfoUtil.makeEventInfo("ShipMask", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		List<MaskLot> maskLotList = new ArrayList<MaskLot>();
		for (Element eleMask : eleMaskLotList)
		{
			String factoryName = SMessageUtil.getChildText(eleMask, "FACTORYNAME", true);
			String maskLotName = SMessageUtil.getChildText(eleMask, "MASKLOTNAME", true);

			MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().selectByKey(false, new Object[] { maskLotName });

			if (maskLotData.getMaskLotProcessState().equalsIgnoreCase(constantMap.MaskLotProcessState_Run))
				throw new CustomException("OLEDMASK-0002", new Object[] { constantMap.MaskLotProcessState_Run + "(" + maskLotName + ")" });

			maskLotData.setFactoryName(factoryName);
			maskLotData.setMaskLotName(maskLotName);
			maskLotData.setMaskLotProcessState("");
			maskLotData.setMaskLotState(constantMap.MaskLotState_Shipped);
			maskLotData.setReasonCodeType("");
			maskLotData.setReasonCode("");
			maskLotData.setLastEventComment(eventInfo.getEventComment());
			maskLotData.setLastEventName(eventInfo.getEventName());
			maskLotData.setLastEventTime(eventInfo.getEventTime());
			maskLotData.setLastEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
			maskLotData.setLastEventUser(eventInfo.getEventUser());
			maskLotList.add(maskLotData);

			String maskKind = maskLotData.getMaskKind();
			if (!StringUtil.equals(maskKind, constantMap.OLEDMaskKind_TFE))
			{
				String frameName = maskLotData.getFrameName();

				MaskFrame frameData = ExtendedObjectProxy.getMaskFrameService().selectByKey(false, new Object[] { frameName });
				frameData.setFrameState("Shipped");
				frameData.setShippingDate(eventInfo.getEventTime());
				frameData.setLastEventComment(eventInfo.getEventComment());
				frameData.setLastEventName(eventInfo.getEventName());
				frameData.setLastEventTime(eventInfo.getEventTime());
				frameData.setLastEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
				frameData.setLastEventUser(eventInfo.getEventUser());

				ExtendedObjectProxy.getMaskFrameService().modify(eventInfo, frameData);
			}
		}

		ExtendedObjectProxy.getMaskLotService().modify(eventInfo, maskLotList);
		// Ship End

		return doc;
	}

}
