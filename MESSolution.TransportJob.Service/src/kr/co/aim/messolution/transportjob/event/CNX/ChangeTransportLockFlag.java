package kr.co.aim.messolution.transportjob.event.CNX;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

public class ChangeTransportLockFlag extends SyncHandler {
	private static Log log = LogFactory.getLog(ChangeTransportLockFlag.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String cstName = SMessageUtil.getBodyItemValue(doc, "DURABLENAME", true);
		String transportFlag = SMessageUtil.getBodyItemValue(doc, "TRANSPORTLOCKFLAG", true);

		EventInfo eventinfo = EventInfoUtil.makeEventInfo("ChangeTransportLockFlag", getEventUser(), getEventComment(), "", "");

		if (StringUtil.isNotEmpty(cstName))
		{
			Durable cstData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(cstName);

			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.getUdfs().put("TRANSPORTLOCKFLAG", transportFlag);

			DurableServiceProxy.getDurableService().setEvent(cstData.getKey(), eventinfo, setEventInfo);

			log.info("Change TransportLockFlag Complete ! ");
		}
		return doc;
	}

}
