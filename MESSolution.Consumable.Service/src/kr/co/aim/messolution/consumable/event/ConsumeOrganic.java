package kr.co.aim.messolution.consumable.event;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.CrucibleLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.info.SetEventInfo;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class ConsumeOrganic extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> consumerbleList = SMessageUtil.getBodySequenceItemList(doc, "CONSUMERBLELIST", true);
		String crucibleLotName = SMessageUtil.getBodyItemValue(doc, "CRUCIBLRLOTNAME", true);
		String durableName = SMessageUtil.getBodyItemValue(doc, "DURABLENAME", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ConsumeOrganic", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		for (Element consumableE : consumerbleList)
		{
			String consumableName = consumableE.getChild("CONSUMABLENAME").getText();

			Consumable consumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(consumableName);

			consumableData.setMaterialLocationName("");
			consumableData.setQuantity(0);
			consumableData.setConsumableState("NotAvailable");

			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.getUdfs().put("CARRIERNAME", "");
			setEventInfo.getUdfs().put("MACHINENAME", "");
			setEventInfo.getUdfs().put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MaterialLocation_Bank);
			setEventInfo.getUdfs().put("ASSIGNEDQTY", "0");
			setEventInfo.getUdfs().put("CRUCIBLELOTNAME", "");
			setEventInfo.getUdfs().put("KITQUANTITY", "0");
			setEventInfo.getUdfs().put("UNKITTIME", TimeStampUtil.toTimeString(eventInfo.getEventTime()));
			setEventInfo.getUdfs().put("KITUSER", "");

			ConsumableServiceProxy.getConsumableService().update(consumableData);
			MESConsumableServiceProxy.getConsumableServiceImpl().setEvent(consumableData.getKey().getConsumableName(), setEventInfo, eventInfo);
		}

		// Set CrucibleData Qty
		CrucibleLot crucibleLotData = setCrucibleLotInfo(eventInfo, crucibleLotName);

		if (crucibleLotData.getWeight() == 0)
		{
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableName);

			durableData.setLotQuantity(0);
			durableData.setDurableState("Available");

			kr.co.aim.greentrack.durable.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.durable.management.info.SetEventInfo();

			DurableServiceProxy.getDurableService().update(durableData);
			MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
		}

		return doc;
	}

	private CrucibleLot setCrucibleLotInfo(EventInfo eventInfo, String crucibleLotName) throws greenFrameDBErrorSignal, CustomException
	{
		List<Consumable> organicList = new ArrayList<Consumable>();

		String condition = " CRUCIBLELOTNAME = ? ";
		Object[] bindSet = new Object[] { crucibleLotName };

		double weight = 0;

		try
		{
			organicList = ConsumableServiceProxy.getConsumableService().select(condition, bindSet);

			for (Consumable organic : organicList)
				weight = CommonUtil.doubleAdd(weight, organic.getQuantity());
		}
		catch (Exception e)
		{
		}

		CrucibleLot dataInfo = ExtendedObjectProxy.getCrucibleLotService().selectByKey(true, new Object[] { crucibleLotName });

		if (weight == 0)
		{
			dataInfo.setCrucibleLotState("Completed");
			dataInfo.setDurableName("");
		}

		dataInfo.setWeight(weight);
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
		dataInfo.setLastEventUser(eventInfo.getEventUser());

		dataInfo = ExtendedObjectProxy.getCrucibleLotService().modify(eventInfo, dataInfo);

		return dataInfo;
	}
}
