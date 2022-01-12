package kr.co.aim.messolution.durable.event;

import java.util.Map;

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

public class ChangeCSTType extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		String changeType = SMessageUtil.getBodyItemValue(doc, "CHANGETYPE", true);

		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeCSTType", getEventUser(), getEventComment(), "", "");

		Map<String, String> udfs = durableData.getUdfs();

		if (!durableData.getDurableState().equals("Available"))
			throw new CustomException("DURABLE-0006");
		
		if (!udfs.get("DURABLEHOLDSTATE").equals("N"))
			throw new CustomException("DURABLE-0007");

		durableData.setDurableType(changeType);

		DurableServiceProxy.getDurableService().update(durableData);
		SetEventInfo setEventInfo = new SetEventInfo();

		setEventInfo.getUdfs().put("DURABLETYPE", changeType);
		setEventInfo.getUdfs().put("DURABLETYPE1", changeType);
		
		MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);

		return doc;
	}

}
