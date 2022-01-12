package kr.co.aim.messolution.machine.event;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.info.SetEventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

public class HoldEQP extends SyncHandler {
	private static Log log = LogFactory.getLog(HoldEQP.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String sMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String sReasonCodeType = SMessageUtil.getBodyItemValue(doc, "REASONCODETYPE", true);
		String sReasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODE", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("HoldEQP", getEventUser(), getEventComment(), sReasonCodeType, sReasonCode);

		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(sMachineName);

		if (StringUtil.equals(machineData.getUdfs().get("MACHINEHOLDSTATE"), "N") || StringUtil.equals(machineData.getUdfs().get("MACHINEHOLDSTATE"), ""))
		{
			try
			{
				SetEventInfo setEventInfo = new SetEventInfo();
				setEventInfo.getUdfs().put("MACHINEHOLDSTATE", "Y");

				MESMachineServiceProxy.getMachineServiceImpl().setEvent(machineData, setEventInfo, eventInfo);
			}
			catch (Exception e)
			{
				log.error(sMachineName + "Hold failed");
				
				//MACHINE-0050: Machine [{0}] Hold failed
				throw new CustomException("MACHINE-0050" ,sMachineName );
			}
		}
		else
		{
			log.info(sMachineName + " has been hold  state ! ");
			
			//MACHINE-0051: Machine[{0}] has been hold  state
			throw new CustomException("MACHINE-0051" , sMachineName);
		}

		return doc;

	}
}
