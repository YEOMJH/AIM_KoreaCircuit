package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.FirstRunPanel;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FirstRunPanelService extends CTORMService<FirstRunPanel> {
	
	public static Log logger = LogFactory.getLog(EVAReserveSpecService.class);
	
	//private final String historyEntity = "";
	
	public List<FirstRunPanel> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<FirstRunPanel> result = super.select(condition, bindSet, FirstRunPanel.class);
		
		return result;
	}
	
	public FirstRunPanel selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(FirstRunPanel.class, isLock, keySet);
	}
	
	public boolean create(EventInfo eventInfo, FirstRunPanel dataInfo)
		throws CustomException
	{
		
		super.insert(dataInfo);
				
		//super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return true;
	}
	
	public void remove(EventInfo eventInfo, FirstRunPanel dataInfo)
		throws CustomException
	{
		//super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public FirstRunPanel modify(EventInfo eventInfo, FirstRunPanel dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		//super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
