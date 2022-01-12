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

public class ChangeMaskNote extends SyncHandler {
	public Object doWorks(Document doc) throws CustomException
	{

		String MaskName = SMessageUtil.getBodyItemValue(doc, "MASKNAME", true);
		String MaskNote = SMessageUtil.getBodyItemValue(doc, "MASKNOTE", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeMaskNote", this.getEventUser(), this.getEventComment(), "", "");

		Durable maskData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(MaskName);

		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.getUdfs().put("MASKNOTE", MaskNote);
		DurableServiceProxy.getDurableService().setEvent(maskData.getKey(), eventInfo, setEventInfo);

		return doc;
	}

}
