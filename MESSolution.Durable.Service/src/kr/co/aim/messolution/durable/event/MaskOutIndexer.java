package kr.co.aim.messolution.durable.event;

import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ReserveMaskList;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

public class MaskOutIndexer extends AsyncHandler {

	private static Log log = LogFactory.getLog(MaskOutIndexer.class);

	public void doWorks(Document doc) throws CustomException
	{

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", this.getEventUser(), this.getEventComment(), null, null);

		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
		String subUnitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false);
		String maskName = SMessageUtil.getBodyItemValue(doc, "MASKNAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", false);
		String maskCarrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);

		Map<String, String> udfs = new HashMap<String, String>();
		udfs.put("MACHINENAME", machineName);
		udfs.put("POSITIONNAME", unitName);
		udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().Dur_MOVING);

		maskCarrierName = "";
		String sMaskPosition = "";

		SetEventInfo setEventInfo = MESDurableServiceProxy.getDurableInfoUtil().setDeassignEVAMaskCSTInfo(maskCarrierName, sMaskPosition);
		setEventInfo.setUdfs(udfs);

		DurableKey durableKey = new DurableKey();
		durableKey.setDurableName(maskName);

		eventInfo = EventInfoUtil.makeEventInfo("IndexerOut", this.getEventUser(), this.getEventComment(), null, null);
		DurableServiceProxy.getDurableService().setEvent(durableKey, eventInfo, setEventInfo);

		eventInfo = EventInfoUtil.makeEventInfo("TrackIn", this.getEventUser(), this.getEventComment(), null, null);
		DurableServiceProxy.getDurableService().setEvent(durableKey, eventInfo, setEventInfo);

		eventInfo = EventInfoUtil.makeEventInfo("Deassign", this.getEventUser(), this.getEventComment(), null, null);
		DurableServiceProxy.getDurableService().setEvent(durableKey, eventInfo, setEventInfo);

	}
}
