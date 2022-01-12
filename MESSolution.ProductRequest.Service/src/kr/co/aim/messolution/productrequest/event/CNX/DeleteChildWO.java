package kr.co.aim.messolution.productrequest.event.CNX;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class DeleteChildWO extends SyncHandler {

	public Object doWorks(Document doc) throws CustomException
	{
		String productRequestName = SMessageUtil.getBodyItemValue(doc, "PRODUCTREQUESTNAME", true);

		// Get ProductRequest Data
		ProductRequest productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(productRequestName);

		// Validation - ProductRequestState
		if (StringUtils.equals(productRequestData.getProductRequestHoldState(), GenericServiceProxy.getConstantMap().Prq_OnHold))
			throw new CustomException("PRODUCTREQUEST-0024", productRequestData.getKey().getProductRequestName(), productRequestData.getProductRequestHoldState());

		if(Long.parseLong(productRequestData.getUdfs().get("CREATEDQUANTITY").toString()) != 0)
			throw new CustomException("PRODUCTREQUEST-0036");
		
		if(productRequestData.getReleasedQuantity() != 0)
			throw new CustomException("PRODUCTREQUEST-0037");
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeleteChildWorkOrder", getEventUser(), getEventComment(), "", "");

		MESWorkOrderServiceProxy.getProductRequestServiceImpl().remove(eventInfo, productRequestData);

		return doc;
	}
}
