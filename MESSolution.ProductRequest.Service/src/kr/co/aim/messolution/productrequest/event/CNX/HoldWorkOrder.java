package kr.co.aim.messolution.productrequest.event.CNX;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.info.MakeOnHoldInfo;

import org.jdom.Document;

public class HoldWorkOrder extends SyncHandler {

	public Object doWorks(Document doc) throws CustomException
	{
		String productRequestName = SMessageUtil.getBodyItemValue(doc, "PRODUCTREQUESTNAME", true);

		ProductRequest productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(productRequestName);
		
		CommonValidation.checkProductRequestState(productRequestData);
		CommonValidation.checkProductRequestHoldState(productRequestData, GenericServiceProxy.getConstantMap().Prq_OnHold);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("HoldWorkOrder", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		MakeOnHoldInfo makeOnHoldInfo = new MakeOnHoldInfo();
		
		MESWorkOrderServiceProxy.getProductRequestServiceImpl().makeOnHold(productRequestData, makeOnHoldInfo , eventInfo);

		return doc;
	}

}
