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

public class CSTOutUnit extends AsyncHandler {

	private static Log log = LogFactory.getLog(CSTOutUnit.class);

	@Override
	public void doWorks(Document doc) throws CustomException
	{

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("UnitOut", this.getEventUser(), this.getEventComment(), null, null);

		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);

		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);

		SetEventInfo setEventInfo = new SetEventInfo();

		setEventInfo.getUdfs().put("MACHINENAME", machineName);
		setEventInfo.getUdfs().put("UNITNAME", unitName);

		durableData = DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEventInfo);
	}

}
