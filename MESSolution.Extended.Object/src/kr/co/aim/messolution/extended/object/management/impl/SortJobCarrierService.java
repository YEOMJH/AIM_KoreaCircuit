package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.SortJobCarrier;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SortJobCarrierService extends CTORMService<SortJobCarrier> {
	public static Log logger = LogFactory.getLog(SortJobCarrierService.class);
	private static Log log = LogFactory.getLog(SortJobCarrierService.class);

	private final String historyEntity = "SortJobCarrierHist";

	public List<SortJobCarrier> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<SortJobCarrier> result = super.select(condition, bindSet, SortJobCarrier.class);

		return result;
	}

	public SortJobCarrier selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(SortJobCarrier.class, isLock, keySet);
	}

	public SortJobCarrier create(EventInfo eventInfo, SortJobCarrier dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, SortJobCarrier dataInfo) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public SortJobCarrier modify(EventInfo eventInfo, SortJobCarrier dataInfo)
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public SortJobCarrier getSortJobCarrierData(String jobName, String carrierName) throws greenFrameDBErrorSignal, CustomException
	{
		SortJobCarrier dataInfo = new SortJobCarrier();

		try
		{
			dataInfo = ExtendedObjectProxy.getSortJobCarrierService().selectByKey(false, new Object[] { jobName, carrierName });
		}
		catch (Exception e)
		{
			log.info("Data Not Found - JobName : " + jobName + ", CarrierName : " + carrierName);
			dataInfo = null;
		}

		return dataInfo;
	}

	public List<SortJobCarrier> getSortJobCarrierList(String jobName, String carrierName)
	{
		String condition = "JOBNAME = ? AND CARRIERNAME = ?";
		Object[] bindSet = new Object[] { jobName, carrierName };

		List<SortJobCarrier> dataInfoList = new ArrayList<SortJobCarrier>();

		try
		{
			dataInfoList = ExtendedObjectProxy.getSortJobCarrierService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			log.info("Data Not Found - JobName : " + jobName + ", CarrierName : " + carrierName);
			dataInfoList = null;
		}

		return dataInfoList;
	}

	public List<SortJobCarrier> getSortJobCarrierListByJobName(String jobName)
	{
		String condition = "JOBNAME = ?";
		Object[] bindSet = new Object[] { jobName };

		List<SortJobCarrier> dataInfoList = new ArrayList<SortJobCarrier>();

		try
		{
			dataInfoList = ExtendedObjectProxy.getSortJobCarrierService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			log.info("Data Not Found - JobName : " + jobName);
			dataInfoList = null;
		}

		return dataInfoList;
	}

	public List<SortJobCarrier> getSortJobCarrierListLotNameNotNull(String jobName)
	{
		String condition = " JOBNAME = ? AND LOTNAME IS NOT NULL ";
		Object[] bindSet = new Object[] { jobName };

		List<SortJobCarrier> dataInfoList = new ArrayList<SortJobCarrier>();

		try
		{
			dataInfoList = ExtendedObjectProxy.getSortJobCarrierService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			log.info("Data Not Found - JobName : " + jobName);
			dataInfoList = null;
		}

		return dataInfoList;
	}

	public List<SortJobCarrier> getSortJobCarrierSourceList(String jobName)
	{
		String condition = " JOBNAME = ? AND LOTNAME IS NOT NULL AND TRANSFERDIRECTION = 'SOURCE' ";
		Object[] bindSet = new Object[] { jobName };

		List<SortJobCarrier> dataInfoList = new ArrayList<SortJobCarrier>();

		try
		{
			dataInfoList = ExtendedObjectProxy.getSortJobCarrierService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			log.info("Data Not Found - JobName : " + jobName);
			dataInfoList = null;
		}

		return dataInfoList;
	}

	public void InsertSortJobCarrier(EventInfo eventInfo, String jobName, String carrierName, String lotName, String machineName, String portName, String transferDirection, String loadFlag,
			String loadTimekey) throws greenFrameDBErrorSignal, CustomException
	{
		log.info("Start InsertSortJobCarrier");

		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		SortJobCarrier dataInfo = new SortJobCarrier();
		dataInfo.setJobName(jobName);
		dataInfo.setCarrierName(carrierName);
		dataInfo.setLotName(lotName);
		dataInfo.setMachineName(machineName);
		dataInfo.setPortName(portName);
		dataInfo.setTransferDirection(transferDirection);
		dataInfo.setLoadFlag(loadFlag);
		dataInfo.setLoadTimekey(loadTimekey);
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setLastEventName(eventInfo.getEventName());

		ExtendedObjectProxy.getSortJobCarrierService().create(eventInfo, dataInfo);

		log.info("End InsertSortJobCarrier");
	}

	public void deleteSortJobCarrier(EventInfo eventInfo, String jobName, String carrierName) throws greenFrameDBErrorSignal, CustomException
	{
		log.info("Start DeleteSortJobCarrier");

		SortJobCarrier dataInfo = ExtendedObjectProxy.getSortJobCarrierService().getSortJobCarrierData(jobName, carrierName);

		if (dataInfo != null)
		{
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			ExtendedObjectProxy.getSortJobCarrierService().remove(eventInfo, dataInfo);
		}

		log.info("End DeleteSortJobCarrier");
	}

	public void deleteSortJobCarrier(EventInfo eventInfo, String jobName)
	{
		List<SortJobCarrier> dataInfoList = this.getSortJobCarrierListByJobName(jobName);

		if (dataInfoList != null)
		{
			for (SortJobCarrier dataInfo : dataInfoList)
				this.remove(eventInfo, dataInfo);
		}
	}
}
