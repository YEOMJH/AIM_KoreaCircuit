package kr.co.aim.messolution.consumable.event;

import java.util.HashMap;
import java.util.Map;

import org.jdom.Document;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ScrapCrate;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.data.ConsumableKey;
import kr.co.aim.greentrack.consumable.management.info.DecrementQuantityInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class ScrapCrateReport extends AsyncHandler {

	public void doWorks(Document doc) throws CustomException
	{

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ScrapCrate", getEventUser(), getEventComment(), null, null);

		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", false);
		String workOrder = SMessageUtil.getBodyItemValue(doc, "WORKORDER", false);
		String crateName = SMessageUtil.getBodyItemValue(doc, "CRATENAME", true);
		String scrapQty = "1";
		String virtualGlassId = SMessageUtil.getBodyItemValue(doc, "SCRAPGLASSNAME", true);
		String scrapCode = SMessageUtil.getBodyItemValue(doc, "SCRAPCODE", true);

		Consumable consumableData = ConsumableServiceProxy.getConsumableService().selectByKey(new ConsumableKey(crateName));

		String timekey = TimeStampUtil.getCurrentEventTimeKey();

		ScrapCrate scrapCrate = new ScrapCrate(timekey, virtualGlassId, crateName);
		scrapCrate.setTimekey(timekey);
		scrapCrate.setFactoryName(consumableData.getFactoryName());
		scrapCrate.setWorkOrder(workOrder);
		scrapCrate.setScrapCode(scrapCode);
		scrapCrate.setScrapQty(Long.valueOf(scrapQty));
		scrapCrate.setConsumableSpecName(consumableData.getConsumableSpecName());
		scrapCrate.setConsumableSpecVersion(consumableData.getConsumableSpecVersion());
		scrapCrate.setEventName(eventInfo.getEventName());
		scrapCrate.setEventTime(eventInfo.getEventTime());

		ExtendedObjectProxy.getScrapCrateService().create(eventInfo, scrapCrate);
	}

	private void decreaseCrateQuantity(EventInfo eventInfo, String lotName, String consumableName, double quantity) throws CustomException
	{
		eventInfo.setEventName("Consume");
		Map<String, String> udfs = new HashMap<String, String>();
		Consumable consumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(consumableName);

		DecrementQuantityInfo transitionInfo = MESConsumableServiceProxy.getConsumableInfoUtil().decrementQuantityInfo(lotName, "", "", "",
				TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()), quantity, udfs);

		MESConsumableServiceProxy.getConsumableServiceImpl().decrementQuantity(consumableData, transitionInfo, eventInfo);
	}
}
