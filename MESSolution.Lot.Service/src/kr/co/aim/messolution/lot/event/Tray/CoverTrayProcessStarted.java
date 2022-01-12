package kr.co.aim.messolution.lot.event.Tray;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
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
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.management.data.Port;

public class CoverTrayProcessStarted extends AsyncHandler 
{
	private static Log log = LogFactory.getLog(CoverTrayProcessStarted.class);

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", false);
		String portType = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", false);
		String portUseType = SMessageUtil.getBodyItemValue(doc, "PORTUSETYPE", false);
		String trayName = SMessageUtil.getBodyItemValue(doc, "TRAYNAME", true);
		String readTrayName = SMessageUtil.getBodyItemValue(doc, "BCRTRAYNAME", true);
		String machineRecipeName = SMessageUtil.getBodyItemValue(doc, "MACHINERECIPENAME", false);
		String operationMode = SMessageUtil.getBodyItemValue(doc, "OPERATIONMODE", true);
		
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
		Durable trayData = MESDurableServiceProxy.getDurableServiceUtil().getDurableDataForUpdate(trayName);
		
		CommonValidation.checkMachineHold(machineData);
		CommonValidation.CheckDurableState(trayData);
		CommonValidation.CheckDurableHoldState(trayData);
		//CommonValidation.CheckDurableCleanState(trayData);
		
		if (StringUtil.equals(CommonUtil.getValue(portData.getUdfs(), "PORTTYPE"), "PL"))
		{
			CommonValidation.checkTrayIsNotEmpty(trayData);
		}
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CoverTrayProcessStarted", getEventUser(), getEventComment());
		
		// set Event
		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.getUdfs().put("MACHINENAME", machineName);
		setEventInfo.getUdfs().put("PORTNAME", portName);
		setEventInfo.getUdfs().put("BCRFLAG", "N");

		trayData = DurableServiceProxy.getDurableService().setEvent(trayData.getKey(), eventInfo, setEventInfo);
		
		if(!readTrayName.equals(trayName))
		{
			setEventInfo.getUdfs().clear();
			setEventInfo.getUdfs().put("DURABLEHOLDSTATE", "Y");
		    EventInfo holdEventInfo = EventInfoUtil.makeEventInfo("Hold", getEventUser(), getEventComment());
			DurableServiceProxy.getDurableService().setEvent(trayData.getKey(),holdEventInfo, setEventInfo);
		}
	}

}
