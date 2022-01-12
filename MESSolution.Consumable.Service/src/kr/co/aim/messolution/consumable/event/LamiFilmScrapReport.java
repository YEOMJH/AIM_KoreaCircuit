package kr.co.aim.messolution.consumable.event;

import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.info.DecrementQuantityInfo;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class LamiFilmScrapReport extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String messageName = SMessageUtil.getMessageName(doc);	
		String materialName = SMessageUtil.getBodyItemValue(doc, "MATERIALNAME", false);
		String boxName = SMessageUtil.getBodyItemValue(doc, "BOXNAME", false);
		String scrapType = SMessageUtil.getBodyItemValue(doc, "SCRAPTYPE", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo_InUseConsumable(messageName, this.getEventUser(), this.getEventComment(), null, null);
		String timeKey = TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime());

		eventInfo.setEventTimeKey(timeKey);
		
		Consumable consumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(materialName);
		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(boxName);
		
		// Mantis - 0000387
		//double quantity = Double.parseDouble(no);

		eventInfo.setEventName("LamiFilmScrappedReport");
		Map<String, String> udfs = CommonUtil.setNamedValueSequence(SMessageUtil.getBodyElement(doc), Consumable.class.getSimpleName());

		DecrementQuantityInfo transitionInfo = decrementQuantityInfo(null, null, null, null, eventInfo.getEventTimeKey(), scrapType, 1, udfs);
		MESConsumableServiceProxy.getConsumableServiceImpl().decrementQuantity(consumableData, transitionInfo, eventInfo);

		Consumable newConsumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(materialName);

		
		// 2020-12-15	dhko	Modify
		// If the quantity of Consumable is zero, force the ConsumableState to 'NotAvailable' and sign the contract.
		if(newConsumableData.getQuantity() == 0)
		{
			//Change Film State
			eventInfo = EventInfoUtil.makeEventInfo("ChangeState", getEventUser(), getEventComment(), null, null);
			timeKey = TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime());
			eventInfo.setEventTimeKey(timeKey);
			
			newConsumableData.setConsumableState("NotAvailable");
			ConsumableServiceProxy.getConsumableService().update(newConsumableData);
			
			//Deassign CST
			eventInfo.setEventName("DeassignMaterialToBox");
			kr.co.aim.greentrack.consumable.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.consumable.management.info.SetEventInfo();
			setEventInfo.getUdfs().put("CARRIERNAME", "");
			setEventInfo.getUdfs().put("SEQ", "");
			ConsumableServiceProxy.getConsumableService().setEvent(newConsumableData.getKey(), eventInfo, setEventInfo);
			
			if(durableData.getLotQuantity() == 1)
				durableData.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);

			durableData.setLotQuantity(durableData.getLotQuantity() - 1);

			DurableServiceProxy.getDurableService().update(durableData);
			SetEventInfo setEventInfoDur = new SetEventInfo();
			durableData = DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEventInfoDur);
		}
		
		if(durableData.getLotQuantity() == 0 && StringUtils.equals(durableData.getDurableState(), "InUse"))
		{
			durableData.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);
			
			DurableServiceProxy.getDurableService().update(durableData);
			SetEventInfo setEventInfo = new SetEventInfo();
			DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEventInfo);
		}

	}

	private DecrementQuantityInfo decrementQuantityInfo(String consumerLotName, String consumerPOName, 
			String consumerPOVersion, String consumerProductName, String consumerTimeKey, String scrapType , double quantity, Map<String, String> udfs)
	{
		DecrementQuantityInfo decrementQuantityInfo = new DecrementQuantityInfo();
		decrementQuantityInfo.setConsumerLotName(consumerLotName);
		decrementQuantityInfo.setConsumerPOName(consumerPOName);
		decrementQuantityInfo.setConsumerPOVersion(consumerPOVersion);
		decrementQuantityInfo.setConsumerProductName(consumerProductName);
		decrementQuantityInfo.setConsumerTimeKey(consumerTimeKey);
		decrementQuantityInfo.setQuantity(quantity);

		Map<String, String> consumableUdfs = udfs;
		consumableUdfs.put("SCRAPTYPE", scrapType);
		decrementQuantityInfo.setUdfs(consumableUdfs);

		return decrementQuantityInfo;
	}
}