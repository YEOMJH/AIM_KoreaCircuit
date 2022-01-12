package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.AMSUser;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AMSUserService extends CTORMService<AMSUser> {
	
	public static Log logger = LogFactory.getLog(AMSUserService.class);
	
	private final String historyEntity = "AMSUserHistory";
	
	public List<AMSUser> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<AMSUser> result = super.select(condition, bindSet, AMSUser.class);
		
		return result;
	}
	
	public AMSUser selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(AMSUser.class, isLock, keySet);
	}
	
	public AMSUser create(EventInfo eventInfo, AMSUser dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, AMSUser dataInfo)
		throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public AMSUser modify(EventInfo eventInfo, AMSUser dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
}
