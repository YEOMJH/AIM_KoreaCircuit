package kr.co.aim.messolution.durable.event.CNX;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class ChangeMaskTransportState extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String messageName = SMessageUtil.getMessageName(doc);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo(messageName, this.getEventUser(), this.getEventComment(), "", "");

		String sDurableName = SMessageUtil.getBodyItemValue(doc, "DURABLENAME", true);
		String sTransportState = SMessageUtil.getBodyItemValue(doc, "TRANSPORTSTATE", false);

		// getDurableData
		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sDurableName);

		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.getUdfs().put("TRANSPORTSTATE", sTransportState);
		
		if (sTransportState.equalsIgnoreCase("UNKNOWN"))
			setEventInfo.getUdfs().put("MACHINENAME", "");

		// Excute greenTrack API call- setEvent
		DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEventInfo);

		return doc;
	}

}
