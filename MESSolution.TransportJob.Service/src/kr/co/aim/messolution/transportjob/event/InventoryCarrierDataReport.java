package kr.co.aim.messolution.transportjob.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.transportjob.MESTransportServiceProxy;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetAreaInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.management.data.Machine;

import org.apache.commons.collections.map.ListOrderedMap;
import org.jdom.Document;
import org.jdom.Element;

public class InventoryCarrierDataReport extends AsyncHandler {

	/**
	 * MessageSpec [MCS -> TEX]
	 * 
	 * <Body>
	 *    <MACHINENAME />
	 *    <CARRIERLIST>
	 *       <CARRIER>
	 *          <CARRIERNAME />
	 *          <CURRENTPOSITIONTYPE />
	 *          <CURRENTPOSITIONNAME />
	 *          <CURRENTZONENAME />
	 *          <CARRIERSTATE />
	 *       </CARRIER>
	 *    </CARRIERLIST>
	 * </Body>
	 */
	
	@SuppressWarnings("unchecked")
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("InventoryCarrierDataReport", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		List<Element> carrierList = SMessageUtil.getBodySequenceItemList(doc, "CARRIERLIST", false);

		if (carrierList == null || carrierList.size() == 0)
		{
			eventLog.info("<CARRIERLIST> is Empty.");
			return ;
		}
		
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);

		String sql = "SELECT DURABLENAME FROM DURABLE WHERE MACHINENAME = :MACHINENAME ";
		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("MACHINENAME", machineName);

		List<Map<String, Object>> carrierListInMachine = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

		for (Element carrierE : carrierList)
		{
			String carrierName = carrierE.getChildText("CARRIERNAME");
			carrierName = MESTransportServiceProxy.getTransportJobServiceUtil().unknownCarrierChangeName(carrierName);
			// for GarbageCarrier Location Update
			ListOrderedMap listOrderMap = new ListOrderedMap();
			listOrderMap.put("DURABLENAME", carrierName);

			if (carrierListInMachine.contains(listOrderMap))
			{
				carrierListInMachine.remove(listOrderMap);
			}

			String currentPositionType = carrierE.getChildText("CURRENTPOSITIONTYPE");
			String currentPositionName = carrierE.getChildText("CURRENTPOSITIONNAME");
			String currentZoneName = carrierE.getChildText("CURRENTZONENAME");

			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);

			Map<String, String> durableUdfs = new HashMap<String, String>();
			durableUdfs.put("MACHINENAME", machineName);

			if (GenericServiceProxy.getConstantMap().MCS_POSITIONTYPE_PORT.equals(currentPositionType))
			{
				durableUdfs.put("PORTNAME", currentPositionName);
				durableUdfs.put("POSITIONTYPE", currentPositionType);
				durableUdfs.put("POSITIONNAME", "");
				durableUdfs.put("ZONENAME", "");
			}
			else if (GenericServiceProxy.getConstantMap().MCS_POSITIONTYPE_SHELF.equals(currentPositionType))
			{
				durableUdfs.put("PORTNAME", "");
				durableUdfs.put("POSITIONTYPE", currentPositionType);
				durableUdfs.put("POSITIONNAME", currentPositionName);
				durableUdfs.put("ZONENAME", currentZoneName);
			}

			// SetArea Info
			SetAreaInfo setAreaInfo = new SetAreaInfo();
			setAreaInfo.setAreaName(machineData.getAreaName());
			setAreaInfo.setUdfs(durableUdfs);

			DurableServiceProxy.getDurableService().setArea(durableData.getKey(), eventInfo, setAreaInfo);
		}

		// Update Not reported CST Location
		for (Map<String, Object> carrierInMachine : carrierListInMachine)
		{
			String carrierName = ConvertUtil.getMapValueByName(carrierInMachine, "DURABLENAME");
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
			Map<String, String> durableUdfs = new HashMap<String, String>();

			durableUdfs.put("MACHINENAME", "");
			durableUdfs.put("PORTNAME", "");
			durableUdfs.put("POSITIONNAME", "");
			durableUdfs.put("POSITIONTYPE", "");
			durableUdfs.put("ZONENAME", "");

			SetAreaInfo setAreaInfo = new SetAreaInfo();
			setAreaInfo.setAreaName(machineData.getAreaName());
			setAreaInfo.setUdfs(durableUdfs);

			DurableServiceProxy.getDurableService().setArea(durableData.getKey(), eventInfo, setAreaInfo);
		}
	}
}
