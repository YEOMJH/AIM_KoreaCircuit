package kr.co.aim.messolution.port.event;

import org.jdom.Document;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.info.MakeTransferStateInfo;

public class ChangePortTransferState extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{

		String sPortName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String sMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);

		String sTransferStateName = SMessageUtil.getBodyItemValue(doc, "TRANSFERSTATE", true);

		String sReasonCodeType = SMessageUtil.getBodyItemValue(doc, "REASONCODETYPE", false);
		String sReasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODE", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeTransferState", this.getEventUser(), this.getEventComment(), sReasonCodeType, sReasonCode);

		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(sMachineName, sPortName);

		MakeTransferStateInfo transitionInfo = MESPortServiceProxy.getPortInfoUtil().makeTransferStateInfo(portData, sTransferStateName);

		MESPortServiceProxy.getPortServiceImpl().makeTransferState(portData, transitionInfo, eventInfo);

		return doc;
	}

}
