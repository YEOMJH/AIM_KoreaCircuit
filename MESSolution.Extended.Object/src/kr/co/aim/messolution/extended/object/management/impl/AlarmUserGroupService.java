package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.AlarmUserGroup;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AlarmUserGroupService extends CTORMService<AlarmUserGroup> {
	
	public static Log logger = LogFactory.getLog(AlarmUserGroupService.class);
	
	private final String historyEntity = "";
	
	public List<AlarmUserGroup> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<AlarmUserGroup> result = super.select(condition, bindSet, AlarmUserGroup.class);
		
		return result;
	}
	
	public AlarmUserGroup selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(AlarmUserGroup.class, isLock, keySet);
	}
	
	public AlarmUserGroup create(EventInfo eventInfo, AlarmUserGroup dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		//super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, AlarmUserGroup dataInfo)
		throws CustomException
	{
		//super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public AlarmUserGroup modify(EventInfo eventInfo, AlarmUserGroup dataInfo)
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
		Object result = super.ormExecute( CTORMUtil.createDataInfo(AlarmUserGroup.class), resultList);

		if ((result instanceof List))
		{
			return (List) result;
		}

		List<Object> resultSet = new ArrayList();
		resultSet.add((AlarmUserGroup) result);
		return resultSet;
	}
}
