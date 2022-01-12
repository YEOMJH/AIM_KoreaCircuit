package kr.co.aim.messolution.port.event;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.info.MakePortStateByStateInfo;

import org.jdom.Document;

public class ChangePortState extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{

		String sPortName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String sMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);

		String sPortStateName = SMessageUtil.getBodyItemValue(doc, "PORTSTATENAME", true);

		String sReasonCodeType = SMessageUtil.getBodyItemValue(doc, "REASONCODETYPE", false);
		String sReasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODE", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangePortState", this.getEventUser(), this.getEventComment(), sReasonCodeType, sReasonCode);

		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(sMachineName, sPortName);

		MakePortStateByStateInfo portInfo = MESPortServiceProxy.getPortInfoUtil().makePortStateByStateInfo(portData, sPortStateName);

		MESPortServiceProxy.getPortServiceImpl().makePortStateByState(portData, portInfo, eventInfo);

		return doc;
	}

}
