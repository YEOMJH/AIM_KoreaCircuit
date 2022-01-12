package kr.co.aim.messolution.port.event;

import org.jdom.Document;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.info.MakeAccessModeInfo;

public class PortAccessModeChanged extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{

		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);

		String portAccessMode = SMessageUtil.getBodyItemValue(doc, "PORTACCESSMODE", true);

		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeAccessMode", getEventUser(), getEventComment(), null, null);

		MakeAccessModeInfo transitionInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portData, portAccessMode);

		MESPortServiceProxy.getPortServiceImpl().makeAccessMode(portData, transitionInfo, eventInfo);

		// success then report to FMB
		GenericServiceProxy.getESBServive().sendBySenderToFMB(doc);
	}
}
