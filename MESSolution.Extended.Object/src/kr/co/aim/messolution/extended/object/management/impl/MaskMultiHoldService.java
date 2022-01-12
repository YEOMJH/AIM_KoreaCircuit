package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.MaskMultiHold;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class MaskMultiHoldService extends CTORMService<MaskMultiHold> {
	public static Log logger = LogFactory.getLog(MaskMultiHoldService.class);

	private final String historyEntity = "MaskMultiHoldHistory";

	public List<MaskMultiHold> select(String condition, Object[] bindSet) throws CustomException
	{
		List<MaskMultiHold> result = super.select(condition, bindSet, MaskMultiHold.class);

		return result;
	}

	public MaskMultiHold selectByKey(boolean isLock, Object[] keySet) throws CustomException
	{
		return super.selectByKey(MaskMultiHold.class, isLock, keySet);
	}

	public MaskMultiHold create(EventInfo eventInfo, MaskMultiHold dataInfo) throws CustomException
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, MaskMultiHold dataInfo) throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public MaskMultiHold modify(EventInfo eventInfo, MaskMultiHold dataInfo) throws CustomException
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
