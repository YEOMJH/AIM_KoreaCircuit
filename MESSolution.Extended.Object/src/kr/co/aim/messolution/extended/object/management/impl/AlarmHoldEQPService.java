package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.AlarmHoldEQP;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AlarmHoldEQPService extends CTORMService<AlarmHoldEQP> {
	
	public static Log logger = LogFactory.getLog(AlarmHoldEQPService.class);
	
	private final String historyEntity = "";
	
	public List<AlarmHoldEQP> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<AlarmHoldEQP> result = super.select(condition, bindSet, AlarmHoldEQP.class);
		
		return result;
	}
	
	public AlarmHoldEQP selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(AlarmHoldEQP.class, isLock, keySet);
	}
	
	public AlarmHoldEQP create(EventInfo eventInfo, AlarmHoldEQP dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		//super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, AlarmHoldEQP dataInfo)
		throws CustomException
	{
		//super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public AlarmHoldEQP modify(EventInfo eventInfo, AlarmHoldEQP dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		//super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
}
