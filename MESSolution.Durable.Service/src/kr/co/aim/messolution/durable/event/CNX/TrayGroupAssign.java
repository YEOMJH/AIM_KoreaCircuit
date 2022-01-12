package kr.co.aim.messolution.durable.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
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
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.jdom.Document;
import org.jdom.Element;

public class TrayGroupAssign extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String coverName = SMessageUtil.getBodyItemValue(doc, "COVERNAME", false);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String judge = SMessageUtil.getBodyItemValue(doc, "JUDGE", true);
		String coverPosition = SMessageUtil.getBodyItemValue(doc, "COVERPOSITION", false);
		String actionName = SMessageUtil.getBodyItemValue(doc, "ACTION", true);

		List<Element> trayListEl = SMessageUtil.getBodySequenceItemList(doc, "TRAYLIST", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);

		long totalLotQty = 0;
		if (StringUtil.equals(actionName, "OK"))
		{
			Durable coverTrayData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(coverName);

			CommonValidation.CheckDurableState(coverTrayData);

			eventInfo = EventInfoUtil.makeEventInfo("MVIAssignTrayGroup", getEventUser(), getEventComment(), null, null);

			for (Element element : trayListEl)
			{
				String trayName = element.getChildText("DURABLENAME");
				String position = element.getChildText("POSITION");

				Durable trayData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayName);
				CommonValidation.CheckDurableState(trayData);

				totalLotQty += trayData.getLotQuantity();

				Map<String, String> udfs = new HashMap<>();
				udfs.put("COVERNAME", coverName);
				udfs.put("POSITION", position);
				udfs.put("DURABLETYPE1", "Tray");

				trayData.setDurableState(GenericServiceProxy.getConstantMap().Dur_InUse);
				trayData.setUdfs(udfs);
				DurableServiceProxy.getDurableService().update(trayData);

				SetEventInfo setEventInfo = new SetEventInfo();
				MESDurableServiceProxy.getDurableServiceImpl().setEvent(trayData, setEventInfo, eventInfo);
			}

			Map<String, String> udfs = new HashMap<>();

			udfs.put("COVERNAME", coverName);
			udfs.put("POSITION", String.valueOf((Integer.parseInt(coverPosition))));
			udfs.put("DURABLETYPE1", "CoverTray");

			coverTrayData.setDurableType("CoverTray");
			coverTrayData.setLotQuantity(totalLotQty);
			coverTrayData.setDurableState(GenericServiceProxy.getConstantMap().Dur_InUse);
			coverTrayData.setUdfs(udfs);
			DurableServiceProxy.getDurableService().update(coverTrayData);

			SetEventInfo setEventInfo = new SetEventInfo();
			DurableServiceProxy.getDurableService().setEvent(coverTrayData.getKey(), eventInfo, setEventInfo);

			ExtendedObjectProxy.getMVIAssignTrayService().deleteMVIAssignTrayData(eventInfo, machineName, judge);
		}
		else if (StringUtil.equals(actionName, "Delete"))
		{
			// DELETE MVI assign Tray
			eventInfo = EventInfoUtil.makeEventInfo("DeleteMVIAssignTray", getEventUser(), getEventComment(), null, null);

			for (Element element : trayListEl)
			{
				String trayName = element.getChildText("DURABLENAME");
				ExtendedObjectProxy.getMVIAssignTrayService().deleteMVIAssignTrayData(eventInfo, machineName, judge, trayName);
			}
		}

		return doc;
	}

}
