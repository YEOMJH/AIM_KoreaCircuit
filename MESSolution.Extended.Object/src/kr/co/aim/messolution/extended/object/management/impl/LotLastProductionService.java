package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMServiceNoCT;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.LotLastProduction;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class LotLastProductionService extends CTORMServiceNoCT<LotLastProduction>
{
	public static Log logger = LogFactory.getLog(AlarmService.class);
	
	private final String historyEntity = "";
	
	public List<LotLastProduction> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<LotLastProduction> result = super.select(condition, bindSet, LotLastProduction.class);
		
		return result;
	}
	
	public LotLastProduction selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(LotLastProduction.class, isLock, keySet);
	}
	
	public LotLastProduction create(EventInfo eventInfo, LotLastProduction dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, LotLastProduction dataInfo)
		throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public LotLastProduction modify(EventInfo eventInfo, LotLastProduction dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 	
}
