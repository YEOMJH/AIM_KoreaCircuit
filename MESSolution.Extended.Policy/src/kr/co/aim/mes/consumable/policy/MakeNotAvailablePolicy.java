package kr.co.aim.mes.consumable.policy;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.greenframe.orm.info.DataInfo;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.info.MakeNotAvailableInfo;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.ExceptionKey;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.info.TransitionInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

public class MakeNotAvailablePolicy extends kr.co.aim.greentrack.consumable.management.policy.MakeNotAvailablePolicy
{
	public void makeNotAvailable(DataInfo oldConsumableData, DataInfo newConsumableData, EventInfo eventInfo,
									TransitionInfo transitionInfo)
			throws FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal, InvalidStateTransitionSignal
	{
		Consumable oldData = (Consumable) oldConsumableData;
		Consumable newData = (Consumable) newConsumableData;
		MakeNotAvailableInfo makeNotAvailableInfo = (MakeNotAvailableInfo) transitionInfo;

		if (StringUtil.in(newData.getConsumableType(), "Organic Adhesive", "Organic", "BottomLamination", "TopLamination", "PatternFilm"))
		{
             if(!StringUtil.in(newData.getConsumableState(),GenericServiceProxy.getConstantMap().Cons_InUse,GenericServiceProxy.getConstantMap().Cons_Available))
            	 throw new FrameworkErrorSignal(ExceptionKey.CanNotDoAtConsumableState_Exception,ObjectUtil.getString(newData.getKey()), "makeNotAvailable", newData.getConsumableState());
		}
		else
		{
			executeValidation(eventInfo.getBehaviorName(), this.getClass().getName(), "CanNotDoAtConsumableState", newData.getKey(), oldData.getConsumableState());
		}
		

		if(StringUtil.equals(newData.getConsumableState(), "InUse"))
			newData.setConsumableState(GenericServiceProxy.getConstantMap().Cons_NotAvailable);
		else
			makeConsumableState(newData, GenericServiceProxy.getConstantMap().Cons_NotAvailable);
	}
}
