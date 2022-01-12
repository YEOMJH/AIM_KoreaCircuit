package kr.co.aim.messolution.consumable.event;

import org.jdom.Document;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.OrganicExtractCard;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class AddOrganicExtractQty extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		String materialID = SMessageUtil.getBodyItemValue(doc, "MATERIALID", true);
		String strquantity=SMessageUtil.getBodyItemValue(doc, "QUANTITY", true);;		
		double quantity = (!StringUtil.equals(strquantity, "")) ? Double.parseDouble(strquantity) : 0;
		double totalQuantity;
		
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("AddOrganicExtractQty", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		OrganicExtractCard dataInfo = new OrganicExtractCard();
		try{
			dataInfo = ExtendedObjectProxy.getOrganicExtractCardService().selectByKey(false, new Object[]{materialID});
		}
		catch(Exception e){
			dataInfo = new OrganicExtractCard();
		}
		
		if(dataInfo!= null){
			if(dataInfo.getTotalQuantity() !=null){				
				totalQuantity = (!StringUtil.equals(dataInfo.getTotalQuantity().toString(), "")) ? Double.parseDouble(dataInfo.getTotalQuantity().toString()) : 0;	
			}else
			{
				totalQuantity=0;
			}
			
			totalQuantity = CommonUtil.doubleAdd(totalQuantity, quantity);
			
			dataInfo.setQuantity(quantity);
			dataInfo.setTotalQuantity(totalQuantity);
			dataInfo.setLastEventName(eventInfo.getEventName());
			dataInfo.setLastEventUser(eventInfo.getEventUser());
			dataInfo.setLastEventTime(eventInfo.getEventTime());
			dataInfo.setLastEventComment(eventInfo.getEventComment());
			dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
			
			ExtendedObjectProxy.getOrganicExtractCardService().modify(eventInfo,dataInfo);
		}
	
		return doc;
	}

}
