package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.SampleLot;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;

public class SampleLotService extends CTORMService<SampleLot> {

	private static Log log = LogFactory.getLog(SampleLotService.class);
	public static Log logger = LogFactory.getLog(SampleLotService.class);

	private final String historyEntity = "SampleLotHist";

	public List<SampleLot> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<SampleLot> result = super.select(condition, bindSet, SampleLot.class);

		return result;
	}

	public SampleLot selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(SampleLot.class, isLock, keySet);
	}

	public SampleLot create(EventInfo eventInfo, SampleLot dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, SampleLot dataInfo) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public SampleLot modify(EventInfo eventInfo, SampleLot dataInfo)
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public SampleLot insertSampleLot(EventInfo eventInfo, String lotName, String factoryName, String productSpecName, String productSpecVersion, String processFlowName, String processFlowVersion,
			String processOperationName, String processOperationVersion, String machineName, String toProcessFlowName, String toProcessFlowVersion, String toProcessOperationName,
			String toProcessOperationVersion, String lotSampleFlag, String lotSampleCount, String currentLotCount, String totalLotCount, String productSampleCount, String productSamplePosition,
			String actualProductCount, String actualSamplePosition, String manualSampleFlag, String lotGrade, int priority, String returnProcessFlowName, String returnProcessFlowVersion,
			String returnOperationName, String returnOperationVersion, String forceSamplingFlag) throws greenFrameDBErrorSignal, CustomException
	{
		log.info("Start InsertSampleLot - lotName : " + lotName + ", FromInfo : " + processFlowName + "/" + processOperationName + ", ToInfo : " + toProcessFlowName + "/" + toProcessOperationName);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		SampleLot dataInfo = new SampleLot();
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
		dataInfo.setLotSampleFlag(lotSampleFlag);
		dataInfo.setLotSampleCount(lotSampleCount);
		dataInfo.setCurrentLotCount(currentLotCount);
		dataInfo.setTotalLotCount(totalLotCount);
		dataInfo.setProductSampleCount(productSampleCount);
		dataInfo.setProductSamplePosition(productSamplePosition);
		dataInfo.setActualProductCount(actualProductCount);
		dataInfo.setActualSamplePosition(actualSamplePosition);
		dataInfo.setManualSampleFlag(manualSampleFlag);
		dataInfo.setLotGrade(lotGrade);
		dataInfo.setPriority(priority);
		dataInfo.setReturnProcessFlowName(returnProcessFlowName);
		dataInfo.setReturnProcessFlowVersion(returnProcessFlowVersion);
		dataInfo.setReturnOperationName(returnOperationName);
		dataInfo.setReturnOperationVersion(returnOperationVersion);
		dataInfo.setForceSamplingFlag(forceSamplingFlag);
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setLastEventTime(eventInfo.getEventTime());

		SampleLot sampleLotData = ExtendedObjectProxy.getSampleLotService().create(eventInfo, dataInfo);

		log.info("End InsertSampleLotData");

		return sampleLotData;
	}

	public SampleLot insertSampleLot(EventInfo eventInfo, String lotName, String factoryName, String productSpecName, String productSpecVersion, String processFlowName, String processFlowVersion,
			String processOperationName, String processOperationVersion, String machineName, String toProcessFlowName, String toProcessFlowVersion, String toProcessOperationName,
			String toProcessOperationVersion, String lotSampleFlag, String lotSampleCount, String currentLotCount, String totalLotCount, String productSampleCount, String productSamplePosition,
			String actualProductCount, String actualSamplePosition, String manualSampleFlag, String lotGrade, int priority, String returnProcessFlowName, String returnProcessFlowVersion,
			String returnOperationName, String returnOperationVersion, String forceSamplingFlag, String completeFlowName, String completeFlowVersion, String completeOperationName,
			String completeOperationVersion) throws greenFrameDBErrorSignal, CustomException
	{
		log.info("Start InsertSampleLot - lotName : " + lotName + ", FromInfo : " + processFlowName + "/" + processOperationName + ", ToInfo : " + toProcessFlowName + "/" + toProcessOperationName);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		SampleLot dataInfo = new SampleLot();
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
		dataInfo.setLotSampleFlag(lotSampleFlag);
		dataInfo.setLotSampleCount(lotSampleCount);
		dataInfo.setCurrentLotCount(currentLotCount);
		dataInfo.setTotalLotCount(totalLotCount);
		dataInfo.setProductSampleCount(productSampleCount);
		dataInfo.setProductSamplePosition(productSamplePosition);
		dataInfo.setActualProductCount(actualProductCount);
		dataInfo.setActualSamplePosition(actualSamplePosition);
		dataInfo.setManualSampleFlag(manualSampleFlag);
		dataInfo.setLotGrade(lotGrade);
		dataInfo.setPriority(priority);
		dataInfo.setReturnProcessFlowName(returnProcessFlowName);
		dataInfo.setReturnProcessFlowVersion(returnProcessFlowVersion);
		dataInfo.setReturnOperationName(returnOperationName);
		dataInfo.setReturnOperationVersion(returnOperationVersion);
		dataInfo.setForceSamplingFlag(forceSamplingFlag);
		dataInfo.setCompleteFlowName(completeFlowName);
		dataInfo.setCompleteFlowVersion(completeFlowVersion);
		dataInfo.setCompleteOperationName(completeOperationName);
		dataInfo.setCompleteOperationVersion(completeOperationVersion);
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setLastEventTime(eventInfo.getEventTime());

		SampleLot sampleLotData = ExtendedObjectProxy.getSampleLotService().create(eventInfo, dataInfo);

		log.info("End InsertSampleLotData");

		return sampleLotData;
	}
	
	public SampleLot insertSampleLotForBackUp(EventInfo eventInfo, String lotName, String factoryName, String productSpecName, String productSpecVersion, String processFlowName, String processFlowVersion,
			String processOperationName, String processOperationVersion, String machineName, String toProcessFlowName, String toProcessFlowVersion, String toProcessOperationName,
			String toProcessOperationVersion, String lotSampleFlag, String lotSampleCount, String currentLotCount, String totalLotCount, String productSampleCount, String productSamplePosition,
			String actualProductCount, String actualSamplePosition, String manualSampleFlag, String lotGrade, int priority, String returnProcessFlowName, String returnProcessFlowVersion,
			String returnOperationName, String returnOperationVersion, String forceSamplingFlag, String backupSamplingFlag) throws greenFrameDBErrorSignal, CustomException
	{
		log.info("Start InsertSampleLot - lotName : " + lotName + ", FromInfo : " + processFlowName + "/" + processOperationName + ", ToInfo : " + toProcessFlowName + "/" + toProcessOperationName);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		SampleLot dataInfo = new SampleLot();
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
		dataInfo.setLotSampleFlag(lotSampleFlag);
		dataInfo.setLotSampleCount(lotSampleCount);
		dataInfo.setCurrentLotCount(currentLotCount);
		dataInfo.setTotalLotCount(totalLotCount);
		dataInfo.setProductSampleCount(productSampleCount);
		dataInfo.setProductSamplePosition(productSamplePosition);
		dataInfo.setActualProductCount(actualProductCount);
		dataInfo.setActualSamplePosition(actualSamplePosition);
		dataInfo.setManualSampleFlag(manualSampleFlag);
		dataInfo.setLotGrade(lotGrade);
		dataInfo.setPriority(priority);
		dataInfo.setReturnProcessFlowName(returnProcessFlowName);
		dataInfo.setReturnProcessFlowVersion(returnProcessFlowVersion);
		dataInfo.setReturnOperationName(returnOperationName);
		dataInfo.setReturnOperationVersion(returnOperationVersion);
		dataInfo.setForceSamplingFlag(forceSamplingFlag);
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setBackupSamplingFlag(backupSamplingFlag);

		SampleLot sampleLotData = ExtendedObjectProxy.getSampleLotService().create(eventInfo, dataInfo);

		log.info("End InsertSampleLotData");

		return sampleLotData;
	}

	public SampleLot insertReserveSampleLot(EventInfo eventInfo, String lotName, String factoryName, String productSpecName, String productSpecVersion, String processFlowName,
			String processFlowVersion, String processOperationName, String processOperationVersion, String machineName, String toProcessFlowName, String toProcessFlowVersion,
			String toProcessOperationName, String toProcessOperationVersion, String lotSampleFlag, String lotSampleCount, String currentLotCount, String totalLotCount, String productSampleCount,
			String productSamplePosition, String actualProductCount, String actualSamplePosition, String manualSampleFlag, String lotGrade, int priority, String returnProcessFlowName,
			String returnProcessFlowVersion, String returnOperationName, String returnOperationVersion, String forceSamplingFlag) throws greenFrameDBErrorSignal, CustomException
	{
		log.info("Start InsertSampleLot - lotName : " + lotName + ", FromInfo : " + processFlowName + "/" + processOperationName + ", ToInfo : " + toProcessFlowName + "/" + toProcessOperationName);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		SampleLot dataInfo = new SampleLot();
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
		dataInfo.setLotSampleFlag(lotSampleFlag);
		dataInfo.setLotSampleCount(lotSampleCount);
		dataInfo.setCurrentLotCount(currentLotCount);
		dataInfo.setTotalLotCount(totalLotCount);
		dataInfo.setProductSampleCount(productSampleCount);
		dataInfo.setProductSamplePosition(productSamplePosition);
		dataInfo.setActualProductCount(actualProductCount);
		dataInfo.setActualSamplePosition(actualSamplePosition);
		dataInfo.setManualSampleFlag(manualSampleFlag);
		dataInfo.setLotGrade(lotGrade);
		dataInfo.setPriority(priority);
		dataInfo.setReturnProcessFlowName(returnProcessFlowName);
		dataInfo.setReturnProcessFlowVersion(returnProcessFlowVersion);
		dataInfo.setReturnOperationName(returnOperationName);
		dataInfo.setReturnOperationVersion(returnOperationVersion);
		dataInfo.setForceSamplingFlag(forceSamplingFlag);
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setLastEventTime(eventInfo.getEventTime());

		SampleLot sampleLotData = ExtendedObjectProxy.getSampleLotService().create(eventInfo, dataInfo);

		// MantisID : 0000022
		// Add Lot history
		EventInfo lotEventInfo = EventInfoUtil.makeEventInfo("ReserveSample", eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);
		lotEventInfo.setBehaviorName("L");
		lotEventInfo.setEventTimeKey(eventInfo.getEventTimeKey());

		LotServiceProxy.getLotService().setEvent(new LotKey(lotName), lotEventInfo, new SetEventInfo());

		log.info("End InsertSampleLotData");

		return sampleLotData;
	}

	public SampleLot insertForceSampleLot(EventInfo eventInfo, String lotName, String factoryName, String productSpecName, String productSpecVersion, String processFlowName,
			String processFlowVersion, String processOperationName, String processOperationVersion, String machineName, String toProcessFlowName, String toProcessFlowVersion,
			String toProcessOperationName, String toProcessOperationVersion, String lotSampleFlag, String lotSampleCount, String currentLotCount, String totalLotCount, String productSampleCount,
			String productSamplePosition, String actualProductCount, String actualSamplePosition, String manualSampleFlag, String lotGrade, int priority, String returnProcessFlowName,
			String returnProcessFlowVersion, String returnOperationName, String returnOperationVersion, String forceSamplingFlag, String completeFlowName, String completeFlowVersion,
			String completeOperationName, String completeOperationVersion) throws greenFrameDBErrorSignal, CustomException
	{
		log.info("Start InsertSampleLot - lotName : " + lotName + ", FromInfo : " + processFlowName + "/" + processOperationName + ", ToInfo : " + toProcessFlowName + "/" + toProcessOperationName);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		SampleLot dataInfo = new SampleLot();
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
		dataInfo.setLotSampleFlag(lotSampleFlag);
		dataInfo.setLotSampleCount(lotSampleCount);
		dataInfo.setCurrentLotCount(currentLotCount);
		dataInfo.setTotalLotCount(totalLotCount);
		dataInfo.setProductSampleCount(productSampleCount);
		dataInfo.setProductSamplePosition(productSamplePosition);
		dataInfo.setActualProductCount(actualProductCount);
		dataInfo.setActualSamplePosition(actualSamplePosition);
		dataInfo.setManualSampleFlag(manualSampleFlag);
		dataInfo.setLotGrade(lotGrade);
		dataInfo.setPriority(priority);
		dataInfo.setReturnProcessFlowName(returnProcessFlowName);
		dataInfo.setReturnProcessFlowVersion(returnProcessFlowVersion);
		dataInfo.setReturnOperationName(returnOperationName);
		dataInfo.setReturnOperationVersion(returnOperationVersion);
		dataInfo.setForceSamplingFlag(forceSamplingFlag);
		dataInfo.setCompleteFlowName(completeFlowName);
		dataInfo.setCompleteFlowVersion(completeFlowVersion);
		dataInfo.setCompleteOperationName(completeOperationName);
		dataInfo.setCompleteOperationVersion(completeOperationVersion);
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setLastEventTime(eventInfo.getEventTime());

		SampleLot sampleLotData = ExtendedObjectProxy.getSampleLotService().create(eventInfo, dataInfo);

		log.info("End InsertSampleLotData");

		return sampleLotData;
	}

	public List<SampleLot> updateSampleLotWithoutMachineName(EventInfo eventInfo, String lotName, String factoryName, String productSpecName, String productSpecVersion, String processFlowName,
			String processFlowVersion, String processOperationName, String processOperationVersion, String toProcessFlowName, String toProcessFlowVersion, String toProcessOperationName,
			String toProcessOperationVersion, String lotSampleFlag, String lotSampleCount, String currentLotCount, String totalLotCount, String productSampleCount, String productSamplePosition,
			String actualProductCount, String actualSamplePosition, String manualSampleFlag, String lotGrade, String priority, String returnProcessFlowName, String returnProcessFlowVersion,
			String returnOperationName, String returnOperationVersion, String forceSamplingFlag) throws greenFrameDBErrorSignal, CustomException
	{
		List<SampleLot> sampleLotList = ExtendedObjectProxy.getSampleLotService().getSampleLotDataListWithOutMachineName(lotName, factoryName, productSpecName, productSpecVersion, processFlowName,
				processFlowVersion, processOperationName, processOperationVersion, toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion);

		List<SampleLot> returnList = new ArrayList<SampleLot>();

		if (sampleLotList != null)
		{
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			for (SampleLot dataInfo : sampleLotList)
			{
				if (StringUtils.isNotEmpty(lotSampleFlag))
					dataInfo.setLotSampleFlag(lotSampleFlag);

				if (StringUtils.isNotEmpty(lotSampleCount))
					dataInfo.setLotSampleCount(lotSampleCount);

				if (StringUtils.isNotEmpty(currentLotCount))
					dataInfo.setCurrentLotCount(currentLotCount);

				if (StringUtils.isNotEmpty(totalLotCount))
					dataInfo.setTotalLotCount(totalLotCount);

				if (StringUtils.isNotEmpty(productSampleCount))
					dataInfo.setProductSampleCount(productSampleCount);

				if (StringUtils.isNotEmpty(productSamplePosition))
					dataInfo.setProductSamplePosition(productSamplePosition);

				if (StringUtils.isNotEmpty(actualProductCount))
					dataInfo.setActualProductCount(actualProductCount);

				if (StringUtils.isNotEmpty(actualSamplePosition))
					dataInfo.setActualSamplePosition(actualSamplePosition);

				if (StringUtils.isNotEmpty(manualSampleFlag))
					dataInfo.setManualSampleFlag(manualSampleFlag);

				if (StringUtils.isNotEmpty(lotGrade))
					dataInfo.setLotGrade(lotGrade);

				if (StringUtils.isNotEmpty(priority))
					dataInfo.setPriority(Integer.parseInt(priority));

				if (StringUtils.isNotEmpty(returnProcessFlowName))
					dataInfo.setReturnProcessFlowName(returnProcessFlowName);

				if (StringUtils.isNotEmpty(returnProcessFlowVersion))
					dataInfo.setReturnProcessFlowVersion(returnProcessFlowVersion);

				if (StringUtils.isNotEmpty(returnOperationName))
					dataInfo.setReturnOperationName(returnOperationName);

				if (StringUtils.isNotEmpty(returnOperationVersion))
					dataInfo.setReturnOperationVersion(returnOperationVersion);

				if (StringUtils.isNotEmpty(forceSamplingFlag))
					dataInfo.setForceSamplingFlag(forceSamplingFlag);

				dataInfo.setLastEventUser(eventInfo.getEventUser());
				dataInfo.setLastEventName(eventInfo.getEventName());
				dataInfo.setLastEventComment(eventInfo.getEventComment());
				dataInfo.setLastEventTime(eventInfo.getEventTime());

				SampleLot sampleLotData = ExtendedObjectProxy.getSampleLotService().modify(eventInfo, dataInfo);

				returnList.add(sampleLotData);
			}
		}

		return returnList;
	}

	public SampleLot updateSampleLot(EventInfo eventInfo, String lotName, String factoryName, String productSpecName, String productSpecVersion, String processFlowName, String processFlowVersion,
			String processOperationName, String processOperationVersion, String machineName, String toProcessFlowName, String toProcessFlowVersion, String toProcessOperationName,
			String toProcessOperationVersion, String lotSampleFlag, String lotSampleCount, String currentLotCount, String totalLotCount, String productSampleCount, String productSamplePosition,
			String actualProductCount, String actualSamplePosition, String manualSampleFlag, String lotGrade, String priority, String returnProcessFlowName, String returnProcessFlowVersion,
			String returnOperationName, String returnOperationVersion, String forceSamplingFlag) throws greenFrameDBErrorSignal, CustomException
	{
		log.info("Start UpdateSampleLot - lotName : " + lotName + ", FromInfo : " + processFlowName + "/" + processOperationName + ", ToInfo : " + toProcessFlowName + "/" + toProcessOperationName);

		SampleLot dataInfo = ExtendedObjectProxy.getSampleLotService().getSampleLotData(lotName, factoryName, productSpecName, productSpecVersion, processFlowName, processFlowVersion,
				processOperationName, processOperationVersion, machineName, toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion);

		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		SampleLot sampleLotData = new SampleLot();

		if (dataInfo != null)
		{
			if (StringUtils.isNotEmpty(lotSampleFlag))
				dataInfo.setLotSampleFlag(lotSampleFlag);

			if (StringUtils.isNotEmpty(lotSampleCount))
				dataInfo.setLotSampleCount(lotSampleCount);

			if (StringUtils.isNotEmpty(currentLotCount))
				dataInfo.setCurrentLotCount(currentLotCount);

			if (StringUtils.isNotEmpty(totalLotCount))
				dataInfo.setTotalLotCount(totalLotCount);

			if (StringUtils.isNotEmpty(productSampleCount))
				dataInfo.setProductSampleCount(productSampleCount);

			if (StringUtils.isNotEmpty(productSamplePosition))
				dataInfo.setProductSamplePosition(productSamplePosition);

			if (StringUtils.isNotEmpty(actualProductCount))
				dataInfo.setActualProductCount(actualProductCount);

			if (StringUtils.isNotEmpty(actualSamplePosition))
				dataInfo.setActualSamplePosition(actualSamplePosition);

			if (StringUtils.isNotEmpty(manualSampleFlag))
				dataInfo.setManualSampleFlag(manualSampleFlag);

			if (StringUtils.isNotEmpty(lotGrade))
				dataInfo.setLotGrade(lotGrade);

			if (StringUtils.isNotEmpty(priority))
				dataInfo.setPriority(Integer.parseInt(priority));

			if (StringUtils.isNotEmpty(returnProcessFlowName))
				dataInfo.setReturnProcessFlowName(returnProcessFlowName);

			if (StringUtils.isNotEmpty(returnProcessFlowVersion))
				dataInfo.setReturnProcessFlowVersion(returnProcessFlowVersion);

			if (StringUtils.isNotEmpty(returnOperationName))
				dataInfo.setReturnOperationName(returnOperationName);

			if (StringUtils.isNotEmpty(returnOperationVersion))
				dataInfo.setReturnOperationVersion(returnOperationVersion);

			if (StringUtils.isNotEmpty(forceSamplingFlag))
				dataInfo.setForceSamplingFlag(forceSamplingFlag);

			dataInfo.setLastEventUser(eventInfo.getEventUser());
			dataInfo.setLastEventName(eventInfo.getEventName());
			dataInfo.setLastEventComment(eventInfo.getEventComment());
			dataInfo.setLastEventTime(eventInfo.getEventTime());

			sampleLotData = ExtendedObjectProxy.getSampleLotService().modify(eventInfo, dataInfo);
		}
		else
		{
			sampleLotData = null;
		}

		log.info("End UpdateSampleLot");

		return sampleLotData;
	}

	public List<SampleLot> updateSampleLotWithoutOperation(EventInfo eventInfo, String lotName, String factoryName, String productSpecName, String productSpecVersion, String processFlowName,
			String processFlowVersion, String toProcessFlowName, String toProcessFlowVersion, String lotSampleFlag, String lotSampleCount, String currentLotCount, String totalLotCount,
			String productSampleCount, String productSamplePosition, String actualProductCount, String actualSamplePosition, String manualSampleFlag, String lotGrade, String priority,
			String returnProcessFlowName, String returnProcessFlowVersion, String returnOperationName, String returnOperationVersion, String forceSamplingFlag) throws greenFrameDBErrorSignal,
			CustomException
	{
		log.info("Start UpdateSampleLot - lotName : " + lotName + ", FromInfo : " + processFlowName + ", ToInfo : " + toProcessFlowName);

		String condition = "LOTNAME = ? AND FACTORYNAME = ? AND PRODUCTSPECNAME = ? AND PRODUCTSPECVERSION = ? AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ? AND TOPROCESSFLOWNAME = ? AND TOPROCESSFLOWVERSION = ?";
		Object[] bindSet = new Object[] { lotName, factoryName, productSpecName, productSpecVersion, processFlowName, processFlowVersion, toProcessFlowName, toProcessFlowVersion };

		List<SampleLot> sampleLotList = new ArrayList<SampleLot>();

		try
		{
			sampleLotList = ExtendedObjectProxy.getSampleLotService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			sampleLotList = null;
		}

		if (sampleLotList != null)
		{
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			for (SampleLot dataInfo : sampleLotList)
			{
				if (StringUtils.isNotEmpty(lotSampleFlag))
					dataInfo.setLotSampleFlag(lotSampleFlag);

				if (StringUtils.isNotEmpty(lotSampleCount))
					dataInfo.setLotSampleCount(lotSampleCount);

				if (StringUtils.isNotEmpty(currentLotCount))
					dataInfo.setCurrentLotCount(currentLotCount);

				if (StringUtils.isNotEmpty(totalLotCount))
					dataInfo.setTotalLotCount(totalLotCount);

				if (StringUtils.isNotEmpty(productSampleCount))
					dataInfo.setProductSampleCount(productSampleCount);

				if (StringUtils.isNotEmpty(productSamplePosition))
					dataInfo.setProductSamplePosition(productSamplePosition);

				if (StringUtils.isNotEmpty(actualProductCount))
					dataInfo.setActualProductCount(actualProductCount);

				if (StringUtils.isNotEmpty(actualSamplePosition))
					dataInfo.setActualSamplePosition(actualSamplePosition);

				if (StringUtils.isNotEmpty(manualSampleFlag))
					dataInfo.setManualSampleFlag(manualSampleFlag);

				if (StringUtils.isNotEmpty(lotGrade))
					dataInfo.setLotGrade(lotGrade);

				if (StringUtils.isNotEmpty(priority))
					dataInfo.setPriority(Integer.parseInt(priority));

				if (StringUtils.isNotEmpty(returnProcessFlowName))
					dataInfo.setReturnProcessFlowName(returnProcessFlowName);

				if (StringUtils.isNotEmpty(returnProcessFlowVersion))
					dataInfo.setReturnProcessFlowVersion(returnProcessFlowVersion);

				if (StringUtils.isNotEmpty(returnOperationName))
					dataInfo.setReturnOperationName(returnOperationName);

				if (StringUtils.isNotEmpty(returnOperationVersion))
					dataInfo.setReturnOperationVersion(returnOperationVersion);

				if (StringUtils.isNotEmpty(forceSamplingFlag))
					dataInfo.setForceSamplingFlag(forceSamplingFlag);

				dataInfo.setLastEventUser(eventInfo.getEventUser());
				dataInfo.setLastEventName(eventInfo.getEventName());
				dataInfo.setLastEventComment(eventInfo.getEventComment());
				dataInfo.setLastEventTime(eventInfo.getEventTime());

				ExtendedObjectProxy.getSampleLotService().modify(eventInfo, dataInfo);
			}
		}

		log.info("End UpdateSampleLot");

		return sampleLotList;
	}

	public void updatePriority(EventInfo eventInfo, String lotName, String processFlowName, String processFlowVersion, String priority) throws CustomException
	{
		eventInfo.setEventName("ChangeSamplePriority");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		String condition = "LOTNAME = ? AND TOPROCESSFLOWNAME = ? AND TOPROCESSFLOWVERSION = ?";
		Object[] bindSet = new Object[] { lotName, processFlowName, processFlowVersion };

		List<SampleLot> sampleLotList = new ArrayList<SampleLot>();

		try
		{
			sampleLotList = ExtendedObjectProxy.getSampleLotService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			sampleLotList = null;
		}

		if (sampleLotList != null)
		{
			for (SampleLot sampleLot : sampleLotList)
			{
				sampleLot.setPriority(Integer.parseInt(priority));
				ExtendedObjectProxy.getSampleLotService().modify(eventInfo, sampleLot);
			}
		}
	}

	public List<SampleLot> updateSampleLotReturnList(EventInfo eventInfo, String lotName, String factoryName, String productSpecName, String productSpecVersion, String processFlowName,
			String processFlowVersion, String processOperationName, String processOperationVersion, String machineName, String toProcessFlowName, String toProcessFlowVersion,
			String toProcessOperationName, String toProcessOperationVersion, String lotSampleFlag, String lotSampleCount, String currentLotCount, String totalLotCount, String productSampleCount,
			String productSamplePosition, String actualProductCount, String actualSamplePosition, String manualSampleFlag, String lotGrade, String priority, String returnProcessFlowName,
			String returnProcessFlowVersion, String returnOperationName, String returnOperationVersion, String forceSamplingFlag) throws greenFrameDBErrorSignal, CustomException
	{
		log.info("Start InsertSampleLot - lotName : " + lotName + ", FromInfo : " + processFlowName + "/" + processOperationName + ", ToInfo : " + toProcessFlowName + "/" + toProcessOperationName);

		SampleLot dataInfo = ExtendedObjectProxy.getSampleLotService().getSampleLotData(lotName, factoryName, productSpecName, productSpecVersion, processFlowName, processFlowVersion,
				processOperationName, processOperationVersion, machineName, toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion);

		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		SampleLot sampleLotData = new SampleLot();

		if (dataInfo != null)
		{

			if (StringUtils.isNotEmpty(lotSampleFlag))
				dataInfo.setLotSampleFlag(lotSampleFlag);

			if (StringUtils.isNotEmpty(lotSampleCount))
				dataInfo.setLotSampleCount(lotSampleCount);

			if (StringUtils.isNotEmpty(currentLotCount))
				dataInfo.setCurrentLotCount(currentLotCount);

			if (StringUtils.isNotEmpty(totalLotCount))
				dataInfo.setTotalLotCount(totalLotCount);

			if (StringUtils.isNotEmpty(productSampleCount))
				dataInfo.setProductSampleCount(productSampleCount);

			if (StringUtils.isNotEmpty(productSamplePosition))
				dataInfo.setProductSamplePosition(productSamplePosition);

			if (StringUtils.isNotEmpty(actualProductCount))
				dataInfo.setActualProductCount(actualProductCount);

			if (StringUtils.isNotEmpty(actualSamplePosition))
				dataInfo.setActualSamplePosition(actualSamplePosition);

			if (StringUtils.isNotEmpty(manualSampleFlag))
				dataInfo.setManualSampleFlag(manualSampleFlag);

			if (StringUtils.isNotEmpty(lotGrade))
				dataInfo.setLotGrade(lotGrade);

			if (StringUtils.isNotEmpty(priority))
				dataInfo.setPriority(Integer.parseInt(priority));

			if (StringUtils.isNotEmpty(returnProcessFlowName))
				dataInfo.setReturnProcessFlowName(returnProcessFlowName);

			if (StringUtils.isNotEmpty(returnProcessFlowVersion))
				dataInfo.setReturnProcessFlowVersion(returnProcessFlowVersion);

			if (StringUtils.isNotEmpty(returnOperationName))
				dataInfo.setReturnOperationName(returnOperationName);

			if (StringUtils.isNotEmpty(returnOperationVersion))
				dataInfo.setReturnOperationVersion(returnOperationVersion);

			if (StringUtils.isNotEmpty(forceSamplingFlag))
				dataInfo.setForceSamplingFlag(forceSamplingFlag);

			dataInfo.setLastEventUser(eventInfo.getEventUser());
			dataInfo.setLastEventName(eventInfo.getEventName());
			dataInfo.setLastEventComment(eventInfo.getEventComment());
			dataInfo.setLastEventTime(eventInfo.getEventTime());

			sampleLotData = ExtendedObjectProxy.getSampleLotService().modify(eventInfo, dataInfo);
		}
		else
		{
			sampleLotData = null;
		}

		List<SampleLot> updateSampleLotList = new ArrayList<SampleLot>();

		if (sampleLotData != null)
		{
			updateSampleLotList.add(sampleLotData);
		}
		else
		{
			updateSampleLotList = null;
		}

		log.info("End UpdateSampleLot");

		return updateSampleLotList;
	}

	public SampleLot getSampleLotData(String lotName, String factoryName, String productSpecName, String productSpecVersion, String processFlowName, String processFlowVersion,
			String processOperationName, String processOperationVersion, String machineName, String toProcessFlowName, String toProcessFlowVersion, String toProcessOperationName,
			String toProcessOperationVersion) throws greenFrameDBErrorSignal, CustomException
	{

		log.info("GetSampleLot - lotName : " + lotName + ", FromInfo : " + processFlowName + "/" + processOperationName + ", ToInfo : " + toProcessFlowName + "/" + toProcessOperationName);

		Object[] keySet = new Object[] { lotName, factoryName, productSpecName, productSpecVersion, processFlowName, processFlowVersion, processOperationName, processOperationVersion, machineName,
				toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion };

		SampleLot sampleLotData = new SampleLot();

		try
		{
			sampleLotData = ExtendedObjectProxy.getSampleLotService().selectByKey(false, keySet);
		}
		catch (Exception e)
		{
			sampleLotData = null;
		}

		log.info("End GetSampleLot");

		return sampleLotData;
	}

	public List<SampleLot> getSampleLotDataList(String lotName, String factoryName, String productSpecName, String productSpecVersion, String processFlowName, String processFlowVersion,
			String processOperationName, String processOperationVersion, String machineName, String toProcessFlowName, String toProcessFlowVersion, String toProcessOperationName,
			String toProcessOperationVersion) throws greenFrameDBErrorSignal, CustomException
	{
		log.info("GetSampleLot - lotName : " + lotName + ", FromInfo : " + processFlowName + "/" + processOperationName + ", ToInfo : " + toProcessFlowName + "/" + toProcessOperationName);

		String condition = "LOTNAME = ? AND FACTORYNAME = ? AND PRODUCTSPECNAME = ? AND PRODUCTSPECVERSION = ? AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ? AND PROCESSOPERATIONNAME = ? AND PROCESSOPERATIONVERSION = ? AND MACHINENAME = ? AND TOPROCESSFLOWNAME = ? AND TOPROCESSFLOWVERSION = ? AND TOPROCESSOPERATIONNAME = ? AND TOPROCESSOPERATIONVERSION = ?";
		Object[] bindSet = new Object[] { lotName, factoryName, productSpecName, productSpecVersion, processFlowName, processFlowVersion, processOperationName, processOperationVersion, machineName,
				toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion };

		List<SampleLot> sampleLotList = new ArrayList<SampleLot>();

		try
		{
			sampleLotList = ExtendedObjectProxy.getSampleLotService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			sampleLotList = null;
		}

		log.info("End GetSampleLot");

		return sampleLotList;
	}

	public List<SampleLot> getSampleLotDataListByInfo(String lotName, String factoryName, String productSpecName, String productSpecVersion, String processFlowName, String processFlowVersion,
			String processOperationName, String processOperationVersion) throws greenFrameDBErrorSignal, CustomException
	{
		log.info("GetSampleLot - lotName : " + lotName + ", FromInfo : " + processFlowName + "/" + processOperationName);

		String condition = "LOTNAME = ? AND FACTORYNAME = ? AND PRODUCTSPECNAME = ? AND PRODUCTSPECVERSION = ? AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ? AND PROCESSOPERATIONNAME = ? AND PROCESSOPERATIONVERSION = ?";
		Object[] bindSet = new Object[] { lotName, factoryName, productSpecName, productSpecVersion, processFlowName, processFlowVersion, processOperationName, processOperationVersion };

		List<SampleLot> sampleLotList = new ArrayList<SampleLot>();

		try
		{
			sampleLotList = ExtendedObjectProxy.getSampleLotService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			sampleLotList = null;
		}

		log.info("End GetSampleLot");

		return sampleLotList;
	}

	public List<SampleLot> getSampleLotDataListForNextNode(String lotName, String factoryName, String productSpecName, String productSpecVersion, String processFlowName, String processFlowVersion,
			String processOperationName, String processOperationVersion) throws greenFrameDBErrorSignal, CustomException
	{
		log.info("GetSampleLot - lotName : " + lotName + ", FromInfo : " + processFlowName + "/" + processOperationName);

		String condition = "LOTNAME = ? AND FACTORYNAME = ? AND PRODUCTSPECNAME = ? AND PRODUCTSPECVERSION = ? AND TOPROCESSFLOWNAME = ? AND TOPROCESSFLOWVERSION = ? AND TOPROCESSOPERATIONNAME = ? AND TOPROCESSOPERATIONVERSION = ?";
		Object[] bindSet = new Object[] { lotName, factoryName, productSpecName, productSpecVersion, processFlowName, processFlowVersion, processOperationName, processOperationVersion };

		List<SampleLot> sampleLotList = new ArrayList<SampleLot>();

		try
		{
			sampleLotList = ExtendedObjectProxy.getSampleLotService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			sampleLotList = null;
		}

		log.info("End GetSampleLot");

		return sampleLotList;
	}

	public List<SampleLot> getSampleLotDataListByToInfo(String lotName, String factoryName, String productSpecName, String productSpecVersion, String toProcessFlowName, String toProcessFlowVersion,
			String toProcessOperationName, String toProcessOperationVersion) throws greenFrameDBErrorSignal, CustomException
	{
		log.info("GetSampleLot - lotName : " + lotName + ", ToInfo : " + toProcessFlowName + "/" + toProcessOperationName);

		String condition = "LOTNAME = ? AND FACTORYNAME = ? AND PRODUCTSPECNAME = ? AND PRODUCTSPECVERSION = ? AND TOPROCESSFLOWNAME = ? AND TOPROCESSFLOWVERSION = ? AND TOPROCESSOPERATIONNAME = ? AND TOPROCESSOPERATIONVERSION = ?";
		Object[] bindSet = new Object[] { lotName, factoryName, productSpecName, productSpecVersion, toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion };

		List<SampleLot> sampleLotList = new ArrayList<SampleLot>();

		try
		{
			sampleLotList = ExtendedObjectProxy.getSampleLotService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			sampleLotList = null;
		}

		log.info("end GetSampleLot");

		return sampleLotList;
	}
	
	public List<SampleLot> getSampleLotDataListByToInfoForForceSampling(String lotName, String factoryName, String productSpecName, String productSpecVersion, String toProcessFlowName, String toProcessFlowVersion,
			String toProcessOperationName, String toProcessOperationVersion) throws greenFrameDBErrorSignal, CustomException
	{
		log.info("GetSampleLot - lotName : " + lotName + ", ToInfo : " + toProcessFlowName + "/" + toProcessOperationName);

		String condition = "LOTNAME = ? AND FACTORYNAME = ? AND PRODUCTSPECNAME = ? AND PRODUCTSPECVERSION = ? AND TOPROCESSFLOWNAME = ? AND TOPROCESSFLOWVERSION = ? AND TOPROCESSOPERATIONNAME = ? AND TOPROCESSOPERATIONVERSION = ? AND FORCESAMPLINGFLAG = 'Y'";
		Object[] bindSet = new Object[] { lotName, factoryName, productSpecName, productSpecVersion, toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion };

		List<SampleLot> sampleLotList = new ArrayList<SampleLot>();

		try
		{
			sampleLotList = ExtendedObjectProxy.getSampleLotService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			sampleLotList = null;
		}

		log.info("end GetSampleLot");

		return sampleLotList;
	}

	public List<SampleLot> getSampleLotDataListByReturnInfo(String lotName, String factoryName, String productSpecName, String productSpecVersion, String returnProcessFlowName,
			String returnProcessFlowVersion, String returnProcessOperationName, String returnProcessOperationVersion) throws greenFrameDBErrorSignal, CustomException
	{
		log.info("GetSampleLot - lotName : " + lotName + ", ReturnInfo : " + returnProcessFlowName + "/" + returnProcessOperationName);

		String condition = "LOTNAME = ? AND FACTORYNAME = ? AND PRODUCTSPECNAME = ? AND PRODUCTSPECVERSION = ? AND RETURNPROCESSFLOWNAME = ? AND RETURNPROCESSFLOWVERSION = ? AND RETURNOPERATIONNAME = ? AND RETURNOPERATIONVERSION = ?";
		Object[] bindSet = new Object[] { lotName, factoryName, productSpecName, productSpecVersion, returnProcessFlowName, returnProcessFlowVersion, returnProcessOperationName,
				returnProcessOperationVersion };

		List<SampleLot> sampleLotList = new ArrayList<SampleLot>();

		try
		{
			sampleLotList = ExtendedObjectProxy.getSampleLotService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			sampleLotList = null;
		}

		log.info("end GetSampleLot");

		return sampleLotList;
	}

	public List<SampleLot> getSampleLotDataList(String lotName)
	{
		String condition = " LOTNAME = ? ";
		Object[] bindSet = new Object[] { lotName };

		List<SampleLot> dataInfoList = new ArrayList<SampleLot>();

		try
		{
			dataInfoList = this.select(condition, bindSet);
		}
		catch (Exception e)
		{
			dataInfoList = null;
		}

		return dataInfoList;
	}

	public List<SampleLot> getSampleLotDataListByToFlow(String lotName, String factoryName, String productSpecName, String productSpecVersion, String toProcessFlowName, String toProcessFlowVersion)
			throws greenFrameDBErrorSignal, CustomException
	{
		log.info("GetSampleLot - lotName : " + lotName + ", ToInfo : " + toProcessFlowName);

		String condition = "LOTNAME = ? AND FACTORYNAME = ? AND PRODUCTSPECNAME = ? AND PRODUCTSPECVERSION = ? AND TOPROCESSFLOWNAME = ? AND TOPROCESSFLOWVERSION = ?";
		Object[] bindSet = new Object[] { lotName, factoryName, productSpecName, productSpecVersion, toProcessFlowName, toProcessFlowVersion };

		List<SampleLot> sampleLotList = new ArrayList<SampleLot>();

		try
		{
			sampleLotList = ExtendedObjectProxy.getSampleLotService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			sampleLotList = null;
		}

		log.info("end GetSampleLot");

		return sampleLotList;
	}

	public List<SampleLot> getSampleLotDataListByToFlow(String lotName, String factoryName, String productSpecName, String productSpecVersion, String processFlowName, String processFlowVersion,
			String processOperationName, String processOperationVersion, String toProcessFlowName, String toProcessFlowVersion) throws greenFrameDBErrorSignal, CustomException
	{
		log.info("GetSampleLot - lotName : " + lotName + ", ToInfo : " + toProcessFlowName);

		String condition = "LOTNAME = ? AND FACTORYNAME = ? AND PRODUCTSPECNAME = ? AND PRODUCTSPECVERSION = ? AND PROCESSFLOWNAME =? AND PROCESSFLOWVERSION = ? AND PROCESSOPERATIONNAME = ? AND PROCESSOPERATIONVERSION = ? AND TOPROCESSFLOWNAME = ? AND TOPROCESSFLOWVERSION = ?";
		Object[] bindSet = new Object[] { lotName, factoryName, productSpecName, productSpecVersion, processFlowName, processFlowVersion, processOperationName, processOperationVersion,
				toProcessFlowName, toProcessFlowVersion };

		List<SampleLot> sampleLotList = new ArrayList<SampleLot>();

		try
		{
			sampleLotList = ExtendedObjectProxy.getSampleLotService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			sampleLotList = null;
		}

		log.info("end GetSampleLot");

		return sampleLotList;
	}

	public List<SampleLot> getForceSampleLotDataAll(String lotName, String factoryName, String productSpecName, String productSpecVersion, String toProcessFlowName, String toProcessFlowVersion)
	{
		log.info("GetSampleLot - lotName : " + lotName + ", ToProcessFlow : " + toProcessFlowName);

		String condition = "LOTNAME = ? AND FACTORYNAME = ? AND PRODUCTSPECNAME = ? AND PRODUCTSPECVERSION = ? AND TOPROCESSFLOWNAME = ? AND TOPROCESSFLOWVERSION = ? AND FORCESAMPLINGFLAG = 'Y'";
		Object[] bindSet = new Object[] { lotName, factoryName, productSpecName, productSpecVersion, toProcessFlowName, toProcessFlowVersion };

		List<SampleLot> sampleLotList = new ArrayList<SampleLot>();

		try
		{
			sampleLotList = ExtendedObjectProxy.getSampleLotService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			sampleLotList = null;
		}

		log.info("End GetSampleLot");

		return sampleLotList;
	}

	public List<SampleLot> getForceSampleLotDataListByToInfo(String lotName, String factoryName, String productSpecName, String productSpecVersion, String toProcessFlowName,
			String toProcessFlowVersion, String toProcessOperationName, String toProcessOperationVersion, String forceSamplingFlag) throws greenFrameDBErrorSignal, CustomException
	{
		log.info("GetForceSampleLot - lotName : " + lotName + ", ToInfo : " + toProcessFlowName + "/" + toProcessOperationName);

		String condition = "LOTNAME = ? AND FACTORYNAME = ? AND PRODUCTSPECNAME = ? AND PRODUCTSPECVERSION = ? AND TOPROCESSFLOWNAME = ? AND TOPROCESSFLOWVERSION = ? AND TOPROCESSOPERATIONNAME = ? AND TOPROCESSOPERATIONVERSION = ? AND FORCESAMPLINGFLAG = ?";
		Object[] bindSet = new Object[] { lotName, factoryName, productSpecName, productSpecVersion, toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion,
				forceSamplingFlag };

		List<SampleLot> sampleLotList = new ArrayList<SampleLot>();

		try
		{
			sampleLotList = ExtendedObjectProxy.getSampleLotService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			sampleLotList = null;
		}

		log.info("End GetForceSampleLot");

		return sampleLotList;
	}

	public List<SampleLot> getForceSampleLotDataListbyToProcessFlow(String lotName, String factoryName, String productSpecName, String productSpecVersion, String toProcessFlowName,
			String toProcessFlowVersion) throws greenFrameDBErrorSignal, CustomException
	{
		log.info("GetForceSampleLot - lotName : " + lotName + ", ToProcessFlow : " + toProcessFlowName);

		String condition = "LOTNAME = ? AND FACTORYNAME = ? AND PRODUCTSPECNAME = ? AND PRODUCTSPECVERSION = ? AND NVL(FORCESAMPLINGFLAG, 'N') = 'N' AND TOPROCESSFLOWNAME = ? AND TOPROCESSFLOWVERSION = ?";
		Object[] bindSet = new Object[] { lotName, factoryName, productSpecName, productSpecVersion, toProcessFlowName, toProcessFlowVersion };

		List<SampleLot> sampleLotList = new ArrayList<SampleLot>();

		try
		{
			sampleLotList = ExtendedObjectProxy.getSampleLotService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			sampleLotList = null;
		}

		log.info("End GetForceSampleLot");

		return sampleLotList;
	}

	public List<SampleLot> getForceSampleLotDataList(String lotName, String factoryName, String productSpecName, String productSpecVersion, String processFlowName, String processFlowVersion,
			String prcessOperationName, String processOperationVersion) throws greenFrameDBErrorSignal, CustomException
	{
		log.info("GetForceSampleLot - lotName : " + lotName + ", FromInfo : " + processFlowName);

		String condition = "LOTNAME = ? AND FACTORYNAME = ? AND PRODUCTSPECNAME = ? AND PRODUCTSPECVERSION = ? AND NVL(FORCESAMPLINGFLAG, 'N') = 'N' AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ? AND PROCESSOPERATIONNAME = ? AND PROCESSOPERATIONVERSION = ?";
		Object[] bindSet = new Object[] { lotName, factoryName, productSpecName, productSpecVersion, processFlowName, processFlowVersion, prcessOperationName, processOperationVersion };

		List<SampleLot> sampleLotList = new ArrayList<SampleLot>();

		try
		{
			sampleLotList = ExtendedObjectProxy.getSampleLotService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			sampleLotList = null;
		}

		log.info("End GetForceSampleLot");

		return sampleLotList;
	}

	public List<SampleLot> getSampleLotDataByPolicy(Lot lotData, Map<String, Object> samplePolicy) throws CustomException
	{
		log.info("GetSampleLot By Policy - lotName : " + lotData.getKey().getLotName());
		String condition = "";
		Object[] bindSet = new Object[]{};
		ProcessFlow processFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);
		
		// for BackUpFlow
		if (StringUtils.equals(processFlowData.getProcessFlowType(), "BackUp"))
		{
			condition = "LOTNAME = ? AND FACTORYNAME = ? AND PRODUCTSPECNAME = ? AND PRODUCTSPECVERSION = ? AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ? AND PROCESSOPERATIONNAME = ? AND PROCESSOPERATIONVERSION = ? AND TOPROCESSFLOWNAME = ? AND TOPROCESSFLOWVERSION = ? AND TOPROCESSOPERATIONNAME = ? AND TOPROCESSOPERATIONVERSION = ?";
			bindSet = new Object[] { lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getUdfs().get("BACKUPMAINFLOWNAME"),
					lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), (String) samplePolicy.get("TOPROCESSFLOWNAME"),
					(String) samplePolicy.get("TOPROCESSFLOWVERSION"), (String) samplePolicy.get("TOPROCESSOPERATIONNAME"), (String) samplePolicy.get("TOPROCESSOPERATIONVERSION") };
		}
		else
		{
			condition = "LOTNAME = ? AND FACTORYNAME = ? AND PRODUCTSPECNAME = ? AND PRODUCTSPECVERSION = ? AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ? AND PROCESSOPERATIONNAME = ? AND PROCESSOPERATIONVERSION = ? AND TOPROCESSFLOWNAME = ? AND TOPROCESSFLOWVERSION = ? AND TOPROCESSOPERATIONNAME = ? AND TOPROCESSOPERATIONVERSION = ?";
			bindSet = new Object[] { lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProcessFlowName(),
					lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), (String) samplePolicy.get("TOPROCESSFLOWNAME"),
					(String) samplePolicy.get("TOPROCESSFLOWVERSION"), (String) samplePolicy.get("TOPROCESSOPERATIONNAME"), (String) samplePolicy.get("TOPROCESSOPERATIONVERSION") };
		}
		

		List<SampleLot> sampleLotList = new ArrayList<SampleLot>();

		try
		{
			sampleLotList = ExtendedObjectProxy.getSampleLotService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			sampleLotList = null;
		}

		log.info("End GetSampleLot By Policy");

		return sampleLotList;
	}

	public List<SampleLot> getSampleLotDataListWithOutMachineName(String lotName, String factoryName, String productSpecName, String productSpecVersion, String processFlowName,
			String processFlowVersion, String processOperationName, String processOperationVersion, String toProcessFlowName, String toProcessFlowVersion, String toProcessOperationName,
			String toProcessOperationVersion) throws greenFrameDBErrorSignal, CustomException
	{
		log.info("GetSampleLot - lotName : " + lotName + ", FromInfo : " + processFlowName + "/" + processOperationName + ", ToInfo : " + toProcessFlowName + "/" + toProcessOperationName);

		String condition = "LOTNAME = ? AND FACTORYNAME = ? AND PRODUCTSPECNAME = ? AND PRODUCTSPECVERSION = ? AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ? AND PROCESSOPERATIONNAME = ? AND PROCESSOPERATIONVERSION = ? AND TOPROCESSFLOWNAME = ? AND TOPROCESSFLOWVERSION = ? AND TOPROCESSOPERATIONNAME = ? AND TOPROCESSOPERATIONVERSION = ?";
		Object[] bindSet = new Object[] { lotName, factoryName, productSpecName, productSpecVersion, processFlowName, processFlowVersion, processOperationName, processOperationVersion,
				toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion };

		List<SampleLot> sampleLotList = new ArrayList<SampleLot>();

		try
		{
			sampleLotList = ExtendedObjectProxy.getSampleLotService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			sampleLotList = null;
		}

		log.info("End GetSampleLot");

		return sampleLotList;
	}

	public List<SampleLot> getSampleLotDataListWithOutMachineNameForSkip(String lotName, String factoryName, String productSpecName, String productSpecVersion, String processFlowName,
			String processFlowVersion, String processOperationName, String processOperationVersion, String toProcessFlowName, String toProcessOperationName) throws greenFrameDBErrorSignal,
			CustomException
	{
		log.info("GetSampleLot - lotName : " + lotName + ", FromInfo : " + processFlowName + "/" + processOperationName + ", ToInfo : " + toProcessFlowName + "/" + toProcessOperationName);

		// Mantis - 0000026
		//String condition = "LOTNAME = ? AND FACTORYNAME = ? AND PRODUCTSPECNAME = ? AND PRODUCTSPECVERSION = ? AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ? AND PROCESSOPERATIONNAME = ? AND PROCESSOPERATIONVERSION = ? AND TOPROCESSFLOWNAME <> ? AND TOPROCESSOPERATIONNAME <> ? ORDER BY PRIORITY ";
		String condition = "LOTNAME = ? AND FACTORYNAME = ? AND PRODUCTSPECNAME = ? AND PRODUCTSPECVERSION = ? AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ? AND PROCESSOPERATIONNAME = ? AND PROCESSOPERATIONVERSION = ? AND TOPROCESSOPERATIONNAME <> ? ORDER BY PRIORITY ";
		Object[] bindSet = new Object[] { lotName, factoryName, productSpecName, productSpecVersion, processFlowName, processFlowVersion, processOperationName, processOperationVersion,
			      toProcessOperationName };

		List<SampleLot> sampleLotList = new ArrayList<SampleLot>();

		try
		{
			sampleLotList = ExtendedObjectProxy.getSampleLotService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			sampleLotList = null;
		}

		log.info("End GetSampleLot");

		return sampleLotList;
	}

	public List<SampleLot> getSampleLotDataListByComment(String lotName, String factoryName, String productSpecName, String productSpecVersion, String processFlowName, String processFlowVersion,
			String toProcessFlowName, String toProcessFlowVersion, String toProcessOperationName, String toProcessOperationVersion, String comment) throws greenFrameDBErrorSignal, CustomException
	{
		String condition = "LOTNAME = :LOTNAME AND FACTORYNAME = :FACTORYNAME AND PRODUCTSPECNAME = :PRODUCTSPECNAME AND PRODUCTSPECVERSION = :PRODUCTSPECVERSION AND PROCESSFLOWNAME = :PROCESSFLOWNAME AND PROCESSFLOWVERSION = :PROCESSFLOWVERSION AND TOPROCESSFLOWNAME = :TOPROCESSFLOWNAME AND TOPROCESSFLOWVERSION = :TOPROCESSFLOWVERSION AND TOPROCESSOPERATIONNAME = :TOPROCESSOPERATIONNAME AND TOPROCESSOPERATIONVERSION = :TOPROCESSOPERATIONVERSION AND MACHINENAME = 'NA' AND MANUALSAMPLEFLAG = 'Y' AND EVENTCOMMENT LIKE :EVENTCOMMENT";
		Object[] bindSet = new Object[] { lotName, factoryName, productSpecName, productSpecVersion, processFlowName, processFlowVersion, toProcessFlowName, toProcessFlowVersion,
				toProcessOperationName, toProcessOperationVersion, comment };

		List<SampleLot> sampleLotList = new ArrayList<SampleLot>();

		try
		{
			sampleLotList = ExtendedObjectProxy.getSampleLotService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			sampleLotList = null;
		}

		return sampleLotList;
	}

	public void deleteSampleLotWithOutMachineName(EventInfo eventInfo, String lotName, String factoryName, String productSpecName, String productSpecVersion, String processFlowName,
			String processFlowVersion, String processOperationName, String processOperationVersion, String toProcessFlowName, String toProcessFlowVersion, String toProcessOperationName,
			String toProcessOperationVersion) throws greenFrameDBErrorSignal, CustomException
	{
		log.info("Start DeleteSampleLot - lotName : " + lotName + ", FromInfo : " + processFlowName + "/" + processOperationName + ", ToInfo : " + toProcessFlowName + "/" + toProcessOperationName);

		List<SampleLot> sampleLotList = ExtendedObjectProxy.getSampleLotService().getSampleLotDataListWithOutMachineName(lotName, factoryName, productSpecName, productSpecVersion, processFlowName,
				processFlowVersion, processOperationName, processOperationVersion, toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion);

		if (sampleLotList != null)
		{
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			for (SampleLot sampleLot : sampleLotList)
			{
				ExtendedObjectProxy.getSampleLotService().remove(eventInfo, sampleLot);
			}
		}

		log.info("End DeleteSampleLot");
	}

	public void deleteSampleLotWithOutOperationName(EventInfo eventInfo, String lotName, String factoryName, String productSpecName, String productSpecVersion, String processFlowName,
			String processFlowVersion, String toProcessFlowName, String toProcessFlowVersion) throws greenFrameDBErrorSignal, CustomException
	{
		log.info("Start DeleteSampleLot - lotName : " + lotName + ", FromInfo : " + processFlowName + ", ToInfo : " + toProcessFlowName);

		String condition = "LOTNAME = ? AND FACTORYNAME = ? AND PRODUCTSPECNAME = ? AND PRODUCTSPECVERSION = ? AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ? AND TOPROCESSFLOWNAME = ? AND TOPROCESSFLOWVERSION = ? ";
		Object[] bindSet = new Object[] { lotName, factoryName, productSpecName, productSpecVersion, processFlowName, processFlowVersion, toProcessFlowName, toProcessFlowVersion };

		List<SampleLot> sampleLotList = new ArrayList<SampleLot>();

		try
		{
			sampleLotList = ExtendedObjectProxy.getSampleLotService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			sampleLotList = null;
		}

		if (sampleLotList != null)
		{
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			for (SampleLot sampleLot : sampleLotList)
			{
				ExtendedObjectProxy.getSampleLotService().remove(eventInfo, sampleLot);
			}
		}

		log.info("End DeleteSampleLot");
	}

	public void deleteSampleLotDataByToInfo(EventInfo eventInfo, String lotName, String factoryName, String productSpecName, String productSpecVersion, String toProcessFlowName,
			String toProcessFlowVersion, String toProcessOperationName, String toProcessOperationVersion) throws greenFrameDBErrorSignal, CustomException
	{
		log.info("Start DeleteSampleLot - lotName : " + lotName + ", ToInfo : " + toProcessFlowName + "/" + toProcessOperationName);

		String condition = "LOTNAME = ? AND FACTORYNAME = ? AND PRODUCTSPECNAME = ? AND PRODUCTSPECVERSION = ? AND TOPROCESSFLOWNAME = ? AND TOPROCESSFLOWVERSION = ? AND TOPROCESSOPERATIONNAME = ? AND TOPROCESSOPERATIONVERSION = ?";
		Object[] bindSet = new Object[] { lotName, factoryName, productSpecName, productSpecVersion, toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion };

		List<SampleLot> sampleLotList = new ArrayList<SampleLot>();
		try
		{
			sampleLotList = ExtendedObjectProxy.getSampleLotService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			sampleLotList = null;
		}

		if (sampleLotList != null)
		{
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			for (SampleLot sampleLot : sampleLotList)
			{
				ExtendedObjectProxy.getSampleLotService().remove(eventInfo, sampleLot);
			}
		}

		log.info("End DeleteSampleLot");
	}

	public void deleteReserveSampleLotDataByToInfo(EventInfo eventInfo, String lotName, String factoryName, String productSpecName, String productSpecVersion, String toProcessFlowName,
			String toProcessFlowVersion, String toProcessOperationName, String toProcessOperationVersion) throws greenFrameDBErrorSignal, CustomException
	{
		log.info("Start DeleteSampleLot - lotName : " + lotName + ", ToInfo : " + toProcessFlowName + "/" + toProcessOperationName);

		String condition = "LOTNAME = ? AND FACTORYNAME = ? AND PRODUCTSPECNAME = ? AND PRODUCTSPECVERSION = ? AND TOPROCESSFLOWNAME = ? AND TOPROCESSFLOWVERSION = ? AND TOPROCESSOPERATIONNAME = ? AND TOPROCESSOPERATIONVERSION = ?";
		Object[] bindSet = new Object[] { lotName, factoryName, productSpecName, productSpecVersion, toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion };

		List<SampleLot> sampleLotList = new ArrayList<SampleLot>();
		try
		{
			sampleLotList = ExtendedObjectProxy.getSampleLotService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			sampleLotList = null;
		}

		if (sampleLotList != null)
		{
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			EventInfo lotEventInfo = EventInfoUtil.makeEventInfo("CancelReserveSample", eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);
			lotEventInfo.setBehaviorName("L");
			lotEventInfo.setEventTimeKey(eventInfo.getEventTimeKey());

			for (SampleLot sampleLot : sampleLotList)
			{
				ExtendedObjectProxy.getSampleLotService().remove(eventInfo, sampleLot);

				// MantisID : 0000022
				// Add Lot history
				LotServiceProxy.getLotService().setEvent(new LotKey(sampleLot.getLotName()), lotEventInfo, new SetEventInfo());
			}
		}

		log.info("End DeleteSampleLot");
	}

	public void deleteSampleLotDataByInfo(EventInfo eventInfo, String lotName, String factoryName, String productSpecName, String productSpecVersion, String processFlowName,
			String processFlowVersion, String processOperationName, String processOperationVersion) throws greenFrameDBErrorSignal, CustomException
	{
		log.info("Start DeleteSampleLot - lotName : " + lotName + ", ToInfo : " + processFlowName + "/" + processOperationName);

		String condition = "LOTNAME = ? AND FACTORYNAME = ? AND PRODUCTSPECNAME = ? AND PRODUCTSPECVERSION = ? AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ? AND PROCESSOPERATIONNAME = ? AND PROCESSOPERATIONVERSION = ?";
		Object[] bindSet = new Object[] { lotName, factoryName, productSpecName, productSpecVersion, processFlowName, processFlowVersion, processOperationName, processOperationVersion };

		List<SampleLot> sampleLotList = new ArrayList<SampleLot>();
		try
		{
			sampleLotList = ExtendedObjectProxy.getSampleLotService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			sampleLotList = null;
		}

		if (sampleLotList != null)
		{
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			for (SampleLot sampleLot : sampleLotList)
			{
				ExtendedObjectProxy.getSampleLotService().remove(eventInfo, sampleLot);
			}
		}

		log.info("End DeleteSampleLot");
	}

	public List<SampleLot> getSampleLotDataListBySpec(String lotName, String factoryName, String productSpecName, String productSpecVersion)
	{
		String condition = "LOTNAME = ? AND FACTORYNAME = ? AND PRODUCTSPECNAME = ? AND PRODUCTSPECVERSION = ?";
		Object[] bindSet = new Object[] { lotName, factoryName, productSpecName, productSpecVersion };

		List<SampleLot> sampleLotList = new ArrayList<SampleLot>();

		try
		{
			sampleLotList = ExtendedObjectProxy.getSampleLotService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			sampleLotList = null;
		}

		return sampleLotList;
	}

	public List<SampleLot> getSampleLotDataList(String lotName, String factoryName, String productSpecName, String productSpecVersion, String processFlowName, String processFlowVersion,
			String returnOperationName, String returnOperationVersion)
	{
		String condition = "LOTNAME = ? AND FACTORYNAME = ? AND PRODUCTSPECNAME = ? AND PRODUCTSPECVERSION = ? AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ? AND RETURNOPERATIONNAME = ? AND RETURNOPERATIONVERSION = ?";
		Object[] bindSet = new Object[] { lotName, factoryName, productSpecName, productSpecVersion, processFlowName, processFlowVersion, returnOperationName, returnOperationVersion };

		List<SampleLot> sampleLotList = new ArrayList<SampleLot>();

		try
		{
			sampleLotList = ExtendedObjectProxy.getSampleLotService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			sampleLotList = null;
		}

		return sampleLotList;
	}

	public void checkLotForceSampling(Lot lotData) throws CustomException
	{
		String condition = "LOTNAME = ? AND FACTORYNAME = ? AND PRODUCTSPECNAME = ? AND PRODUCTSPECVERSION = ? AND TOPROCESSFLOWNAME = ? AND TOPROCESSFLOWVERSION = ? AND TOPROCESSOPERATIONNAME = ? AND TOPROCESSOPERATIONVERSION = ? AND FORCESAMPLINGFLAG = ?";
		Object[] bindSet = new Object[] { lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProcessFlowName(),
				lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), "Y" };

		List<SampleLot> sampleLotList = new ArrayList<SampleLot>();

		try
		{
			sampleLotList = ExtendedObjectProxy.getSampleLotService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			sampleLotList = null;
		}

		if (sampleLotList != null)
		{
			throw new CustomException("LOT-0120", lotData.getKey().getLotName());
		}
	}
}
