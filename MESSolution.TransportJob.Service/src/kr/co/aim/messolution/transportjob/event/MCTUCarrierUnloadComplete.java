package kr.co.aim.messolution.transportjob.event;

import java.util.HashMap;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetAreaInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class MCTUCarrierUnloadComplete extends AsyncHandler {

	/**
	 * MessageSpec [MCS -> TEX -> FMC]
	 * 
	 * <Body>
	 *    <CARRIERNAME />
	 *    <MACHINENAME />
	 *    <PORTNAME />
	 * </Body>
	 */
	
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Unload", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);

		// change carrier TransferState & LocationInfo
		Durable carrierData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);

		Map<String, String> carrierUdfs = new HashMap<String, String>();
		carrierUdfs.put("MACHINENAME", "");
		carrierUdfs.put("PORTNAME", "");
		carrierUdfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().Dur_MOVING);
		carrierUdfs.put("POSITIONTYPE", "");
		carrierUdfs.put("POSITIONNAME", "");
		carrierUdfs.put("ZONENAME", "");

		SetAreaInfo setAreaInfo = new SetAreaInfo();
		setAreaInfo.setAreaName("");
		setAreaInfo.setUdfs(carrierUdfs);

		EventInfo setAreaEventInfo = EventInfoUtil.makeEventInfo(eventInfo.getEventName(), eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);
		DurableServiceProxy.getDurableService().setArea(carrierData.getKey(), setAreaEventInfo, setAreaInfo);

		// change portInfo
		MESPortServiceProxy.getPortServiceUtil().unLoadComplete(eventInfo, machineName, portName);

		// append full state
		Element eleFullState = new Element("FULLSTATE");
		eleFullState.setText("EMPTY");
		SMessageUtil.getBodyElement(doc).addContent(eleFullState);

		// success then report to FMB
		GenericServiceProxy.getESBServive().sendBySenderToFMB(doc);
	}
}