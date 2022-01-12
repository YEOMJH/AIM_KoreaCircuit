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

public class DeleteWorkOrder extends SyncHandler {

	public Object doWorks(Document doc) throws CustomException
	{
		String productRequestName = SMessageUtil.getBodyItemValue(doc, "PRODUCTREQUESTNAME", true);

		// Get ProductRequest Data
		ProductRequest productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(productRequestName);

		// Validation - ProductRequestState
		if (!StringUtils.equals(productRequestData.getProductRequestState(), GenericServiceProxy.getConstantMap().Prq_Created))
			throw new CustomException("PRODUCTREQUEST-0011", productRequestName, productRequestData.getProductRequestState());

		if (StringUtils.equals(productRequestData.getProductRequestHoldState(), GenericServiceProxy.getConstantMap().Prq_OnHold))
		{
			throw new CustomException("PRODUCTREQUEST-0024", productRequestData.getKey().getProductRequestName(), productRequestData.getProductRequestHoldState());
		}

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeleteWorkOrder", getEventUser(), getEventComment(), "", "");

		MESWorkOrderServiceProxy.getProductRequestServiceImpl().remove(eventInfo, productRequestData);

		return doc;
	}
}
