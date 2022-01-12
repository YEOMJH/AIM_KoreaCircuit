package kr.co.aim.messolution.durable.event.CNX;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.jdom.Document;

public class ChangeOperationOLEDMaskLot extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		// UnComplete
		String sFactoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String maskLotName = SMessageUtil.getBodyItemValue(doc, "MASKLOTNAME", true);
		String newOperationName = SMessageUtil.getBodyItemValue(doc, "NEWPROCESSOPERATIONNAME", true);
		String nodeStack = SMessageUtil.getBodyItemValue(doc, "NODESTACK", true);
		String doAlsoOthers = SMessageUtil.getBodyItemValue(doc, "DO_ALSO_OTHERS", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeOperationMask", this.getEventUser(), this.getEventComment(), "", "", "Y");
		eventInfo.setLastEventTimekey(TimeStampUtil.getCurrentEventTimeKey());

		MaskLot maskLot = ExtendedObjectProxy.getMaskLotService().selectByKey(false, new Object[] { maskLotName });
		List<MaskLot> maskLotList = new ArrayList<MaskLot>();
		if(StringUtil.equals(doAlsoOthers, "NO") && StringUtil.isNotEmpty(maskLot.getCarrierName()))
		{
			throw new CustomException("MASKNO-0001");
		}
		if (StringUtil.equals(doAlsoOthers, "YES") && StringUtil.isNotEmpty(maskLot.getCarrierName()))
		{
			String condition = " WHERE CARRIERNAME = ? ";
			Object[] bindSet = new Object[] { maskLot.getCarrierName() };
			maskLotList = ExtendedObjectProxy.getMaskLotService().select(condition, bindSet);
		}
		else
		{
			maskLotList.add(maskLot);
		}

		for (MaskLot maskLotData : maskLotList)
		{
			ExtendedObjectProxy.getMaskLotService().changeOperationMask(eventInfo, maskLotData, sFactoryName, newOperationName, nodeStack);
		}

		return doc;
	}
}
