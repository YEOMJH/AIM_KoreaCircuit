package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.sql.Timestamp;
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
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class TrayGroupCleanTrackOut  extends SyncHandler {
	private static Log log = LogFactory.getLog(TrayGroupCleanTrackOut.class);
	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);

		String coverName = SMessageUtil.getBodyItemValue(doc, "COVERNAME", true);

		String coverPosition = SMessageUtil.getBodyItemValue(doc, "COVERPOSITION", false);

		List<Element> trayList = SMessageUtil.getBodySequenceItemList(doc, "TRAYLIST", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);

		Durable coverTrayData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(coverName);
		CommonValidation.CheckDurableState(coverTrayData);
		String lastCleanTime = TimeStampUtil.getCurrentTimestamp().toString();
        long totalQty=0;
		if (coverTrayData.getLotQuantity()>0)
		{
			throw new CustomException("CST-0024", coverTrayData.getKey().getDurableName());
		}
		eventInfo = EventInfoUtil.makeEventInfo("ChangeTrayCleanState", getEventUser(), getEventComment(), null, null);
		for (Element element : trayList)
		{
			String trayName = element.getChildText("DURABLENAME");
			String position = element.getChildText("POSITION");

			Durable trayData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayName);
			CommonValidation.CheckDurableState(trayData);
			
			if (trayData.getLotQuantity()>0)
			{
				throw new CustomException("CST-0024", trayData.getKey().getDurableName());
				
			}
			totalQty += trayData.getLotQuantity();
			
			trayData.setDurableCleanState("Clean");
			DurableServiceProxy.getDurableService().update(trayData);
			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.getUdfs().put("COVERNAME", coverName);
			setEventInfo.getUdfs().put("POSITION", position);
			setEventInfo.getUdfs().put("DURABLETYPE1", "Tray");
			setEventInfo.getUdfs().put("LASTCLEANTIME",lastCleanTime);
			MESDurableServiceProxy.getDurableServiceImpl().setEvent(trayData, setEventInfo, eventInfo);
		}

		coverTrayData.setDurableType("CoverTray");
		coverTrayData.setLotQuantity(totalQty);
		coverTrayData.setDurableState(GenericServiceProxy.getConstantMap().Dur_InUse);
		coverTrayData.setDurableCleanState("Clean");
		DurableServiceProxy.getDurableService().update(coverTrayData);

		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.getUdfs().put("COVERNAME", coverName);
		setEventInfo.getUdfs().put("POSITION", String.valueOf((Integer.parseInt(coverPosition) + 1)));
		setEventInfo.getUdfs().put("DURABLETYPE1", "CoverTray");
		setEventInfo.getUdfs().put("LASTCLEANTIME",lastCleanTime);
		DurableServiceProxy.getDurableService().setEvent(coverTrayData.getKey(), eventInfo, setEventInfo);

		
		return doc;
	}

}
