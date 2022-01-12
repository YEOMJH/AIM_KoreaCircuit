package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class TrayGroupCleanTrackIn  extends SyncHandler{
	private static Log log = LogFactory.getLog(TrayGroupCleanTrackOut.class);
	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		//String coverName = SMessageUtil.getBodyItemValue(doc, "COVERNAME", true);
		//String machineName=SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		//String portName=SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);

		//Durable coverTrayData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(coverName);

		//CommonValidation.CheckDurableState(coverTrayData);

		long totalLotQty = 0;
		eventInfo = EventInfoUtil.makeEventInfo("TrayGroupCleanTrackIn", getEventUser(), getEventComment(), null, null);

		List<Element>  trayList =SMessageUtil.getBodySequenceItemList(doc, "CARRIERLIST", true);

		for (Element durable : trayList)	
		{
			String trayName = durable.getChildText("TRAYNAME");

			Durable trayData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayName);
			CommonValidation.CheckDurableState(trayData);

			if (trayData.getLotQuantity()> 0)
			{
				throw new CustomException("CST-0024", trayName);
			}
			trayData.setDurableCleanState("Dirty");
			SetEventInfo setEventInfo = new SetEventInfo();
			//setEventInfo.getUdfs().put("COVERNAME", "");
			//setEventInfo.getUdfs().put("POSITION", "");
			setEventInfo.getUdfs().put("DURABLETYPE1", "Tray");
			//setEventInfo.getUdfs().put("MACHINENAME", machineName);
			//setEventInfo.getUdfs().put("PORTNAME", portName);
			setEventInfo.getUdfs().put("POSITIONTYPE", "PORT");
			MESDurableServiceProxy.getDurableServiceImpl().setEvent(trayData, setEventInfo, eventInfo);
		}

		//coverTrayData.setDurableType("Tray");
		//coverTrayData.setLotQuantity(0);
		//coverTrayData.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);
		//DurableServiceProxy.getDurableService().update(coverTrayData);

		//SetEventInfo setEventInfo = new SetEventInfo();
		//setEventInfo.getUdfs().put("COVERNAME", "");
		//setEventInfo.getUdfs().put("POSITION", "");
		//setEventInfo.getUdfs().put("DURABLETYPE1", "Tray");
		//setEventInfo.getUdfs().put("MACHINENAME", machineName);
		//setEventInfo.getUdfs().put("PORTNAME", portName);
		//setEventInfo.getUdfs().put("POSITIONTYPE", "PORT");
		//DurableServiceProxy.getDurableService().setEvent(coverTrayData.getKey(), eventInfo, setEventInfo);

		return doc;
	}

}
