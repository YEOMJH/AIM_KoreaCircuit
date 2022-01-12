package kr.co.aim.messolution.port.event;

import java.util.Map;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.info.SetEventInfo;

import org.jdom.Document;

public class ChangePortUseType extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String sPortName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String sMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeUseType", this.getEventUser(), this.getEventComment(), null, null);

		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(sMachineName, sPortName);

		Map<String, String> udfs = CommonUtil.setNamedValueSequence(SMessageUtil.getBodyElement(doc), Port.class.getSimpleName());

		SetEventInfo transitionInfo = MESPortServiceProxy.getPortInfoUtil().setEventInfo(udfs);
		MESPortServiceProxy.getPortServiceImpl().setEvent(portData, transitionInfo, eventInfo);

		return doc;
	}

}
