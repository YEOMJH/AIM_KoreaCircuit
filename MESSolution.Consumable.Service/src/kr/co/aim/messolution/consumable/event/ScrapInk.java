package kr.co.aim.messolution.consumable.event;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.info.MakeNotAvailableInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.info.SetMaterialLocationInfo;

import org.jdom.Document;

public class ScrapInk extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String consumableName = SMessageUtil.getBodyItemValue(doc, "CONSUMABLENAME", true);

		Consumable consumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getMaterialData(consumableName);
		CommonValidation.checkConsumableState(consumableData);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ScrapInk", getEventUser(), getEventComment(), null, null);

		MakeNotAvailableInfo makeNotAvailableInfo = new MakeNotAvailableInfo();
		MESConsumableServiceProxy.getConsumableServiceImpl().makeNotAvailable(consumableData, makeNotAvailableInfo, eventInfo);

		SetMaterialLocationInfo setMaterialLocationInfo = MESConsumableServiceProxy.getConsumableInfoUtil().setMaterialLocationInfo(consumableData, "Bank");
		MESConsumableServiceProxy.getConsumableServiceImpl().setMaterialLocation(consumableData, setMaterialLocationInfo, eventInfo);

		return doc;
	}

}
