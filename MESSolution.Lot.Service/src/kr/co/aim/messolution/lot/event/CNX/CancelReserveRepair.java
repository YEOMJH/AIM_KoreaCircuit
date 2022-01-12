package kr.co.aim.messolution.lot.event.CNX;

import java.util.List;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ReserveRepairProduct;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import org.jdom.Document;
import org.jdom.Element;

public class CancelReserveRepair extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> productElementList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", false);

		for (Element productElement : productElementList)
		{
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelReserveRepair", this.getEventUser(), this.getEventComment(), "", "");

			String lotName = productElement.getChildText("LOTNAME");
			String productName = productElement.getChildText("PRODUCTNAME");
			
			ReserveRepairProduct dataInfo = ExtendedObjectProxy.getReserveRepairProductService().selectByKey(false, new Object[] { productName, lotName });
			
			ExtendedObjectProxy.getReserveRepairProductService().remove(eventInfo, dataInfo);;
		}

		return doc;
	}
}
