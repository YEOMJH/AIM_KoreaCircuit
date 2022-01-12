package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.SampleLot;
import kr.co.aim.messolution.extended.object.management.data.SampleProduct;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.ProductKey;

public class SampleProductService extends CTORMService<SampleProduct> {

	private static Log log = LogFactory.getLog(SampleProductService.class);
	public static Log logger = LogFactory.getLog(SampleProductService.class);

	private final String historyEntity = "SampleProductHist";

	public List<SampleProduct> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<SampleProduct> result = super.select(condition, bindSet, SampleProduct.class);

		return result;
	}

	public SampleProduct selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(SampleProduct.class, isLock, keySet);
	}

	public SampleProduct create(EventInfo eventInfo, SampleProduct dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, SampleProduct dataInfo) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public SampleProduct modify(EventInfo eventInfo, SampleProduct dataInfo)
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public SampleProduct getSampleProductData(String productName, String lotName, String factoryName, String productSpecName, String productSpecVersion, String processFlowName,
			String processFlowVersion, String processOperationName, String processOperationVersion, String machineName, String toProcessFlowName, String toProcessFlowVersion,
			String toProcessOperationName, String toProcessOperationVersion) throws greenFrameDBErrorSignal, CustomException
	{
		log.info("GetSampleProduct - ProductName : " + productName + ", lotName : " + lotName + ", FromInfo : " + processFlowName + "/" + processOperationName + ", ToInfo : " + toProcessFlowName
				+ "/" + toProcessOperationName);

		Object[] keySet = new Object[] { productName, lotName, factoryName, productSpecName, productSpecVersion, processFlowName, processFlowVersion, processOperationName, processOperationVersion,
				machineName, toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion };

		SampleProduct sampleProduct = new SampleProduct();

		try
		{
			sampleProduct = ExtendedObjectProxy.getSampleProductService().selectByKey(false, keySet);
		}
		catch (Exception e)
		{
			sampleProduct = null;
		}

		log.info("end GetSampleProduct");

		return sampleProduct;
	}

	public List<SampleProduct> getManualSampleProductData(String productName, String lotName, String factoryName, String productSpecName, String productSpecVersion, String processFlowName,
			String processFlowVersion, String processOperationName, String processOperationVersion, String machineName, String toProcessFlowName, String toProcessFlowVersion,
			String toProcessOperationName, String toProcessOperationVersion, String manualSampleFlag) throws greenFrameDBErrorSignal, CustomException
	{
		String condition = "PRODUCTNAME = ? AND LOTNAME = ? AND FACTORYNAME = ? AND PRODUCTSPECNAME = ? AND PRODUCTSPECVERSION = ? AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ? AND PROCESSOPERATIONNAME = ? AND PROCESSOPERATIONVERSION = ? AND MACHINENAME = ? AND TOPROCESSFLOWNAME = ? AND TOPROCESSFLOWVERSION = ? AND TOPROCESSOPERATIONNAME = ? AND TOPROCESSOPERATIONVERSION = ? AND MANUALSAMPLEFLAG = ?";
		Object[] bindSet = new Object[] { productName, lotName, factoryName, productSpecName, productSpecVersion, processFlowName, processFlowVersion, processOperationName, processOperationVersion,
				machineName, toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion, manualSampleFlag };

		List<SampleProduct> sampleProductDataList = new ArrayList<SampleProduct>();
		try
		{
			sampleProductDataList = ExtendedObjectProxy.getSampleProductService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			sampleProductDataList = null;
		}

		log.info("End GetSampleProduct");

		return sampleProductDataList;
	}

	public List<SampleProduct> getSampleProductDataList(String productName, String lotName, String factoryName, String productSpecName, String productSpecVersion, String processFlowName,
			String processFlowVersion, String processOperationName, String processOperationVersion, String machineName, String toProcessFlowName, String toProcessFlowVersion,
			String toProcessOperationName, String toProcessOperationVersion) throws greenFrameDBErrorSignal, CustomException
	{
		log.info("GetSampleProduct - ProductName : " + productName + ", lotName : " + lotName + ", FromInfo : " + processFlowName + "/" + processOperationName + ", ToInfo : " + toProcessFlowName
				+ "/" + toProcessOperationName);

		String condition = "PRODUCTNAME = ? AND LOTNAME = ? AND FACTORYNAME = ? AND PRODUCTSPECNAME = ? AND PRODUCTSPECVERSION = ? AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ? AND PROCESSOPERATIONNAME = ? AND PROCESSOPERATIONVERSION = ? AND MACHINENAME = ? AND TOPROCESSFLOWNAME = ? AND TOPROCESSFLOWVERSION = ? AND TOPROCESSOPERATIONNAME = ? AND TOPROCESSOPERATIONVERSION = ?";
		Object[] bindSet = new Object[] { productName, lotName, factoryName, productSpecName, productSpecVersion, processFlowName, processFlowVersion, processOperationName, processOperationVersion,
				machineName, toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion };

		List<SampleProduct> sampleProductDataList = new ArrayList<SampleProduct>();
		try
		{
			sampleProductDataList = ExtendedObjectProxy.getSampleProductService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			sampleProductDataList = null;
		}

		log.info("End GetSampleProduct");

		return sampleProductDataList;
	}

	public List<SampleProduct> getSampleProductDataListWithoutMachineName(String productName, String lotName, String factoryName, String productSpecName, String productSpecVersion,
			String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion, String toProcessFlowName, String toProcessFlowVersion,
			String toProcessOperationName, String toProcessOperationVersion) throws greenFrameDBErrorSignal, CustomException
	{
		log.info("GetSampleProduct - ProductName : " + productName + ", lotName : " + lotName + ", FromInfo : " + processFlowName + "/" + processOperationName + ", ToInfo : " + toProcessFlowName
				+ "/" + toProcessOperationName);

		String condition = "PRODUCTNAME = ? AND LOTNAME = ? AND FACTORYNAME = ? AND PRODUCTSPECNAME = ? AND PRODUCTSPECVERSION = ? AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ? AND PROCESSOPERATIONNAME = ? AND PROCESSOPERATIONVERSION = ? AND TOPROCESSFLOWNAME = ? AND TOPROCESSFLOWVERSION = ? AND TOPROCESSOPERATIONNAME = ? AND TOPROCESSOPERATIONVERSION = ?";
		Object[] bindSet = new Object[] { productName, lotName, factoryName, productSpecName, productSpecVersion, processFlowName, processFlowVersion, processOperationName, processOperationVersion,
				toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion };

		List<SampleProduct> sampleProductDataList = new ArrayList<SampleProduct>();
		try
		{
			sampleProductDataList = ExtendedObjectProxy.getSampleProductService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			sampleProductDataList = null;
		}

		log.info("End GetSampleProduct");

		return sampleProductDataList;
	}

	public List<SampleProduct> getSampleProductDataListByLotNameAndToInfo(String lotName, String factoryName, String productSpecName, String productSpecVersion, String toProcessFlowName,
			String toProcessFlowVersion, String toProcessOperationName, String toProcessOperationVersion)
	{
		log.info("GetSampleProduct By LotName - lotName : " + lotName + ", ToInfo : " + toProcessFlowName + "/" + toProcessOperationName);

		String condition = "LOTNAME = ? AND FACTORYNAME = ? AND PRODUCTSPECNAME = ? AND PRODUCTSPECVERSION = ? AND TOPROCESSFLOWNAME = ? AND TOPROCESSFLOWVERSION = ? AND TOPROCESSOPERATIONNAME = ? AND TOPROCESSOPERATIONVERSION = ?";
		Object[] bindSet = new Object[] { lotName, factoryName, productSpecName, productSpecVersion, toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion };

		List<SampleProduct> sampleProductList = new ArrayList<SampleProduct>();
		try
		{
			sampleProductList = ExtendedObjectProxy.getSampleProductService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			sampleProductList = null;
		}

		log.info("End GetSampleProduct By LotName");

		return sampleProductList;
	}

	public List<SampleProduct> getSampleProductDataListByLotName(String lotName, String factoryName, String productSpecName, String productSpecVersion, String processFlowName,
			String processFlowVersion, String processOperationName, String processOperationVersion, String machineName, String toProcessFlowName, String toProcessFlowVersion,
			String toProcessOperationName, String toProcessOperationVersion)
	{
		log.info("GetSampleProduct By LotName - lotName : " + lotName + ", FromInfo : " + processFlowName + "/" + processOperationName + ", ToInfo : " + toProcessFlowName + "/"
				+ toProcessOperationName);

		String condition = "LOTNAME = ? AND FACTORYNAME = ? AND PRODUCTSPECNAME = ? AND PRODUCTSPECVERSION = ? AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ? AND PROCESSOPERATIONNAME = ? AND PROCESSOPERATIONVERSION = ? AND MACHINENAME = ? AND TOPROCESSFLOWNAME = ? AND TOPROCESSFLOWVERSION = ? AND TOPROCESSOPERATIONNAME = ? AND TOPROCESSOPERATIONVERSION = ?";
		Object[] bindSet = new Object[] { lotName, factoryName, productSpecName, productSpecVersion, processFlowName, processFlowVersion, processOperationName, processOperationVersion, machineName,
				toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion };

		List<SampleProduct> sampleProductList = new ArrayList<SampleProduct>();
		try
		{
			sampleProductList = ExtendedObjectProxy.getSampleProductService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			sampleProductList = null;
		}

		log.info("End GetSampleProduct By LotName");

		return sampleProductList;
	}

	public List<SampleProduct> getSampleProductDataListByLotName(String lotName, String factoryName, String productSpecName, String productSpecVersion, String processFlowName,
			String processFlowVersion, String processOperationName, String processOperationVersion, String toProcessFlowName, String toProcessFlowVersion, String toProcessOperationName,
			String toProcessOperationVersion)
	{
		log.info("GetSampleProduct By LotName - lotName : " + lotName + ", FromInfo : " + processFlowName + "/" + processOperationName + ", ToInfo : " + toProcessFlowName + "/"
				+ toProcessOperationName);

		String condition = "LOTNAME = ? AND FACTORYNAME = ? AND PRODUCTSPECNAME = ? AND PRODUCTSPECVERSION = ? AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ? AND PROCESSOPERATIONNAME = ? AND PROCESSOPERATIONVERSION = ? AND TOPROCESSFLOWNAME = ? AND TOPROCESSFLOWVERSION = ? AND TOPROCESSOPERATIONNAME = ? AND TOPROCESSOPERATIONVERSION = ?";
		Object[] bindSet = new Object[] { lotName, factoryName, productSpecName, productSpecVersion, processFlowName, processFlowVersion, processOperationName, processOperationVersion,
				toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion };

		List<SampleProduct> sampleProductList = new ArrayList<SampleProduct>();
		try
		{
			sampleProductList = ExtendedObjectProxy.getSampleProductService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			sampleProductList = null;
		}

		log.info("End GetSampleProduct By LotName");

		return sampleProductList;
	}

	public List<SampleProduct> getSampleProductListByComment(SampleLot sampleLot)
	{
		String condition = "LOTNAME = ? AND FACTORYNAME = ? AND PRODUCTSPECNAME = ? AND PRODUCTSPECVERSION = ? AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ? AND TOPROCESSFLOWNAME = ? AND TOPROCESSFLOWVERSION = ? AND TOPROCESSOPERATIONNAME = ? AND TOPROCESSOPERATIONVERSION = ? AND MACHINENAME = 'NA' AND MANUALSAMPLEFLAG = 'Y' AND EVENTCOMMENT LIKE ?";
		Object[] bindSet = new Object[] { sampleLot.getLotName(), sampleLot.getFactoryName(), sampleLot.getProductSpecName(), sampleLot.getProductSpecVersion(), sampleLot.getProcessFlowName(),
				sampleLot.getProcessFlowVersion(), sampleLot.getProcessOperationName(), sampleLot.getProcessOperationVersion(), sampleLot.getToProcessFlowName(), sampleLot.getToProcessFlowVersion(),
				sampleLot.getToProcessOperationName(), sampleLot.getToProcessOperationVersion(), "SetSampleDataByFutureAction%" };

		List<SampleProduct> sampleProductList = new ArrayList<SampleProduct>();

		try
		{
			sampleProductList = ExtendedObjectProxy.getSampleProductService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			sampleProductList = null;
		}

		return sampleProductList;
	}

	public SampleProduct insertSampleProduct(EventInfo eventInfo, String productName, String lotName, String factoryName, String productSpecName, String productSpecVersion, String processFlowName,
			String processFlowVersion, String processOperationName, String processOperationVersion, String machineName, String toProcessFlowName, String toProcessFlowVersion,
			String toProcessOperationName, String toProcessOperationVersion, String productSampleFlag, String productSampleCount, String productSamplePosition, String actualSamplePosition,
			String actualSampleSlotPosition, String manualSampleFlag) throws greenFrameDBErrorSignal, CustomException
	{
		log.info("Start InsertSampleProduct - ProductName : " + productName + ", lotName : " + lotName + ", FromInfo : " + processFlowName + "/" + processOperationName + ", ToInfo : "
				+ toProcessFlowName + "/" + toProcessOperationName);

		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		SampleProduct dataInfo = new SampleProduct();
		dataInfo.setProductName(productName);
		dataInfo.setLotName(lotName);
		dataInfo.setFactoryName(factoryName);
		dataInfo.setProductSpecName(productSpecName);
		dataInfo.setProductSpecVersion(productSpecVersion);
		dataInfo.setProcessFlowName(processFlowName);
		dataInfo.setProcessFlowVersion(processFlowVersion);
		dataInfo.setProcessOperationName(processOperationName);
		dataInfo.setProcessOperationVersion(processOperationVersion);
		dataInfo.setMachineName(machineName);
		dataInfo.setToProcessFlowName(toProcessFlowName);
		dataInfo.setToProcessFlowVersion(toProcessFlowVersion);
		dataInfo.setToProcessOperationName(toProcessOperationName);
		dataInfo.setToProcessOperationVersion(toProcessOperationVersion);
		dataInfo.setProductSampleFlag(productSampleFlag);
		dataInfo.setProductSampleCount(productSampleCount);
		dataInfo.setProductSamplePosition(productSamplePosition);
		dataInfo.setActualSamplePosition(actualSamplePosition);
		dataInfo.setActualSampleSlotPosition(actualSampleSlotPosition);
		dataInfo.setManualSampleFlag(manualSampleFlag);
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setLastEventTime(eventInfo.getEventTime());

		SampleProduct sampleProductData = ExtendedObjectProxy.getSampleProductService().create(eventInfo, dataInfo);

		log.info("End insertSampleProduct");

		return sampleProductData;
	}
	
	public SampleProduct insertSampleProductWithFlag(List<SampleProduct> sampleProductList,EventInfo eventInfo, String productName, String lotName, String factoryName, String productSpecName, String productSpecVersion, String processFlowName,
			String processFlowVersion, String processOperationName, String processOperationVersion, String machineName, String toProcessFlowName, String toProcessFlowVersion,
			String toProcessOperationName, String toProcessOperationVersion, String productSampleFlag, String productSampleCount, String productSamplePosition, String actualSamplePosition,
			String actualSampleSlotPosition) throws greenFrameDBErrorSignal, CustomException
	{
		log.info("Start InsertSampleProduct - ProductName : " + productName + ", lotName : " + lotName + ", FromInfo : " + processFlowName + "/" + processOperationName + ", ToInfo : "
				+ toProcessFlowName + "/" + toProcessOperationName);

		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		String oldmanualSampleFlag="";
		String oldforceSamplingFlag="";
		String olddepartment="";
		String oldReasonCode="";
		
		if(sampleProductList!=null)
		{
			for(SampleProduct sampleProductInfo:sampleProductList)
			{
				if(StringUtils.equals(sampleProductInfo.getProductName(), productName))
				{
					oldmanualSampleFlag=sampleProductInfo.getManualSampleFlag();
					oldforceSamplingFlag=sampleProductInfo.getForceSamplingFlag();
					olddepartment=sampleProductInfo.getDepartment();
					//oldReasonCode=sampleProductInfo.getReasonCode();
					break;
				}
			}
		}

		SampleProduct dataInfo = new SampleProduct();
		dataInfo.setProductName(productName);
		dataInfo.setLotName(lotName);
		dataInfo.setFactoryName(factoryName);
		dataInfo.setProductSpecName(productSpecName);
		dataInfo.setProductSpecVersion(productSpecVersion);
		dataInfo.setProcessFlowName(processFlowName);
		dataInfo.setProcessFlowVersion(processFlowVersion);
		dataInfo.setProcessOperationName(processOperationName);
		dataInfo.setProcessOperationVersion(processOperationVersion);
		dataInfo.setMachineName(machineName);
		dataInfo.setToProcessFlowName(toProcessFlowName);
		dataInfo.setToProcessFlowVersion(toProcessFlowVersion);
		dataInfo.setToProcessOperationName(toProcessOperationName);
		dataInfo.setToProcessOperationVersion(toProcessOperationVersion);
		dataInfo.setProductSampleFlag(productSampleFlag);
		dataInfo.setProductSampleCount(productSampleCount);
		dataInfo.setProductSamplePosition(productSamplePosition);
		dataInfo.setActualSamplePosition(actualSamplePosition);
		dataInfo.setActualSampleSlotPosition(actualSampleSlotPosition);
		dataInfo.setManualSampleFlag(oldmanualSampleFlag);
		dataInfo.setForceSamplingFlag(oldforceSamplingFlag);
		dataInfo.setDepartment(olddepartment);
		//dataInfo.setReasonCode(oldReasonCode);
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setLastEventTime(eventInfo.getEventTime());

		SampleProduct sampleProductData = ExtendedObjectProxy.getSampleProductService().create(eventInfo, dataInfo);

		log.info("End insertSampleProduct");

		return sampleProductData;
	}

	//20210801 houxk
	public SampleProduct insertSampleProduct(EventInfo eventInfo, String productName, String lotName, String factoryName, String productSpecName, String productSpecVersion, String processFlowName,
			String processFlowVersion, String processOperationName, String processOperationVersion, String machineName, String toProcessFlowName, String toProcessFlowVersion,
			String toProcessOperationName, String toProcessOperationVersion, String productSampleFlag, String productSampleCount, String productSamplePosition, String actualSamplePosition,
			String actualSampleSlotPosition, String manualSampleFlag, String forceSamplingFlag, String department, String reasonCode) throws greenFrameDBErrorSignal, CustomException
	{
		log.info("Start InsertSampleProduct - ProductName : " + productName + ", lotName : " + lotName + ", FromInfo : " + processFlowName + "/" + processOperationName + ", ToInfo : "
				+ toProcessFlowName + "/" + toProcessOperationName);

		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		SampleProduct dataInfo = new SampleProduct();
		dataInfo.setProductName(productName);
		dataInfo.setLotName(lotName);
		dataInfo.setFactoryName(factoryName);
		dataInfo.setProductSpecName(productSpecName);
		dataInfo.setProductSpecVersion(productSpecVersion);
		dataInfo.setProcessFlowName(processFlowName);
		dataInfo.setProcessFlowVersion(processFlowVersion);
		dataInfo.setProcessOperationName(processOperationName);
		dataInfo.setProcessOperationVersion(processOperationVersion);
		dataInfo.setMachineName(machineName);
		dataInfo.setToProcessFlowName(toProcessFlowName);
		dataInfo.setToProcessFlowVersion(toProcessFlowVersion);
		dataInfo.setToProcessOperationName(toProcessOperationName);
		dataInfo.setToProcessOperationVersion(toProcessOperationVersion);
		dataInfo.setProductSampleFlag(productSampleFlag);
		dataInfo.setProductSampleCount(productSampleCount);
		dataInfo.setProductSamplePosition(productSamplePosition);
		dataInfo.setActualSamplePosition(actualSamplePosition);
		dataInfo.setActualSampleSlotPosition(actualSampleSlotPosition);
		dataInfo.setManualSampleFlag(manualSampleFlag);
		dataInfo.setForceSamplingFlag(forceSamplingFlag);
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setDepartment(department);
		//dataInfo.setReasonCode(reasonCode);

		SampleProduct sampleProductData = ExtendedObjectProxy.getSampleProductService().create(eventInfo, dataInfo);

		log.info("End insertSampleProduct");

		return sampleProductData;
	}
	
	public SampleProduct insertReserveSampleProduct(EventInfo eventInfo, String productName, String lotName, String factoryName, String productSpecName, String productSpecVersion,
			String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion, String machineName, String toProcessFlowName, String toProcessFlowVersion,
			String toProcessOperationName, String toProcessOperationVersion, String productSampleFlag, String productSampleCount, String productSamplePosition, String actualSamplePosition,
			String actualSampleSlotPosition, String manualSampleFlag) throws greenFrameDBErrorSignal, CustomException
	{
		log.info("Start InsertSampleProduct - ProductName : " + productName + ", lotName : " + lotName + ", FromInfo : " + processFlowName + "/" + processOperationName + ", ToInfo : "
				+ toProcessFlowName + "/" + toProcessOperationName);

		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		SampleProduct dataInfo = new SampleProduct();
		dataInfo.setProductName(productName);
		dataInfo.setLotName(lotName);
		dataInfo.setFactoryName(factoryName);
		dataInfo.setProductSpecName(productSpecName);
		dataInfo.setProductSpecVersion(productSpecVersion);
		dataInfo.setProcessFlowName(processFlowName);
		dataInfo.setProcessFlowVersion(processFlowVersion);
		dataInfo.setProcessOperationName(processOperationName);
		dataInfo.setProcessOperationVersion(processOperationVersion);
		dataInfo.setMachineName(machineName);
		dataInfo.setToProcessFlowName(toProcessFlowName);
		dataInfo.setToProcessFlowVersion(toProcessFlowVersion);
		dataInfo.setToProcessOperationName(toProcessOperationName);
		dataInfo.setToProcessOperationVersion(toProcessOperationVersion);
		dataInfo.setProductSampleFlag(productSampleFlag);
		dataInfo.setProductSampleCount(productSampleCount);
		dataInfo.setProductSamplePosition(productSamplePosition);
		dataInfo.setActualSamplePosition(actualSamplePosition);
		dataInfo.setActualSampleSlotPosition(actualSampleSlotPosition);
		dataInfo.setManualSampleFlag(manualSampleFlag);
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setLastEventTime(eventInfo.getEventTime());

		SampleProduct sampleProductData = ExtendedObjectProxy.getSampleProductService().create(eventInfo, dataInfo);

		// MantisID : 0000022
		// Add Product history
		EventInfo productEventInfo = EventInfoUtil.makeEventInfo("ReserveSample", eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);
		productEventInfo.setBehaviorName("P");
		productEventInfo.setEventTimeKey(eventInfo.getEventTimeKey());

		ProductServiceProxy.getProductService().setEvent(new ProductKey(productName), productEventInfo, new kr.co.aim.greentrack.product.management.info.SetEventInfo());

		log.info("End insertSampleProduct");

		return sampleProductData;
	}

	//20210801 houxk
	public SampleProduct insertReserveSampleProduct(EventInfo eventInfo, String productName, String lotName, String factoryName, String productSpecName, String productSpecVersion,
			String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion, String machineName, String toProcessFlowName, String toProcessFlowVersion,
			String toProcessOperationName, String toProcessOperationVersion, String productSampleFlag, String productSampleCount, String productSamplePosition, String actualSamplePosition,
			String actualSampleSlotPosition, String manualSampleFlag, String forceSamplingFlag, String department, String reasonCode) throws greenFrameDBErrorSignal, CustomException
	{
		log.info("Start InsertSampleProduct - ProductName : " + productName + ", lotName : " + lotName + ", FromInfo : " + processFlowName + "/" + processOperationName + ", ToInfo : "
				+ toProcessFlowName + "/" + toProcessOperationName);

		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		SampleProduct dataInfo = new SampleProduct();
		dataInfo.setProductName(productName);
		dataInfo.setLotName(lotName);
		dataInfo.setFactoryName(factoryName);
		dataInfo.setProductSpecName(productSpecName);
		dataInfo.setProductSpecVersion(productSpecVersion);
		dataInfo.setProcessFlowName(processFlowName);
		dataInfo.setProcessFlowVersion(processFlowVersion);
		dataInfo.setProcessOperationName(processOperationName);
		dataInfo.setProcessOperationVersion(processOperationVersion);
		dataInfo.setMachineName(machineName);
		dataInfo.setToProcessFlowName(toProcessFlowName);
		dataInfo.setToProcessFlowVersion(toProcessFlowVersion);
		dataInfo.setToProcessOperationName(toProcessOperationName);
		dataInfo.setToProcessOperationVersion(toProcessOperationVersion);
		dataInfo.setProductSampleFlag(productSampleFlag);
		dataInfo.setProductSampleCount(productSampleCount);
		dataInfo.setProductSamplePosition(productSamplePosition);
		dataInfo.setActualSamplePosition(actualSamplePosition);
		dataInfo.setActualSampleSlotPosition(actualSampleSlotPosition);
		dataInfo.setManualSampleFlag(manualSampleFlag);
		dataInfo.setForceSamplingFlag(forceSamplingFlag);
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setDepartment(department);
		//dataInfo.setReasonCode(reasonCode);
		
		SampleProduct sampleProductData = ExtendedObjectProxy.getSampleProductService().create(eventInfo, dataInfo);

		// MantisID : 0000022
		// Add Product history
		EventInfo productEventInfo = EventInfoUtil.makeEventInfo("ReserveSample", eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);
		productEventInfo.setBehaviorName("P");
		productEventInfo.setEventTimeKey(eventInfo.getEventTimeKey());

		ProductServiceProxy.getProductService().setEvent(new ProductKey(productName), productEventInfo, new kr.co.aim.greentrack.product.management.info.SetEventInfo());

		log.info("End insertSampleProduct");

		return sampleProductData;
	}
	
	public void deleteSampleProduct(EventInfo eventInfo, List<String> productNameList) throws greenFrameDBErrorSignal, CustomException
	{
		if (productNameList.size() > 0)
		{
			for (String productName : productNameList)
			{
				String condition = "PRODUCTNAME = ?";
				Object[] bindSet = new Object[] { productName };

				List<SampleProduct> sampleProductList = new ArrayList<SampleProduct>();

				try
				{
					sampleProductList = ExtendedObjectProxy.getSampleProductService().select(condition, bindSet);
				}
				catch (Exception e)
				{
					sampleProductList = null;
				}

				if (sampleProductList != null)
				{
					for (SampleProduct dataInfo : sampleProductList)
					{
						ExtendedObjectProxy.getSampleProductService().remove(eventInfo, dataInfo);
					}
				}
			}
		}
	}

	public void deleteSampleProductList(EventInfo eventInfo, List<String> productList) throws greenFrameDBErrorSignal, CustomException
	{
		if (productList.size() > 0)
		{
			for (String productName : productList)
			{
				String condition = "PRODUCTNAME = ?";
				Object[] bindSet = new Object[] { productName };

				List<SampleProduct> sampleProductList = new ArrayList<SampleProduct>();

				try
				{
					sampleProductList = ExtendedObjectProxy.getSampleProductService().select(condition, bindSet);
				}
				catch (Exception e)
				{
					sampleProductList = null;
				}

				if (sampleProductList != null)
				{
					for (SampleProduct dataInfo : sampleProductList)
					{
						ExtendedObjectProxy.getSampleProductService().remove(eventInfo, dataInfo);
					}
				}
			}
		}
	}

	public void deleteSampleProductByToInfo(EventInfo eventInfo, String productName, String lotName, String factoryName, String productSpecName, String productSpecVersion, String toProcessFlowName,
			String toProcessFlowVersion, String toProcessOperationName, String toProcessOperationVersion) throws greenFrameDBErrorSignal, CustomException
	{
		log.info("Start DeleteSampleProduct - ProductName : " + productName + ", lotName : " + lotName + ", ToInfo : " + toProcessFlowName + "/" + toProcessOperationName);

		String condition = "PRODUCTNAME = ? AND LOTNAME = ? AND FACTORYNAME = ? AND PRODUCTSPECNAME = ? AND PRODUCTSPECVERSION = ? AND TOPROCESSFLOWNAME = ? AND TOPROCESSFLOWVERSION = ? AND TOPROCESSOPERATIONNAME = ? AND TOPROCESSOPERATIONVERSION = ?";
		Object[] bindSet = new Object[] { productName, lotName, factoryName, productSpecName, productSpecVersion, toProcessFlowName, toProcessFlowVersion, toProcessOperationName,
				toProcessOperationVersion };

		List<SampleProduct> sampleProductList = new ArrayList<SampleProduct>();

		try
		{
			sampleProductList = ExtendedObjectProxy.getSampleProductService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			sampleProductList = null;
		}

		if (sampleProductList != null)
		{
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			for (SampleProduct sampleProduct : sampleProductList)
			{
				ExtendedObjectProxy.getSampleProductService().remove(eventInfo, sampleProduct);
			}
		}

		log.info("End DeleteSampleProduct");
	}

	public void deleteSampleProductByLotNameAndToInfo(EventInfo eventInfo, String lotName, String factoryName, String productSpecName, String productSpecVersion, String toProcessFlowName,
			String toProcessFlowVersion, String toProcessOperationName, String toProcessOperationVersion) throws greenFrameDBErrorSignal, CustomException
	{
		log.info("Start DeleteSampleProduct By LotName - lotName : " + lotName + ", ToInfo : " + toProcessFlowName + "/" + toProcessOperationName);

		String condition = "LOTNAME = ? AND FACTORYNAME = ? AND PRODUCTSPECNAME = ? AND PRODUCTSPECVERSION = ? AND TOPROCESSFLOWNAME = ? AND TOPROCESSFLOWVERSION = ? AND TOPROCESSOPERATIONNAME = ? AND TOPROCESSOPERATIONVERSION = ?";
		Object[] bindSet = new Object[] { lotName, factoryName, productSpecName, productSpecVersion, toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion };

		List<SampleProduct> sampleProductList = new ArrayList<SampleProduct>();

		try
		{
			sampleProductList = ExtendedObjectProxy.getSampleProductService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			sampleProductList = null;
		}

		if (sampleProductList != null)
		{
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			for (SampleProduct sampleProduct : sampleProductList)
			{
				ExtendedObjectProxy.getSampleProductService().remove(eventInfo, sampleProduct);
			}
		}

		log.info("end DeleteSampleProduct By LotName");
	}

	public void deleteSampleProductByLotNameAndInfo(EventInfo eventInfo, String lotName, String factoryName, String productSpecName, String productSpecVersion, String processFlowName,
			String processFlowVersion, String processOperationName, String processOperationVersion) throws greenFrameDBErrorSignal, CustomException
	{
		log.info("Start DeleteSampleProduct By LotName - lotName : " + lotName + ", Info : " + processFlowName + "/" + processOperationName);

		String condition = "LOTNAME = ? AND FACTORYNAME = ? AND PRODUCTSPECNAME = ? AND PRODUCTSPECVERSION = ? AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ? AND PROCESSOPERATIONNAME = ? AND PROCESSOPERATIONVERSION = ?";
		Object[] bindSet = new Object[] { lotName, factoryName, productSpecName, productSpecVersion, processFlowName, processFlowVersion, processOperationName, processOperationVersion };

		List<SampleProduct> sampleProductList = new ArrayList<SampleProduct>();

		try
		{
			sampleProductList = ExtendedObjectProxy.getSampleProductService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			sampleProductList = null;
		}

		if (sampleProductList != null)
		{
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			for (SampleProduct sampleProduct : sampleProductList)
			{
				ExtendedObjectProxy.getSampleProductService().remove(eventInfo, sampleProduct);
			}
		}

		log.info("end DeleteSampleProduct By LotName");
	}

	public void deleteSampleProductByLotName(EventInfo eventInfo, String lotName, String factoryName, String productSpecName, String productSpecVersion, String processFlowName,
			String processFlowVersion, String processOperationName, String processOperationVersion, String machineName, String toProcessFlowName, String toProcessFlowVersion,
			String toProcessOperationName, String toProcessOperationVersion) throws greenFrameDBErrorSignal, CustomException
	{
		log.info("Start DeleteSampleProduct By LotName - lotName : " + lotName + ", FromInfo : " + processFlowName + "/" + processOperationName + ", ToInfo : " + toProcessFlowName + "/"
				+ toProcessOperationName);

		String condition = "LOTNAME = ? AND FACTORYNAME = ? AND PRODUCTSPECNAME = ? AND PRODUCTSPECVERSION = ? AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ? AND PROCESSOPERATIONNAME = ? AND PROCESSOPERATIONVERSION = ? AND MACHINENAME = ? AND TOPROCESSFLOWNAME = ? AND TOPROCESSFLOWVERSION = ? AND TOPROCESSOPERATIONNAME = ? AND TOPROCESSOPERATIONVERSION = ?";
		Object[] bindSet = new Object[] { lotName, factoryName, productSpecName, productSpecVersion, processFlowName, processFlowVersion, processOperationName, processOperationVersion, machineName,
				toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion };

		List<SampleProduct> sampleProductList = new ArrayList<SampleProduct>();

		try
		{
			sampleProductList = ExtendedObjectProxy.getSampleProductService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			sampleProductList = null;
		}

		if (sampleProductList != null)
		{
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			for (SampleProduct sampleProduct : sampleProductList)
			{
				ExtendedObjectProxy.getSampleProductService().remove(eventInfo, sampleProduct);
			}
		}

		log.info("end DeleteSampleProduct");
	}

	public void deleteReserveSampleProductByLotName(EventInfo eventInfo, String lotName, String factoryName, String productSpecName, String productSpecVersion, String processFlowName,
			String processFlowVersion, String processOperationName, String processOperationVersion, String machineName, String toProcessFlowName, String toProcessFlowVersion,
			String toProcessOperationName, String toProcessOperationVersion) throws greenFrameDBErrorSignal, CustomException
	{
		log.info("Start DeleteSampleProduct By LotName - lotName : " + lotName + ", FromInfo : " + processFlowName + "/" + processOperationName + ", ToInfo : " + toProcessFlowName + "/"
				+ toProcessOperationName);

		String condition = "LOTNAME = ? AND FACTORYNAME = ? AND PRODUCTSPECNAME = ? AND PRODUCTSPECVERSION = ? AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ? AND PROCESSOPERATIONNAME = ? AND PROCESSOPERATIONVERSION = ? AND MACHINENAME = ? AND TOPROCESSFLOWNAME = ? AND TOPROCESSFLOWVERSION = ? AND TOPROCESSOPERATIONNAME = ? AND TOPROCESSOPERATIONVERSION = ?";
		Object[] bindSet = new Object[] { lotName, factoryName, productSpecName, productSpecVersion, processFlowName, processFlowVersion, processOperationName, processOperationVersion, machineName,
				toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion };

		List<SampleProduct> sampleProductList = new ArrayList<SampleProduct>();

		try
		{
			sampleProductList = ExtendedObjectProxy.getSampleProductService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			sampleProductList = null;
		}

		if (sampleProductList != null)
		{
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			EventInfo productEventInfo = EventInfoUtil.makeEventInfo("CancelReserveSample", eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);
			productEventInfo.setBehaviorName("P");
			productEventInfo.setEventTimeKey(eventInfo.getEventTimeKey());

			for (SampleProduct sampleProduct : sampleProductList)
			{
				ExtendedObjectProxy.getSampleProductService().remove(eventInfo, sampleProduct);

				// MantisID : 0000022
				// Add Product history
				ProductServiceProxy.getProductService().setEvent(new ProductKey(sampleProduct.getProductName()), productEventInfo, new kr.co.aim.greentrack.product.management.info.SetEventInfo());
			}
		}

		log.info("end DeleteSampleProduct");
	}

	public void deleteSampleProduct(EventInfo eventInfo, String productName, String lotName, String factoryName, String productSpecName, String productSpecVersion, String processFlowName,
			String processFlowVersion, String processOperationName, String processOperationVersion, String machineName, String toProcessFlowName, String toProcessFlowVersion,
			String toProcessOperationName, String toProcessOperationVersion) throws greenFrameDBErrorSignal, CustomException
	{
		log.info("Start DeleteSampleProduct By LotName - lotName : " + lotName + ", FromInfo : " + processFlowName + "/" + processOperationName + ", ToInfo : " + toProcessFlowName + "/"
				+ toProcessOperationName);

		SampleProduct sampleProduct = ExtendedObjectProxy.getSampleProductService().getSampleProductData(productName, lotName, factoryName, productSpecName, productSpecVersion, processFlowName,
				processFlowVersion, processOperationName, processOperationVersion, machineName, toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion);

		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		ExtendedObjectProxy.getSampleProductService().remove(eventInfo, sampleProduct);

		log.info("end DeleteSampleProduct");
	}

	public void deleteSampleProductWithoutMachineName(EventInfo eventInfo, String productName, String lotName, String factoryName, String productSpecName, String productSpecVersion,
			String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion, String toProcessFlowName, String toProcessFlowVersion,
			String toProcessOperationName, String toProcessOperationVersion) throws greenFrameDBErrorSignal, CustomException
	{
		log.info("Start DeleteSampleProduct By LotName - lotName : " + lotName + ", FromInfo : " + processFlowName + "/" + processOperationName + ", ToInfo : " + toProcessFlowName + "/"
				+ toProcessOperationName);

		List<SampleProduct> sampleProductList = ExtendedObjectProxy.getSampleProductService().getSampleProductDataListWithoutMachineName(productName, lotName, factoryName, productSpecName,
				productSpecVersion, processFlowName, processFlowVersion, processOperationName, processOperationVersion, toProcessFlowName, toProcessFlowVersion, toProcessOperationName,
				toProcessOperationVersion);

		if (sampleProductList != null)
		{
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			for (SampleProduct sampleProduct : sampleProductList)
			{
				ExtendedObjectProxy.getSampleProductService().remove(eventInfo, sampleProduct);
			}
		}

		log.info("end DeleteSampleProduct");
	}

	public void updateSampleProduct(EventInfo eventInfo, String productName, String lotName, String factoryName, String productSpecName, String productSpecVersion, String processFlowName,
			String processFlowVersion, String processOperationName, String processOperationVersion, String machineName, String toProcessFlowName, String toProcessFlowVersion,
			String toProcessOperationName, String toProcessOperationVersion, String productSampleFlag, String productSampleCount, String productSamplePosition, String actualSamplePosition,
			String actualSampleSlotPosition, String manualSampleFlag) throws greenFrameDBErrorSignal, CustomException
	{
		log.info("Start UpdateSampleProduct - ProductName : " + productName + ", lotName : " + lotName + ", FromInfo : " + processFlowName + "/" + processOperationName + ", ToInfo : "
				+ toProcessFlowName + "/" + toProcessOperationName);

		SampleProduct sampleProduct = ExtendedObjectProxy.getSampleProductService().getSampleProductData(productName, lotName, factoryName, productSpecName, productSpecVersion, processFlowName,
				processFlowVersion, processOperationName, processOperationVersion, machineName, toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion);

		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		if (sampleProduct != null)
		{
			if (StringUtils.isNotEmpty(productSampleFlag))
				sampleProduct.setProductSampleFlag(productSampleFlag);

			if (StringUtils.isNotEmpty(productSampleCount))
				sampleProduct.setProductSampleCount(productSampleCount);

			if (StringUtils.isNotEmpty(productSamplePosition))
				sampleProduct.setProductSamplePosition(productSamplePosition);

			if (StringUtils.isNotEmpty(actualSamplePosition))
				sampleProduct.setActualSamplePosition(actualSamplePosition);

			if (StringUtils.isNotEmpty(actualSampleSlotPosition))
				sampleProduct.setActualSampleSlotPosition(actualSampleSlotPosition);

			if (StringUtils.isNotEmpty(manualSampleFlag))
				sampleProduct.setManualSampleFlag(manualSampleFlag);

			sampleProduct.setLastEventComment(eventInfo.getEventComment());
			sampleProduct.setLastEventName(eventInfo.getEventName());
			sampleProduct.setLastEventTime(eventInfo.getEventTime());
			sampleProduct.setLastEventUser(eventInfo.getEventUser());

			ExtendedObjectProxy.getSampleProductService().modify(eventInfo, sampleProduct);
		}

		log.info("End UpdateSampleProduct");
	}

	public List<SampleProduct> getSampleProductDataListByProductList(String lotName, List<String> prodList)
	{
		String condition = "LOTNAME = ? AND PRODUCTNAME IN (";

		int count = 0;

		for (String prod : prodList)
		{
			condition += "'" + prod + "'";
			count += 1;

			if (count != prodList.size())
			{
				condition += ",";
			}
		}

		condition += ")";

		Object[] bindSet = new Object[] { lotName };

		List<SampleProduct> sampleProductList = new ArrayList<SampleProduct>();
		try
		{
			sampleProductList = ExtendedObjectProxy.getSampleProductService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			sampleProductList = null;
		}

		return sampleProductList;
	}

	public List<SampleProduct> getSampleProductDataByLotAndProductList(List<String> lotList, List<String> productList)
	{
		String condition = "LOTNAME IN (";
		int count = 0;

		for (String lotName : lotList)
		{
			condition += "'" + lotName + "'";
			count += 1;

			if (count != lotList.size())
			{
				condition += ",";
			}
		}

		condition += ") AND PRODUCTNAME IN (";
		count = 0;

		for (String productName : productList)
		{
			condition += "'" + productName + "'";
			count += 1;

			if (count != productList.size())
			{
				condition += ",";
			}
		}

		condition += ")";
		Object[] bindSet = new Object[] {};

		List<SampleProduct> dataInfoList = new ArrayList<SampleProduct>();

		try
		{
			dataInfoList = ExtendedObjectProxy.getSampleProductService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			dataInfoList = null;
		}

		return dataInfoList;
	}

	public void updateSampleProductWithoutMachineName(EventInfo eventInfo, String productName, String lotName, String factoryName, String productSpecName, String productSpecVersion,
			String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion, String toProcessFlowName, String toProcessFlowVersion,
			String toProcessOperationName, String toProcessOperationVersion, String productSampleFlag, String productSampleCount, String productSamplePosition, String actualSamplePosition,
			String actualSampleSlotPosition, String manualSampleFlag) throws greenFrameDBErrorSignal, CustomException
	{
		log.info("Start UpdateSampleProduct");

		List<SampleProduct> sampleProductList = ExtendedObjectProxy.getSampleProductService().getSampleProductDataListWithoutMachineName(productName, lotName, factoryName, productSpecName,
				productSpecVersion, processFlowName, processFlowVersion, processOperationName, processOperationVersion, toProcessFlowName, toProcessFlowVersion, toProcessOperationName,
				toProcessOperationVersion);

		if (sampleProductList != null)
		{
			for (SampleProduct sampleProductData : sampleProductList)
			{
				eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

				if (StringUtils.isNotEmpty(productSampleFlag))
					sampleProductData.setProductSampleFlag(productSampleFlag);

				if (StringUtils.isNotEmpty(productSampleCount))
					sampleProductData.setProductSampleCount(productSampleCount);

				if (StringUtils.isNotEmpty(productSamplePosition))
					sampleProductData.setProductSamplePosition(productSamplePosition);

				if (StringUtils.isNotEmpty(actualSamplePosition))
					sampleProductData.setActualSamplePosition(actualSamplePosition);

				if (StringUtils.isNotEmpty(actualSampleSlotPosition))
					sampleProductData.setActualSampleSlotPosition(actualSampleSlotPosition);

				if (StringUtils.isNotEmpty(manualSampleFlag))
					sampleProductData.setManualSampleFlag(manualSampleFlag);

				sampleProductData.setLastEventUser(eventInfo.getEventUser());
				sampleProductData.setLastEventName(eventInfo.getEventName());
				sampleProductData.setLastEventComment(eventInfo.getEventComment());
				sampleProductData.setLastEventTime(eventInfo.getEventTime());

				ExtendedObjectProxy.getSampleProductService().update(sampleProductData);
			}
		}

		log.info("End UpdateSampleProduct");
	}

	public void updateSampleProductKey(SampleProduct sampleProductData, EventInfo eventInfo, String productName, String lotName, String factoryName, String productSpecName, String productSpecVersion,
			String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion, String machineName, String toProcessFlowName, String toProcessFlowVersion,
			String toProcessOperationName, String toProcessOperationVersion, String productSampleFlag, String productSampleCount, String productSamplePosition, String actualSamplePosition,
			String actualSampleSlotPosition, String manualSampleFlag) throws greenFrameDBErrorSignal, CustomException
	{
		log.info("Start UpdateSampleProduct Key");

		SampleProduct oldSampleProductData = (SampleProduct) ObjectUtil.copyTo(sampleProductData);

		if (StringUtils.isNotEmpty(productName))
			sampleProductData.setProductName(productName);

		if (StringUtils.isNotEmpty(lotName))
			sampleProductData.setLotName(lotName);

		if (StringUtils.isNotEmpty(factoryName))
			sampleProductData.setFactoryName(factoryName);

		if (StringUtils.isNotEmpty(productSpecName))
			sampleProductData.setProductSpecName(productSpecName);

		if (StringUtils.isNotEmpty(productSpecVersion))
			sampleProductData.setProductSpecVersion(productSpecVersion);

		if (StringUtils.isNotEmpty(processFlowName))
			sampleProductData.setProcessFlowName(processFlowName);

		if (StringUtils.isNotEmpty(processFlowVersion))
			sampleProductData.setProcessFlowVersion(processFlowVersion);

		if (StringUtils.isNotEmpty(processOperationName))
			sampleProductData.setProcessOperationName(processOperationName);

		if (StringUtils.isNotEmpty(processOperationVersion))
			sampleProductData.setProcessOperationVersion(processOperationVersion);

		if (StringUtils.isNotEmpty(machineName))
			sampleProductData.setMachineName(machineName);

		if (StringUtils.isNotEmpty(toProcessFlowName))
			sampleProductData.setToProcessFlowName(toProcessFlowName);

		if (StringUtils.isNotEmpty(toProcessFlowVersion))
			sampleProductData.setToProcessFlowVersion(toProcessFlowVersion);

		if (StringUtils.isNotEmpty(toProcessOperationName))
			sampleProductData.setToProcessOperationName(toProcessOperationName);

		if (StringUtils.isNotEmpty(toProcessOperationVersion))
			sampleProductData.setToProcessOperationVersion(toProcessOperationVersion);

		if (StringUtils.isNotEmpty(productSampleFlag))
			sampleProductData.setProductSampleFlag(productSampleFlag);

		if (StringUtils.isNotEmpty(productSampleCount))
			sampleProductData.setProductSampleCount(productSampleCount);

		if (StringUtils.isNotEmpty(productSamplePosition))
			sampleProductData.setProductSamplePosition(productSamplePosition);

		if (StringUtils.isNotEmpty(actualSamplePosition))
			sampleProductData.setActualSamplePosition(actualSamplePosition);

		if (StringUtils.isNotEmpty(actualSampleSlotPosition))
			sampleProductData.setActualSampleSlotPosition(actualSampleSlotPosition);

		if (StringUtils.isNotEmpty(manualSampleFlag))
			sampleProductData.setManualSampleFlag(manualSampleFlag);

		sampleProductData.setLastEventUser(eventInfo.getEventUser());
		sampleProductData.setLastEventName(eventInfo.getEventName());
		sampleProductData.setLastEventComment(eventInfo.getEventComment());
		sampleProductData.setLastEventTime(eventInfo.getEventTime());

		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		ExtendedObjectProxy.getSampleProductService().updateToNew(oldSampleProductData, sampleProductData);
		super.addHistory(eventInfo, this.historyEntity, sampleProductData, logger);

		log.info("End UpdateSampleProduct Key");
	}

	public void deleteSampleProductByToFlow(EventInfo eventInfo, String lotName, String factoryName, String productSpecName, String productSpecVersion, String toProcessFlowName,
			String toProcessFlowVersion) throws greenFrameDBErrorSignal, CustomException
	{
		log.info("Start DeleteSampleProduct");

		String condition = "LOTNAME = ? AND FACTORYNAME = ? AND PRODUCTSPECNAME = ? AND PRODUCTSPECVERSION = ? AND TOPROCESSFLOWNAME = ? AND TOPROCESSFLOWVERSION = ?";
		Object[] bindSet = new Object[] { lotName, factoryName, productSpecName, productSpecVersion, toProcessFlowName, toProcessFlowVersion };

		List<SampleProduct> sampleProductList = new ArrayList<SampleProduct>();

		try
		{
			sampleProductList = ExtendedObjectProxy.getSampleProductService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			sampleProductList = null;
		}

		if (sampleProductList != null)
		{
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			for (SampleProduct sampleProduct : sampleProductList)
			{
				ExtendedObjectProxy.getSampleProductService().remove(eventInfo, sampleProduct);
			}
		}

		log.info("End DeleteSampleProduct");
	}
	
	public List<SampleProduct> getSampleProductDataListWithMachineName(String productName, String lotName, String factoryName, String productSpecName, String productSpecVersion,
			String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion, String toProcessFlowName, String toProcessFlowVersion,
			String toProcessOperationName, String toProcessOperationVersion, String machineName) throws greenFrameDBErrorSignal, CustomException
	{
		log.info("GetSampleProduct - ProductName : " + productName + ", lotName : " + lotName + ", FromInfo : " + processFlowName + "/" + processOperationName + ", ToInfo : " + toProcessFlowName
				+ "/" + toProcessOperationName);

		String condition = "PRODUCTNAME = ? AND LOTNAME = ? AND FACTORYNAME = ? AND PRODUCTSPECNAME = ? AND PRODUCTSPECVERSION = ? AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ? AND PROCESSOPERATIONNAME = ? AND PROCESSOPERATIONVERSION = ? AND TOPROCESSFLOWNAME = ? AND TOPROCESSFLOWVERSION = ? AND TOPROCESSOPERATIONNAME = ? AND TOPROCESSOPERATIONVERSION = ? AND MACHINENAME = ?";
		Object[] bindSet = new Object[] { productName, lotName, factoryName, productSpecName, productSpecVersion, processFlowName, processFlowVersion, processOperationName, processOperationVersion,
				toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion, machineName};

		List<SampleProduct> sampleProductDataList = new ArrayList<SampleProduct>();
		try
		{
			sampleProductDataList = ExtendedObjectProxy.getSampleProductService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			sampleProductDataList = null;
		}

		log.info("End GetSampleProduct");

		return sampleProductDataList;
	}
	
	public void setSampleProductTrackInEvent(EventInfo eventInfo,String lotName, String factoryName, String productSpecName, String productSpecVersion, String toProcessFlowName,
			String toProcessFlowVersion, String toProcessOperationName, String toProcessOperationVersion) throws CustomException
	{
		log.info("setSampleProductTrackInEvent By LotName - lotName : " + lotName + ", ToInfo : " + toProcessFlowName + "/" + toProcessOperationName);
		EventInfo newEventInfo=(EventInfo) ObjectUtil.copyTo(eventInfo);
		newEventInfo.setEventName("TrackIn");
        List<SampleProduct> sampleProductList=getSampleProductDataListByLotNameAndToInfo(lotName, factoryName, productSpecName, productSpecVersion, toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion);
        if(sampleProductList!=null)
        {
     	   for(SampleProduct sampleProductInfo:sampleProductList)
     	   {
     		   sampleProductInfo.setLastEventName("TrackIn");
     		   sampleProductInfo.setLastEventUser(newEventInfo.getEventUser());
     		   sampleProductInfo.setLastEventTime(newEventInfo.getEventTime());
     		   sampleProductInfo.setLastEventComment("TrackIn");
     		   ExtendedObjectProxy.getSampleProductService().modify(newEventInfo, sampleProductInfo);
     	   }
        }
        log.info("setSampleProductTrackInEvent By LotName End");  
           
	}
}
