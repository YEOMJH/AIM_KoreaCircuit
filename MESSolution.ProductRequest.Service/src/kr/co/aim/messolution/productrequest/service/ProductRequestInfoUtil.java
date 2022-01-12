package kr.co.aim.messolution.productrequest.service;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.productrequest.management.info.CreateInfo;
import kr.co.aim.greentrack.productrequest.management.info.DecrementScrappedQuantityByInfo;
import kr.co.aim.greentrack.productrequest.management.info.IncrementFinishedQuantityByInfo;
import kr.co.aim.greentrack.productrequest.management.info.IncrementReleasedQuantityByInfo;
import kr.co.aim.greentrack.productrequest.management.info.IncrementScrappedQuantityByInfo;
import kr.co.aim.greentrack.productrequest.management.info.MakeCompletedInfo;
import kr.co.aim.greentrack.productrequest.management.info.MakeNotOnHoldInfo;
import kr.co.aim.greentrack.productrequest.management.info.MakeOnHoldInfo;
import kr.co.aim.greentrack.productrequest.management.info.MakeReleasedInfo;

public class ProductRequestInfoUtil implements ApplicationContextAware {

	private ApplicationContext applicationContext;
	private static Log log = LogFactory.getLog("ProductRequestServiceImpl");

	public void setApplicationContext(ApplicationContext arg0) throws BeansException
	{
		applicationContext = arg0;
	}

	public CreateInfo createInfo(String productRequestName, String factoryName, Timestamp planFinishedTime, long planQuantity, Timestamp planReleasedTime, String orlPlanReleasedTime,
			String productRequestType, String productSpecName, String productSpecVersion, String processFlowName, String processFlowVersion)
	{

		CreateInfo createInfo = new CreateInfo();
		createInfo.setFactoryName(factoryName);
		createInfo.setProductRequestName(productRequestName);
		createInfo.setPlanFinishedTime(planFinishedTime);
		createInfo.setPlanQuantity(planQuantity);
		createInfo.setPlanReleasedTime(planReleasedTime);
		createInfo.setProductRequestType(productRequestType);
		createInfo.setProductSpecName(productSpecName);
		createInfo.setProductSpecVersion(productSpecVersion);

		Map<String, String> productRequestUdfs = new HashMap<String, String>();
		productRequestUdfs.put("PROCESSFLOWNAME", processFlowName);
		productRequestUdfs.put("PROCESSFLOWVERSION", processFlowVersion);

		createInfo.setUdfs(productRequestUdfs);

		return createInfo;
	}

	public DecrementScrappedQuantityByInfo decrementScrappedQuantityByInfo(ProductRequest productRequestData, long quantity)
	{
		DecrementScrappedQuantityByInfo decrementScrappedQuantityByInfo = new DecrementScrappedQuantityByInfo();
		decrementScrappedQuantityByInfo.setQuantity(quantity);

		return decrementScrappedQuantityByInfo;
	}

	public IncrementFinishedQuantityByInfo incrementFinishedQuantityByInfo(ProductRequest productRequestData, long quantity)
	{
		IncrementFinishedQuantityByInfo incrementFinishedQuantityByInfo = new IncrementFinishedQuantityByInfo();
		incrementFinishedQuantityByInfo.setQuantity(quantity);

		return incrementFinishedQuantityByInfo;
	}

	public IncrementReleasedQuantityByInfo incrementReleasedQuantityByInfo(ProductRequest productRequestData, long quantity)
	{
		IncrementReleasedQuantityByInfo incrementReleasedQuantityByInfo = new IncrementReleasedQuantityByInfo();
		incrementReleasedQuantityByInfo.setQuantity(quantity);

		return incrementReleasedQuantityByInfo;
	}

	public IncrementScrappedQuantityByInfo incrementScrappedQuantityByInfo(ProductRequest productRequestData, long quantity)
	{
		IncrementScrappedQuantityByInfo incrementScrappedQuantityByInfo = new IncrementScrappedQuantityByInfo();
		incrementScrappedQuantityByInfo.setQuantity(quantity);

		return incrementScrappedQuantityByInfo;
	}

	public MakeCompletedInfo makeCompletedInfo(ProductRequest productRequestData)
	{
		MakeCompletedInfo makeCompletedInfo = new MakeCompletedInfo();

		return makeCompletedInfo;
	}

