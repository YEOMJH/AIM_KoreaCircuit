package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.AMSRuleAssign;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AMSRuleAssignService extends CTORMService<AMSRuleAssign> {
	
	public static Log logger = LogFactory.getLog(AMSRuleAssignService.class);
	
	private final String historyEntity = "AMSRuleHistory";
	
	public List<AMSRuleAssign> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<AMSRuleAssign> result = super.select(condition, bindSet, AMSRuleAssign.class);
		
		return result;
	}
	
	public AMSRuleAssign selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(AMSRuleAssign.class, isLock, keySet);
	}
	
	public AMSRuleAssign create(EventInfo eventInfo, AMSRuleAssign dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, AMSRuleAssign dataInfo)
		throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public AMSRuleAssign modify(EventInfo eventInfo, AMSRuleAssign dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
}
