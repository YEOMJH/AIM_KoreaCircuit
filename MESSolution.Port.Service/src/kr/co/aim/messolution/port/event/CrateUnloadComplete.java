package kr.co.aim.messolution.port.event;

import org.jdom.Document;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.port.management.data.Port;

public class CrateUnloadComplete extends AsyncHandler {
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Unload", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String cateName = SMessageUtil.getBodyItemValue(doc, "CRATENAME", true);

		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);

		MESConsumableServiceProxy.getConsumableServiceUtil().changeConsumableLocation(eventInfo, cateName, portData.getAreaName(), machineName, portName);

		// change portInfo
		MESPortServiceProxy.getPortServiceUtil().unLoadComplete(eventInfo, machineName, portName);
	}
}