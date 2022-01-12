package kr.co.aim.messolution.productrequest.event.CNX;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.SuperProductRequest;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.productrequest.ProductRequestServiceProxy;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.info.CreateInfo;
import kr.co.aim.greentrack.productrequest.management.info.MakeReleasedInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class SplitWO extends SyncHandler {

	public Object doWorks(Document doc) throws CustomException
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String seq = SMessageUtil.getBodyItemValue(doc, "SEQ", true);
		String superProductRequestName = SMessageUtil.getBodyItemValue(doc, "SUPERPRODUCTREQUESTNAME", true);
		String planQty = SMessageUtil.getBodyItemValue(doc, "PLANQTY", true);
		String riskFlag = SMessageUtil.getBodyItemValue(doc, "RISKFLAG", true);
		String prOwner = SMessageUtil.getBodyItemValue(doc, "PROWNER", false);

		SuperProductRequest superProductRequest = ExtendedObjectProxy.getSuperProductRequestService().selectByKey(false, new Object[]{ superProductRequestName });

		checkDataWithSuperProductRequest(superProductRequest, planQty);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("SplitWorkOrder", getEventUser(), getEventComment(), "", "");
		
		ProductSpec productSpec = GenericServiceProxy.getSpecUtil().getProductSpec(superProductRequest.getFactoryName(), superProductRequest.getProductSpecName(), superProductRequest.getProductSpecVersion());
		
		String factoryCode="";
		if(StringUtil.equals(productSpec.getUdfs().get("PRODUCTSPECTYPE"), "L"))
		{
			factoryCode="A";
		}
		else if(StringUtil.equals(productSpec.getUdfs().get("PRODUCTSPECTYPE"), "C")||StringUtil.equals(productSpec.getUdfs().get("PRODUCTSPECTYPE"), "F"))
		{
			factoryCode="F";
		}
		else if(StringUtil.equals(productSpec.getUdfs().get("PRODUCTSPECTYPE"), "B")||StringUtil.equals(productSpec.getUdfs().get("PRODUCTSPECTYPE"), "P")
				||StringUtil.equals(productSpec.getUdfs().get("PRODUCTSPECTYPE"), "T"))
		{
			factoryCode=productSpec.getUdfs().get("PRODUCTSPECTYPE");
		}
		
		
		String productRequestName = superProductRequestName +factoryCode+ seq;
		
		CreateInfo createInfo = MESWorkOrderServiceProxy.getProductRequestInfoUtil().createInfo(productRequestName, factoryName, superProductRequest.getPlanFinishedTime(),
				Long.parseLong(planQty), superProductRequest.getPlanReleasedTime(), "", superProductRequest.getProductRequestType(), superProductRequest.getProductSpecName(), 
				superProductRequest.getProductSpecVersion(), superProductRequest.getProcessFlowName(), superProductRequest.getProcessFlowVersion());

			
		Map<String, String> udfs = createInfo.getUdfs();
		udfs.put("AUTOSHIPPINGFLAG", superProductRequest.getAutoShippingFlag());
		udfs.put("CREATEDQUANTITY", "0");
		udfs.put("PLANSEQUENCE", "NA");
		udfs.put("SUBPRODUCTIONTYPE", superProductRequest.getSubProductionType());
		udfs.put("DESCRIPTION", superProductRequest.getDescription());
		udfs.put("SUPERPRODUCTREQUESTNAME", superProductRequestName);
		udfs.put("RISKFLAG", riskFlag);
		udfs.put("PROWNER", prOwner);
		udfs.put("PRODUCTTYPE", superProductRequest.getProductType());
		udfs.put("PROJECTPRODUCTREQUESTNAME", superProductRequest.getProjectProductRequestName());
		udfs.put("COSTDEPARTMENT", superProductRequest.getCostDepartment());
		
		createInfo.setUdfs(udfs);

		MESWorkOrderServiceProxy.getProductRequestServiceImpl().create(eventInfo, createInfo, productRequestName);
		//IF Split SuperWO, ChildWO State 'Released' Update 
		ProductRequest productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(productRequestName);
		productRequestData.setProductRequestHoldState("N");
		MakeReleasedInfo makeReleasedInfo = MESWorkOrderServiceProxy.getProductRequestInfoUtil().makeReleasedInfo(productRequestData);
		MESWorkOrderServiceProxy.getProductRequestServiceImpl().makeReleased(productRequestData, makeReleasedInfo, eventInfo);
		
		return doc;
	}
	
	private void checkDataWithSuperProductRequest(SuperProductRequest superProductRequest, String sPlanQuantity) throws CustomException
	{
		long planQuantity = Long.parseLong(sPlanQuantity);

		List<ProductRequest> productRequestList = null;
		
		try 
		{
			productRequestList = ProductRequestServiceProxy.getProductRequestService().select(" superProductRequestName = ? ", new Object[] { superProductRequest.getProductRequestName()});
		} 
		catch (Exception e) 
		{}

		long sumPlanQuantity = planQuantity;
		
		if(productRequestList != null)
		{
			for(ProductRequest productRequestInfo : productRequestList)
			{
				sumPlanQuantity += productRequestInfo.getPlanQuantity();
			}
		}

		if(superProductRequest.getPlanQuantity() < sumPlanQuantity)
			throw new CustomException("WORKORDER-0005", superProductRequest.getPlanQuantity(), sumPlanQuantity);
	}
}
