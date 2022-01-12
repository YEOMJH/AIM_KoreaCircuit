package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.SortJobProduct;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class SortJobProductService extends CTORMService<SortJobProduct> {
	public static Log logger = LogFactory.getLog(SortJobProductService.class);
	private static Log log = LogFactory.getLog(SortJobProductService.class);

	private final String historyEntity = "SortJobProductHist";

	public List<SortJobProduct> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<SortJobProduct> result = super.select(condition, bindSet, SortJobProduct.class);

		return result;
	}

	public SortJobProduct selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(SortJobProduct.class, isLock, keySet);
	}

	public SortJobProduct create(EventInfo eventInfo, SortJobProduct dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, SortJobProduct dataInfo) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public SortJobProduct modify(EventInfo eventInfo, SortJobProduct dataInfo)
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public SortJobProduct getSortJobProduct(String jobName, String productName)
	{
		SortJobProduct dataInfo = new SortJobProduct();

		try
		{
			dataInfo = ExtendedObjectProxy.getSortJobProductService().selectByKey(false, new Object[] { jobName, productName });
		}
		catch (Exception e)
		{
			log.info("Data Not Found - JobName :" + jobName + ", productName : " + productName);
			dataInfo = null;
		}

		return dataInfo;
	}

	public List<SortJobProduct> getSortJobProductListByFromCarrier(String jobName, String fromCarrierName)
	{
		String condition = "JOBNAME = ? AND FROMCARRIERNAME = ? ORDER BY TO_NUMBER(FROMPOSITION), FROMSLOTPOSITION, TO_NUMBER(TOPOSITION), TOSLOTPOSITION";
		Object[] bindSet = new Object[] { jobName, fromCarrierName };

		List<SortJobProduct> sortJobProductList = new ArrayList<SortJobProduct>();

		try
		{
			sortJobProductList = ExtendedObjectProxy.getSortJobProductService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			log.info("Data Not Found - JobName :" + jobName + ", FROMCARRIERNAME : " + fromCarrierName);
			sortJobProductList = null;
		}

		return sortJobProductList;
	}

	public List<SortJobProduct> getSortJobProductByList(String jobName, List<String> productNameList)
	{
		String condition = "JOBNAME = ? AND PRODUCTNAME IN (";

		int count = 0;
		for (String productName : productNameList)
		{
			condition += "'" + productName + "'";
			count += 1;

			if (count != productNameList.size())
			{
				condition += ",";
			}
		}

		condition += ")";

		Object[] bindSet = new Object[] { jobName };

		List<SortJobProduct> dataInfoList = new ArrayList<SortJobProduct>();

		try
		{
			dataInfoList = ExtendedObjectProxy.getSortJobProductService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			log.info("Data Not Found JobName : " + jobName + ", ProductNameList : " + productNameList);

			dataInfoList = null;
		}

		return dataInfoList;
	}

	public List<SortJobProduct> getSortJobProductByJobName(String jobName)
	{
		String condition = " JOBNAME = ? ";
		Object[] bindSet = new Object[] { jobName };

		List<SortJobProduct> dataInfoList = new ArrayList<SortJobProduct>();

		try
		{
			dataInfoList = this.select(condition, bindSet);
		}
		catch (Exception e)
		{
			log.info("Data Not Found - JobName : " + jobName);
			dataInfoList = null;
		}

		return dataInfoList;
	}

	public void insertSortJobProduct(EventInfo eventInfo, String jobName, String productName, String machineName, String fromLotName, String fromCarrierName, String fromPortName, String fromPosition,
			String toLotName, String toCarrierName, String toPortName, String toPosition, String sortProductState, String turnFlag, String scrapFlag, String turnDegree, String slotPosition,
			String fromSlotPosition, String toSlotPosition, String reasonType, String reasonCode) throws greenFrameDBErrorSignal, CustomException
	{
		log.info("Start InsertSortJobProduct");

		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		SortJobProduct dataInfo = new SortJobProduct();
		dataInfo.setJobName(jobName);
		dataInfo.setProductName(productName);
		dataInfo.setMachineName(machineName);
		dataInfo.setFromLotName(fromLotName);
		dataInfo.setFromCarrierName(fromCarrierName);
		dataInfo.setFromPortName(fromPortName);
		dataInfo.setFromPosition(fromPosition);
		dataInfo.setToLotName(toLotName);
		dataInfo.setToCarrierName(toCarrierName);
		dataInfo.setToPortName(toPortName);
		dataInfo.setToPosition(toPosition);
		dataInfo.setSortProductState(sortProductState);
		dataInfo.setTurnFlag(turnFlag);
		dataInfo.setScrapFlag(scrapFlag);
		dataInfo.setTurnDegree(turnDegree);
		dataInfo.setSlotPosition(toSlotPosition);
		dataInfo.setFromSlotPosition(fromSlotPosition);
		dataInfo.setToSlotPosition(toSlotPosition);
		dataInfo.setReasonType(reasonType);
		dataInfo.setReasonCode(reasonCode);
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setLastEventUser(eventInfo.getEventUser());

		ExtendedObjectProxy.getSortJobProductService().create(eventInfo, dataInfo);

		log.info("End InsertSortJobProduct");
	}

	public void insertSortJobProduct(EventInfo eventInfo, String jobName, String productName, String machineName, String fromLotName, String fromCarrierName, String fromPortName, String fromPosition,
			String toLotName, String toCarrierName, String toPortName, String toPosition, String sortProductState, String cutFlag, String turnFlag, String scrapFlag, String turnDegree,
			String slotPosition, String fromSlotPosition, String toSlotPosition, String reasonType, String reasonCode) throws greenFrameDBErrorSignal, CustomException
	{
		log.info("Start InsertSortJobProduct");

		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		SortJobProduct dataInfo = new SortJobProduct();
		dataInfo.setJobName(jobName);
		dataInfo.setProductName(productName);
		dataInfo.setMachineName(machineName);
		dataInfo.setFromLotName(fromLotName);
		dataInfo.setFromCarrierName(fromCarrierName);
		dataInfo.setFromPortName(fromPortName);
		dataInfo.setFromPosition(fromPosition);
		dataInfo.setToLotName(toLotName);
		dataInfo.setToCarrierName(toCarrierName);
		dataInfo.setToPortName(toPortName);
		dataInfo.setToPosition(toPosition);
		dataInfo.setSortProductState(sortProductState);
		dataInfo.setCutFlag(cutFlag);
		dataInfo.setTurnFlag(turnFlag);
		dataInfo.setScrapFlag(scrapFlag);
		dataInfo.setTurnDegree(turnDegree);
		dataInfo.setSlotPosition(toSlotPosition);
		dataInfo.setFromSlotPosition(fromSlotPosition);
		dataInfo.setToSlotPosition(toSlotPosition);
		dataInfo.setReasonType(reasonType);
		dataInfo.setReasonCode(reasonCode);
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setLastEventUser(eventInfo.getEventUser());

		ExtendedObjectProxy.getSortJobProductService().create(eventInfo, dataInfo);

		log.info("End InsertSortJobProduct");
	}

	public void deleteSortJobProductByList(EventInfo eventInfo, String jobName, List<String> productNameList) throws greenFrameDBErrorSignal, CustomException
	{
		log.info("Start DeleteSortJobProduct");

		List<SortJobProduct> dataInfoList = ExtendedObjectProxy.getSortJobProductService().getSortJobProductByList(jobName, productNameList);

		if (dataInfoList != null)
		{
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			for (SortJobProduct dataInfo : dataInfoList)
			{
				ExtendedObjectProxy.getSortJobProductService().remove(eventInfo, dataInfo);
			}
		}

		log.info("End DeleteSortJobProduct");
	}

	public void deleteSortJobProduct(EventInfo eventInfo, String jobName)
	{
		List<SortJobProduct> dataInfoList = this.getSortJobProductByJobName(jobName);

		if (dataInfoList != null)
		{
			for (SortJobProduct dataInfo : dataInfoList)
				this.remove(eventInfo, dataInfo);
		}
	}
}
