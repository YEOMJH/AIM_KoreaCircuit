package kr.co.aim.messolution.productrequest.event.CNX;

import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ControlProductRequestAssign;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class DessignForControlWorkOrder extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		List<Element> workOrderList = SMessageUtil.getBodySequenceItemList(doc, "WORKORDERLIST", true);

		for (Element workOrder : workOrderList)
		{
			String productRequestName = SMessageUtil.getChildText(workOrder, "PRODUCTREQUESTNAME", true);
			String toProductRequestName = SMessageUtil.getChildText(workOrder, "TOPRODUCTREQUESTNAME", true);

			EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeassignWorkOrder", this.getEventUser(), this.getEventComment(), "", "");
			eventInfo.setLastEventTimekey(TimeUtils.getCurrentEventTimeKey());

			ControlProductRequestAssign workOrderInfo =ExtendedObjectProxy.getControlProductRequestAssignService().getControlProductRequestAssignData(productRequestName, toProductRequestName);

			ExtendedObjectProxy.getControlProductRequestAssignService().remove(eventInfo, workOrderInfo);
		}

		return doc;
	}

}
