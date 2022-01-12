package kr.co.aim.messolution.durable.event.OledMask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.management.data.Port;

public class OLEDMaskCSTInfoReport extends AsyncHandler {

	private static Log log = LogFactory.getLog(OLEDMaskCSTInfoReport.class);

	public void doWorks(Document doc) throws CustomException
	{

		/*
		 * MACHINENAME UNITNAME CARRIERNAME PORTNAME PORTTYPE PORTUSETYPE MASKQUANTITY SLOTMAP MASKTLIST MASK MASKNAME MASKGROUPNAME POSITION MASKRECIPENAME
		 */
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String subUnitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false);
		String maskPosition = SMessageUtil.getBodyItemValue(doc, "MASKPOSITION", false);
		String maskState = SMessageUtil.getBodyItemValue(doc, "MASKSTATE", false);
		String maskUsedCount = SMessageUtil.getBodyItemValue(doc, "MASKUSEDCOUNT", false);
		List<Element> maskLotList = SMessageUtil.getBodySequenceItemList(doc, "MASKLIST", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("MaskStateChanged", this.getEventUser(), this.getEventComment(), null, null);

		if (maskLotList != null && maskLotList.size() > 0)
		{
			for (int i = 0; i < maskLotList.size(); i++)
			{
				String maskLotName = maskLotList.get(i).getChildText("MASKNAME");
				String position = maskLotList.get(i).getChildText("POSITION");
				String maskRecipeName = maskLotList.get(i).getChildText("MASKRECIPENAME");

				MaskLot maskData = ExtendedObjectProxy.getMaskLotService().selectByKey(true, new Object[] { maskLotName });

				// Set data
				maskData.setMachineName(machineName);
				maskData.setMaterialLocationName(unitName);
				maskData.setChamberName(subUnitName);
				maskData.setPosition(maskPosition);
				maskData.setLastEventUser(eventInfo.getEventUser());
				maskData.setLastEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
				maskData.setLastEventName(eventInfo.getEventName());
				maskData.setLastEventComment(eventInfo.getEventComment());
				maskData.setLastEventTime(eventInfo.getEventTime());

				ExtendedObjectProxy.getMaskLotService().modify(eventInfo, maskData);
			}
		}
	}
}
