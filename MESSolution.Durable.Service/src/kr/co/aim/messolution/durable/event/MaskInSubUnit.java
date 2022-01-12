package kr.co.aim.messolution.durable.event;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;

public class MaskInSubUnit extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", this.getEventUser(), this.getEventComment(), null, null);

		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String subUnitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", true);
		String maskName = SMessageUtil.getBodyItemValue(doc, "MASKNAME", true);
		String fromSlotId = SMessageUtil.getBodyItemValue(doc, "FROMSLOTID", false);
		String toSlotId = SMessageUtil.getBodyItemValue(doc, "TOSLOTID", false);

		MachineSpec machineSpec = CommonUtil.getMachineSpecByMachineName(machineName);

		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.getUdfs().put("MACHINENAME", machineName);
		setEventInfo.getUdfs().put("UNITNAME", unitName);
		setEventInfo.getUdfs().put("POSITIONNAME", subUnitName);
		setEventInfo.getUdfs().put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().Dur_PROCESSING);
		setEventInfo.getUdfs().put("MASKPOSITION", toSlotId);

		DurableKey durableKey = new DurableKey();
		durableKey.setDurableName(maskName);

		eventInfo = EventInfoUtil.makeEventInfo("SubUnitIn", this.getEventUser(), this.getEventComment(), null, null);
		DurableServiceProxy.getDurableService().setEvent(durableKey, eventInfo, setEventInfo);
		
	}
}
