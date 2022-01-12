package kr.co.aim.messolution.durable.event;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

public class CSTInUnit extends AsyncHandler {

	private static Log log = LogFactory.getLog(CSTInUnit.class);

	@Override
	public void doWorks(Document doc) throws CustomException
	{

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("UnitIn", this.getEventUser(), this.getEventComment(), null, null);

		String lotName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);

		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(lotName);

		SetEventInfo setEventInfo = new SetEventInfo();

		setEventInfo.getUdfs().put("MACHINENAME", machineName);
		setEventInfo.getUdfs().put("UNITNAME", unitName);
		setEventInfo.getUdfs().put("PORTNAME", "");

		durableData = DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEventInfo);
	}

}
