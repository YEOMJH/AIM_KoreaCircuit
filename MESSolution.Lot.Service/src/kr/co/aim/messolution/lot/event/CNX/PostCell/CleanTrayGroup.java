package kr.co.aim.messolution.lot.event.CNX.PostCell;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import org.jdom.Document;
import org.jdom.Element;
import java.util.List;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
public class CleanTrayGroup  extends SyncHandler{

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		//String machineName=SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		//String portName=SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
		eventInfo = EventInfoUtil.makeEventInfo("CleanTrayGroup", getEventUser(), getEventComment(), null, null);
		
		List<Element>  trayList =SMessageUtil.getBodySequenceItemList(doc, "CARRIERLIST", true);
		String lastCleanTime = TimeStampUtil.getCurrentTimestamp().toString();
		
		for (Element durable : trayList)	
		{
			String trayName = durable.getChildText("TRAYNAME");

			Durable trayData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayName);
			CommonValidation.CheckDurableState(trayData);
			if (trayData.getLotQuantity()>0)
			{
				throw new CustomException("CST-0024", trayData.getKey().getDurableName());
				
			}
			trayData.setDurableCleanState("Clean");
			SetEventInfo setEventInfo = new SetEventInfo();
			//setEventInfo.getUdfs().put("MACHINENAME", machineName);
			//setEventInfo.getUdfs().put("PORTNAME", portName);
			//setEventInfo.getUdfs().put("POSITIONTYPE", "PORT");
			setEventInfo.getUdfs().put("LASTCLEANTIME", lastCleanTime);
			DurableServiceProxy.getDurableService().update(trayData);
			MESDurableServiceProxy.getDurableServiceImpl().setEvent(trayData, setEventInfo, eventInfo);
		}
		return doc;
	}	
}
