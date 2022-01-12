package kr.co.aim.messolution.consumable.event;

import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.OrganicMapping;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class DeleteOrganicMapping extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> mappingList = SMessageUtil.getBodySequenceItemList(doc, "MAPPINGLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeleteOrganicMapping", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		for (Element mapping : mappingList)
		{
			String crucibleName = mapping.getChildText("CRUCIBLENAME");

			OrganicMapping dataInfo = ExtendedObjectProxy.getOrganicMappingService().getOrganicMappingData(crucibleName);
			
			if(dataInfo == null)
				throw new CustomException("CRUCIBLE-0003", crucibleName);
			
			ExtendedObjectProxy.getOrganicMappingService().delete(dataInfo);
		}

		return doc;
	}

}
