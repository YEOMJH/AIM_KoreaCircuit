package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.RunBanPreventRule;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class RunBanPreventRuleService extends CTORMService<RunBanPreventRule> {
	
	public static Log logger = LogFactory.getLog(RunBanPreventRuleService.class);
	
	private final String historyEntity = "";
	
	public List<RunBanPreventRule> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<RunBanPreventRule> result = super.select(condition, bindSet, RunBanPreventRule.class);
		
		return result;
	}
	
	public RunBanPreventRule selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(RunBanPreventRule.class, isLock, keySet);
	}
	
	public RunBanPreventRule create(EventInfo eventInfo, RunBanPreventRule dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, RunBanPreventRule dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public RunBanPreventRule modify(EventInfo eventInfo, RunBanPreventRule dataInfo)
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
}
