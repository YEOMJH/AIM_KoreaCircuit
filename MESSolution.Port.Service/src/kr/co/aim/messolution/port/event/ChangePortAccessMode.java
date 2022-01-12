package kr.co.aim.messolution.port.event;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.info.MakeAccessModeInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class ChangePortAccessMode extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String sPortName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String sMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String sAccessMode = SMessageUtil.getBodyItemValue(doc, "ACCESSMODE", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeAccessMode", this.getEventUser(), this.getEventComment(), null, null);

		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(sMachineName, sPortName);

		if (StringUtils.equals(sAccessMode, portData.getAccessMode()))
		{
			throw new CustomException("MACHINE-0109", portData.getAccessMode());
		}

		MakeAccessModeInfo transitionInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portData, sAccessMode);

		MESPortServiceProxy.getPortServiceImpl().makeAccessMode(portData, transitionInfo, eventInfo);

		// success then report to FMC
		GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("FMB"), doc, "FMBSender");

		return doc;
	}

}
