package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.Alarm;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AlarmService extends CTORMService<Alarm> {
	
	public static Log logger = LogFactory.getLog(AlarmService.class);
	
	private final String historyEntity = "AlarmHistory";
	
	public List<Alarm> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<Alarm> result = super.select(condition, bindSet, Alarm.class);
		
		return result;
	}
	
	public Alarm selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(Alarm.class, isLock, keySet);
	}
	
	public Alarm create(EventInfo eventInfo, Alarm dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, Alarm dataInfo)
		throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public Alarm modify(EventInfo eventInfo, Alarm dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
}
