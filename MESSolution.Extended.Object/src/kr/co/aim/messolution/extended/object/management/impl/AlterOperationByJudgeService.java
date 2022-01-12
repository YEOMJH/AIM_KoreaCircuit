package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.AlterOperationByJudge;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AlterOperationByJudgeService extends CTORMService<AlterOperationByJudge> {
	public static Log logger = LogFactory.getLog(AlterOperationByJudgeService.class);

	//private final String historyEntity = "ShieldSpecHistory";

	public List<AlterOperationByJudge> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<AlterOperationByJudge> result = super.select(condition, bindSet, AlterOperationByJudge.class);

		return result;
	}

	public AlterOperationByJudge selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(AlterOperationByJudge.class, isLock, keySet);
	}

	public AlterOperationByJudge create(EventInfo eventInfo, AlterOperationByJudge dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		//super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, AlterOperationByJudge dataInfo) throws greenFrameDBErrorSignal
	{
		//super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public AlterOperationByJudge modify(EventInfo eventInfo, AlterOperationByJudge dataInfo)
	{
		super.update(dataInfo);

		//super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
