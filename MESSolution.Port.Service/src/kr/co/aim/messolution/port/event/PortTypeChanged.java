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
import kr.co.aim.greentrack.port.management.data.PortSpec;
import kr.co.aim.greentrack.port.management.info.SetEventInfo;
import kr.co.aim.greentrack.portspec.management.info.ChangeSpecInfo;

public class PortTypeChanged extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String portType = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", true);

		MachineSpec machineSpec = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);

		if (StringUtils.equalsIgnoreCase(machineSpec.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Sorter) && StringUtils.equals(portType, "PB"))
		{
			eventLog.info("Sort PB change to PS");
			portType = "PS";
		}

		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeType", getEventUser(), getEventComment(), null, null);

		if (CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals(portType))
		{
			eventLog.warn(String.format("Attribute[%s] is still [%s]", "PORTTYPE", portType));
		}
		else
		{
			Map<String, String> udfs = new HashMap<String, String>();

			SetEventInfo transitionInfo = MESPortServiceProxy.getPortInfoUtil().setEventInfo(udfs);
			transitionInfo.getUdfs().put("PORTTYPE", portType);
			
			MESPortServiceProxy.getPortServiceImpl().setEvent(portData, transitionInfo, eventInfo);
		}

		PortSpec portSpec = MESPortServiceProxy.getPortServiceUtil().getPortSpecInfo(machineName, portName);

		if (!StringUtils.equals(portType, portSpec.getPortType()))
		{
			// Change PortSpec table in DB
			ChangeSpecInfo changeSpecInfo = MESPortServiceProxy.getPortInfoUtil().changeSpecInfo(portSpec, portType, portSpec.getDescription(), portSpec.getFactoryName(), portSpec.getAreaName(),
					portSpec.getVendor(), portSpec.getModel(), portSpec.getSerialNo(), portSpec.getPortStateModelName());

			MESPortServiceProxy.getPortServiceImpl().changePortType(portSpec, changeSpecInfo, eventInfo);
		}
	}
}
