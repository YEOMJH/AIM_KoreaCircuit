package kr.co.aim.mes.lot.policy;

import java.math.BigDecimal;

import kr.co.aim.greenframe.orm.info.DataInfo;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.info.TransitionInfo;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.AssignNewProductsInfo;

public class AssignNewProductsPolicy extends kr.co.aim.greentrack.lot.management.policy.AssignNewProductsPolicy {
	
	public void incrementQuantity(DataInfo oldLot, DataInfo newLot, EventInfo eventInfo, TransitionInfo transitionInfo)
			throws NotFoundSignal, DuplicateNameSignal, FrameworkErrorSignal, InvalidStateTransitionSignal
	{
		Lot newLotData = (Lot) newLot;
		Lot oldLotData = (Lot) oldLot;

		AssignNewProductsInfo assignCarrierInfo = (AssignNewProductsInfo) transitionInfo;

		executeValidation(eventInfo.getBehaviorName(), "CanNotDoAtLotState", newLotData.getKey(), oldLotData.getLotState(), oldLotData.getLotProcessState(), oldLotData.getLotHoldState());

		executeValidation(eventInfo.getBehaviorName(), "ProductQuantityOutOfRange", newLotData.getKey(), assignCarrierInfo.getProductQuantity());

		BigDecimal qty = BigDecimal.valueOf(newLotData.getProductQuantity());
		BigDecimal dQty = BigDecimal.valueOf(assignCarrierInfo.getProductQuantity());
		BigDecimal resultQty = qty.add(dQty);

		// newLotData.setProductQuantity(newLotData.getProductQuantity() + assignCarrierInfo.getProductQuantity());
		newLotData.setProductQuantity(resultQty.doubleValue());

		executeValidation(eventInfo.getBehaviorName(), "CurrentProductQuantityOutOfRange", newLotData.getKey(), newLotData.getProductQuantity());
	}
}
