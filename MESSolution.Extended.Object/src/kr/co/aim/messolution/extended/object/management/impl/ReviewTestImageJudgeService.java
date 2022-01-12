package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.ReviewTestImageJudge;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ReviewTestImageJudgeService extends CTORMService<ReviewTestImageJudge> {

	public static Log logger = LogFactory.getLog(ReviewTestImageJudge.class);
	
	public List<ReviewTestImageJudge> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<ReviewTestImageJudge> result = super.select(condition, bindSet, ReviewTestImageJudge.class);

		return result;
	}

	public ReviewTestImageJudge selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(ReviewTestImageJudge.class, isLock, keySet);
	}

	public ReviewTestImageJudge create(EventInfo eventInfo, ReviewTestImageJudge dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, ReviewTestImageJudge dataInfo) throws greenFrameDBErrorSignal
	{
		super.delete(dataInfo);
	}

	public ReviewTestImageJudge modify(EventInfo eventInfo, ReviewTestImageJudge dataInfo)
	{
		super.update(dataInfo);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
