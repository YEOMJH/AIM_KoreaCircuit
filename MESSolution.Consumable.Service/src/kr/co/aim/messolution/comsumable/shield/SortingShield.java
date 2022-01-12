package kr.co.aim.messolution.comsumable.shield;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ShieldLot;
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
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class SortingShield extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String durableName = SMessageUtil.getBodyItemValue(doc, "DURABLENAME", true);
		String sortType = SMessageUtil.getBodyItemValue(doc, "SORTTYPE", true);
		
		List<Element> shieldList = SMessageUtil.getBodySequenceItemList(doc, "SHIELDLIST", true);

		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableName);
		CommonValidation.checkShieldCarrierState(durableData);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("SortingShield", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());

		if(sortType.equals("Sort"))
		{
			ShieldLot durShield = ExtendedObjectProxy.getShieldLotService().checkShieldSpecNotCreate(durableName, shieldList, durableData);
		}
		else if (sortType.equals("Pack"))
		{
			ShieldLot durShield = ExtendedObjectProxy.getShieldLotService().checkShieldSpecForRun(durableName, shieldList, durableData);
		}		
		
		for (Element shield : shieldList)
		{
			String shieldName = SMessageUtil.getChildText(shield, "SHIELDLOTNAME", true);

			ShieldLot shieldLotData = ExtendedObjectProxy.getShieldLotService().selectByKey(false, new Object[] { shieldName });

			CommonValidation.checkShieldLotHoldStateN(shieldLotData);			
			
			shieldLotData.setCarrierName(durableName);
			shieldLotData.setLastEventComment(eventInfo.getEventComment());
			shieldLotData.setLastEventName(eventInfo.getEventName());
			shieldLotData.setLastEventTime(eventInfo.getEventTime());
			shieldLotData.setLastEventTimekey(TimeStampUtil.getCurrentEventTimeKey());
			shieldLotData.setLastEventUser(eventInfo.getEventUser());
			
			ExtendedObjectProxy.getShieldLotService().modify(eventInfo, shieldLotData);			
		}
		updateLotQuantity(eventInfo, shieldList, durableName);

		return doc;
	}

	private void updateLotQuantity(EventInfo eventInfo, List<Element> shieldList, String toCarrier) throws CustomException
	{
		Map<String, String> sumMap = new HashMap<String, String>();
		List<String> carrierList = new ArrayList<String>();

		for (Element shield : shieldList)
		{
			int iCount = 0;

			String carrierName = SMessageUtil.getChildText(shield, "CARRIERNAME", false);

			if (StringUtils.isNotEmpty(carrierName))
			{
				String count = sumMap.get(carrierName);

				if (StringUtils.isNotEmpty(count))
				{
					iCount = Integer.parseInt(count) + 1;
				}
				else
				{
					iCount = 1;
				}

				sumMap.put(carrierName, Integer.toString(iCount));

				if (!carrierList.contains(carrierName))
					carrierList.add(carrierName);
			}
		}

		// FromCarrier
		if (carrierList.size() > 0)
		{
			for (String carrier : carrierList)
			{
				Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrier);

				String count = sumMap.get(carrier);
				long lotQuantity = durableData.getLotQuantity() - Long.parseLong(count);

				durableData.setLotQuantity(lotQuantity);

				if (lotQuantity < 0)
				{
					throw new CustomException("SHIELD-0009");
				}

				if (lotQuantity == 0)
				{
					durableData.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);
				}

				DurableServiceProxy.getDurableService().update(durableData);
				SetEventInfo setEventInfo = new SetEventInfo();
				DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEventInfo);
			}
		}

		// ToCarrier
		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(toCarrier);
		durableData.setLotQuantity(durableData.getLotQuantity() + shieldList.size());

		if (!StringUtil.equals(durableData.getDurableState(), GenericServiceProxy.getConstantMap().Dur_InUse))
		{
			durableData.setDurableState(GenericServiceProxy.getConstantMap().Dur_InUse);
		}

		DurableServiceProxy.getDurableService().update(durableData);
		SetEventInfo setEventInfo = new SetEventInfo();
		DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEventInfo);
	}
}
