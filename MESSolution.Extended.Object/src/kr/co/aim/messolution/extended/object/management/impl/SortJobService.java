package kr.co.aim.messolution.extended.object.management.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.SortJob;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SortJobService extends CTORMService<SortJob> {
	public static Log logger = LogFactory.getLog(SortJobService.class);
	private static Log log = LogFactory.getLog(SortJobService.class);

	public enum JobState {
		RESERVED("", "CONFIRMED"), CONFIRMED("RESERVED", "STARTED"), STARTED("CONFIRMED", "ABORT", "ENDED"), CANCELED("CONFIRMED", "NOTTING"), ABORT("STARTED", "NOTTING"), ENDED("STARTED", "NOTTING");

		private String[] name;

		private JobState(String... arg)
		{
			this.name = arg;
		}

		public static boolean validate(String inputArg)
		{
			if (inputArg == null || StringUtil.isEmpty(inputArg))
				return false;

			JobState[] values = JobState.values();

			for (JobState var : values)
			{
				if (var.toString().equals(inputArg))
					return true;
			}

			return false;
		}

		public String getPreviousState()
		{
			return this.name[0];
		}

		public String[] getNextState()
		{
			return Arrays.copyOfRange(this.name, 1, this.name.length);
		}
	}

	private final String historyEntity = "SortJobHist";

	public List<SortJob> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<SortJob> result = super.select(condition, bindSet, SortJob.class);

		return result;
	}

	public SortJob selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(SortJob.class, isLock, keySet);
	}

	public SortJob create(EventInfo eventInfo, SortJob dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, SortJob dataInfo) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public SortJob modify(EventInfo eventInfo, SortJob dataInfo)
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public String getPreJobState(String targetState)
	{
		if (!JobState.validate(targetState))
			return "";
		return JobState.valueOf(targetState).getPreviousState();
	}

	public String[] getNextState(String targetState)
	{
		if (!JobState.validate(targetState))
			return new String[] {};
		return JobState.valueOf(targetState).getNextState();
	}

	public SortJob getSortJobData(String jobName)
	{
		SortJob dataInfo = new SortJob();

		try
		{
			dataInfo = ExtendedObjectProxy.getSortJobService().selectByKey(false, new Object[] { jobName });
		}
		catch (Exception e)
		{
			log.info("Data Not Found - JobName : " + jobName);
			dataInfo = null;
		}

		return dataInfo;
	}

	public List<SortJob> getSortJobList(String jobName)
	{
		String condition = "JOBNAME = ?";
		Object[] bindSet = new Object[] { jobName };

		List<SortJob> dataInfoList = new ArrayList<SortJob>();

		try
		{
			dataInfoList = ExtendedObjectProxy.getSortJobService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			log.info("Data Not Found - JobName : " + jobName);
			dataInfoList = null;
		}

		return dataInfoList;
	}

	public List<SortJob> getSortJobListByLotName(String lotName)
	{
		String condition = "LOTNAME = ?";
		Object[] bindSet = new Object[] { lotName };

		List<SortJob> dataInfoList = new ArrayList<SortJob>();

		try
		{
			dataInfoList = ExtendedObjectProxy.getSortJobService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			log.info("Data Not Found - LotName : " + lotName);
			dataInfoList = null;
		}

		return dataInfoList;
	}

	public List<SortJob> getSortJobListStateCOST()
	{
		String condition = "JOBSTATE IN('CONFIRMED','STARTED')";
		Object[] bindSet = new Object[] {};

		List<SortJob> dataInfoList = new ArrayList<SortJob>();

		try
		{
			dataInfoList = ExtendedObjectProxy.getSortJobService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			log.info("Data Not Found");
			dataInfoList = null;
		}

		return dataInfoList;
	}

	public List<SortJob> getSortJobListByState(String jobState)
	{
		String condition = "JOBSTATE = :JOBSTATE ORDER BY LASTEVENTTIMEKEY";
		Object[] bindSet = new Object[] { jobState };

		List<SortJob> dataInfoList = new ArrayList<SortJob>();

		try
		{
			dataInfoList = ExtendedObjectProxy.getSortJobService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			log.info("Data Not Found");
			dataInfoList = null;
		}

		return dataInfoList;
	}

	public void insertSortJob(EventInfo eventInfo, String jobName, String jobState, String jobType, int priority, String reasonType, String reasonCode) throws greenFrameDBErrorSignal, CustomException
	{
		log.info("Start InsertSortJob");

		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		SortJob dataInfo = new SortJob();
		dataInfo.setJobName(jobName);
		dataInfo.setJobState(jobState);
		dataInfo.setJobType(jobType);
		dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setCreateTime(eventInfo.getEventTime());
		dataInfo.setCreateUser(eventInfo.getEventUser());
		dataInfo.setPriority(priority);
		dataInfo.setReasonType(reasonType);
		dataInfo.setReasonCode(reasonCode);

		ExtendedObjectProxy.getSortJobService().create(eventInfo, dataInfo);

		log.info("End InsertSortJob");
	}

	public void updateSortJob(EventInfo eventInfo, String jobName, String jobState, String jobType, String priority, String reasonType, String reasonCode) throws greenFrameDBErrorSignal,
			CustomException
	{
		log.info("Start UpdateSortJob");

		SortJob dataInfo = ExtendedObjectProxy.getSortJobService().getSortJobData(jobName);

		if (dataInfo != null)
		{
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			if (StringUtils.isNotEmpty(jobState))
			{
				dataInfo.setJobState(jobState);
			}
			if (StringUtils.isNotEmpty(jobType))
			{
				dataInfo.setJobType(jobType);
			}
			if (StringUtils.isNotEmpty(priority))
			{
				dataInfo.setPriority(Integer.parseInt(priority));
			}
			if (StringUtils.isNotEmpty(reasonType))
			{
				dataInfo.setReasonType(reasonType);
			}
			if (StringUtils.isNotEmpty(reasonCode))
			{
				dataInfo.setReasonCode(reasonCode);
			}

			dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
			dataInfo.setLastEventTime(eventInfo.getEventTime());
			dataInfo.setLastEventName(eventInfo.getEventName());
			dataInfo.setLastEventUser(eventInfo.getEventUser());
			dataInfo.setLastEventComment(eventInfo.getEventComment());

			ExtendedObjectProxy.getSortJobService().modify(eventInfo, dataInfo);
		}

		log.info("End UpdateSortJob");
	}

	public void updateSortJobWithCreateTime(EventInfo eventInfo, String jobName, String jobState, String jobType, String priority, String reasonType, String reasonCode, Timestamp createTime)
			throws greenFrameDBErrorSignal, CustomException
	{
		log.info("Start UpdateSortJob");

		SortJob dataInfo = ExtendedObjectProxy.getSortJobService().getSortJobData(jobName);

		if (dataInfo != null)
		{
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			if (StringUtils.isNotEmpty(jobState))
			{
				dataInfo.setJobState(jobState);
			}
			if (StringUtils.isNotEmpty(jobType))
			{
				dataInfo.setJobType(jobType);
			}
			if (StringUtils.isNotEmpty(priority))
			{
				dataInfo.setPriority(Integer.parseInt(priority));
			}
			if (StringUtils.isNotEmpty(reasonType))
			{
				dataInfo.setReasonType(reasonType);
			}
			if (StringUtils.isNotEmpty(reasonCode))
			{
				dataInfo.setReasonCode(reasonCode);
			}

			dataInfo.setCreateTime(createTime);
			dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
			dataInfo.setLastEventTime(eventInfo.getEventTime());
			dataInfo.setLastEventName(eventInfo.getEventName());
			dataInfo.setLastEventUser(eventInfo.getEventUser());
			dataInfo.setLastEventComment(eventInfo.getEventComment());

			ExtendedObjectProxy.getSortJobService().modify(eventInfo, dataInfo);
		}

		log.info("End UpdateSortJob");
	}

	public void deleteSortJob(EventInfo eventInfo, String jobName) throws greenFrameDBErrorSignal, CustomException
	{
		log.info("Start DeleteSortJob");

		SortJob dataInfo = ExtendedObjectProxy.getSortJobService().getSortJobData(jobName);

		if (dataInfo != null)
		{
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			ExtendedObjectProxy.getSortJobService().remove(eventInfo, dataInfo);
		}

		log.info("End DeleteSortJob");
	}

	public List<Map<String, Object>> getSortJobNameList(String lotName, String jobType)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT SJ.JOBNAME, SJ.JOBSTATE ");
		sql.append("  FROM CT_SORTJOB SJ, CT_SORTJOBPRODUCT SP ");
		sql.append(" WHERE SJ.JOBNAME = SP.JOBNAME ");
		sql.append("   AND SJ.JOBSTATE = 'RESERVED' ");
		sql.append("   AND SJ.JOBTYPE = :JOBTYPE ");
		sql.append("   AND (SP.FROMLOTNAME = :FROMLOTNAME ");
		sql.append("     OR SP.TOLOTNAME = :TOLOTNAME) ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("JOBTYPE", jobType);
		args.put("FROMLOTNAME", lotName);
		args.put("TOLOTNAME", lotName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		return sqlResult;
	}
}
