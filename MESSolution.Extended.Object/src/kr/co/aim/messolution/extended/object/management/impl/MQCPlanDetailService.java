package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.MQCPlanDetail;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.management.data.Lot;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MQCPlanDetailService extends CTORMService<MQCPlanDetail> {

	public static Log logger = LogFactory.getLog(MQCPlanDetailService.class);

	private final String historyEntity = "MQCPLANDETAILHISTORY";

	public List<MQCPlanDetail> select(String condition, Object[] bindSet) throws CustomException
	{
		List<MQCPlanDetail> result = super.select(condition, bindSet, MQCPlanDetail.class);

		return result;
	}

	public MQCPlanDetail selectByKey(boolean isLock, Object[] keySet) throws CustomException
	{
		return super.selectByKey(MQCPlanDetail.class, isLock, keySet);
	}

	public MQCPlanDetail create(EventInfo eventInfo, MQCPlanDetail dataInfo) throws CustomException
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void create(EventInfo eventInfo, List<MQCPlanDetail> dataInfoList) throws CustomException
	{
		super.insert(dataInfoList);

		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
	}

	public void remove(EventInfo eventInfo, MQCPlanDetail dataInfo) throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public MQCPlanDetail modify(EventInfo eventInfo, MQCPlanDetail dataInfo) throws CustomException
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public MQCPlanDetail getMQCPlanDetailData(String jobName, String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion)
	{
		Object[] keySet = new Object[] { jobName, processFlowName, processFlowVersion, processOperationName, processOperationVersion };

		MQCPlanDetail dataInfo = new MQCPlanDetail();

		try
		{
			dataInfo = ExtendedObjectProxy.getMQCPlanDetailService().selectByKey(false, keySet);
		}
		catch (Exception e)
		{
			dataInfo = null;
		}

		return dataInfo;
	}

	public List<MQCPlanDetail> getMQCPlanDetailDataByJobName(String jobName)
	{
		String condition = "JOBNAME = ?";
		Object[] bindSet = new Object[] { jobName };

		List<MQCPlanDetail> dataInfoList = new ArrayList<MQCPlanDetail>();

		try
		{
			dataInfoList = ExtendedObjectProxy.getMQCPlanDetailService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			dataInfoList = null;
		}

		return dataInfoList;
	}

	public List<MQCPlanDetail> MQCPlanDetailData(String jobName, String processOperationName) throws CustomException
	{
		String condition = "WHERE jobName = ? and PROCESSOPERATIONNAME = ? AND MQCReleaseFlag = ?";
		Object[] bindSet = new Object[] { jobName, processOperationName, "Y" };
		List<MQCPlanDetail> result = null;
		try
		{
			result = super.select(condition, bindSet, MQCPlanDetail.class);
		}
		catch (Exception e)
		{
			logger.info("This processOperation needn't do Release MQC");
		}
		return result;
	}

	public List<MQCPlanDetail> MQCPlanDetailData(String jobName, String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion, String mqcReleaseFlag,
			String countingFlag)
	{
		String condition = "WHERE JOBNAME = ? AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ? AND PROCESSOPERATIONNAME = ? AND PROCESSOPERATIONVERSION = ? AND MQCRELEASEFLAG = ? AND COUNTINGFLAG = ?";
		Object[] bindSet = new Object[] { jobName, processFlowName, processFlowVersion, processOperationName, processOperationVersion, mqcReleaseFlag, countingFlag };

		List<MQCPlanDetail> dataInfoList = new ArrayList<MQCPlanDetail>();

		try
		{
			dataInfoList = ExtendedObjectProxy.getMQCPlanDetailService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			dataInfoList = null;
		}

		return dataInfoList;
	}

	public List<MQCPlanDetail> MQCPlanDetailData(String jobName, String processFlowName, String processFlowVersion, String mqcReleaseFlag, String countingFlag)
	{
		String condition = "WHERE JOBNAME = ? AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ? AND MQCRELEASEFLAG = ? AND COUNTINGFLAG = ?";
		Object[] bindSet = new Object[] { jobName, processFlowName, processFlowVersion, mqcReleaseFlag, countingFlag };

		List<MQCPlanDetail> dataInfoList = new ArrayList<MQCPlanDetail>();

		try
		{
			dataInfoList = ExtendedObjectProxy.getMQCPlanDetailService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			dataInfoList = null;
		}

		return dataInfoList;
	}

	public void UpdateCarrierChangedforMQCJob(EventInfo eventInfo, Lot lotData, String jobName, String carrierName) throws CustomException
	{
		String condition = "JOBNAME = ? AND LOTNAME = ?";
		Object[] bindSet = new Object[] { jobName, lotData.getKey().getLotName() };

		List<MQCPlanDetail> dataInfoList = new ArrayList<MQCPlanDetail>();

		try
		{
			dataInfoList = ExtendedObjectProxy.getMQCPlanDetailService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			dataInfoList = null;
		}

		if (dataInfoList != null)
		{
			for (MQCPlanDetail dataInfo : dataInfoList)
			{
				dataInfo.setCarrierName(carrierName);
				ExtendedObjectProxy.getMQCPlanDetailService().modify(eventInfo, dataInfo);
			}
		}
	}

	public void UpdatePositionChangedforMQCJob(EventInfo eventInfo, Lot lotData, String jobName, String position) throws CustomException
	{
		String condition = "JOBNAME = ? AND LOTNAME = ?";
		Object[] bindSet = new Object[] { jobName, lotData.getKey().getLotName() };

		List<MQCPlanDetail> dataInfoList = new ArrayList<MQCPlanDetail>();

		try
		{
			dataInfoList = ExtendedObjectProxy.getMQCPlanDetailService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			dataInfoList = null;
		}

		if (dataInfoList != null)
		{
			for (MQCPlanDetail dataInfo : dataInfoList)
			{
				dataInfo.setPosition(position);
				ExtendedObjectProxy.getMQCPlanDetailService().modify(eventInfo, dataInfo);
			}
		}
	}

	public void deleteMQCPlanDetailByJobName(EventInfo eventInfo, String jobName) throws CustomException
	{
		List<MQCPlanDetail> dataInfoList = ExtendedObjectProxy.getMQCPlanDetailService().getMQCPlanDetailDataByJobName(jobName);

		if (dataInfoList != null)
		{
			for (MQCPlanDetail dataInfo : dataInfoList)
			{
				ExtendedObjectProxy.getMQCPlanDetailService().remove(eventInfo, dataInfo);
			}
		}
	}

	public void deleteMQCPlanDetailByFlow(EventInfo eventInfo, String jobName, String processFlowName, String processFlowVersion) throws CustomException
	{
		logger.info(" Start delete from CT_MQCPLANDETAIL for RecycleMQC");

		String condition = "JOBNAME = ? AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ?";
		Object[] bindSet = new Object[] { jobName, processFlowName, processFlowVersion };

		List<MQCPlanDetail> dataInfoList = new ArrayList<MQCPlanDetail>();

		try
		{
			dataInfoList = ExtendedObjectProxy.getMQCPlanDetailService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			dataInfoList = null;
		}

		if (dataInfoList != null)
		{
			for (MQCPlanDetail dataInfo : dataInfoList)
			{
				ExtendedObjectProxy.getMQCPlanDetailService().remove(eventInfo, dataInfo);
			}
		}

		logger.info("End MQCJob: " + jobName + " ,RecycleFlow: " + processFlowName + " delete from CT_MQCPLANDETAIL Success");
	}

	public MQCPlanDetail insertMQCPlanDetail(EventInfo eventInfo, String jobName, String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion,
			long dummyUsedLimit, String lotName, String carrierName, String position, String recipeName, String mqcReleaseFlag) throws CustomException
	{
		MQCPlanDetail planDetail = new MQCPlanDetail(jobName, processFlowName, processFlowVersion, processOperationName, processOperationVersion);
		planDetail.setProcessFlowName(processFlowName);
		planDetail.setProcessFlowVersion(processFlowVersion);
		planDetail.setProcessOperationName(processOperationName);
		planDetail.setProcessOperationVersion(processOperationVersion);
		planDetail.setDummyUsedLimit(dummyUsedLimit);
		planDetail.setLotName(lotName);
		planDetail.setCarrierName(carrierName);
		planDetail.setPosition(position);
		planDetail.setRecipeName(recipeName);
		planDetail.setLastEventComment(eventInfo.getEventComment());
		planDetail.setLastEventName(eventInfo.getEventName());
		planDetail.setLastEventTime(eventInfo.getEventTime());
		planDetail.setLastEventUser(eventInfo.getEventUser());
		planDetail.setMQCReleaseFlag(mqcReleaseFlag);

		planDetail = ExtendedObjectProxy.getMQCPlanDetailService().create(eventInfo, planDetail);

		return planDetail;
	}

	public void deleteMQCPlanDetail(EventInfo eventInfo, String jobName, String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion)
			throws CustomException
	{
		MQCPlanDetail detailPlanData = ExtendedObjectProxy.getMQCPlanDetailService().getMQCPlanDetailData(jobName, processFlowName, processFlowVersion, processOperationName, processOperationVersion);

		if (detailPlanData != null)
		{
			ExtendedObjectProxy.getMQCPlanDetailService().deleteMQCPlanDetail(eventInfo, detailPlanData);
		}

	}

	public void deleteMQCPlanDetail(EventInfo eventInfo, MQCPlanDetail detailPlanData)
	{
		try
		{
			detailPlanData.setLastEventComment(eventInfo.getEventComment());
			detailPlanData.setLastEventName(eventInfo.getEventName());
			detailPlanData.setLastEventTime(eventInfo.getEventTime());
			detailPlanData.setLastEventUser(eventInfo.getEventUser());

			ExtendedObjectProxy.getMQCPlanDetailService().remove(eventInfo, detailPlanData);
		}
		catch (Exception ex)
		{
			logger.error(String.format("DetailPlan[%s, %s] MQC plan not removed yet", detailPlanData.getProcessFlowName(), detailPlanData.getProcessOperationName()));
		}
	}

	public MQCPlanDetail insertMQCPlanDetail(EventInfo eventInfo, String jobName, String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion,
			String carrierName, String lotName, String positions, long dummyUsedLimit, String recipeName, String machineName, String mqcReleaseFlag, String countingFlag) throws CustomException
	{
		MQCPlanDetail dataInfo = new MQCPlanDetail();
		dataInfo.setJobName(jobName);
		dataInfo.setProcessFlowName(processFlowName);
		dataInfo.setProcessFlowVersion(processFlowVersion);
		dataInfo.setProcessOperationName(processOperationName);
		dataInfo.setProcessOperationVersion(processOperationVersion);
		dataInfo.setCarrierName(carrierName);
		dataInfo.setLotName(lotName);
		dataInfo.setPosition(positions);
		dataInfo.setDummyUsedLimit(dummyUsedLimit);
		dataInfo.setRecipeName(recipeName);
		dataInfo.setMachineName(machineName);
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setMQCReleaseFlag(mqcReleaseFlag);
		dataInfo.setCountingFlag(countingFlag);

		MQCPlanDetail planDetailData = ExtendedObjectProxy.getMQCPlanDetailService().create(eventInfo, dataInfo);

		return planDetailData;
	}
}
