package kr.co.aim.mes.consumable.policy;

import java.math.BigDecimal;

import kr.co.aim.greenframe.orm.info.DataInfo;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.info.SplitInfo;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.info.TransitionInfo;

public class SplitPolicy extends kr.co.aim.greentrack.consumable.management.policy.SplitPolicy {

	
	public void decrementQuantity(DataInfo oData, DataInfo nData, EventInfo eventInfo, TransitionInfo transitionInfo)
			throws FrameworkErrorSignal
	{
		Consumable oldData = (Consumable) oData;
		Consumable newData = (Consumable) nData;
		SplitInfo splitInfo = (SplitInfo) transitionInfo;

		executeValidation(eventInfo.getBehaviorName(), this.getClass().getName(), "CanNotDoAtConsumableState",
			newData.getKey(), oldData.getConsumableState());

		executeValidation(eventInfo.getBehaviorName(), this.getClass().getName(), "ConsumableQuantityOutOfRange",
			newData.getKey(), splitInfo.getQuantity());
		
		BigDecimal qty = BigDecimal.valueOf(newData.getQuantity());
		BigDecimal dQty = BigDecimal.valueOf(splitInfo.getQuantity());
		BigDecimal resultQty = qty.subtract(dQty);

		newData.setQuantity(resultQty.doubleValue());

		executeValidation(eventInfo.getBehaviorName(), this.getClass().getName(),
			"CurrentConsumableQuantityOutOfRange", newData.getKey(), splitInfo.getQuantity());
	}

}
