package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.LotHoldAction;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class LotHoldActionService extends CTORMService<LotHoldAction> {
	
	public static Log logger = LogFactory.getLog(LotHoldActionService.class);
	
	private final String historyEntity = "LotHoldActionHistory";
	
	public List<LotHoldAction> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<LotHoldAction> result = super.select(condition, bindSet, LotHoldAction.class);
		
		return result;
	}
	
	public LotHoldAction selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(LotHoldAction.class, isLock, keySet);
	}
	
	public LotHoldAction create(EventInfo eventInfo, LotHoldAction dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, LotHoldAction dataInfo)
		throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public LotHoldAction modify(EventInfo eventInfo, LotHoldAction dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
}
