package kr.co.aim.mes.consumable.policy;

import java.math.BigDecimal;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.orm.info.DataInfo;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.info.DecrementQuantityInfo;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.ExceptionKey;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.info.TransitionInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

public class DecrementQuantityPolicy extends kr.co.aim.greentrack.consumable.management.policy.DecrementQuantityPolicy{
	
	@Override
	public void decrementQuantity(DataInfo oData, DataInfo nData, EventInfo eventInfo, TransitionInfo transitionInfo)
			throws FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal, InvalidStateTransitionSignal
	{
		Consumable oldData = (Consumable) oData;
		Consumable newData = (Consumable) nData;
		DecrementQuantityInfo decrementQuantityInfo = (DecrementQuantityInfo) transitionInfo;
		
		if (StringUtil.in(newData.getConsumableType(), "Organic Adhesive", "Organic", "BottomLamination", "TopLamination", "PatternFilm"))
		{
             if(!StringUtil.in(newData.getConsumableState(),GenericServiceProxy.getConstantMap().Cons_InUse,GenericServiceProxy.getConstantMap().Cons_Available))
            	 throw new FrameworkErrorSignal(ExceptionKey.CanNotDoAtConsumableState_Exception,ObjectUtil.getString(newData.getKey()), "decrementQuantity", newData.getConsumableState());
		}
		else
		{
			executeValidation(eventInfo.getBehaviorName(), this.getClass().getName(), "CanNotDoAtConsumableState", newData.getKey(), oldData.getConsumableState());
		}

		executeValidation(eventInfo.getBehaviorName(), this.getClass().getName(), "ConsumableQuantityOutOfRange",
			newData.getKey(), decrementQuantityInfo.getQuantity());

//		//case 1: assign < == round - round
//		double quantity = Math.round(newData.getQuantity()*100.0 /100.0);
//		double decrementQuantity = Math.round(decrementQuantityInfo.getQuantity()*100.0 /100.0);
//		newData.setQuantity( quantity - decrementQuantity);
		//case 2: assign <== rounded 
//		double calcuatedQauntity = newData.getQuantity() - decrementQuantityInfo.getQuantity();
//		double roundedQuantity= Math.round( calcuatedQauntity *100.0 / 100.0);
//		newData.setQuantity(roundedQuantity );
		
		BigDecimal qty = BigDecimal.valueOf(newData.getQuantity());
		BigDecimal dQty = BigDecimal.valueOf(decrementQuantityInfo.getQuantity());
		BigDecimal resultQty = qty.subtract(dQty);
		
		newData.setQuantity(resultQty.doubleValue());

		executeValidation(eventInfo.getBehaviorName(), this.getClass().getName(),
			"CurrentConsumableQuantityOutOfRange", newData.getKey(), newData.getQuantity());
	}

}
