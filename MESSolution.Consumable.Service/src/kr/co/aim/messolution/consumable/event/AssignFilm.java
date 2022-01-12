package kr.co.aim.messolution.consumable.event;

import java.util.List;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
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

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class AssignFilm extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String durableName = SMessageUtil.getBodyItemValue(doc, "DURABLENAME", true);

		List<Element> filmList = SMessageUtil.getBodySequenceItemList(doc, "FILMLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("AssignFilm", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());

		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableName);

		CommonValidation.CheckDurableCleanState(durableData);
		CommonValidation.CheckDurableHoldState(durableData);
		CommonValidation.CheckDurableState(durableData);
		// check is film Box
		CommonValidation.checkFilmBox(durableData);

		long lotQuantity = durableData.getLotQuantity() + filmList.size();

		if (lotQuantity > durableData.getCapacity())
		{
			throw new CustomException("CONSUMABLE-0002");
		}
		// mantis 0000134
		if (durableData.getCapacity() - lotQuantity <= 0)
		{

			//FILM-0007:CapaCity - LotQty < 0 Can Not Assign
			throw new CustomException("FILM-0007");
		}

		for (Element film : filmList)
		{
			String filmName = SMessageUtil.getChildText(film, "CONSUMABLENAME", true);
			String seq = SMessageUtil.getChildText(film, "SEQ", true);

			Consumable consumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(filmName);

			CommonValidation.checkConsumableState(consumableData);

			consumableData.setConsumableState("InUse");
			consumableData.getUdfs().put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MaterialLocation_OutStock);
			ConsumableServiceProxy.getConsumableService().update(consumableData);

			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.getUdfs().put("CARRIERNAME", durableName);
			setEventInfo.getUdfs().put("SEQ", seq);

			// mantis 0000134
			if (consumableData.getQuantity() <= 0)
			{
				//FILM-0008: FilmQty Is 0 Can Not Assign
				throw new CustomException("FILM-0008");
			}

			ConsumableServiceProxy.getConsumableService().setEvent(consumableData.getKey(), eventInfo, setEventInfo);
		}

		durableData.setLotQuantity(lotQuantity);

		if (StringUtils.equals(durableData.getDurableState(), "Available"))
		{
			durableData.setDurableState("InUse");
		}

		DurableServiceProxy.getDurableService().update(durableData);
		kr.co.aim.greentrack.durable.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.durable.management.info.SetEventInfo();
		DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEventInfo);

		return doc;
	}

}
