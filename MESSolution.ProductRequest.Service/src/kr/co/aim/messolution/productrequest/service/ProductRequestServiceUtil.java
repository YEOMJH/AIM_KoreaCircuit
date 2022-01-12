package kr.co.aim.messolution.productrequest.service;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.management.data.ProductRequestHistory;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greenframe.util.xml.JdomUtils;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.name.NameServiceProxy;
import kr.co.aim.greentrack.productrequest.ProductRequestServiceProxy;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequestKey;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ProductRequestServiceUtil implements ApplicationContextAware {

	private ApplicationContext applicationContext;
	private static Log log = LogFactory.getLog("ProductRequestServiceUtil");

	public void setApplicationContext(ApplicationContext arg0) throws BeansException
	{
		// TODO Auto-generated method stub
		applicationContext = arg0;
	}

	public Element generateWOName(String poName, String quantity)
	{
		log.info("Generate wo Name");

		if (log.isInfoEnabled())
		{
			log.info("poName = " + poName);
			log.info("quantity = " + quantity);
		}

		int count = Integer.parseInt(quantity);

		List<String> argSeq = new ArrayList<String>();
		argSeq.add(poName);

		List<String> woNameList = null;

		try
		{
			woNameList = NameServiceProxy.getNameGeneratorRuleDefService().generateName("WONAME", argSeq, Long.valueOf(quantity));
		}
		catch (FrameworkErrorSignal e)
		{
			log.error(e);
		}
		catch (NotFoundSignal e)
		{
		}

		log.info("WONameList Length : " + woNameList.size());
		Element list = new Element("WOLIST");

		for (int i = 0; i < count; i++)
		{
			JdomUtils.addElement(list, "PRODUCTREQUESTNAME", woNameList.get(i));
			log.info(list.getAttributeValue("PRODUCTREQUESTNAME"));
		}

		return list;
	}

	public ProductRequest getProductRequestData(String productRequestName) throws CustomException
	{
		if (StringUtils.isEmpty(productRequestName))
		{
			throw new CustomException("PRODUCTREQUEST-9001", productRequestName);
		}

		ProductRequest workOrderData = new ProductRequest();
		try
		{
			workOrderData = ProductRequestServiceProxy.getProductRequestService().selectByKey(new ProductRequestKey(productRequestName));
		}
		catch (Exception e)
		{
			throw new CustomException("PRODUCTREQUEST-9001", productRequestName);
		}

		return workOrderData;
	}

	public Element generatePOName(String factoryName, String productSpecName, String quantity)
	{
		log.info("Generate po Name");

		if (log.isInfoEnabled())
		{
			log.info("factoryName = " + factoryName);
			log.info("productSpecName = " + productSpecName);
			log.info("quantity = " + quantity);
		}

		int count = Integer.parseInt(quantity);

		List<String> argSeq = new ArrayList<String>();
		argSeq.add(factoryName);
		argSeq.add(productSpecName);

		List<String> poNameList = new ArrayList<String>();

		try
		{
			poNameList = NameServiceProxy.getNameGeneratorRuleDefService().generateName("PONAME", argSeq, Long.valueOf(quantity));
		}
		catch (FrameworkErrorSignal e)
		{
			log.error(e);
		}
		catch (NotFoundSignal e)
		{
		}

		log.info("PONameList Length : " + poNameList.size());

		Element list = new Element("POLIST");

		for (int i = 0; i < count; i++)
		{
			JdomUtils.addElement(list, "PRODUCTIONORDERNAME", poNameList.get(i));
			log.info(list.getAttributeValue("PRODUCTIONORDERNAME"));
		}
		return list;
	}

	public void addHistory(ProductRequest dataInfo) throws CustomException
	{
		ProductRequestHistory historyData = new ProductRequestHistory(dataInfo.getKey().getProductRequestName(), TimeUtils.getCurrentEventTimeKey());

		historyData.setProductRequestType(dataInfo.getProductRequestType());
		historyData.setFactoryName(dataInfo.getFactoryName());
		historyData.setProductSpecName(dataInfo.getProductSpecName());
		historyData.setProductSpecVersion(dataInfo.getProductSpecVersion());
		historyData.setPlanReleasedTime(dataInfo.getPlanReleasedTime());
		historyData.setPlanFinishedTime(dataInfo.getPlanFinishedTime());
		historyData.setPlanQuantity(dataInfo.getPlanQuantity());

		String createQuantity = CommonUtil.getValue(dataInfo.getUdfs(), "CREATEDQUANTITY");
		if (StringUtils.isNotEmpty(createQuantity))
			historyData.setCreatedQuantity(Long.parseLong(createQuantity));

		historyData.setReleasedQuantity(dataInfo.getReleasedQuantity());
		historyData.setFinishedQuantity(dataInfo.getFinishedQuantity());
		historyData.setScrappedQuantity(dataInfo.getScrappedQuantity());
		historyData.setProductRequestState(dataInfo.getProductRequestState());
		historyData.setProductRequestHoldState(dataInfo.getProductRequestHoldState());
		historyData.setEventName(dataInfo.getLastEventName());
		historyData.setEventTime(dataInfo.getLastEventTime());
		historyData.setEventUser(dataInfo.getLastEventUser());
		historyData.setEventComment(dataInfo.getLastEventComment());
		historyData.setEventFlag(dataInfo.getLastEventFlag());
		historyData.setCreateTime(dataInfo.getCreateTime());
		historyData.setCreateUser(dataInfo.getCreateUser());
		historyData.setReleaseTime(dataInfo.getReleaseTime());
		historyData.setReleaseUser(dataInfo.getReleaseUser());
		historyData.setCompleteTime(dataInfo.getCompleteTime());
		historyData.setCompleteUser(dataInfo.getCompleteUser());

		String planSequence = CommonUtil.getValue(dataInfo.getUdfs(), "PLANSEQUENCE");
		historyData.setPlanSequence(planSequence);

		String processFlowName = CommonUtil.getValue(dataInfo.getUdfs(), "PROCESSFLOWNAME");
		historyData.setProcessFlowName(processFlowName);

		String processFlowVersion = CommonUtil.getValue(dataInfo.getUdfs(), "PROCESSFLOWVERSION");
		historyData.setProcessFlowVersion(processFlowVersion);

		String autoShippingFlag = CommonUtil.getValue(dataInfo.getUdfs(), "AUTOSHIPPINGFLAG");
		historyData.setAutoShippingFlag(autoShippingFlag);

		String description = CommonUtil.getValue(dataInfo.getUdfs(), "DESCRIPTION");
		historyData.setDescription(description);

		String subProductionType = CommonUtil.getValue(dataInfo.getUdfs(), "SUBPRODUCTIONTYPE");
		historyData.setSubProductionType(subProductionType);
		
		String superProductRequestName = CommonUtil.getValue(dataInfo.getUdfs(), "SUPERPRODUCTREQUESTNAME");
		historyData.setSuperProductRequestName(superProductRequestName);

		String productType = CommonUtil.getValue(dataInfo.getUdfs(), "PRODUCTTYPE");
		historyData.setProductType(productType);
		
		String projectProductRequestName = CommonUtil.getValue(dataInfo.getUdfs(), "PROJECTPRODUCTREQUESTNAME");
		historyData.setProjectProductRequestName(projectProductRequestName);
		
		String costDepartment = CommonUtil.getValue(dataInfo.getUdfs(), "COSTDEPARTMENT");
		historyData.setCostDepartment(costDepartment);
		
		String riskFlag = CommonUtil.getValue(dataInfo.getUdfs(), "RISKFLAG");
		historyData.setRiskFlag(riskFlag);
		
		String crateSpecName = CommonUtil.getValue(dataInfo.getUdfs(), "CRATESPECNAME");
		historyData.setCrateSpecName(crateSpecName);
		
		String prOwner = CommonUtil.getValue(dataInfo.getUdfs(), "PROWNER");
		historyData.setPrOwner(prOwner);;
		
		MESWorkOrderServiceProxy.getProductRequestServiceImpl().addHistory(historyData);
	}

	public void addHistory(List<ProductRequest> dataInfoList) throws CustomException
	{
		List<ProductRequestHistory> historyDataList = new ArrayList<ProductRequestHistory>();

		for (ProductRequest dataInfo : dataInfoList)
		{
			ProductRequestHistory historyData = new ProductRequestHistory(dataInfo.getKey().getProductRequestName(), TimeUtils.getCurrentEventTimeKey());

			historyData.setProductRequestType(dataInfo.getProductRequestType());
			historyData.setFactoryName(dataInfo.getFactoryName());
			historyData.setProductSpecName(dataInfo.getProductSpecName());
			historyData.setProductSpecVersion(dataInfo.getProductSpecVersion());
			historyData.setPlanReleasedTime(dataInfo.getPlanReleasedTime());
			historyData.setPlanFinishedTime(dataInfo.getPlanFinishedTime());
			historyData.setPlanQuantity(dataInfo.getPlanQuantity());
			historyData.setReleasedQuantity(dataInfo.getReleasedQuantity());
			historyData.setFinishedQuantity(dataInfo.getFinishedQuantity());
			historyData.setScrappedQuantity(dataInfo.getScrappedQuantity());
			historyData.setProductRequestState(dataInfo.getProductRequestState());
			historyData.setProductRequestHoldState(dataInfo.getProductRequestHoldState());
			historyData.setEventName(dataInfo.getLastEventName());
			historyData.setEventTime(dataInfo.getLastEventTime());
			historyData.setEventUser(dataInfo.getLastEventUser());
			historyData.setEventComment(dataInfo.getLastEventComment());
			historyData.setEventFlag(dataInfo.getLastEventFlag());
			historyData.setCreateTime(dataInfo.getCreateTime());
			historyData.setCreateUser(dataInfo.getCreateUser());
			historyData.setReleaseTime(dataInfo.getReleaseTime());
			historyData.setReleaseUser(dataInfo.getReleaseUser());
			historyData.setCompleteTime(dataInfo.getCompleteTime());
			historyData.setCompleteUser(dataInfo.getCompleteUser());

			String processFlowName = CommonUtil.getValue(dataInfo.getUdfs(), "PROCESSFLOWNAME");
			historyData.setProcessFlowName(processFlowName);

			String processFlowVersion = CommonUtil.getValue(dataInfo.getUdfs(), "PROCESSFLOWVERSION");
			historyData.setProcessFlowVersion(processFlowVersion);

			String autoShippingFlag = CommonUtil.getValue(dataInfo.getUdfs(), "AUTOSHIPPINGFLAG");
			historyData.setAutoShippingFlag(autoShippingFlag);

			historyDataList.add(historyData);
		}

		// insert in CT_PRODUCTREQUESTHISTORY table
		MESWorkOrderServiceProxy.getProductRequestServiceImpl().addHistory(historyDataList);
	}

}
