package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.SampleLotCount;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.management.data.Lot;

public class SampleLotCountService extends CTORMService<SampleLotCount> {

	private static Log log = LogFactory.getLog(SampleLotCountService.class);
	public static Log logger = LogFactory.getLog(SampleLotCountService.class);

	private final String historyEntity = "";

	public List<SampleLotCount> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<SampleLotCount> result = super.select(condition, bindSet, SampleLotCount.class);

		return result;
	}

	public SampleLotCount selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(SampleLotCount.class, isLock, keySet);
	}

	public SampleLotCount create(EventInfo eventInfo, SampleLotCount dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, SampleLotCount dataInfo) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public SampleLotCount modify(EventInfo eventInfo, SampleLotCount dataInfo)
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public SampleLotCount getSampleLotCount(String factoryName, String productSpecName, String productSpecVersion, String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVersion, String machineName, String toProcessFlowName, String toProcessFlowVersion, String toProcessOperationName, String toProcessOperationVersion)
	{
		Object[] keySet = new Object[] { factoryName, productSpecName, productSpecVersion, processFlowName, processFlowVersion, processOperationName, processOperationVersion, machineName,
				toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion };

		SampleLotCount dataInfo = new SampleLotCount();

		try
		{
			dataInfo = ExtendedObjectProxy.getSampleLotCountService().selectByKey(false, keySet);
		}
		catch (Exception e)
		{
			dataInfo = null;
		}

		return dataInfo;
	}

	public List<SampleLotCount> getSampleLotCountWithoutToFlow(String factoryName, String productSpecName, String processFlowName, String processOperationName, String machineName,
			String toProcessOperationName)
	{
		String condition = "FACTORYNAME = ? AND PRODUCTSPECNAME = ? AND PROCESSFLOWNAME = ? AND PROCESSOPERATIONNAME = ? AND MACHINENAME = ? AND TOPROCESSOPERATIONNAME = ?";
		Object[] bindSet = new Object[] { factoryName, productSpecName, processFlowName, processOperationName, machineName, toProcessOperationName };

		List<SampleLotCount> dataInfoList = new ArrayList<SampleLotCount>();

		try
		{
			dataInfoList = ExtendedObjectProxy.getSampleLotCountService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			dataInfoList = null;
		}

		return dataInfoList;
	}

	public List<SampleLotCount> getSampleLotCountByPolicy(Lot lotData, Map<String, Object> samplePolicy) throws CustomException
	{
		List<SampleLotCount> dataInfoList = ExtendedObjectProxy.getSampleLotCountService().getSampleLotCountWithoutToFlow(lotData.getFactoryName(), lotData.getProductSpecName(),
				lotData.getProcessFlowName(), lotData.getProcessOperationName(), (String) samplePolicy.get("MACHINENAME"), (String) samplePolicy.get("TOPROCESSOPERATIONNAME"));

		return dataInfoList;
	}

	public void updateSampleLotCountWithoutToFlow(String factoryName, String productSpecName, String processFlowName, String processOperationName, String machineName, String toProcessOperationName,
			String lotSampleCount, String currentLotCount, String totalLotCount) throws CustomException
	{
		List<SampleLotCount> dataInfoList = ExtendedObjectProxy.getSampleLotCountService().getSampleLotCountWithoutToFlow(factoryName, productSpecName, processFlowName, processOperationName,
				machineName, toProcessOperationName);

		if (dataInfoList != null)
		{
			for (SampleLotCount dataInfo : dataInfoList)
			{
				dataInfo.setLotSampleCount(lotSampleCount);
				dataInfo.setCurrentLotCount(currentLotCount);
				dataInfo.setTotalLotCount(totalLotCount);

				ExtendedObjectProxy.getSampleLotCountService().update(dataInfo);
			}
		}
	}

	public void updateSampleLotCountDataByPolicy(Lot lotData, Map<String, Object> samplePolicy, List<SampleLotCount> sampleLotCount) throws CustomException
	{
		log.info("Start updateSampleLotCountData : TrackOut");

		ExtendedObjectProxy.getSampleLotCountService().updateSampleLotCountWithoutToFlow(lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProcessFlowName(),
				lotData.getProcessOperationName(), (String) samplePolicy.get("MACHINENAME"), (String) samplePolicy.get("TOPROCESSOPERATIONNAME"), (String) samplePolicy.get("LOTSAMPLINGCOUNT"),
				sampleLotCount.get(0).getCurrentLotCount(), sampleLotCount.get(0).getTotalLotCount());
	}

	public void updateSampleLotCountDataByPolicy(Lot lotData, Map<String, Object> samplePolicy, List<SampleLotCount> sampleLotCount, double currentLotCount, double totalLotCount)
			throws CustomException
	{
		log.info("Start updateSampleLotCountData : TrackOut");

		ExtendedObjectProxy.getSampleLotCountService().updateSampleLotCountWithoutToFlow(lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProcessFlowName(),
				lotData.getProcessOperationName(), (String) samplePolicy.get("MACHINENAME"), (String) samplePolicy.get("TOPROCESSOPERATIONNAME"), sampleLotCount.get(0).getLotSampleCount(),
				CommonUtil.StringValueOf(currentLotCount), CommonUtil.StringValueOf(totalLotCount));
	}

	public void insertSampleLotCount(String factoryName, String productSpecName, String productSpecVersion, String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVersion, String machineName, String toProcessFlowName, String toProcessFlowVersion, String toProcessOperationName, String toProcessOperationVersion,
			String lotSampleCount, String currentLotCount, String totalLotCount) throws greenFrameDBErrorSignal, CustomException
	{
		SampleLotCount dataInfo = new SampleLotCount();
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
		dataInfo.setLotSampleCount(lotSampleCount);
		dataInfo.setCurrentLotCount(currentLotCount);
		dataInfo.setTotalLotCount(totalLotCount);

		ExtendedObjectProxy.getSampleLotCountService().insert(dataInfo);
	}

	public void insertSampleLotCountByPolicy(Lot lotData, Map<String, Object> samplePolicy) throws greenFrameDBErrorSignal, CustomException
	{
		log.info("Start insertSampleLotCountData : TrackOut");

		ExtendedObjectProxy.getSampleLotCountService().insertSampleLotCount(lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProcessFlowName(),
				lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), (String) samplePolicy.get("MACHINENAME"),
				(String) samplePolicy.get("TOPROCESSFLOWNAME"), (String) samplePolicy.get("TOPROCESSFLOWVERSION"), (String) samplePolicy.get("TOPROCESSOPERATIONNAME"),
				(String) samplePolicy.get("TOPROCESSOPERATIONVERSION"), (String) samplePolicy.get("LOTSAMPLINGCOUNT"), "0", "0");
	}
}
