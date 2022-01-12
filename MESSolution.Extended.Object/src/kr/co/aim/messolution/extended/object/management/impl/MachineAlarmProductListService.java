package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMServiceNoCT;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.Alarm;
import kr.co.aim.messolution.extended.object.management.data.CustomAlarm;
import kr.co.aim.messolution.extended.object.management.data.MachineAlarmProductList;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class MachineAlarmProductListService extends CTORMService<MachineAlarmProductList> {
	
	public static Log logger = LogFactory.getLog(MachineAlarmProductListService.class);
	
	
	public List<MachineAlarmProductList> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<MachineAlarmProductList> result = super.select(condition, bindSet, MachineAlarmProductList.class);
		
		return result;
	}
	
	public MachineAlarmProductList selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(MachineAlarmProductList.class, isLock, keySet);
	}
	
	public MachineAlarmProductList create(EventInfo eventInfo, MachineAlarmProductList dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, MachineAlarmProductList dataInfo)
		throws CustomException
	{
		
		super.delete(dataInfo);
	}
	
	public MachineAlarmProductList modify(EventInfo eventInfo, MachineAlarmProductList dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
}
