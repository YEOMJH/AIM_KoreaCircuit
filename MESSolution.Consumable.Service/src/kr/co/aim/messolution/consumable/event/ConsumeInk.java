package kr.co.aim.messolution.consumable.event;

import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.info.DecrementQuantityInfo;
import kr.co.aim.greentrack.consumable.management.info.MakeNotAvailableInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.info.SetMaterialLocationInfo;
import kr.co.aim.greentrack.generic.info.TransitionInfo;

import org.jdom.Document;

public class ConsumeInk extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String consumableName = SMessageUtil.getBodyItemValue(doc, "CONSUMABLENAME", true);
		String Quantity = SMessageUtil.getBodyItemValue(doc, "QUANTITY", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ConsumeInk", getEventUser(), getEventComment(), null, null);
		
		Consumable consumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(consumableName);
		
		CommonValidation.checkConsumableState(consumableData);
		
		Double oldQuantity = consumableData.getQuantity();
		Double newQuantity = Double.parseDouble(Quantity);
		
		Map<String, String> udfs = CommonUtil.setNamedValueSequence(SMessageUtil.getBodyElement(doc), Consumable.class.getSimpleName());
		
		TransitionInfo transitionInfo = MESConsumableServiceProxy.getConsumableInfoUtil().decrementQuantityInfo(null, null, null, null, eventInfo.getEventTimeKey(),
				CommonUtil.doubleSubtract(oldQuantity, newQuantity), udfs);
		MESConsumableServiceProxy.getConsumableServiceImpl().decrementQuantity(consumableData, (DecrementQuantityInfo) transitionInfo, eventInfo);

		// Scrap Event
		if (newQuantity == 0)
		{
			MakeNotAvailableInfo makeNotAvailableInfo = new MakeNotAvailableInfo();
			MESConsumableServiceProxy.getConsumableServiceImpl().makeNotAvailable(consumableData, makeNotAvailableInfo, eventInfo);
			SetMaterialLocationInfo setMaterialLocationInfo = MESConsumableServiceProxy.getConsumableInfoUtil().setMaterialLocationInfo(consumableData, "Bank");
			MESConsumableServiceProxy.getConsumableServiceImpl().setMaterialLocation(consumableData, setMaterialLocationInfo, eventInfo);
		}

		return doc;
	}

}
