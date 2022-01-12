package kr.co.aim.messolution.transportjob.event;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.transportjob.MESTransportServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class GetMaskDataReply extends AsyncHandler {

	/**
	 * MessageSpec [MCS -> TEX -> OIC]
	 * 
	 * <Body>
	 *    <MASKNAME />
	 *    <CURRENTMACHINENAME />
	 *    <CURRENTPOSITIONTYPE />
	 *    <CURRENTPOSITIONNAME />
	 *    <CURRENTZONENAME />
	 *    <CURRENTCARRIERNAME />
	 *    <CURRENTCARRIERSLOTNO />
	 *    <TRANSFERSTATE />
	 *    <MASKTYPE />
	 * </Body>
	 */
	
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String maskName = SMessageUtil.getBodyItemValue(doc, "MASKNAME", true);
		String currentMachineName = SMessageUtil.getBodyItemValue(doc, "CURRENTMACHINENAME", false);
		String currentPositionType = SMessageUtil.getBodyItemValue(doc, "CURRENTPOSITIONTYPE", false);
		String currentPositionName = SMessageUtil.getBodyItemValue(doc, "CURRENTPOSITIONNAME", false);
		String currentZoneName = SMessageUtil.getBodyItemValue(doc, "CURRENTZONENAME", false);
		String currentCarrierName = SMessageUtil.getBodyItemValue(doc, "CURRENTCARRIERNAME", false);
		String currentCarrierSlotNo = SMessageUtil.getBodyItemValue(doc, "CURRENTCARRIERSLOTNO", false);
		String transferState = SMessageUtil.getBodyItemValue(doc, "TRANSFERSTATE", false);
		String returnCode = SMessageUtil.getReturnItemValue(doc, "RETURNCODE", false);
		String returnMessage = SMessageUtil.getReturnItemValue(doc, "RETURNMESSAGE", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("GetMaskDataReply", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		// Change Mask Location
		MaskLot maskData = ExtendedObjectProxy.getMaskLotService().selectByKey(false, new Object[] { maskName });

		// Set CarrierSlotNo
		currentCarrierSlotNo = ConvertUtil.toStringForIntTypeValue(currentCarrierSlotNo);

		maskData = MESTransportServiceProxy.getTransportJobServiceUtil().changeCurrentMaskLocation(maskData,
				currentMachineName, currentPositionType, currentPositionName, currentZoneName,
				currentCarrierName, currentCarrierSlotNo, transferState, "", eventInfo,returnCode,returnMessage);

		String originalSourceSubjectName = getOriginalSourceSubjectName();

		if (StringUtils.isNotEmpty(originalSourceSubjectName))
		{
			// Send Reply to OIC
			GenericServiceProxy.getESBServive().sendBySenderWithoutChangeReturnElement(originalSourceSubjectName, doc, "OICSender");
		}
	}
}
