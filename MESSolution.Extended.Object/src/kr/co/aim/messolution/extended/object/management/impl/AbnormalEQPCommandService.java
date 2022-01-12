package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.AbnormalEQPCommand;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AbnormalEQPCommandService extends CTORMService<AbnormalEQPCommand> {
	
	public static Log logger = LogFactory.getLog(AbnormalEQPCommandService.class);
	
	private final String historyEntity = "AbnormalEQPCommandHistory";
	
	public List<AbnormalEQPCommand> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<AbnormalEQPCommand> result = super.select(condition, bindSet, AbnormalEQPCommand.class);
		
		return result;
	}
	
	public AbnormalEQPCommand selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(AbnormalEQPCommand.class, isLock, keySet);
	}
	
	public AbnormalEQPCommand create(EventInfo eventInfo, AbnormalEQPCommand dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, AbnormalEQPCommand dataInfo)
		throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public AbnormalEQPCommand modify(EventInfo eventInfo, AbnormalEQPCommand dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
}

