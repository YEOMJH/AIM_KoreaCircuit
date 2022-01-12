package kr.co.aim.messolution.lot.event.CNX.PostCell;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.product.management.data.Product;

public class MVIVCRReadyReport extends SyncHandler {
	private static Log log = LogFactory.getLog(MVIVCRReadyReport.class);
	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String vcrProductName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", false);
		String eventUser = SMessageUtil.getBodyItemValue(doc, "EVENTUSER", true);

		Lot lotData = new Lot();
		String factoryName = "";
		try
		{
			lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
			factoryName = lotData.getFactoryName();
		}
		catch (Exception e)
		{
			eventLog.info("Not exist Panel Info on Lot Table");// Modify by wangys -20200413
		}
		EventInfo eventInfo=new EventInfo();
        if(eventUser.equals("MVI"))
        {
        	
            eventInfo = EventInfoUtil.makeEventInfo("MVIVCRReadyReport", getEventUser(), "MVIVCRReadyReport", "", "");	
        }
        else
        {
            eventInfo = EventInfoUtil.makeEventInfo("FQCVCRReadyReport", getEventUser(), "FQCVCRReadyReport", "", "");	
        }
		
		SetEventInfo setLotEventInfo = new SetEventInfo();
		setLotEventInfo.getUdfs().put("VCRPRODUCTNAME", vcrProductName);

		LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setLotEventInfo);
		MESLotServiceProxy.getLotServiceImpl().updateLotData("MACHINENAME", machineName, lotData.getKey().getLotName());
		updateLotHistory(lotData, eventInfo.getEventName(), eventInfo.getEventTimeKey(),machineName);
		
		return doc;
	}
	private void updateLotHistory(Lot lotData, String eventName, String eventTimeKey,String machineName) throws CustomException
	{

		String condition = "UPDATE LOTHISTORY SET MACHINENAME=?  WHERE LOTNAME=? AND TIMEKEY =? AND EVENTNAME=? ";
		Object[] bindSet = new Object[] {};
		bindSet = new Object[] { machineName, lotData.getKey().getLotName(), eventTimeKey, eventName};
		kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().update(condition, bindSet);

	}

}
