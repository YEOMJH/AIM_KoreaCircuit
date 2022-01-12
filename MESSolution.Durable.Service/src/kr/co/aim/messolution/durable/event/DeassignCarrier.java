package kr.co.aim.messolution.durable.event;

import java.util.List;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.DeassignCarrierInfo;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.jdom.Document;

public class DeassignCarrier extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String messageName = SMessageUtil.getMessageName(doc);
		String sDurableName = SMessageUtil.getBodyItemValue(doc, "DURABLENAME", true);
		String sLotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);

		Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(sLotName);

		CommonValidation.checkJobDownFlag(lotData);

		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sDurableName);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo(messageName, getEventUser(), getEventComment(), "", "");

		List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);
		DeassignCarrierInfo createInfo = MESLotServiceProxy.getLotInfoUtil().deassignCarrierInfo(lotData, durableData, productUSequence);
		MESLotServiceProxy.getLotServiceImpl().deassignCarrier(lotData, createInfo, eventInfo);

		return doc;
	}

}
