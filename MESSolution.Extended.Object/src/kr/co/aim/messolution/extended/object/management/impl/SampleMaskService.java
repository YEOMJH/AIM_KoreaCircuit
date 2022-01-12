package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.SampleMask;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class SampleMaskService extends CTORMService<SampleMask>
{
	public static Log logger = LogFactory.getLog(SampleMaskService.class);

	private final String historyEntity = "SampleMaskHistory";

	public List<SampleMask> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<SampleMask> result = super.select(condition, bindSet, SampleMask.class);

		return result;
	}

	public SampleMask selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(SampleMask.class, isLock, keySet);
	}

	public SampleMask create(EventInfo eventInfo, SampleMask dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void create(EventInfo eventInfo, List<SampleMask> dataInfoList) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfoList);

		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
	}

	public SampleMask modify(EventInfo eventInfo, SampleMask dataInfo) throws greenFrameDBErrorSignal
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void modify(EventInfo eventInfo, List<SampleMask> dataInfoList) throws greenFrameDBErrorSignal
	{
		super.update(dataInfoList);

		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
	}

	public void remove(EventInfo eventInfo, SampleMask dataInfo) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}
	
	public SampleMask getSampleMask(boolean isLock, String maskLotName, String factoryName, String maskSpecName,
			String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVersion)
	{
		SampleMask sampleMask = null;
		try
		{
			sampleMask = selectByKey(isLock, new Object[] { maskLotName, factoryName, maskSpecName,
					processFlowName, processFlowVersion, processOperationName, processOperationVersion });
		}
		catch(greenFrameDBErrorSignal nfdes) {}
		return sampleMask;
	}
}