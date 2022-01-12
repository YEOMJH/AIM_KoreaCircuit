package kr.co.aim.messolution.port.event;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.info.MakeAccessModeInfo;
import kr.co.aim.greentrack.port.management.info.SetEventInfo;

public class PortDisableReport extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{

		String sPortName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String sMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);

		String sAccessMode = SMessageUtil.getBodyItemValue(doc, "PORTACCESSMODE", true);
		String sPortType = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", true);
		String sPortUseType = SMessageUtil.getBodyItemValue(doc, "PORTUSETYPE", true);

		MachineSpec machineSpec = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(sMachineName);
		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(sMachineName, sPortName);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeAccessMode", getEventUser(), getEventComment(), null, null);

		MakeAccessModeInfo transitionInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portData, sAccessMode);
		MESPortServiceProxy.getPortServiceImpl().makeAccessMode(portData, transitionInfo, eventInfo);

		// change port type
		if (StringUtils.equalsIgnoreCase(machineSpec.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Sorter))
		{
			eventLog.info("Skip Change PortType for MachineGroupName SORTER");
		}
		else
		{
			if (CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals(sPortType))
			{
				eventLog.warn(String.format("Attribute[%s] is still [%s]", "PORTTYPE", sPortType));
			}
			else
			{
				eventInfo.setEventName("ChangeType");

				Map<String, String> udfs = new HashMap<String, String>();
				udfs.put("PORTTYPE", sPortType);
				
				SetEventInfo setEventInfo = MESPortServiceProxy.getPortInfoUtil().setEventInfo(udfs);
				MESPortServiceProxy.getPortServiceImpl().setEvent(portData, setEventInfo, eventInfo);
			}
		}

		// change port use type
		if (CommonUtil.getValue(portData.getUdfs(), "PORTUSETYPE").equals(sPortUseType))
		{
			eventLog.warn(String.format("Attribute[%s] is still [%s]", "PORTUSETYPE", sPortUseType));
		}
		else
		{
			eventInfo.setEventName("ChangeUseType");

			Map<String, String> udfs = new HashMap<String, String>();
			udfs.put("PORTUSETYPE", sPortUseType);
			
			SetEventInfo setEventInfo = MESPortServiceProxy.getPortInfoUtil().setEventInfo(udfs);
			MESPortServiceProxy.getPortServiceImpl().setEvent(portData, setEventInfo, eventInfo);
		}

		// success then report to FMB
		GenericServiceProxy.getESBServive().sendBySenderToFMB(doc);
	}
}
