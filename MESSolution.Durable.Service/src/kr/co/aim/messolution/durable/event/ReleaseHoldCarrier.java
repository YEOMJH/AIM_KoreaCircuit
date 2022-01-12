package kr.co.aim.messolution.durable.event;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class ReleaseHoldCarrier extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String messageName = SMessageUtil.getMessageName(doc);
		String sDurableName = SMessageUtil.getBodyItemValue(doc, "DURABLENAME", true);
		String sDurableHoldState = SMessageUtil.getBodyItemValue(doc, "DURABLEHOLDSTATE", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo(messageName, getEventUser(), getEventComment(), "", "");

		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sDurableName);

		CommonValidation.CheckDurableNotHoldState(durableData);
		
		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.getUdfs().put("DURABLEHOLDSTATE", sDurableHoldState);

		MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);

		return doc;
	}

}
