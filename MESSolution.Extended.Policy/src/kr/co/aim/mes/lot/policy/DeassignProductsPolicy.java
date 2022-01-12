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
import kr.co.aim.greentrack.lot.management.info.DeassignProductsInfo;

public class DeassignProductsPolicy extends kr.co.aim.greentrack.lot.management.policy.DeassignProductsPolicy {
	
	public void decrementQuantity(DataInfo oldLot, DataInfo newLot, EventInfo eventInfo, TransitionInfo transitionInfo)
			throws NotFoundSignal, DuplicateNameSignal, FrameworkErrorSignal, InvalidStateTransitionSignal
	{
		Lot newLotData = (Lot) newLot;
		Lot oldLotData = (Lot) oldLot;
		DeassignProductsInfo deassignProductsInfo = (DeassignProductsInfo) transitionInfo;

		executeValidation(eventInfo.getBehaviorName(), "CanNotDoAtLotState", newLotData.getKey(), oldLotData.getLotState(), oldLotData.getLotProcessState(), oldLotData.getLotHoldState());

		newLotData.setDestinationLotName(deassignProductsInfo.getConsumerLotName());

		if (StringUtils.isNotEmpty(newLotData.getDestinationLotName()))
		{
			newLotData.setLastEventFlag("S");
		}

		executeValidation(eventInfo.getBehaviorName(), "ProductQuantityOutOfRange", newLotData.getKey(), deassignProductsInfo.getProductQuantity());

		BigDecimal qty = BigDecimal.valueOf(newLotData.getProductQuantity());
		BigDecimal dQty = BigDecimal.valueOf(deassignProductsInfo.getProductQuantity());
		BigDecimal resultQty = qty.subtract(dQty);

		// newLotData.setProductQuantity(newLotData.getProductQuantity() - deassignProductsInfo.getProductQuantity());
		newLotData.setProductQuantity(resultQty.doubleValue());
		executeValidation(eventInfo.getBehaviorName(), "CurrentProductQuantityOutOfRange", newLotData.getKey(), newLotData.getProductQuantity());

		if (newLotData.getProductQuantity() == 0 && StringUtils.equals(deassignProductsInfo.getEmptyFlag(), "Y"))
		{
			this.makeLotState(oldLotData, newLotData, getConstantMap().Lot_Emptied);
			newLotData.setProductQuantity(0);
			newLotData.setSubProductQuantity(0);
			newLotData.setSubProductQuantity1(0);
			newLotData.setSubProductQuantity2(0);
		}
	}
}
