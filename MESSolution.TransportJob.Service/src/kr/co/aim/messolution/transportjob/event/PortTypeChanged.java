package kr.co.aim.messolution.transportjob.event;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.info.SetEventInfo;

public class PortTypeChanged extends AsyncHandler {

	/**
	 * MessageSpec [MCS -> TEX]
	 * 
	 * <Body>
	 *    <MACHINENAME />
	 *    <PORTNAME />
	 *    <PORTACCESSMODE />
	 *    <PORTTYPE />
	 * </Body>
	 */
	
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeType", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String portType = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", true);
        
		//*********TEX can't Process PortTypeChange which Machine is not StorageMachine**********//
		//add by xiaoxh 2020/12/11
		this.checkMachineType(machineName);
		
		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);

		Map<String, String> udfs = new HashMap<String, String>();

		SetEventInfo setEventInfo = MESPortServiceProxy.getPortInfoUtil().setEventInfo(udfs);
		setEventInfo.getUdfs().put("PORTTYPE", portType);
		MESPortServiceProxy.getPortServiceImpl().setEvent(portData, setEventInfo, eventInfo);
	}
	
	public void checkMachineType(String machineName)throws CustomException
	{
		MachineSpec machineSpecData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);
		String MachineType = machineSpecData.getMachineType();
		if(!StringUtils.equals(MachineType, "StorageMachine"))
		{
			throw new CustomException("MACHINE-0046", machineName);
		}
		
	}
}