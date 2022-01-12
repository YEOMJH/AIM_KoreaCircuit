package kr.co.aim.messolution.productrequest.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.SuperProductRequest;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.info.CreateInfo;
import kr.co.aim.greentrack.productrequest.management.info.MakeReleasedInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.w3c.dom.css.ElementCSSInlineStyle;

public class ReleaseWorkOrder extends SyncHandler {

	public Object doWorks(Document doc) throws CustomException
	{
		String productRequestName = SMessageUtil.getBodyItemValue(doc, "PRODUCTREQUESTNAME", true);
		String productRequestType = SMessageUtil.getBodyItemValue(doc, "PRODUCTREQUESTTYPE", true);
		String prOwner = SMessageUtil.getBodyItemValue(doc, "OWNER", false);
        String processFlowName=SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
        String processFlowNameVision=SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWVERSION", true);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ReleaseWorkOrder", getEventUser(), getEventComment(), "", "");
		
		
		//if productionType in ('M','D'),release productRequest
		if(StringUtils.equals(productRequestType, "M") || StringUtils.equals(productRequestType, "D") )
		{				
			ProductRequest productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(productRequestName);

			if (!StringUtils.equals(productRequestData.getProductRequestState(), GenericServiceProxy.getConstantMap().Prq_Created))
					throw new CustomException("PRODUCTREQUEST-0011", productRequestName, productRequestData.getProductRequestState());

			// Mantis - 0000037
			CommonValidation.checkProductRequestHoldState(productRequestData, "Y");
				
			productRequestData.setReleasedQuantity(1);

			MakeReleasedInfo makeReleasedInfo = MESWorkOrderServiceProxy.getProductRequestInfoUtil().makeReleasedInfo(productRequestData);

			MESWorkOrderServiceProxy.getProductRequestServiceImpl().makeReleased(productRequestData, makeReleasedInfo, eventInfo);
			
		}
		else
		{			
			//if productionType in ('T','P','E'),Release SuperWorkOrder
			SuperProductRequest productRequestData = ExtendedObjectProxy.getSuperProductRequestService().selectByKey(false, new Object[]{ productRequestName });
			
			if (!StringUtils.equals(productRequestData.getProductRequestState(), GenericServiceProxy.getConstantMap().Prq_Created))
				throw new CustomException("PRODUCTREQUEST-0011", productRequestName, productRequestData.getProductRequestState());
			
			if (StringUtils.equals(productRequestData.getProductRequestHoldState(), "Y"))
				throw new CustomException("WORKORDER-0004", productRequestData.getProductRequestName(), productRequestData.getProductRequestHoldState());
			
			productRequestData.setProductRequestState(GenericServiceProxy.getConstantMap().Prq_Released);
			productRequestData.setProcessFlowName(processFlowName);
			productRequestData.setProcessFlowVersion(processFlowNameVision);
			productRequestData.setLastEventName(eventInfo.getEventName());
			productRequestData.setLastEventComment(eventInfo.getEventComment());
			productRequestData.setLastEventFlag("N");
			productRequestData.setLastEventTime(eventInfo.getEventTime());
			productRequestData.setLastEventTimeKey(eventInfo.getEventTimeKey());
			productRequestData.setLastEventUser(eventInfo.getEventUser());
			productRequestData.setReleaseTime(eventInfo.getEventTime());
			productRequestData.setReleaseUser(eventInfo.getEventUser());
			
			ExtendedObjectProxy.getSuperProductRequestService().modify(eventInfo, productRequestData);
			
			//Auto Create and Release productRequest
			ProductSpec productSpec = GenericServiceProxy.getSpecUtil().getProductSpec(productRequestData.getFactoryName(), productRequestData.getProductSpecName(), productRequestData.getProductSpecVersion());
			
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
			String mesProductRequest = productRequestName+factoryCode+"01";
			CreateInfo createInfo = MESWorkOrderServiceProxy.getProductRequestInfoUtil().createInfo(mesProductRequest, productRequestData.getFactoryName(), productRequestData.getPlanFinishedTime(),
					productRequestData.getPlanQuantity(), productRequestData.getPlanReleasedTime(), "", productRequestData.getProductRequestType(), productRequestData.getProductSpecName(), 
					productRequestData.getProductSpecVersion(), productRequestData.getProcessFlowName(), productRequestData.getProcessFlowVersion());
			
			Map<String, String> udfs = createInfo.getUdfs();
			udfs.put("AUTOSHIPPINGFLAG", productRequestData.getAutoShippingFlag());
			udfs.put("CREATEDQUANTITY", "0");
			udfs.put("PLANSEQUENCE", "NA");
			udfs.put("SUBPRODUCTIONTYPE", productRequestData.getSubProductionType());
			udfs.put("DESCRIPTION", productRequestData.getDescription());
			udfs.put("SUPERPRODUCTREQUESTNAME", productRequestName);
			udfs.put("RISKFLAG", productRequestData.getRiskFlag());
			udfs.put("PRODUCTTYPE", productRequestData.getProductType());
			udfs.put("PROJECTPRODUCTREQUESTNAME", productRequestData.getProjectProductRequestName());
			udfs.put("COSTDEPARTMENT", productRequestData.getCostDepartment());
			udfs.put("PROWNER", prOwner);
			
			createInfo.setUdfs(udfs);

			MESWorkOrderServiceProxy.getProductRequestServiceImpl().create(eventInfo, createInfo, mesProductRequest);
			//IF Split SuperWO, ChildWO State 'Released' Update 
			ProductRequest mesProductRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(mesProductRequest);
			productRequestData.setProductRequestHoldState("N");
			MakeReleasedInfo makeReleasedInfo = MESWorkOrderServiceProxy.getProductRequestInfoUtil().makeReleasedInfo(mesProductRequestData);
			MESWorkOrderServiceProxy.getProductRequestServiceImpl().makeReleased(mesProductRequestData, makeReleasedInfo, eventInfo);			
		}


		return doc;
	}
}
