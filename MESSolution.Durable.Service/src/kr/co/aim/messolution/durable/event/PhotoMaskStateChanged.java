package kr.co.aim.messolution.durable.event;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

public class PhotoMaskStateChanged extends AsyncHandler {
	private static Log log = LogFactory.getLog(PhotoMaskStateChanged.class);

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String smachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String sDurableName = SMessageUtil.getBodyItemValue(doc, "MASKNAME", true);
		String sTransportState = SMessageUtil.getBodyItemValue(doc, "TRANSPORTSTATE", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeState", getEventUser(), getEventComment(), "", "");

		// getDurableData
		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sDurableName);

		// SetEvent Info create
		SetEventInfo setEventInfo = new SetEventInfo();

		setEventInfo.getUdfs().put("MACHINENAME", smachineName);
		setEventInfo.getUdfs().put("TRANSPORTSTATE", sTransportState);

		if (sTransportState.equalsIgnoreCase("UNKNOWN"))
			setEventInfo.getUdfs().put("MACHINENAME", "");

		// Excute greenTrack API call- setEvent
		MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
		log.info("DurableName = " + sDurableName + "Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());

	}

}
