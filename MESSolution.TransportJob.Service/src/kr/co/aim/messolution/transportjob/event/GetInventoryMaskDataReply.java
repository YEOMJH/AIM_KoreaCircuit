package kr.co.aim.messolution.transportjob.event;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class GetInventoryMaskDataReply extends AsyncHandler {

	/**
	 * MessageSpec [MCS -> TEX -> OIC]
	 * 
	 * <Body>
	 *    <MACHINENAME />
	 *    <MASKLIST>
	 *       <MASK>
	 *          <MASKNAME />
	 *          <CURRENTPOSITIONTYPE />
	 *          <CURRENTPOSITIONNAME />
	 *          <CURRENTZONENAME />
	 *          <CURRENTCARRIERNAME />
	 *          <CURRENTCARRIERSLOTNO />
	 *          <MASKTYPE />
	 *       <MASK>
	 *    <MASKLIST>
	 * </Body>
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("GetInventoryMaskDataReply", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		String sql = "";
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String returnCode = SMessageUtil.getReturnItemValue(doc, "RETURNCODE", false);
		String returnMessage = SMessageUtil.getReturnItemValue(doc, "RETURNMESSAGE", false);

		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("MACHINENAME", machineName);

		// Mask List
		List<Element> maskList = SMessageUtil.getBodySequenceItemList(doc, "MASKLIST", true);
		sql = "SELECT MASKLOTNAME FROM CT_MASKLOT WHERE MACHINENAME = :MACHINENAME ";

		List<Map<String, Object>> maskListInMachine = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

		for (Element maskE : maskList)
		{
			try
			{
				String maskName = maskE.getChildText("MASKNAME");
				maskName = MESTransportServiceProxy.getTransportJobServiceUtil().unknownCarrierChangeName(maskName);

				// for GarbageCarrier Location Update
				ListOrderedMap listOrderMap = new ListOrderedMap();
				listOrderMap.put("MASKLOTNAME", maskName);

				if (maskListInMachine.contains(listOrderMap))
				{
					maskListInMachine.remove(listOrderMap);
				}

				String currentPositionType = maskE.getChildText("CURRENTPOSITIONTYPE");
				String currentPositionName = maskE.getChildText("CURRENTPOSITIONNAME");
				String currentZoneName = maskE.getChildText("CURRENTZONENAME");
				String currentCarrierName = maskE.getChildText("CURRENTCARRIERNAME");
				String currentCarrierSlotNo = maskE.getChildText("CURRENTCARRIERSLOTNO");
				String transferState = GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_INSTK;

				// Set TransferState
				if (StringUtils.equals(currentPositionType, GenericServiceProxy.getConstantMap().MCS_POSITIONTYPE_PORT))
				{
					if (StringUtils.isEmpty(currentCarrierName) || StringUtils.isEmpty(currentCarrierSlotNo))
					{
						continue;
					}

					transferState = GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONCAU;
				}

				// Set CarrierSlotNo
				currentCarrierSlotNo = ConvertUtil.toStringForIntTypeValue(currentCarrierSlotNo);

				MaskLot maskData = ExtendedObjectProxy.getMaskLotService().selectByKey(false, new Object[] { maskName });

				maskData = MESTransportServiceProxy.getTransportJobServiceUtil().changeCurrentMaskLocation(maskData,
						machineName, currentPositionType, currentPositionName, currentZoneName,
						currentCarrierName, currentCarrierSlotNo, transferState, "", eventInfo,returnCode,returnMessage);
			}
			catch (Exception e)
			{
				eventLog.info("Error Occurred to update Location for Reported Mask. Mask ID: " + maskE.getChildText("MASKNAME"));
			}
		}

		// Update Not reported CST Location
		for (Map<String, Object> maskInMachine : maskListInMachine)
		{
			String maskName = ConvertUtil.getMapValueByName(maskInMachine, "MASKLOTNAME");

			try
			{
				MaskLot maskData = ExtendedObjectProxy.getMaskLotService().selectByKey(false, new Object[] { maskName });

				if (StringUtils.equals(maskData.getMachineName(), machineName) && StringUtils.equals(maskData.getTransferState(), "INSTK"))
				{
//					maskData.setMachineName("");
//					maskData.setMaterialLocationName("");
//					maskData.setZoneName("");
//					maskData.setTransferState("MOVING");
					maskData.setLastEventComment("Mask Not In "+ machineName);	
					ExtendedObjectProxy.getMaskLotService().modify(eventInfo, maskData);
				}
			}
			catch (Exception e)
			{
				eventLog.info("Error Occurred to update Location for Not Reported Mask. Mask ID: " + maskName);
			}
		}

		String originalSourceSubjectName = getOriginalSourceSubjectName();

		if (StringUtils.isNotEmpty(originalSourceSubjectName))
		{
			// Send Reply to OIC
			GenericServiceProxy.getESBServive().sendBySenderWithoutChangeReturnElement(originalSourceSubjectName, doc, "OICSender");
		}
	}
}
