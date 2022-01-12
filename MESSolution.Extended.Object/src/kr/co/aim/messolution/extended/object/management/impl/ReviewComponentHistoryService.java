package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.ReviewComponentHistory;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class ReviewComponentHistoryService extends CTORMService<ReviewComponentHistory>{
	
	public static Log logger = LogFactory.getLog(ReviewComponentHistory.class);

	public List<ReviewComponentHistory> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<ReviewComponentHistory> result = super.select(condition, bindSet, ReviewComponentHistory.class);

		return result;
	}

	public ReviewComponentHistory selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(ReviewComponentHistory.class, isLock, keySet);
	}

	public ReviewComponentHistory create(EventInfo eventInfo, ReviewComponentHistory dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void create(EventInfo eventInfo, List<ReviewComponentHistory> dataInfoList) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfoList);
	}

	public void remove(EventInfo eventInfo, ReviewComponentHistory dataInfo) throws greenFrameDBErrorSignal
	{
		super.delete(dataInfo);
	}

	public void remove(EventInfo eventInfo, List<ReviewComponentHistory> dataInfoList) throws greenFrameDBErrorSignal
	{
		super.delete(dataInfoList);
	}

	public ReviewComponentHistory modify(EventInfo eventInfo, ReviewComponentHistory dataInfo)
	{
		super.update(dataInfo);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void modify(EventInfo eventInfo, List<ReviewComponentHistory> dataInfoList) throws greenFrameDBErrorSignal
	{
		super.update(dataInfoList);
	}

}
