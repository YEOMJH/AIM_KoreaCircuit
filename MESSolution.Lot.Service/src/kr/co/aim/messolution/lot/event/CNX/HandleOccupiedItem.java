package kr.co.aim.messolution.lot.event.CNX;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.OccupyItem;
import kr.co.aim.messolution.extended.object.management.impl.OccupyItemService;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.exception.ErrorSignal;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class HandleOccupiedItem extends SyncHandler {
	private static Log log = LogFactory.getLog(HandleOccupiedItem.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String flag = SMessageUtil.getBodyItemValue(doc, "FLAG", true);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo(flag, getEventUser(), getEventComment(), "", "");
		String uiName = SMessageUtil.getBodyItemValue(doc, "UINAME", true);
		String menuName = SMessageUtil.getBodyItemValue(doc, "MENUNAME", true);
		String itemType = SMessageUtil.getBodyItemValue(doc, "ITEMTYPE", false);
		List<Element> itemListElement = SMessageUtil.getBodySequenceItemList(doc, "ITEMLIST", false);

		OccupyItemService ois = ExtendedObjectProxy.getOccupyItemService();

		Timestamp currentTime = new Timestamp(System.currentTimeMillis());

		List<OccupyItem> itemListToBeCreated = new ArrayList<OccupyItem>();
		List<OccupyItem> itemListToBeRenewed = new ArrayList<OccupyItem>();
		List<OccupyItem> itemListToBeFreed = new ArrayList<OccupyItem>();

		if (StringUtil.equals(flag, "Occupy"))
		{
			int freedOccupiedItemCount = ois.freeOccupiedItemsByUserAndMenuName(eventInfo, "Occupied", uiName, menuName);
			for (int i = 0; i < itemListElement.size(); i++)
			{
				String itemName = itemListElement.get(i).getChildText("ITEMNAME");

				OccupyItem itemData = null;
				try
				{
					itemData = ois.selectByKey(true, new Object[] { itemType, itemName });
					if (StringUtil.equals(itemData.getState(), "Occupied") && currentTime.getTime() < itemData.getExpireTime().getTime()
							&& !StringUtil.equals(eventInfo.getEventUser(), itemData.getUserId()))
					{
						String errorMessage = "Already [" + itemName + "] has been occupied by user [" + itemData.getUserId() + "]";
						throw new CustomException("SYS-0010", errorMessage);
					}
					else
					{
						itemListToBeRenewed.add(itemData);
					}
				}
				catch (greenFrameDBErrorSignal nfdes)
				{
					if (nfdes.getErrorCode().equals(ErrorSignal.NotFoundSignal))
					{
						itemData = new OccupyItem(itemType, itemName);
						itemListToBeCreated.add(itemData);
					}
				}
			}
		}
		else if (StringUtil.equals(flag, "Free"))
		{
			int freedOccupiedItemCount = ois.freeOccupiedItemsByUserAndMenuName(eventInfo, "Occupied", uiName, menuName);
		}
		else
		{
			log.info("Occupy flag is empty.");
		}

		if (itemListToBeCreated.size() > 0)
			ois.createOccupyItem("Occupied", itemListToBeCreated, eventInfo, currentTime, uiName, menuName);

		if (itemListToBeRenewed.size() > 0)
			ois.renewOccupyItem("Occupied", itemListToBeRenewed, eventInfo, currentTime, uiName, menuName);

		if (itemListToBeFreed.size() > 0)
			ois.modify(eventInfo, itemListToBeFreed);

		return doc;
	}
}