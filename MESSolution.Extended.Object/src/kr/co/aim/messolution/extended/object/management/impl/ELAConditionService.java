package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.AlarmActionDef;
import kr.co.aim.messolution.extended.object.management.data.AlarmDefinition;
import kr.co.aim.messolution.extended.object.management.data.ELACondition;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class ELAConditionService extends CTORMService<ELACondition> {
	
	public static Log logger = LogFactory.getLog(ELAConditionService.class);
	
	public List<ELACondition> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<ELACondition> result = super.select(condition, bindSet, ELACondition.class);
		
		return result;
	}
	
	public ELACondition selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(ELACondition.class, isLock, keySet);
	}
	
	public ELACondition create(EventInfo eventInfo, ELACondition dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, ELACondition dataInfo)
		throws CustomException
	{
		
		super.delete(dataInfo);
	}
	
	public ELACondition modify(EventInfo eventInfo, ELACondition dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
	
	public List<Object> tranform (List resultList)
	{
		if (resultList==null || resultList.size() == 0)
		{
			return null;
		}
		Object result = super.ormExecute( CTORMUtil.createDataInfo(ELACondition.class), resultList);

		if ((result instanceof List))
		{
			return (List) result;
		}

		List<Object> resultSet = new ArrayList();
		resultSet.add((ELACondition) result);
		return resultSet;
	}
}
