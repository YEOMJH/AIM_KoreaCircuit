package kr.co.aim.mes.durable.policy;

import java.math.BigDecimal;

import kr.co.aim.greenframe.orm.info.DataInfo;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.IncrementDurationUsedInfo;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.info.TransitionInfo;

public class IncrementDurationUsedPolicy extends kr.co.aim.greentrack.durable.management.policy.IncrementDurationUsedPolicy {

	public void incrementDurationUsed(DataInfo oldDurableData, DataInfo newDurableData, EventInfo eventInfo, TransitionInfo transitionInfo)
			throws NotFoundSignal, InvalidStateTransitionSignal, DuplicateNameSignal, FrameworkErrorSignal
	{
		Durable oldData = (Durable) oldDurableData;
		Durable newData = (Durable) newDurableData;
		IncrementDurationUsedInfo incrementDurationUsedInfo = (IncrementDurationUsedInfo) transitionInfo;

		executeValidation(eventInfo.getBehaviorName(), this.getClass().getName(), "CanNotDoAtDurableState", newData.getKey(), oldData.getDurableState(), oldData.getDurableCleanState());

		executeValidation(eventInfo.getBehaviorName(), this.getClass().getName(), "DurationOutOfRange", newData.getKey(), incrementDurationUsedInfo.getDurationUsed());

		BigDecimal qty = BigDecimal.valueOf(newData.getDurationUsed());
		BigDecimal dQty = BigDecimal.valueOf(incrementDurationUsedInfo.getDurationUsed());
		BigDecimal resultQty = qty.add(dQty);

		// newData.setDurationUsed(newData.getDurationUsed() + incrementDurationUsedInfo.getDurationUsed());
		newData.setDurationUsed(resultQty.doubleValue());

		executeValidation(eventInfo.getBehaviorName(), this.getClass().getName(), "CurrentDurationUsedOutOfRange", newData.getKey(), incrementDurationUsedInfo.getDurationUsed());

	}
}
