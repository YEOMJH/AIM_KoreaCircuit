package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.ShieldSpec;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ShieldSpecService extends CTORMService<ShieldSpec> {
	public static Log logger = LogFactory.getLog(ShieldSpecService.class);

	private final String historyEntity = "ShieldSpecHistory";

	public List<ShieldSpec> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<ShieldSpec> result = super.select(condition, bindSet, ShieldSpec.class);

		return result;
	}

	public ShieldSpec selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(ShieldSpec.class, isLock, keySet);
	}

	public ShieldSpec create(EventInfo eventInfo, ShieldSpec dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, ShieldSpec dataInfo) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public ShieldSpec modify(EventInfo eventInfo, ShieldSpec dataInfo)
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
