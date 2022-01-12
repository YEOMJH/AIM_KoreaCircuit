package kr.co.aim.messolution.consumable.service;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.data.ConsumableHistory;
import kr.co.aim.greentrack.consumable.management.data.ConsumableKey;
import kr.co.aim.greentrack.consumable.management.info.AssignTransportGroupInfo;
import kr.co.aim.greentrack.consumable.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.consumable.management.info.CreateInfo;
import kr.co.aim.greentrack.consumable.management.info.DeassignTransportGroupInfo;
import kr.co.aim.greentrack.consumable.management.info.DecrementQuantityInfo;
import kr.co.aim.greentrack.consumable.management.info.IncrementQuantityInfo;
import kr.co.aim.greentrack.consumable.management.info.MakeAvailableInfo;
import kr.co.aim.greentrack.consumable.management.info.MakeNotAvailableInfo;
import kr.co.aim.greentrack.consumable.management.info.MergeInfo;
import kr.co.aim.greentrack.consumable.management.info.SetAreaInfo;
import kr.co.aim.greentrack.consumable.management.info.SetEventInfo;
import kr.co.aim.greentrack.consumable.management.info.SplitInfo;
import kr.co.aim.greentrack.consumable.management.info.UndoInfo;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.info.SetMaterialLocationInfo;
import kr.co.aim.greentrack.generic.policy.CommonPolicy;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import java.math.BigDecimal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import sun.net.www.content.text.Generic;

public class ConsumableServiceImpl extends CommonPolicy implements ApplicationContextAware {
	private ApplicationContext applicationContext;
	private static Log log = LogFactory.getLog(ConsumableServiceImpl.class);

	public void setApplicationContext(ApplicationContext arg0) throws BeansException
	{
		applicationContext = arg0;
	}

