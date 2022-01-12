package kr.co.aim.messolution.productrequest.event.CNX;

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
import kr.co.aim.greentrack.productrequest.management.info.CreateInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class CreateWorkOrder extends SyncHandler {

	public Object doWorks(Document doc) throws CustomException
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String productRequestName = SMessageUtil.getBodyItemValue(doc, "PRODUCTREQUESTNAME", true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String productSpecVersion = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECVERSION", true);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String processFlowVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWVERSION", true);
		String planQuantity = SMessageUtil.getBodyItemValue(doc, "PLANQUANTITY", true);
		String planReleasedTime = SMessageUtil.getBodyItemValue(doc, "PLANRELEASEDTIME", true);
		String planFinishedTime = SMessageUtil.getBodyItemValue(doc, "PLANFINISHEDTIME", true);
		String autoShippingFlag = SMessageUtil.getBodyItemValue(doc, "AUTOSHIPPINGFLAG", true);
		String subProductionType = SMessageUtil.getBodyItemValue(doc, "SUBPRODUCTIONTYPE", false);
		String description = SMessageUtil.getBodyItemValue(doc, "DESCTIPTION", false);
		String riskFlag = SMessageUtil.getBodyItemValue(doc, "RISKFLAG", false);
		String crateSpecName = SMessageUtil.getBodyItemValue(doc, "CRATESPECNAME", false);

		ProductSpec productSpec = GenericServiceProxy.getSpecUtil().getProductSpec(factoryName, productSpecName, productSpecVersion);

		if ((StringUtils.equals(productSpec.getProductionType(), "E")
				||StringUtils.equals(productSpec.getProductionType(), "P")
				||StringUtils.equals(productSpec.getProductionType(), "T"))
				&& StringUtils.isEmpty(subProductionType))
			throw new CustomException("PRODUCTREQUEST-0033");

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateWorkOrder", getEventUser(), getEventComment(), "", "");
		
		if(StringUtils.equals(productSpec.getProductionType(), "M") || StringUtils.equals(productSpec.getProductionType(), "D"))
		{				
			CreateInfo createInfo = MESWorkOrderServiceProxy.getProductRequestInfoUtil().createInfo(productRequestName, factoryName, TimeUtils.getTimestamp(planFinishedTime),
					Long.parseLong(planQuantity), TimeUtils.getTimestamp(planReleasedTime), "", productSpec.getProductionType(), productSpecName, productSpecVersion, processFlowName, processFlowVersion);

			Map<String, String> udfs = createInfo.getUdfs();
			udfs.put("AUTOSHIPPINGFLAG", autoShippingFlag);
			udfs.put("CREATEDQUANTITY", "0");
			udfs.put("PLANSEQUENCE", "NA");
			udfs.put("SUBPRODUCTIONTYPE", subProductionType);
			udfs.put("DESCRIPTION", description);
			udfs.put("CRATESPECNAME", crateSpecName);
			createInfo.setUdfs(udfs);

			MESWorkOrderServiceProxy.getProductRequestServiceImpl().create(eventInfo, createInfo, productRequestName);			
		}
		else
		{
			SuperProductRequest createInfo = new SuperProductRequest();
			createInfo.setProductRequestName(productRequestName);
			createInfo.setFactoryName(factoryName);
			createInfo.setPlanFinishedTime(TimeUtils.getTimestamp(planFinishedTime));
			createInfo.setPlanQuantity(Long.parseLong(planQuantity));
			createInfo.setPlanReleasedTime(TimeUtils.getTimestamp(planReleasedTime));
			createInfo.setProductRequestType(productSpec.getProductionType());
			createInfo.setProductSpecName(productSpecName);
			createInfo.setProductSpecVersion(productSpecVersion);
			createInfo.setProcessFlowName(processFlowName);
			createInfo.setProcessFlowVersion(processFlowVersion);
			createInfo.setAutoShippingFlag(autoShippingFlag);
			createInfo.setCreatedQuantity(0);
			createInfo.setPlanSequence("NA");
			createInfo.setSubProductionType(subProductionType);
			createInfo.setDescription(description);
			createInfo.setLastEventComment(eventInfo.getEventComment());
			createInfo.setCreateTime(eventInfo.getEventTime());
			createInfo.setCreateUser(eventInfo.getEventUser());
			createInfo.setLastEventFlag("N");
			createInfo.setLastEventName(eventInfo.getEventName());
			createInfo.setLastEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			createInfo.setLastEventUser(eventInfo.getEventUser());
			createInfo.setLastEventTime(eventInfo.getEventTime());
			createInfo.setProductRequestState(GenericServiceProxy.getConstantMap().Prq_Created);
			createInfo.setProductRequestHoldState("N");
			createInfo.setRiskFlag(riskFlag);
			if(!StringUtil.equals(factoryName, "POSTCELL"))
			{
				createInfo.setProductType("SHT");
			}
			else {
				createInfo.setProductType("PCS");
			}
			
			
			ExtendedObjectProxy.getSuperProductRequestService().create(eventInfo, createInfo);
		}
		
		return doc;
	}
}
