package kr.co.aim.messolution.consumable.event;

import java.util.List;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.info.SetEventInfo;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class DeassignFilm extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String durableName = SMessageUtil.getBodyItemValue(doc, "DURABLENAME", true);

		List<Element> filmList = SMessageUtil.getBodySequenceItemList(doc, "FILMLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeassignFilm", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());

		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableName);

		CommonValidation.CheckDurableCleanState(durableData);
		CommonValidation.CheckDurableHoldState(durableData);
		CommonValidation.CheckDurableState(durableData);

		for (Element film : filmList)
		{
			String filmName = SMessageUtil.getChildText(film, "CONSUMABLENAME", true);

			Consumable consumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(filmName);

			consumableData.setConsumableState("Available");
			ConsumableServiceProxy.getConsumableService().update(consumableData);

			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.getUdfs().put("CARRIERNAME", "");
			setEventInfo.getUdfs().put("SEQ", "");

			ConsumableServiceProxy.getConsumableService().setEvent(consumableData.getKey(), eventInfo, setEventInfo);
		}

		long lotQuantity = durableData.getLotQuantity() - filmList.size();

		durableData.setLotQuantity(lotQuantity);

		if (lotQuantity < 1)
		{
			durableData.setDurableState("Available");
		}

		DurableServiceProxy.getDurableService().update(durableData);
		kr.co.aim.greentrack.durable.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.durable.management.info.SetEventInfo();
		DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEventInfo);

		return doc;
	}

}
