package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMServiceNoCT;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.Alarm;
import kr.co.aim.messolution.extended.object.management.data.CustomAlarm;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class CustomAlarmService extends CTORMService<CustomAlarm> {
	
	public static Log logger = LogFactory.getLog(CustomAlarmService.class);
	
	private final String historyEntity = "CustomAlarmHistory";
	
	public List<CustomAlarm> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<CustomAlarm> result = super.select(condition, bindSet, CustomAlarm.class);
		
		return result;
	}
	
	public CustomAlarm selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(CustomAlarm.class, isLock, keySet);
	}
	
	public CustomAlarm create(EventInfo eventInfo, CustomAlarm dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, CustomAlarm dataInfo)
		throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public CustomAlarm modify(EventInfo eventInfo, CustomAlarm dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
}
