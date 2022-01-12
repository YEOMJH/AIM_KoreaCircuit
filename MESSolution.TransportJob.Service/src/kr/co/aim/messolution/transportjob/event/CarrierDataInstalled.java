package kr.co.aim.messolution.transportjob.event;

import java.util.HashMap;
import java.util.Map;

import org.jdom.Document;

import kr.co.aim.messolution.transportjob.MESTransportServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetAreaInfo;

public class CarrierDataInstalled extends AsyncHandler {

	/**
	 * MessageSpec [MCS -> TEX]
	 * 
	 * <Body>
	 *    <CARRIERNAME />
	 *    <CURRENTMACHINENAME />
	 *    <CURRENTPOSITIONTYPE />
	 *    <CURRENTPOSITIONNAME />
	 *    <CURRENTZONENAME />
	 *    <CARRIERSTATE />
	 *    <TRANSFERSTATE />
	 * </Body>
	 */
	
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		// CarrierDataInstalled Transaction
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "CURRENTMACHINENAME", true);
		String currentPositionType = SMessageUtil.getBodyItemValue(doc, "CURRENTPOSITIONTYPE", false);
		String currentPositionName = SMessageUtil.getBodyItemValue(doc, "CURRENTPOSITIONNAME", false);
		String currentZoneName = SMessageUtil.getBodyItemValue(doc, "CURRENTZONENAME", false);
		String transferState = SMessageUtil.getBodyItemValue(doc, "TRANSFERSTATE", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CarrierDataInstalled", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		carrierName = MESTransportServiceProxy.getTransportJobServiceUtil().unknownCarrierChangeName(carrierName);

		Durable durableData = CommonValidation.checkExistCarrier(carrierName);
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);

		Map<String, String> udfs = new HashMap<String, String>();

		udfs.put("MACHINENAME", machineName);
		udfs.put("POSITIONTYPE", currentPositionType);
		udfs.put("ZONENAME", currentZoneName);
		udfs.put("CARRIERNAME", carrierName);
		udfs.put("TRANSPORTSTATE", transferState);

		if (currentPositionType.equals(GenericServiceProxy.getConstantMap().MCS_POSITIONTYPE_PORT))
		{
			udfs.put("PORTNAME", currentPositionName);
			udfs.put("POSITIONNAME", "");
		}
		else
		{
			udfs.put("PORTNAME", "");
			udfs.put("POSITIONNAME", currentPositionName);
		}

		SetAreaInfo areaInfo = MESDurableServiceProxy.getDurableInfoUtil().AreaInfo(machineData.getAreaName(), udfs);
		MESDurableServiceProxy.getDurableServiceImpl().setArea(durableData, areaInfo, eventInfo);
	}
}
