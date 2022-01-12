package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.HelpDeskInfo;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class HelpDeskInfoService extends CTORMService<HelpDeskInfo> {
	
	public static Log logger = LogFactory.getLog(HelpDeskInfoService.class);
	
	//private final String historyEntity = "HelpDeskInfoHistory";
	
	public List<HelpDeskInfo> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<HelpDeskInfo> result = super.select(condition, bindSet, HelpDeskInfo.class);
		
		return result;
	}
	
	public HelpDeskInfo selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(HelpDeskInfo.class, isLock, keySet);
	}
	
	public boolean create(EventInfo eventInfo, HelpDeskInfo dataInfo)
		throws CustomException
	{
		
		super.insert(dataInfo);
				
		//super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return true;
	}
	
	public void remove(EventInfo eventInfo, HelpDeskInfo dataInfo)
		throws CustomException
	{
		//super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public HelpDeskInfo modify(EventInfo eventInfo, HelpDeskInfo dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		//super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
}
