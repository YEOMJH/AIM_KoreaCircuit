package kr.co.aim.messolution.durable.event.CNX;

import java.util.List;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class ReleaseHoldShieldCarrier extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> durableList = SMessageUtil.getBodySequenceItemList(doc, "DURABLELIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("HoldShieldCarrier", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());

		for (Element durable : durableList)
		{
			String durableName = SMessageUtil.getChildText(durable, "DURABLENAME", true);
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableName);

			CommonValidation.CheckDurableNotHoldState(durableData);
			
			eventInfo.setReasonCodeType("");
			eventInfo.setReasonCode("");

			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.getUdfs().put("DURABLEHOLDSTATE", "N");

			DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEventInfo);
		}

		return doc;
	}
}