package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.AbnormalUnitInfo;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AbnormalUnitInfoService extends CTORMService<AbnormalUnitInfo> {
	
	public static Log logger = LogFactory.getLog(AbnormalUnitInfoService.class);
	
	private final String historyEntity = "AbnormalUnitInfoHistory";
	
	public List<AbnormalUnitInfo> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<AbnormalUnitInfo> result = super.select(condition, bindSet, AbnormalUnitInfo.class);
		
		return result;
	}
	
	public AbnormalUnitInfo selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(AbnormalUnitInfo.class, isLock, keySet);
	}
	
	public AbnormalUnitInfo create(EventInfo eventInfo, AbnormalUnitInfo dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, AbnormalUnitInfo dataInfo)
		throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public AbnormalUnitInfo modify(EventInfo eventInfo, AbnormalUnitInfo dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
}
