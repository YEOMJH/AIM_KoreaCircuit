package kr.co.aim.messolution.productrequest.event.CNX;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.SuperProductRequest;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.productrequest.ProductRequestServiceProxy;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.info.ChangeSpecInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class ModifyChildWOPlanQuantity extends SyncHandler {

	public Object doWorks(Document doc) throws CustomException
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String productRequestName = SMessageUtil.getBodyItemValue(doc, "PRODUCTREQUESTNAME", true);
		String planQuantity = SMessageUtil.getBodyItemValue(doc, "PLANQUANTITY", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ModifyChildWorkOrder", getEventUser(), getEventComment(), "", "");
		
		ProductRequest productRequest = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(productRequestName);
		SuperProductRequest superProductRequest = ExtendedObjectProxy.getSuperProductRequestService().selectByKey(false, new Object[]{ productRequest.getUdfs().get("SUPERPRODUCTREQUESTNAME").toString()});
		
		checkDataWithSuperProductRequest(productRequest, superProductRequest, planQuantity);
		
		ChangeSpecInfo changeSpecInfo = MESWorkOrderServiceProxy.getProductRequestInfoUtil().changeSpecInfo(factoryName, productRequest.getProductRequestType(), productRequest.getProductSpecName(), productRequest.getProductSpecVersion(),
				productRequest.getPlanFinishedTime(), productRequest.getPlanReleasedTime(), Long.parseLong(planQuantity), productRequest.getReleasedQuantity(),
				productRequest.getFinishedQuantity(), productRequest.getScrappedQuantity(), productRequest.getProductRequestState(), productRequest.getProductRequestHoldState(), productRequest.getUdfs().get("PROCESSFLOWNAME").toString(),
				productRequest.getUdfs().get("PROCESSFLOWVERSION").toString(), productRequest.getUdfs().get("AUTOSHIPPINGFLAG").toString(), productRequest.getUdfs().get("PLANSEQUENCE"), productRequest.getUdfs().get("CREATEDQUANTITY"), productRequest.getUdfs().get("DESCRIPTION"));

		MESWorkOrderServiceProxy.getProductRequestServiceImpl().changeSpec(productRequest, changeSpecInfo, eventInfo);
		
		return doc;
	}
	
	private void checkDataWithSuperProductRequest(ProductRequest productRequest, SuperProductRequest superProductRequest, String sPlanQuantity) throws CustomException
	{
		long planQuantity = Long.parseLong(sPlanQuantity);
		
		List<ProductRequest> productRequestList = ProductRequestServiceProxy.getProductRequestService().select(" superProductRequestName = ? and productRequestName != ? ", new Object[] { superProductRequest.getProductRequestName(), productRequest.getKey().getProductRequestName()});
		
		long sumPlanQuantity = planQuantity;
		
		for(ProductRequest productRequestInfo : productRequestList)
		{
			sumPlanQuantity += productRequestInfo.getPlanQuantity();
		}
		
		if(superProductRequest.getPlanQuantity() < sumPlanQuantity)
			throw new CustomException("WORKORDER-0005", superProductRequest.getPlanQuantity(), sumPlanQuantity);
	}
}
