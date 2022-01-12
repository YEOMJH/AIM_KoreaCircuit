package kr.co.aim.messolution.consumable.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.OrganicExtractCard;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.data.ConsumableHistory;
import kr.co.aim.greentrack.consumable.management.info.SetEventInfo;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class ShipMaterial extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> eMaterialList = SMessageUtil.getBodySequenceItemList(doc, "MATERIALLIST", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ShipMaterial", getEventUser(), getEventComment(), null, null);

		List<OrganicExtractCard> updateList = new ArrayList<OrganicExtractCard>();
		
		for (Element materialE : eMaterialList)
		{
			String materialID = materialE.getChildText("MATERIALID");

			OrganicExtractCard card = ExtendedObjectProxy.getOrganicExtractCardService().selectByKey(false, new Object[]{materialID});
			
			if(!card.getState().equals("Created"))
			{
				throw new CustomException("MATERIAL-0003", materialID, card.getState());
			}
			
			card.setState("Shipped");
			card.setLocation("Vendor");
			card.setLastEventUser(eventInfo.getEventUser());
			card.setLastEventTime(eventInfo.getEventTime());
			card.setLastEventTimekey(eventInfo.getEventTimeKey());
			card.setLastEventName(eventInfo.getEventName());
			card.setLastEventComment(eventInfo.getEventComment());
			
			updateList.add(card);
		}
		
		ExtendedObjectProxy.getOrganicExtractCardService().modify(eventInfo, updateList);

		return doc;
	}
}
