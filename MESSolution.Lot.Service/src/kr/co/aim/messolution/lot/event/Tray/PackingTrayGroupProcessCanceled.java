package kr.co.aim.messolution.lot.event.Tray;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.management.data.Port;

public class PackingTrayGroupProcessCanceled extends AsyncHandler 
{
	private static Log log = LogFactory.getLog(PackingTrayGroupProcessCanceled.class);

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", false);
		String portType = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", false);
		String portUseType = SMessageUtil.getBodyItemValue(doc, "PORTUSETYPE", false);
		String coverTrayName = SMessageUtil.getBodyItemValue(doc, "COVERTRAYNAME", true);

		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
		Durable coverTrayData = MESDurableServiceProxy.getDurableServiceUtil().getDurableDataForUpdate(coverTrayName);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("PackingTrayGroupProcessCanceled", getEventUser(), getEventComment());

		SetEventInfo setEventInfo = new SetEventInfo();
		DurableServiceProxy.getDurableService().setEvent(coverTrayData.getKey(), eventInfo, setEventInfo);
	}

}
