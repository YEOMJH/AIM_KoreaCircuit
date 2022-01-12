package kr.co.aim.messolution.durable.event.OledMask;

import org.jdom.Document;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.extended.object.management.data.MaskMultiHold;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

public class OLEDMaskProcessCanceled extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String maskName = SMessageUtil.getBodyItemValue(doc, "MASKNAME", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("FailTrackInMask", getEventUser(), getEventComment(), "", "");
		MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().selectByKey(true, new Object[] { maskName });

		if (maskLotData.getMaskLotProcessState().equals("RUN"))
		{
			//MASK-0102: Mask[{0}] is already started.
			throw new CustomException("MASK-0102", maskLotData.getMaskLotName());
		}

		ExtendedObjectProxy.getMaskLotService().setJobFlag(eventInfo, maskName, "N");
		
		// Hold MaskLot After Cancel for DSP
		EventInfo eventInfoByInfo = EventInfoUtil.makeEventInfo("Hold", getEventUser(), getEventComment(), "", "");
		MaskLot holdMaskLot = ExtendedObjectProxy.getMaskLotService().selectByKey(true, new Object[] { maskName });
		holdMaskLot.setLastEventUser(eventInfoByInfo.getEventUser());
		holdMaskLot.setLastEventName(eventInfoByInfo.getEventName());
		holdMaskLot.setLastEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
		holdMaskLot.setLastEventComment(eventInfoByInfo.getEventComment());
		holdMaskLot.setLastEventTime(TimeStampUtil.getCurrentTimestamp());
		holdMaskLot.setMaskLotHoldState("Y");
		holdMaskLot.setReasonCodeType("CancelHold");
		holdMaskLot.setReasonCode("OLEDMaskProcessCanceled");

		ExtendedObjectProxy.getMaskLotService().modify(eventInfoByInfo, holdMaskLot);
		
		this.MultiHoldMaskLot(maskName, maskLotData, eventInfoByInfo);
		
	}
	
	private void MultiHoldMaskLot(String maskLotName,MaskLot maskLotData,EventInfo eventInfo)throws CustomException
	{
		MaskMultiHold maskMultiHold =new MaskMultiHold();
		maskMultiHold.setMaskLotName(maskLotName);
		maskMultiHold.setFactoryName(maskLotData.getFactoryName());
		maskMultiHold.setMaskProcessOperationName(maskLotData.getMaskProcessOperationName());
		maskMultiHold.setMaskProcessOperationVersion(maskLotData.getMaskProcessOperationVersion());
		maskMultiHold.setReasonCode("OLEDMaskProcessCanceled");
		maskMultiHold.setReasonCodeType("");
		maskMultiHold.setLastEventComment(eventInfo.getEventComment());
		maskMultiHold.setLastEventName(eventInfo.getEventName());
		maskMultiHold.setLastEventTime(eventInfo.getEventTime());
		maskMultiHold.setLastEventUser(eventInfo.getEventUser());
		
		ExtendedObjectProxy.getMaskMultiHoldService().create(eventInfo, maskMultiHold);
	}
}
