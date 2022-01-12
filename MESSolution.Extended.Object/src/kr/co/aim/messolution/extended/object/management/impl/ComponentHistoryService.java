package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.ComponentHistory;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class ComponentHistoryService extends CTORMService<ComponentHistory> {
	
	public static Log logger = LogFactory.getLog(ComponentHistory.class);
	
	private final String historyEntity = "ComponentHistory";
	
	public List<ComponentHistory> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<ComponentHistory> result = super.select(condition, bindSet, ComponentHistory.class);
		
		return result;
	}
	
	public ComponentHistory selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(ComponentHistory.class, isLock, keySet);
	}
	
	public ComponentHistory create(EventInfo eventInfo, ComponentHistory dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public int[] create(EventInfo eventInfo, List<ComponentHistory> dataInfoList)
		throws greenFrameDBErrorSignal
	{
		return super.insert(dataInfoList);
	}
	
	public void remove(EventInfo eventInfo, ComponentHistory dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public ComponentHistory modify(EventInfo eventInfo, ComponentHistory dataInfo)
	{
		super.update(dataInfo);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
}
