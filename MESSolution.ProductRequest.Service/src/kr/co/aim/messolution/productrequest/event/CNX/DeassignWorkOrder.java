package kr.co.aim.messolution.productrequest.event.CNX;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ProductRequestAssign;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class DeassignWorkOrder extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> workOrderList = SMessageUtil.getBodySequenceItemList(doc, "WORKORDERLIST", true);

		for (Element workOrder : workOrderList)
		{
			String productRequestName = SMessageUtil.getChildText(workOrder, "PRODUCTREQUESTNAME", true);
			String toProductRequestName = SMessageUtil.getChildText(workOrder, "TOPRODUCTREQUESTNAME", true);

			EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeassignWorkOrder", this.getEventUser(), this.getEventComment(), "", "");
			eventInfo.setLastEventTimekey(TimeUtils.getCurrentEventTimeKey());

			ProductRequestAssign workOrderInfo = ExtendedObjectProxy.getProductRequestAssignService().getProductRequestAssignData(productRequestName, toProductRequestName);

			ExtendedObjectProxy.getProductRequestAssignService().remove(eventInfo, workOrderInfo);
		}

		return doc;
	}

}
