package kr.co.aim.mes.durable.policy;

import java.math.BigDecimal;

import kr.co.aim.greenframe.orm.info.DataInfo;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.DecrementDurationUsedInfo;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.info.TransitionInfo;

public class DecrementDurationUsedPolicy extends kr.co.aim.greentrack.durable.management.policy.DecrementDurationUsedPolicy {

	public void decrementDurationUsed(DataInfo oldDurableData, DataInfo newDurableData, EventInfo eventInfo, TransitionInfo transitionInfo)
			throws NotFoundSignal, InvalidStateTransitionSignal, DuplicateNameSignal, FrameworkErrorSignal
	{
		Durable oldData = (Durable) oldDurableData;
		Durable newData = (Durable) newDurableData;
		DecrementDurationUsedInfo decrementDurationUsedInfo = (DecrementDurationUsedInfo) transitionInfo;

		executeValidation(eventInfo.getBehaviorName(), this.getClass().getName(), "CanNotDoAtDurableState", newData.getKey(), oldData.getDurableState(), oldData.getDurableCleanState());

		executeValidation(eventInfo.getBehaviorName(), this.getClass().getName(), "DurationOutOfRange", newData.getKey(), decrementDurationUsedInfo.getDurationUsed());

		BigDecimal qty = BigDecimal.valueOf(newData.getDurationUsed());
		BigDecimal dQty = BigDecimal.valueOf(decrementDurationUsedInfo.getDurationUsed());
		BigDecimal resultQty = qty.subtract(dQty);

		// newData.setDurationUsed(newData.getDurationUsed() - decrementDurationUsedInfo.getDurationUsed());
		newData.setDurationUsed(resultQty.doubleValue());

		executeValidation(eventInfo.getBehaviorName(), this.getClass().getName(), "CurrentDurationUsedOutOfRange", newData.getKey(), decrementDurationUsedInfo.getDurationUsed());

	}
}