	public void assignTransportGroup(Consumable consumableData, AssignTransportGroupInfo assignTransportGroupInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal,
			NotFoundSignal, DuplicateNameSignal
	{
		ConsumableServiceProxy.getConsumableService().assignTransportGroup(consumableData.getKey(), eventInfo, assignTransportGroupInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public void changeSpec(Consumable consumableData, ChangeSpecInfo changeSpecInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal,
			DuplicateNameSignal
	{
		ConsumableServiceProxy.getConsumableService().changeSpec(consumableData.getKey(), eventInfo, changeSpecInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public void createCrate(EventInfo eventInfo, String consumableName, CreateInfo createInfo) throws CustomException
	{
		try
		{
			ConsumableKey consumableKey = new ConsumableKey();
			consumableKey.setConsumableName(consumableName);

			ConsumableServiceProxy.getConsumableService().create(consumableKey, eventInfo, createInfo);

			log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
		}
		catch (InvalidStateTransitionSignal ie)
		{
			throw new CustomException("CRATE-9003", consumableName);
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("CRATE-9999", fe.getMessage());
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("CRATE-9002", consumableName);
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("CRATE-9001", consumableName);
		}
	}

	public void createMaterial(EventInfo eventInfo, String consumableName, CreateInfo createInfo) throws CustomException
	{
		try
		{
			ConsumableKey consumableKey = new ConsumableKey();
			consumableKey.setConsumableName(consumableName);

			ConsumableServiceProxy.getConsumableService().create(consumableKey, eventInfo, createInfo);

			log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
		}
		catch (InvalidStateTransitionSignal ie)
		{
			throw new CustomException("MATERIAL-9004", consumableName);
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("MATERIAL-9999", fe.getMessage());
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("MATERIAL-9003", consumableName);
		}
	}

	public void deassignTransportGroup(Consumable consumableData, DeassignTransportGroupInfo deassignTransportGroupInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal,
			FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ConsumableServiceProxy.getConsumableService().deassignTransportGroup(consumableData.getKey(), eventInfo, deassignTransportGroupInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public Consumable decrementQuantity(Consumable consumableData, DecrementQuantityInfo decrementQuantityInfo, EventInfo eventInfo) throws CustomException
	{
		try
		{
			consumableData = ConsumableServiceProxy.getConsumableService().decrementQuantity(consumableData.getKey(), eventInfo, decrementQuantityInfo);
		}
		catch (InvalidStateTransitionSignal ie)
		{
			throw new CustomException("CRATE-9003", consumableData.getKey().getConsumableName());
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("CRATE-9999", fe.getMessage());
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("CRATE-9001", consumableData.getKey().getConsumableName());
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("CRATE-9002", consumableData.getKey().getConsumableName());
		}
		
		return consumableData;
	}

	public void decrementQuantityDoubleType(Consumable consumableData, DecrementQuantityInfo decrementQuantityInfo, EventInfo eventInfo) throws CustomException
	{
		try
		{
			// ConsumableServiceProxy.getConsumableService().decrementQuantity(consumableData.getKey(), eventInfo, decrementQuantityInfo);

			Consumable newData = (Consumable) ObjectUtil.copyTo(consumableData);
			// DecrementQuantityInfo decrementQuantityInfo = (DecrementQuantityInfo) transitionInfo;

			executeValidation(eventInfo.getBehaviorName(), "kr.co.aim.greentrack.consumable.management.policy.DecrementQuantityPolicy", "CanNotDoAtConsumableState", newData.getKey(),
					consumableData.getConsumableState());

			// executeValidation(eventInfo.getBehaviorName(), this.getClass().getName(), "ConsumableQuantityOutOfRange", newData.getKey(), decrementQuantityInfo.getQuantity());

			// Calculate Quantity
			// newData.setQuantity(newData.getQuantity() - decrementQuantityInfo.getQuantity());
			BigDecimal qty = BigDecimal.valueOf(newData.getQuantity());
			BigDecimal dQty = BigDecimal.valueOf(decrementQuantityInfo.getQuantity());
			BigDecimal resultQty = qty.subtract(dQty);

			newData.setQuantity(resultQty.doubleValue());

			// executeValidation(eventInfo.getBehaviorName(), this.getClass().getName(), "CurrentConsumableQuantityOutOfRange", newData.getKey(), newData.getQuantity());

			ConsumableHistory historyData = new ConsumableHistory();

			ConsumableServiceProxy.getConsumableHistoryDataAdaptor().setHV(consumableData, newData, historyData);

			ConsumableServiceProxy.getConsumableHistoryDataAdaptor().setAdditionalInfo(historyData, decrementQuantityInfo.getConsumerLotName(), decrementQuantityInfo.getConsumerProductName(),
					decrementQuantityInfo.getConsumerTimeKey(), decrementQuantityInfo.getConsumerPOName(), decrementQuantityInfo.getConsumerPOVersion());

			historyData.getKey().setTimeKey(eventInfo.getEventTimeKey());
			historyData.setEventComment(eventInfo.getEventComment());
			historyData.setEventName(eventInfo.getEventName());
			historyData.setEventTime(eventInfo.getEventTime());

			ConsumableServiceProxy.getConsumableHistoryService().insert(historyData);
			ConsumableServiceProxy.getConsumableService().update(newData);
		}
		catch (InvalidStateTransitionSignal ie)
		{
			throw new CustomException("CRATE-9003", consumableData.getKey().getConsumableName());
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("CRATE-9999", fe.getMessage());
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("CRATE-9001", consumableData.getKey().getConsumableName());
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("CRATE-9002", consumableData.getKey().getConsumableName());
		}
	}

	public void incrementQuantity(Consumable consumableData, IncrementQuantityInfo incrementQuantityInfo, EventInfo eventInfo) throws CustomException
	{
		try
		{
			ConsumableServiceProxy.getConsumableService().incrementQuantity(consumableData.getKey(), eventInfo, incrementQuantityInfo);

			log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
		}
		catch (InvalidStateTransitionSignal ie)
		{
			throw new CustomException("CRATE-9003", consumableData.getKey().getConsumableName());
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("CRATE-9999", fe.getMessage());
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("CRATE-9001", consumableData.getKey().getConsumableName());
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("CRATE-9002", consumableData.getKey().getConsumableName());
		}
	}

	public void makeAvailable(Consumable consumableData, MakeAvailableInfo makeAvailableInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal,
			DuplicateNameSignal
	{
		ConsumableServiceProxy.getConsumableService().makeAvailable(consumableData.getKey(), eventInfo, makeAvailableInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public void makeNotAvailable(Consumable consumableData, MakeNotAvailableInfo makeNotAvailableInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal,
			DuplicateNameSignal
	{
		ConsumableServiceProxy.getConsumableService().makeNotAvailable(consumableData.getKey(), eventInfo, makeNotAvailableInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public void merge(Consumable consumableData, MergeInfo mergeInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ConsumableServiceProxy.getConsumableService().merge(consumableData.getKey(), eventInfo, mergeInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public void setArea(Consumable consumableData, SetAreaInfo setAreaInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ConsumableServiceProxy.getConsumableService().setArea(consumableData.getKey(), eventInfo, setAreaInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public void setEvent(String consumableName, SetEventInfo setEventInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ConsumableKey consumableKey = new ConsumableKey();
		consumableKey.setConsumableName(consumableName);

		ConsumableServiceProxy.getConsumableService().setEvent(consumableKey, eventInfo, setEventInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public void setMaterialLocation(Consumable consumableData, SetMaterialLocationInfo setMaterialLocationInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal,
			NotFoundSignal, DuplicateNameSignal
	{
		ConsumableServiceProxy.getConsumableService().setMaterialLocation(consumableData.getKey(), eventInfo, setMaterialLocationInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public void split(Consumable consumableData, SplitInfo splitInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ConsumableServiceProxy.getConsumableService().split(consumableData.getKey(), eventInfo, splitInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public void undo(Consumable consumableData, UndoInfo undoInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ConsumableServiceProxy.getConsumableService().undo(consumableData.getKey(), eventInfo, undoInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}
}
