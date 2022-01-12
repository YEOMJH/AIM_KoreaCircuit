package kr.co.aim.messolution.machine.event;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.info.SetEventInfo;

public class CleanModeChanged extends AsyncHandler {
	private static Log log = LogFactory.getLog(CleanModeChanged.class);

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String cleanModeFlag = SMessageUtil.getBodyItemValue(doc, "CLEANMODEFLAG", true);

		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeCleanMode", this.getEventUser(), this.getEventComment());
        if(cleanModeFlag.equals("Y"))
        {
        	String deletePostcellLoadInfoSql ="DELETE FROM CT_POSTCELLLOADINFO WHERE MACHINENAME=:MACHINENAME";
        	List<Object[]> deletePostcellLoadInfo = new ArrayList<Object[]>();
			List<Object> loadInfo = new ArrayList<Object>();
			loadInfo.add(machineName);
			deletePostcellLoadInfo.add(loadInfo.toArray());
			try
			{
			MESLotServiceProxy.getLotServiceUtil().updateBatch(deletePostcellLoadInfoSql, deletePostcellLoadInfo);
			}
			catch (Exception e)
			{
			  log.info("delete Fail");
			}
        	
        }
		if (cleanModeFlag.equals(machineData.getUdfs().get("CLEANMODEFLAG")))
		{
			log.info("CleanMode is same with BC Reported.so do not changed");
		}
		else
		{
			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.getUdfs().put("CLEANMODEFLAG", cleanModeFlag);
			
			MESMachineServiceProxy.getMachineServiceImpl().setEvent(machineData, setEventInfo, eventInfo);
		}
	}
}