	public MakeNotOnHoldInfo makeNotOnHoldInfo(ProductRequest productRequestData)
	{
		MakeNotOnHoldInfo makeNotOnHoldInfo = new MakeNotOnHoldInfo();

		return makeNotOnHoldInfo;
	}

	public MakeOnHoldInfo makeOnHoldInfo(ProductRequest productRequestData)
	{
		MakeOnHoldInfo makeOnHoldInfo = new MakeOnHoldInfo();

		return makeOnHoldInfo;
	}

	public MakeReleasedInfo makeReleasedInfo(ProductRequest productRequestData)
	{
		MakeReleasedInfo makeReleasedInfo = new MakeReleasedInfo();

		return makeReleasedInfo;
	}

	public ChangeSpecInfo changeSpecInfo(String factoryName, String productRequestType, String productSpecName, String productSpecVersion, Timestamp planFinishedTime, Timestamp planReleasedTime,
			long planQuantity, long releasedQuantity, long finishedQuantity, long scrappedQuantity, String productRequestState, String productRequestHoldState, String processFlowName,
			String processFlowVersion, String autoShippingFlag, String planSequence, String createdQuantity, String description)
	{

		ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo();

		changeSpecInfo.setFactoryName(factoryName);
		changeSpecInfo.setProductRequestType(productRequestType);
		changeSpecInfo.setProductSpecName(productSpecName);
		changeSpecInfo.setProductSpecVersion(productSpecVersion);
		changeSpecInfo.setPlanReleasedTime(planReleasedTime);
		changeSpecInfo.setPlanFinishedTime(planFinishedTime);
		changeSpecInfo.setPlanQuantity(planQuantity);
		changeSpecInfo.setReleasedQuantity(releasedQuantity);
		changeSpecInfo.setFinishedQuantity(finishedQuantity);
		changeSpecInfo.setScrappedQuantity(scrappedQuantity);
		changeSpecInfo.setProductRequestState(productRequestState);
		changeSpecInfo.setProductRequestHoldState(productRequestHoldState);

		Map<String, String> productRequestUdfs = new HashMap<String, String>();
		productRequestUdfs.put("PROCESSFLOWNAME", processFlowName);
		productRequestUdfs.put("PROCESSFLOWVERSION", processFlowVersion);
		productRequestUdfs.put("AUTOSHIPPINGFLAG", autoShippingFlag);
		productRequestUdfs.put("PLANSEQUENCE", planSequence);
		productRequestUdfs.put("CREATEDQUANTITY", createdQuantity);
		productRequestUdfs.put("DESCRIPTION", description);

		changeSpecInfo.setUdfs(productRequestUdfs);

		return changeSpecInfo;
	}

	public ChangeSpecInfo changeSpecInfo(String factoryName, String productRequestType, String productSpecName, String productSpecVersion, Timestamp planFinishedTime, Timestamp planReleasedTime,
			long planQuantity, long releasedQuantity, long finishedQuantity, long scrappedQuantity, String productRequestState, String productRequestHoldState, String processFlowName,
			String processFlowVersion)
	{

		ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo();

		changeSpecInfo.setFactoryName(factoryName);
		changeSpecInfo.setProductRequestType(productRequestType);
		changeSpecInfo.setProductSpecName(productSpecName);
		changeSpecInfo.setProductSpecVersion(productSpecVersion);
		changeSpecInfo.setPlanReleasedTime(planReleasedTime);
		changeSpecInfo.setPlanFinishedTime(planFinishedTime);
		changeSpecInfo.setPlanQuantity(planQuantity);
		changeSpecInfo.setReleasedQuantity(releasedQuantity);
		changeSpecInfo.setFinishedQuantity(finishedQuantity);
		changeSpecInfo.setScrappedQuantity(scrappedQuantity);
		changeSpecInfo.setProductRequestState(productRequestState);
		changeSpecInfo.setProductRequestHoldState(productRequestHoldState);

		Map<String, String> productRequestUdfs = new HashMap<String, String>();
		productRequestUdfs.put("PROCESSFLOWNAME", processFlowName);
		productRequestUdfs.put("PROCESSFLOWVERSION", processFlowVersion);

		changeSpecInfo.setUdfs(productRequestUdfs);

		return changeSpecInfo;
	}

}
