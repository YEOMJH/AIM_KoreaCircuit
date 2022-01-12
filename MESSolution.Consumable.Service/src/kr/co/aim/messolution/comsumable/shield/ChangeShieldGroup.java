package kr.co.aim.messolution.comsumable.shield;

import java.util.List;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ShieldLot;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class ChangeShieldGroup extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String carGroupName = SMessageUtil.getBodyItemValue(doc, "CARGROUPNAME", false);
		String basketGroupName = SMessageUtil.getBodyItemValue(doc, "BASKETGROUPNAME", false);
		List<Element> shieldList = SMessageUtil.getBodySequenceItemList(doc, "SHIELDLIST", true);		

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeShieldGroup", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());

		for (Element shield : shieldList)
		{
			String shieldLotName = SMessageUtil.getChildText(shield, "SHIELDLOTNAME", true);
			ShieldLot shieldLotData = ExtendedObjectProxy.getShieldLotService().selectByKey(false, new Object[] { shieldLotName });

			CommonValidation.checkShieldLotHoldStateN(shieldLotData);
			CommonValidation.checkShieldLotProcessStateWait(shieldLotData);
			
			if(StringUtils.isNotEmpty(carGroupName))
			{
				shieldLotData.setCarGroupName(carGroupName);
			}
			if(StringUtils.isNotEmpty(basketGroupName))
			{
				shieldLotData.setBasketGroupName(basketGroupName);
			}
			shieldLotData.setLastEventComment(eventInfo.getEventComment());
			shieldLotData.setLastEventName(eventInfo.getEventName());
			shieldLotData.setLastEventTime(eventInfo.getEventTime());
			shieldLotData.setLastEventTimekey(TimeStampUtil.getCurrentEventTimeKey());
			shieldLotData.setLastEventUser(eventInfo.getEventUser());
			
			ExtendedObjectProxy.getShieldLotService().modify(eventInfo, shieldLotData);
		}
		
		return doc;
	}
}
