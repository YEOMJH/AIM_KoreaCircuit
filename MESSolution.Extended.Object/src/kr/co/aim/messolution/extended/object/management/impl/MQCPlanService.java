package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.MQCPlan;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.management.data.Lot;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MQCPlanService extends CTORMService<MQCPlan> {

	public static Log logger = LogFactory.getLog(MQCPlanService.class);

	private final String historyEntity = "MQCPLANHISTORY";

	public List<MQCPlan> select(String condition, Object[] bindSet) throws CustomException
	{
		List<MQCPlan> result = super.select(condition, bindSet, MQCPlan.class);

		return result;
	}

	public MQCPlan selectByKey(boolean isLock, Object[] keySet) throws CustomException
	{
		return super.selectByKey(MQCPlan.class, isLock, keySet);
	}

	public MQCPlan create(EventInfo eventInfo, MQCPlan dataInfo) throws CustomException
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, MQCPlan dataInfo) throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public MQCPlan modify(EventInfo eventInfo, MQCPlan dataInfo) throws CustomException
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public MQCPlan getMQCPlanData(String jobName)
	{
		Object[] keySet = new Object[] { jobName };

		MQCPlan dataInfo = new MQCPlan();

		try
		{
			dataInfo = ExtendedObjectProxy.getMQCPlanService().selectByKey(false, keySet);
		}
		catch (Exception e)
		{
			dataInfo = null;
		}

		return dataInfo;
	}

	public List<MQCPlan> getMQCPlanDataByLotName(String lotName)
	{
		String condition = "LOTNAME = ?";
		Object[] bindSet = new Object[] { lotName };

		List<MQCPlan> dataInfoList = new ArrayList<MQCPlan>();

		try
		{
			dataInfoList = ExtendedObjectProxy.getMQCPlanService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			dataInfoList = null;
		}

		return dataInfoList;
	}

	public List<MQCPlan> getReleasedMQCPlanDataByLotName(String lotName)
	{
		String condition = "MQCSTATE = 'Released' AND LOTNAME = ?";
		Object[] bindSet = new Object[] { lotName };

		List<MQCPlan> dataInfoList = new ArrayList<MQCPlan>();

		try
		{
			dataInfoList = ExtendedObjectProxy.getMQCPlanService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			dataInfoList = null;
		}

		return dataInfoList;
	}

	public List<MQCPlan> getRecyclingMQCPlanDataByLotName(String lotName)
	{
		String condition = "MQCSTATE = 'Recycling' AND LOTNAME = ?";
		Object[] bindSet = new Object[] { lotName };

		List<MQCPlan> dataInfoList = new ArrayList<MQCPlan>();

		try
		{
			dataInfoList = ExtendedObjectProxy.getMQCPlanService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			dataInfoList = null;
		}

		return dataInfoList;
	}

	public List<MQCPlan> getMQCPlanDataByLotNameMultiState(String lotName)
	{
		String condition = "MQCSTATE IN ('Released', 'Recycling') AND LOTNAME = ?";
		Object[] bindSet = new Object[] { lotName };

		List<MQCPlan> dataInfoList = new ArrayList<MQCPlan>();

		try
		{
			dataInfoList = ExtendedObjectProxy.getMQCPlanService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			dataInfoList = null;
		}

		return dataInfoList;
	}
	
	public List<MQCPlan> getMQCPlanDataByLotNameMultiStateNotIn(String lotName)
	{
		String condition = "MQCSTATE IN ('Released', 'Recycling') AND LOTNAME = ?";
		Object[] bindSet = new Object[] { lotName };

		List<MQCPlan> dataInfoList = new ArrayList<MQCPlan>();

		try
		{
			dataInfoList = ExtendedObjectProxy.getMQCPlanService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			dataInfoList = null;
		}

		return dataInfoList;
	}

	public List<MQCPlan> getMQCPlanDataByJobName(String jobName)
	{
		String condition = "WHERE MQCSTATE = 'Recycling' AND JOBNAME = ?";
		Object[] bindSet = new Object[] { jobName };

		List<MQCPlan> dataInfoList = new ArrayList<MQCPlan>();

		try
		{
			dataInfoList = ExtendedObjectProxy.getMQCPlanService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			dataInfoList = null;
		}

		return dataInfoList;
	}

	public MQCPlan makeWaiting(EventInfo eventInfo, Lot lotData) throws CustomException
	{
		logger.info("MQCPlanService.makeWaiting begin");

		List<MQCPlan> jobList;

		try
		{
			jobList = ExtendedObjectProxy.getMQCPlanService().select("lotName = ? AND MQCstate = ?", new Object[] { lotData.getKey().getLotName(), "Recycling" });
		}
		catch (greenFrameDBErrorSignal de)
		{
			if (de.getErrorCode().equals("NotFoundSignal"))
				throw new NotFoundSignal(de.getDataKey(), de.getSql());
			else
				throw new CustomException("SYS-8001", de.getSql());
		}

		// recycling Lot is only one
		MQCPlan planData = jobList.get(0);

		planData.setMQCState("Suspending");
		planData.setReturnFlowName("");
		planData.setReturnOperationName("");

		// refresh plan state
		planData.setLastEventComment(eventInfo.getEventComment());
		planData.setLastEventName(eventInfo.getEventName());
		planData.setLastEventTime(eventInfo.getEventTime());
		planData.setLastEventUser(eventInfo.getEventUser());

		eventInfo.setEventName("Suspend");

		try
		{
			planData = ExtendedObjectProxy.getMQCPlanService().modify(eventInfo, planData);
		}
		catch (greenFrameDBErrorSignal de)
		{
			throw new CustomException("SYS-8001", de.getSql());
		}
		catch (Exception ex)
		{
			throw new CustomException("SYS-9999", "MQC", ex.getMessage());
		}

		logger.info("MQCPlanService.makeWaiting OK");

		return planData;
	}

	public List<MQCPlan> MQCPlanDataByLot(String lotName) throws CustomException
	{
		String condition = "WHERE LOTNAME = ? AND MQCSTATE = ?";
		Object[] bindSet = new Object[] { lotName, "Released" };

		List<MQCPlan> result = null;

		try
		{
			result = super.select(condition, bindSet, MQCPlan.class);
		}
		catch (Exception e)
		{
			logger.info("This lot isn't exist MQCPlan");
		}

		return result;
	}

	public MQCPlan insertMQCPlan(EventInfo eventInfo, String jobName, long seq, String factoryName, String productSpecName, String productSpecVersion, String processFlowName,
			String processFlowVersion, String lotName, String department, String recycleFlowName, String recycleFlowVersion, String prepareProductSpecName, String prepareProductSpecVersion,
			long recycleLimit) throws CustomException
	{
		MQCPlan planData = new MQCPlan(jobName);
		planData.setSeq(seq);
		planData.setFactoryName(factoryName);
		planData.setProductSpecName(productSpecName);
		planData.setProductSpecVersion(productSpecVersion);
		planData.setProcessFlowName(processFlowName);
		planData.setProcessFlowVersion(processFlowVersion);
		planData.setLotName(lotName);
		planData.setMQCState("Created");
		planData.setLastEventComment(eventInfo.getEventComment());
		planData.setLastEventName(eventInfo.getEventName());
		planData.setLastEventTime(eventInfo.getEventTime());
		planData.setLastEventUser(eventInfo.getEventUser());
		planData.setCreateUser(eventInfo.getEventUser());
		planData.setDepartment(department);
		planData.setRecycleFlowName(recycleFlowName);
		planData.setRecycleFlowVersion(recycleFlowVersion);
		planData.setPrepareSpecName(prepareProductSpecName);
		planData.setPrepareSpecVersion(prepareProductSpecVersion);
		planData.setRecycleLimit(recycleLimit);

		planData = ExtendedObjectProxy.getMQCPlanService().create(eventInfo, planData);

		return planData;
	}

	public MQCPlan insertMQCPlan(EventInfo eventInfo, MQCPlan planData, String jobName, long seq, String lotName, String mqcState) throws CustomException
	{
		planData.setJobName(jobName);
		planData.setSeq(seq);
		planData.setLotName(lotName);
		planData.setMQCState(mqcState);
		planData.setLastEventTime(eventInfo.getEventTime());
		planData.setLastEventName(eventInfo.getEventName());
		planData.setLastEventUser(eventInfo.getEventUser());
		planData.setLastEventComment(eventInfo.getEventComment());
		planData.setCreateUser(eventInfo.getEventUser());
		planData = ExtendedObjectProxy.getMQCPlanService().create(eventInfo, planData);

		return planData;
	}

	public MQCPlan updateMQCPlanReturnInfo(EventInfo eventInfo, MQCPlan planData, String mqcState, String returnFlowName, String returnFlowVersion, String returnOperationName,
			String returnOperationVersion) throws CustomException
	{
		planData.setMQCState(mqcState);
		planData.setReturnFlowName(returnFlowName);
		planData.setReturnFlowVersion(returnFlowVersion);
		planData.setReturnOperationName(returnOperationName);
		planData.setReturnOperationVersion(returnOperationVersion);
		planData.setLastEventComment(eventInfo.getEventComment());
		planData.setLastEventName(eventInfo.getEventName());
		planData.setLastEventTime(eventInfo.getEventTime());
		planData.setLastEventUser(eventInfo.getEventUser());

		planData = ExtendedObjectProxy.getMQCPlanService().modify(eventInfo, planData);

		return planData;
	}

	public MQCPlan updateMQCPlanReturnOper(EventInfo eventInfo, MQCPlan planData, String mqcState, String returnOperationName, String returnOperationVersion) throws CustomException
	{
		planData.setMQCState(mqcState);
		planData.setReturnOperationName(returnOperationName);
		planData.setReturnOperationVersion(returnOperationVersion);
		planData.setLastEventComment(eventInfo.getEventComment());
		planData.setLastEventName(eventInfo.getEventName());
		planData.setLastEventTime(eventInfo.getEventTime());
		planData.setLastEventUser(eventInfo.getEventUser());

		planData = ExtendedObjectProxy.getMQCPlanService().modify(eventInfo, planData);

		return planData;
	}

	public MQCPlan updateMQCPlanCount(EventInfo eventInfo, MQCPlan planData, String mqcState) throws CustomException
	{
		planData.setMQCState(mqcState);

		planData.setRecycleCount(planData.getRecycleCount() + 1);
		planData.setRecycleFlowName("");
		planData.setRecycleFlowVersion("");

		planData.setLastEventComment(eventInfo.getEventComment());
		planData.setLastEventName(eventInfo.getEventName());
		planData.setLastEventTime(eventInfo.getEventTime());
		planData.setLastEventUser(eventInfo.getEventUser());

		planData = ExtendedObjectProxy.getMQCPlanService().modify(eventInfo, planData);

		return planData;
	}

	public void deleteMQCPlanData(EventInfo eventInfo, MQCPlan planData) throws CustomException
	{
		planData.setLastEventComment(eventInfo.getEventComment());
		planData.setLastEventName(eventInfo.getEventName());
		planData.setLastEventTime(eventInfo.getEventTime());
		planData.setLastEventUser(eventInfo.getEventUser());

		ExtendedObjectProxy.getMQCPlanService().remove(eventInfo, planData);
	}

	public void getMQCPlanData(Lot lotData)
	{
		String condition = "WHERE LOTNAME = ? AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ? AND PROCESSOPERATIONNAME = ? AND PROCESSOPERATIONVERSION = ?";
	}
}
