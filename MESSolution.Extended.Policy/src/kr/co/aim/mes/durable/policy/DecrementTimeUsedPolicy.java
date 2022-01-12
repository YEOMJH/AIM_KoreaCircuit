package kr.co.aim.mes.durable.policy;

import java.math.BigDecimal;

import kr.co.aim.greenframe.orm.info.DataInfo;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.DecrementTimeUsedInfo;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.info.TransitionInfo;

public class DecrementTimeUsedPolicy extends kr.co.aim.greentrack.durable.management.policy.DecrementTimeUsedPolicy {

	public void decrementTimeUsed(DataInfo oldDurableData, DataInfo newDurableData, EventInfo eventInfo, TransitionInfo transitionInfo)
			throws NotFoundSignal, InvalidStateTransitionSignal, DuplicateNameSignal, FrameworkErrorSignal
	{
		Durable oldData = (Durable) oldDurableData;
		Durable newData = (Durable) newDurableData;
		DecrementTimeUsedInfo decrementTimeUsedInfo = (DecrementTimeUsedInfo) transitionInfo;

		executeValidation(eventInfo.getBehaviorName(), this.getClass().getName(), "CanNotDoAtDurableState", newData.getKey(), oldData.getDurableState(), oldData.getDurableCleanState());

		executeValidation(eventInfo.getBehaviorName(), this.getClass().getName(), "TimeUsedOutOfRange", newData.getKey(), decrementTimeUsedInfo.getTimeUsed());

		BigDecimal qty = BigDecimal.valueOf(newData.getTimeUsed());
		BigDecimal dQty = BigDecimal.valueOf(decrementTimeUsedInfo.getTimeUsed());
		BigDecimal resultQty = qty.subtract(dQty);

		// newData.setTimeUsed(newData.getTimeUsed() - decrementTimeUsedInfo.getTimeUsed());
		newData.setTimeUsed(resultQty.doubleValue());

		executeValidation(eventInfo.getBehaviorName(), this.getClass().getName(), "CurrentTimeUsedOutOfRange", newData.getKey(), decrementTimeUsedInfo.getTimeUsed());
	}
}
