package kr.co.aim.messolution.durable.event;

import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetAreaInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class ChangeCSTLocation extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String messageName = SMessageUtil.getMessageName(doc);
		String sDurableName = SMessageUtil.getBodyItemValue(doc, "DURABLENAME", true);
		String sAreaName = SMessageUtil.getBodyItemValue(doc, "AREANAME", true);
		String sMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", false);
		String sPortName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", false);
		String sTransportState = SMessageUtil.getBodyItemValue(doc, "TRANSPORTSTATE", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo(messageName, getEventUser(), getEventComment(), "", "");

		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sDurableName);

		Map<String, String> udfs = new HashMap<String, String>();
		udfs.put("MACHINENAME", sMachineName);
		udfs.put("PORTNAME", sPortName);
		udfs.put("TRANSPORTSTATE", sTransportState);

		SetAreaInfo areaInfo = MESDurableServiceProxy.getDurableInfoUtil().AreaInfo(sAreaName, udfs);

		MESDurableServiceProxy.getDurableServiceImpl().setArea(durableData, areaInfo, eventInfo);

		return doc;
	}

}
