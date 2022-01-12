package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.AbnormalEQP;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AbnormalEQPService extends CTORMService<AbnormalEQP> {
	
	public static Log logger = LogFactory.getLog(AbnormalEQPService.class);
	
	private final String historyEntity = "AbnormalEQPHistory";
	
	public List<AbnormalEQP> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<AbnormalEQP> result = super.select(condition, bindSet, AbnormalEQP.class);
		
		return result;
	}
	
	public AbnormalEQP selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(AbnormalEQP.class, isLock, keySet);
	}
	
	public AbnormalEQP create(EventInfo eventInfo, AbnormalEQP dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, AbnormalEQP dataInfo)
		throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public AbnormalEQP modify(EventInfo eventInfo, AbnormalEQP dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
}

