package kr.co.aim.messolution.consumable.event;

import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.OrganicMapping;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableSpec;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class CreateOrganicMapping extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> mappingList = SMessageUtil.getBodySequenceItemList(doc, "MAPPINGLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateOrganicMapping", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		for (Element mapping : mappingList)
		{
			String machineName = mapping.getChildText("MACHINENAME");
			String chamberName = mapping.getChildText("CHAMBERNAME");
			String crucibleName = mapping.getChildText("CRUCIBLENAME");
			//String materialSpecName = mapping.getChildText("MATERIALSPECNAME");
			//String materialSpecVersion = mapping.getChildText("MATERIALSPECVERSION");

			//Start 20210126 houxk
			Durable crucibleData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(crucibleName);;
			DurableSpec crucibleSpecData = GenericServiceProxy.getSpecUtil().getDurableSpec("OLED", crucibleData.getDurableSpecName(), "00001");
			if(crucibleSpecData.getUdfs().get("MATERIALTYPE").equals("Organic"))
			{
				ExtendedObjectProxy.getOrganicMappingService().checkMappingQuantity(chamberName);
			}
			//End 20210126 houxk
			
			OrganicMapping dataInfo = new OrganicMapping();

			dataInfo.setCrucibleName(crucibleName);
			//dataInfo.setMaterialSpecName(materialSpecName);
			//dataInfo.setMaterialSpecVersion(materialSpecVersion);
			dataInfo.setMachineName(machineName);
			dataInfo.setChamberName(chamberName);
			dataInfo.setLastEventName(eventInfo.getEventName());
			dataInfo.setLastEventTime(eventInfo.getEventTime());
			dataInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
			dataInfo.setLastEventUser(eventInfo.getEventUser());
			dataInfo.setLastEventComment(eventInfo.getEventComment());

			ExtendedObjectProxy.getOrganicMappingService().create(eventInfo, dataInfo);
		}

		return doc;
	}

}
