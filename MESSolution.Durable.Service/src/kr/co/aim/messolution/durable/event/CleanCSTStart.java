package kr.co.aim.messolution.durable.event;

import java.util.Map;
import org.jdom.Document;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;

public class CleanCSTStart extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String sCarrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		String sMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String sPortName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", false);

		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sCarrierName);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TrackIn", getEventUser(), getEventComment(), "", "");

		SetEventInfo setEventInfo = new SetEventInfo();

		setEventInfo.getUdfs().put("MACHINENAME", sMachineName);
		setEventInfo.getUdfs().put("PORTNAME", sPortName);

		MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
	}

}
