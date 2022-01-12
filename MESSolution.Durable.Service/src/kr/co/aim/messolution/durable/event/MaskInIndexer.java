package kr.co.aim.messolution.durable.event;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class MaskInIndexer extends AsyncHandler {

	private static Log log = LogFactory.getLog(MaskInIndexer.class);

	@Override
	public void doWorks(Document doc) throws CustomException
	{

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("IndexerIn", this.getEventUser(), this.getEventComment(), null, null);

		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
		String subUnitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false);
		String maskName = SMessageUtil.getBodyItemValue(doc, "MASKNAME", true);
		String fromSlotId = SMessageUtil.getBodyItemValue(doc, "FROMSLOTID", false);
		String toSlotId = SMessageUtil.getBodyItemValue(doc, "TOSLOTID", false);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String maskCarrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);

		toSlotId = toSlotId.replaceFirst("^0+", "");

		Durable durMaskData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(maskName);

		SetEventInfo setEventInfo = new SetEventInfo();

		setEventInfo.getUdfs().put("MACHINENAME", machineName);
		setEventInfo.getUdfs().put("UNITNAME", unitName);
		setEventInfo.getUdfs().put("POSITIONNAME", subUnitName);
		setEventInfo.getUdfs().put("PORTNAME", portName);
		setEventInfo.getUdfs().put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().Dur_MOVING);
		setEventInfo.getUdfs().put("MASKCARRIERNAME", maskCarrierName);
		setEventInfo.getUdfs().put("MASKPOSITION", toSlotId);

		DurableKey durableKey = new DurableKey();
		durableKey.setDurableName(maskName);

		MESDurableServiceProxy.getDurableServiceImpl().setEvent(durMaskData, setEventInfo, eventInfo);

		eventInfo = EventInfoUtil.makeEventInfo("Assign", this.getEventUser(), this.getEventComment(), null, null);

		DurableServiceProxy.getDurableService().setEvent(durableKey, eventInfo, setEventInfo);

		Durable maskCSTData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(maskCarrierName);

		maskCSTData.setDurableState("InUse");
		DurableServiceProxy.getDurableService().update(maskCSTData);

		setEventInfo = new SetEventInfo();

		eventInfo = EventInfoUtil.makeEventInfo("Assign", this.getEventUser(), this.getEventComment(), null, null);
		DurableServiceProxy.getDurableService().setEvent(maskCSTData.getKey(), eventInfo, setEventInfo);
	}

}
