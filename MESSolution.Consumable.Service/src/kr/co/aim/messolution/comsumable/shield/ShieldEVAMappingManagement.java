package kr.co.aim.messolution.comsumable.shield;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ShieldChamberMap;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class ShieldEVAMappingManagement extends SyncHandler {

	public Object doWorks(Document doc) throws CustomException
	{
		String eventType = SMessageUtil.getBodyItemValue(doc, "EVENTTYPE", false);
		
		if(eventType.equals("Del"))
		{
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeleteShieldChamberMap", this.getEventUser(), this.getEventComment(), "", "");
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			
			List<Element> shieldMapList = SMessageUtil.getBodySequenceItemList(doc, "SHIELDMAPLIST", true);
			
			for (Element shieldMap : shieldMapList)
			{
				String shieldSpecName = SMessageUtil.getChildText(shieldMap, "SHIELDSPECNAME", true);
				String EVAChamberName = SMessageUtil.getChildText(shieldMap, "EVACHAMBERNAME", true);
				
				ShieldChamberMap shieldChamberInfo = ExtendedObjectProxy.getShieldChamberMapService().selectByKey(false, new Object[] { shieldSpecName,  EVAChamberName});
				ExtendedObjectProxy.getShieldChamberMapService().remove(eventInfo, shieldChamberInfo);	
			}
		}
		else 
		{
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateShieldChamberMap", this.getEventUser(), this.getEventComment(), "", "");
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			
			String shieldSpecName = SMessageUtil.getBodyItemValue(doc, "SHIELDSPECNAME", true);
			String EVAChamberName = SMessageUtil.getBodyItemValue(doc, "EVACHAMBERNAME", true);
			String quantity = SMessageUtil.getBodyItemValue(doc, "QUANTITY", true);
			
			ShieldChamberMap shieldMap = null;
			
			try
			{
				shieldMap = ExtendedObjectProxy.getShieldChamberMapService().selectByKey(false, new Object[] { shieldSpecName,  EVAChamberName });
			}
			catch (greenFrameDBErrorSignal nfdes)
			{
				// Not found same shield
			}

			if (shieldMap != null)
				throw new CustomException("SHIELD-0005");
			
			shieldMap = new ShieldChamberMap();
			
			shieldMap.setShieldSpecName(shieldSpecName);
			shieldMap.setEVAChamberName(EVAChamberName);
			shieldMap.setShieldQuantity(Integer.parseInt(quantity));
			shieldMap.setLastEventComment(eventInfo.getEventComment());
			shieldMap.setLastEventName(eventInfo.getEventName());
			shieldMap.setLastEventUser(eventInfo.getEventUser());
			shieldMap.setLastEventTime(eventInfo.getEventTime());
			shieldMap.setLastEventTimekey(eventInfo.getEventTimeKey());
			
			ExtendedObjectProxy.getShieldChamberMapService().create(eventInfo, shieldMap);
		}
		return doc;
	}
}
