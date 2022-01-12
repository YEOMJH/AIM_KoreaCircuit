package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.EVAReserveSpec;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class EVAReserveSpecService extends CTORMService<EVAReserveSpec> {
	
	public static Log logger = LogFactory.getLog(EVAReserveSpecService.class);
	
	private final String historyEntity = "EVAReserveSpecHistory";
	
	public List<EVAReserveSpec> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<EVAReserveSpec> result = super.select(condition, bindSet, EVAReserveSpec.class);
		
		return result;
	}
	
	public EVAReserveSpec selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(EVAReserveSpec.class, isLock, keySet);
	}
	
	public boolean create(EventInfo eventInfo, EVAReserveSpec dataInfo)
		throws CustomException
	{
		
		super.insert(dataInfo);
				
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return true;
	}
	
	public void remove(EventInfo eventInfo, EVAReserveSpec dataInfo)
		throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public EVAReserveSpec modify(EventInfo eventInfo, EVAReserveSpec dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
