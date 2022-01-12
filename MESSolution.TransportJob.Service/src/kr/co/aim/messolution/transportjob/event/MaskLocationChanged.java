package kr.co.aim.messolution.transportjob.event;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.extended.object.management.data.TransportJobCommand;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.transportjob.MESTransportServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class MaskLocationChanged extends AsyncHandler {

	/**
	 * MessageSpec [MCS -> TEX]
	 * 
	 * <Body>
	 *    <TRANSPORTJOBNAME />
	 *    <MASKNAME />
	 *    <CURRENTMACHINENAME />
	 *    <CURRENTPOSITIONTYPE />
	 *    <CURRENTPOSITIONNAME />
	 *    <CURRENTZONENAME />
	 *    <CURRENTCARRIERNAME />
	 *    <CURRENTCARRIERSLOTNO />
	 *    <MASKTYPE />
	 *    <TRANSFERSTATE />
	 *    <ALTERNATEFLAG />
	 * </Body>
	 */
	
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeLoc", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		String transportJobName = SMessageUtil.getBodyItemValue(doc, "TRANSPORTJOBNAME", false);
		String maskName = SMessageUtil.getBodyItemValue(doc, "MASKNAME", false);
		String currentMachineName = SMessageUtil.getBodyItemValue(doc, "CURRENTMACHINENAME", false);
		String currentPositionType = SMessageUtil.getBodyItemValue(doc, "CURRENTPOSITIONTYPE", false);
		String currentPositionName = SMessageUtil.getBodyItemValue(doc, "CURRENTPOSITIONNAME", false);
		String currentZoneName = SMessageUtil.getBodyItemValue(doc, "CURRENTZONENAME", false);
		String currentCarrierName = SMessageUtil.getBodyItemValue(doc, "CURRENTCARRIERNAME", false);
		String currentCarrierSlotNo = SMessageUtil.getBodyItemValue(doc, "CURRENTCARRIERSLOTNO", false);
		String transferState = SMessageUtil.getBodyItemValue(doc, "TRANSFERSTATE", false);
		String returnCode = SMessageUtil.getReturnItemValue(doc, "RETURNCODE", false);
		String returnMessage = SMessageUtil.getReturnItemValue(doc, "RETURNMESSAGE", false);

		// 1. Check Exist Mask
		MaskLot maskData = ExtendedObjectProxy.getMaskLotService().selectByKey(false, new Object[] { maskName });

		// Set CarrierSlotNo
		currentCarrierSlotNo = ConvertUtil.toStringForIntTypeValue(currentCarrierSlotNo);

		// 2. If Transport Job is Null, Change Mask Location Only
		if (StringUtils.isEmpty(transportJobName))
		{
			// Change Mask Location
			maskData = MESTransportServiceProxy.getTransportJobServiceUtil().changeCurrentMaskLocation(maskData,
					currentMachineName, currentPositionType, currentPositionName, currentZoneName, currentCarrierName,
					currentCarrierSlotNo, transferState, "", eventInfo,returnCode,returnMessage);

			return;
		}

		TransportJobCommand sqlRow = new TransportJobCommand();
		try
		{
			sqlRow = ExtendedObjectProxy.getTransportJobCommand().selectByKey(true, new Object[] { transportJobName });
		}
		catch (Exception e)
		{
			eventInfo.setEventComment("Requested");
			TransportJobCommand dataInfo = new TransportJobCommand();
			dataInfo.setTransportJobName(transportJobName);
			dataInfo.setTransportJobType("N/A");
			dataInfo.setCarrierName(maskName);

			ExtendedObjectProxy.getTransportJobCommand().create(eventInfo, dataInfo);
			eventInfo.setEventComment("ChangeLoc");
		}

		sqlRow = ExtendedObjectProxy.getTransportJobCommand().selectByKey(true, new Object[] { transportJobName });

		List<TransportJobCommand> sqlResult = new ArrayList<TransportJobCommand>();
		sqlResult.add(sqlRow);

		MESTransportServiceProxy.getTransportJobServiceUtil().checkExistTransportJobCommand(sqlResult, transportJobName);

		// Update CT_TRANSPORTJOBCOMMAND
		MESTransportServiceProxy.getTransportJobServiceUtil().updateTransportJobCommand(transportJobName, doc, eventInfo);

		// Change Mask Location
		maskData = MESTransportServiceProxy.getTransportJobServiceUtil().changeCurrentMaskLocation(maskData,
				currentMachineName, currentPositionType, currentPositionName, currentZoneName, currentCarrierName,
				currentCarrierSlotNo, transferState, "", eventInfo,returnCode,returnMessage);
	}
}