package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.AlarmGroup;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AlarmGroupService extends CTORMService<AlarmGroup> {
	
	public static Log logger = LogFactory.getLog(AlarmGroupService.class);
	
	private final String historyEntity = "";
	
	public List<AlarmGroup> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<AlarmGroup> result = super.select(condition, bindSet, AlarmGroup.class);
		
		return result;
	}
	
	public AlarmGroup selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(AlarmGroup.class, isLock, keySet);
	}
	
	public AlarmGroup create(EventInfo eventInfo, AlarmGroup dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		//super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, AlarmGroup dataInfo)
		throws CustomException
	{
		//super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public AlarmGroup modify(EventInfo eventInfo, AlarmGroup dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		//super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
	
	public List<Object> tranform (List resultList)
	{
		if (resultList==null || resultList.size() == 0)
		{
			return null;
		}
		Object result = super.ormExecute( CTORMUtil.createDataInfo(AlarmGroup.class), resultList);

		if ((result instanceof List))
		{
			return (List) result;
		}

		List<Object> resultSet = new ArrayList();
		resultSet.add((AlarmGroup) result);
		return resultSet;
	}
}
