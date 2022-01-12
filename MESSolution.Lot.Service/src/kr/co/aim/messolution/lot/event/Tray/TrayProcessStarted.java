package kr.co.aim.messolution.lot.event.Tray;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.PortServiceProxy;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.info.MakeTransferStateInfo;

public class TrayProcessStarted extends AsyncHandler 
{
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TrayProcessStarted", getEventUser(), getEventComment());
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", false);
		String trayName = SMessageUtil.getBodyItemValue(doc, "TRAYNAME", true);
		
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
		Durable trayData = MESDurableServiceProxy.getDurableServiceUtil().getDurableDataForUpdate(trayName);
		
		CommonValidation.checkMachineHold(machineData);
		CommonValidation.CheckDurableState(trayData);
		CommonValidation.CheckDurableHoldState(trayData);
		//CommonValidation.CheckDurableCleanState(trayData);
		
		if (StringUtils.equals(CommonUtil.getValue(portData.getUdfs(), "PORTTYPE"), "PL"))
		{
			CommonValidation.checkTrayIsNotEmpty(trayData);
		}
		else if (StringUtils.equals(CommonUtil.getValue(portData.getUdfs(), "PORTTYPE"), "PU"))
		{
			if (StringUtils.equals(portData.getTransferState(), "ReadyToLoad"))
			{
				this.makeTransferState(eventInfo, portData);
			}
		}
		
		// set Event
		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.getUdfs().put("MACHINENAME", machineName);
		setEventInfo.getUdfs().put("PORTNAME", portName);

		DurableServiceProxy.getDurableService().setEvent(trayData.getKey(), eventInfo, setEventInfo);
	}

	private void makeTransferState(EventInfo eventInfo, Port portData)
	{
		if (StringUtils.equals(portData.getTransferState(), GenericServiceProxy.getConstantMap().Port_ReadyToLoad) ||
			StringUtils.equals(CommonUtil.getValue(portData.getUdfs(), "FULLSTATE"), "EMPTY"))
		{
			MakeTransferStateInfo makeTransferStateInfo = new MakeTransferStateInfo();
			makeTransferStateInfo.setTransferState(GenericServiceProxy.getConstantMap().Port_Processing);
			makeTransferStateInfo.setValidateEventFlag("N");
			
			Map<String, String> udfs = new HashMap<String, String>();
			udfs.put("FULLSTATE", "FULL");
			
			makeTransferStateInfo.setUdfs(udfs);
			
			PortServiceProxy.getPortService().makeTransferState(portData.getKey(), eventInfo, makeTransferStateInfo);			
		}
	}
}
