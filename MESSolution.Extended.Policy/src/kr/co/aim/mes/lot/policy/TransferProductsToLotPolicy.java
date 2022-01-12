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
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.AssignProductsInfo;
import kr.co.aim.greentrack.lot.management.info.TransferProductsToLotInfo;

public class TransferProductsToLotPolicy extends kr.co.aim.greentrack.lot.management.policy.TransferProductsToLotPolicy {

	public void decrementQuantity(DataInfo oldLot, DataInfo newLot, EventInfo eventInfo, TransitionInfo transitionInfo)
			throws NotFoundSignal, DuplicateNameSignal, FrameworkErrorSignal, InvalidStateTransitionSignal
	{
		Lot newLotData = (Lot) newLot;
		Lot oldLotData = (Lot) oldLot;
		TransferProductsToLotInfo transferProductsToLotInfo = (TransferProductsToLotInfo) transitionInfo;

		executeValidation(eventInfo.getBehaviorName(), "CanNotDoAtLotState", newLotData.getKey(), oldLotData.getLotState(), oldLotData.getLotProcessState(), oldLotData.getLotHoldState());

		executeValidation(eventInfo.getBehaviorName(), "ProductQuantityOutOfRange", newLotData.getKey(), transferProductsToLotInfo.getProductQuantity());

		BigDecimal qty = BigDecimal.valueOf(newLotData.getProductQuantity());
		BigDecimal dQty = BigDecimal.valueOf(transferProductsToLotInfo.getProductQuantity());
		BigDecimal resultQty = qty.subtract(dQty);

		// newLotData.setProductQuantity(newLotData.getProductQuantity() - transferProductsToLotInfo.getProductQuantity());
		newLotData.setProductQuantity(resultQty.doubleValue());

		executeValidation(eventInfo.getBehaviorName(), "CurrentProductQuantityOutOfRange", newLotData.getKey(), newLotData.getProductQuantity());

		if (newLotData.getProductQuantity() == 0 && StringUtils.equals(transferProductsToLotInfo.getEmptyFlag(), "Y"))
		{
			if (StringUtils.equals(newLotData.getLotState(), getConstantMap().Lot_Released))
			{
				this.makeLotState(oldLotData, newLotData, getConstantMap().Lot_Emptied);
				newLotData.setProductQuantity(0);
				newLotData.setSubProductQuantity(0);
				newLotData.setSubProductQuantity1(0);
				newLotData.setSubProductQuantity2(0);
			}
		}

		newLotData.setDestinationLotName(transferProductsToLotInfo.getDestinationLotName());

		if (StringUtils.isNotEmpty(newLotData.getDestinationLotName()))
		{
			newLotData.setLastEventFlag("S");
		}
	}
}
