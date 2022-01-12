package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.FirstGlassJob;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class FirstGlassJobService extends CTORMService<FirstGlassJob> {

	public static Log logger = LogFactory.getLog(FirstGlassJob.class);

	private final String historyEntity = "FirstGlassJobHIST";

	public List<FirstGlassJob> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<FirstGlassJob> result = super.select(condition, bindSet, FirstGlassJob.class);

		return result;
	}

	public FirstGlassJob selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(FirstGlassJob.class, isLock, keySet);
	}

	public FirstGlassJob create(EventInfo eventInfo, FirstGlassJob dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void create(EventInfo eventInfo, List<FirstGlassJob> dataInfoList) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfoList);

		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
	}

	public void remove(EventInfo eventInfo, FirstGlassJob dataInfo) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public FirstGlassJob modify(EventInfo eventInfo, FirstGlassJob dataInfo)
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void validateJobState(FirstGlassJob jobData, String... stats) throws CustomException
	{
		boolean pass = false;

		for (String state : stats)
		{
			if (jobData.getJobState().equals(state))
			{
				pass = true;
			}
		}

		if (!pass)
		{
			StringBuilder states = new StringBuilder();
			for (String stat : stats)
			{
				states.append(stat).append(" ");
			}

			//FIRSTGLASS-0018:Job state is not [{0}]
			throw new CustomException("FIRSTGLASS-0018", states.toString());
		}

	}
}
