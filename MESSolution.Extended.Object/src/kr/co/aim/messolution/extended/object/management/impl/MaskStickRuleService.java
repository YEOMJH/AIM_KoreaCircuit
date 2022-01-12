package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.MaskStickRule;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;


public class MaskStickRuleService extends CTORMService<MaskStickRule>{
	
	public static Log logger = LogFactory.getLog(MaskStickRule.class);
	
	private final String historyEntity = "";
	
	public List<MaskStickRule> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<MaskStickRule> result = super.select(condition, bindSet, MaskStickRule.class);

		return result;
	}

	public MaskStickRule selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(MaskStickRule.class, isLock, keySet);
	}

	public MaskStickRule create(EventInfo eventInfo, MaskStickRule dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, MaskStickRule dataInfo) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public MaskStickRule modify(EventInfo eventInfo, MaskStickRule dataInfo)
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}	
}
