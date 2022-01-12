package kr.co.aim.mes.lot.policy;

import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;

import kr.co.aim.greenframe.orm.info.DataInfo;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.info.TransitionInfo;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.AssignProductsInfo;

public class AssignProductsPolicy extends kr.co.aim.greentrack.lot.management.policy.AssignProductsPolicy {
	
	public void incrementQuantity(DataInfo oldLot, DataInfo newLot, EventInfo eventInfo, TransitionInfo transitionInfo)
			throws NotFoundSignal, DuplicateNameSignal, FrameworkErrorSignal, InvalidStateTransitionSignal
	{
		Lot newLotData = (Lot) newLot;
		Lot oldLotData = (Lot) oldLot;
		AssignProductsInfo assignProductsInfo = (AssignProductsInfo) transitionInfo;

		if (StringUtils.equals(assignProductsInfo.getValidationFlag(), "N") == false)
		{
			executeValidation(eventInfo.getBehaviorName(), "CanNotDoAtLotState", oldLotData.getKey(), oldLotData.getLotState(), oldLotData.getLotProcessState(), oldLotData.getLotHoldState());
		}

		newLotData.setSourceLotName(assignProductsInfo.getSourceLotName());
		if (StringUtils.isNotEmpty(newLotData.getSourceLotName()))
		{
			newLotData.setLastEventFlag("D");
		}

		executeValidation(eventInfo.getBehaviorName(), "ProductQuantityOutOfRange", oldLotData.getKey(), assignProductsInfo.getProductQuantity());

		BigDecimal qty = BigDecimal.valueOf(newLotData.getProductQuantity());
		BigDecimal dQty = BigDecimal.valueOf(assignProductsInfo.getProductQuantity());
		BigDecimal resultQty = qty.add(dQty);

		// newLotData.setProductQuantity(assignProductsInfo.getProductQuantity());
		// newLotData.setProductQuantity(newLotData.getProductQuantity() + assignProductsInfo.getProductQuantity());
		newLotData.setProductQuantity(resultQty.doubleValue());
		executeValidation(eventInfo.getBehaviorName(), "CurrentProductQuantityOutOfRange", oldLotData.getKey(), newLotData.getProductQuantity());
	}
}
