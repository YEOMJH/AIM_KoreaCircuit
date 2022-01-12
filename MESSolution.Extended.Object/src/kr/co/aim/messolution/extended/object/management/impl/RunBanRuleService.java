package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.RunBanRule;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class RunBanRuleService extends CTORMService<RunBanRule> {
	
	public static Log logger = LogFactory.getLog(RunBanRuleService.class);
	
//	private final String historyEntity = "RunBanRuleHIST";
	
	public List<RunBanRule> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<RunBanRule> result = super.select(condition, bindSet, RunBanRule.class);
		
		return result;
	}
	
	public RunBanRule selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(RunBanRule.class, isLock, keySet);
	}
	
	public RunBanRule create(EventInfo eventInfo, RunBanRule dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		
//		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, RunBanRule dataInfo)
		throws greenFrameDBErrorSignal
	{
//		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public RunBanRule modify(EventInfo eventInfo, RunBanRule dataInfo)
	{
		super.update(dataInfo);
		
//		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
}
