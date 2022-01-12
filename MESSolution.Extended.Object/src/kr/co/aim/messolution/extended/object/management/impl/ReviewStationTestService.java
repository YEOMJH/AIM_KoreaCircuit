package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.ReviewStationTest;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ReviewStationTestService extends CTORMService<ReviewStationTest> {

	public static Log logger = LogFactory.getLog(ReviewStationTest.class);
	private final String historyEntity = "ReviewStationTestHistory";
	

	public List<ReviewStationTest> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<ReviewStationTest> result = super.select(condition, bindSet, ReviewStationTest.class);

		return result;
	}

	public ReviewStationTest selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(ReviewStationTest.class, isLock, keySet);
	}

	public ReviewStationTest create(EventInfo eventInfo, ReviewStationTest dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, ReviewStationTest dataInfo) throws greenFrameDBErrorSignal
	{
		super.delete(dataInfo);
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
	}

	public ReviewStationTest modify(EventInfo eventInfo, ReviewStationTest dataInfo)
	{
		super.update(dataInfo);
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
