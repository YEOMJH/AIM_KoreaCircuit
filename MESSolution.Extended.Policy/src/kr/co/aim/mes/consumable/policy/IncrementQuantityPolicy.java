package kr.co.aim.mes.consumable.policy;

import java.math.BigDecimal;

import kr.co.aim.greenframe.orm.info.DataInfo;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.info.IncrementQuantityInfo;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.info.TransitionInfo;

public class IncrementQuantityPolicy extends kr.co.aim.greentrack.consumable.management.policy.IncrementQuantityPolicy{
	
	@Override 

	public void incrementQuantity(DataInfo oldConsumableData, DataInfo newConsumableData, EventInfo eventInfo,
			TransitionInfo transitionInfo)
					throws FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal, InvalidStateTransitionSignal
	{
		Consumable oldData = (Consumable) oldConsumableData;
		Consumable newData = (Consumable) newConsumableData;
		IncrementQuantityInfo incrementQuantityInfo = (IncrementQuantityInfo) transitionInfo;
		
		executeValidation(eventInfo.getBehaviorName(), this.getClass().getName(), "CanNotDoAtConsumableState",
		newData.getKey(), oldData.getConsumableState());
		
		executeValidation(eventInfo.getBehaviorName(), this.getClass().getName(), "ConsumableQuantityOutOfRange",
		newData.getKey(), incrementQuantityInfo.getQuantity());
		
//		newData.setQuantity(newData.getQuantity() + incrementQuantityInfo.getQuantity());
		BigDecimal qty = BigDecimal.valueOf(newData.getQuantity());
		BigDecimal dQty = BigDecimal.valueOf(incrementQuantityInfo.getQuantity());
		BigDecimal resultQty = qty.add(dQty);
		
		newData.setQuantity(resultQty.doubleValue());

		executeValidation(eventInfo.getBehaviorName(), this.getClass().getName(),
		"CurrentConsumableQuantityOutOfRange", newData.getKey(), incrementQuantityInfo.getQuantity());
	}
}
