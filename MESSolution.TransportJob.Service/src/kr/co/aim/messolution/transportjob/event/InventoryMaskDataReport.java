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
import org.jdom.Document;
import org.jdom.Element;

public class InventoryMaskDataReport extends AsyncHandler {

	/**
	 * MessageSpec [MCS -> TEX]
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
	 *       </MASK>
	 *    </MASKLIST>
	 * </Body>
	 */
	
	@SuppressWarnings("unchecked")
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("InventoryMaskDataReport", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String returnCode = SMessageUtil.getReturnItemValue(doc, "RETURNCODE", false);
		String returnMessage = SMessageUtil.getReturnItemValue(doc, "RETURNMESSAGE", false);

		// Mask List
		List<Element> maskList = SMessageUtil.getBodySequenceItemList(doc, "MASKLIST", true);

		String sql = "SELECT MASKLOTNAME FROM CT_MASKLOT WHERE MACHINENAME = :MACHINENAME ";
		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("MACHINENAME", machineName);
		List<Map<String, Object>> maskListInMachine = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

		for (Element maskE : maskList)
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

			// Set CarrierSlotNo
			currentCarrierSlotNo = ConvertUtil.toStringForIntTypeValue(currentCarrierSlotNo);
			
			MaskLot maskData = ExtendedObjectProxy.getMaskLotService().selectByKey(false, new Object[] { maskName });
			// Change Mask Location
			maskData = MESTransportServiceProxy.getTransportJobServiceUtil().changeCurrentMaskLocation(maskData,
					machineName, currentPositionType, currentPositionName, currentZoneName,
					currentCarrierName, currentCarrierSlotNo, maskData.getTransferState(), "", eventInfo,returnCode,returnMessage);
		}

		// Update Not reported CST Location
		eventInfo.setEventComment("Mask Not In"+ machineName);
		for (Map<String, Object> maskInMachine : maskListInMachine)
		{
			String maskName = ConvertUtil.getMapValueByName(maskInMachine, "MASKLOTNAME");
			MaskLot maskData = ExtendedObjectProxy.getMaskLotService().selectByKey(false, new Object[] { maskName });
//
//			if(maskData.getMachineName().equals(machineName))
//			{
//				maskData.setMachineName("");
//				maskData.setMaterialLocationName("");
//				maskData.setZoneName("");
//			    maskData.setCarrierName("");
//			    maskData.setPosition("");
//				maskData.setTransferState("");
//
//				ExtendedObjectProxy.getMaskLotService().modify(eventInfo, maskData);
//			}
//			else
//			{
				maskData.setLastEventComment("Mask Not In "+ machineName);				
				ExtendedObjectProxy.getMaskLotService().modify(eventInfo, maskData);
//			}
		}
	}
}
