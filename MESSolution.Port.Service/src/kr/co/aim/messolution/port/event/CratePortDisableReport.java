package kr.co.aim.messolution.port.event;

import java.util.HashMap;
import java.util.Map;

import org.jdom.Document;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.info.MakeAccessModeInfo;
import kr.co.aim.greentrack.port.management.info.SetEventInfo;

public class CratePortDisableReport extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{

		String sPortName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String sMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);

		String sAccessMode = SMessageUtil.getBodyItemValue(doc, "PORTACCESSMODE", true);
		String sPortType = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", true);
		String sPortUseType = SMessageUtil.getBodyItemValue(doc, "PORTUSETYPE", true);

		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(sMachineName, sPortName);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeAccessMode", getEventUser(), getEventComment(), null, null);
		MakeAccessModeInfo transitionInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portData, sAccessMode);
		MESPortServiceProxy.getPortServiceImpl().makeAccessMode(portData, transitionInfo, eventInfo);

		// change port type
		if (CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals(sPortType))
		{
			eventLog.warn(String.format("Attribute[%s] is still [%s]", "PORTTYPE", sPortType));
		}
		else
		{
			// eventInfo.setEventName("ChangeType");
			eventInfo = EventInfoUtil.makeEventInfo("ChangeType", eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);

			Map<String, String> udfs = new HashMap<String, String>();
			udfs.put("PORTTYPE", sPortType);
			
			SetEventInfo setEventInfo = MESPortServiceProxy.getPortInfoUtil().setEventInfo(udfs);
			MESPortServiceProxy.getPortServiceImpl().setEvent(portData, setEventInfo, eventInfo);
		}

		// change port use type
		if (CommonUtil.getValue(portData.getUdfs(), "PORTUSETYPE").equals(sPortUseType))
		{
			eventLog.warn(String.format("Attribute[%s] is still [%s]", "PORTUSETYPE", sPortUseType));
		}
		else
		{
			// eventInfo.setEventName("ChangeUseType");
			eventInfo = EventInfoUtil.makeEventInfo("ChangeUseType", eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);

			Map<String, String> udfs = new HashMap<String, String>();
			udfs.put("PORTUSETYPE", sPortUseType);
			
			SetEventInfo setEventInfo = MESPortServiceProxy.getPortInfoUtil().setEventInfo(udfs);
			MESPortServiceProxy.getPortServiceImpl().setEvent(portData, setEventInfo, eventInfo);
		}
	}
}
