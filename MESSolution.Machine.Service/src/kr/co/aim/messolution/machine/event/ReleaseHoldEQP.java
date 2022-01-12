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

public class ReleaseHoldEQP extends SyncHandler {
	private static Log log = LogFactory.getLog(HoldEQP.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String sMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ReleaseHoldEQP", getEventUser(), getEventComment(), "", "");

		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(sMachineName);

		if (StringUtil.equals(machineData.getUdfs().get("MACHINEHOLDSTATE"), "Y"))
		{
			try
			{
				SetEventInfo setEventInfo = new SetEventInfo();
				setEventInfo.getUdfs().put("MACHINEHOLDSTATE", "N");
				MESMachineServiceProxy.getMachineServiceImpl().setEvent(machineData, setEventInfo, eventInfo);

			}
			catch (Exception e)
			{
				log.info(sMachineName + "ReleaseHold failed");
				
				//MACHINE-0052: Machine[{0}] ReleaseHold failed
				throw new CustomException("MACHINE-0052" , sMachineName);
			}
		}
		else
		{
			log.info(sMachineName + "   has not  hold   !");
			
			//MACHINE-0053: Machine[{0}] has not  hold !
			throw new CustomException("MACHINE-0053", sMachineName);
		}

		return doc;

	}
}