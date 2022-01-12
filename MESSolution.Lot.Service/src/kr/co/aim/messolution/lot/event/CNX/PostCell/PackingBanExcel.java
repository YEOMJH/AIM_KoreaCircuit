package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class PackingBanExcel extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> PackingBanList = SMessageUtil.getBodySequenceItemList(doc, "PACKINGBANLIST", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		for (Element code : PackingBanList)
		{
			String actionName = code.getChild("ACTIONNAME").getText();
			String productName = code.getChild("PRODUCTNAME").getText();
			String productType = code.getChild("PRODUCTTYPE").getText();
			String banType = code.getChild("BANTYPE").getText();
			String banReason = code.getChild("BANREASON").getText();
			String description = code.getChild("DESCRIPTION").getText();
			String factoryName = code.getChild("FACTORYNAME").getText();
			String defaultFlag = code.getChild("DEFAULTFLAG").getText();

			if (actionName.equals("Add"))
			{
				eventInfo.setEventName("Add PackingBan");
				try{
					ExtendedObjectProxy.getPackingBanService().createPackingBan(eventInfo, productName, productType, banType, banReason, description, factoryName, defaultFlag);
					
				}catch(Exception ex)
				{
				    throw new CustomException("PRODUCT-9002",productName);	
				}
				
			}
			else if (actionName.equals("Delete"))
			{
				try{
					ExtendedObjectProxy.getPackingBanService().deletePackingBan(productName);
					
				}catch(Exception ex){
					 throw new CustomException("LOT-0215",productName );	
				}
				
			}
		}

		return doc;
	}
}
