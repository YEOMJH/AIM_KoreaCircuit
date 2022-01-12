package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.AMSRule;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AMSRuleService extends CTORMService<AMSRule> {
	
	public static Log logger = LogFactory.getLog(AMSRuleService.class);
	
	private final String historyEntity = "AMSRuleHistory";
	
	public List<AMSRule> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<AMSRule> result = super.select(condition, bindSet, AMSRule.class);
		
		return result;
	}
	
	public AMSRule selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(AMSRule.class, isLock, keySet);
	}
	
	public AMSRule create(EventInfo eventInfo, AMSRule dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, AMSRule dataInfo)
		throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public AMSRule modify(EventInfo eventInfo, AMSRule dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
}
