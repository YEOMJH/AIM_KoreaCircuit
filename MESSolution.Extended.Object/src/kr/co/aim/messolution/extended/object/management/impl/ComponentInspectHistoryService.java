package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.ComponentInspectHistory;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class ComponentInspectHistoryService extends CTORMService<ComponentInspectHistory> {
	
	public static Log logger = LogFactory.getLog(ComponentInspectHistory.class);
	
	private final String historyEntity = "ComponentInspectHistory";
	
	public List<ComponentInspectHistory> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<ComponentInspectHistory> result = super.select(condition, bindSet, ComponentInspectHistory.class);
		
		return result;
	}
	
	public ComponentInspectHistory selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(ComponentInspectHistory.class, isLock, keySet);
	}
	
	public ComponentInspectHistory create(EventInfo eventInfo, ComponentInspectHistory dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public int[] create(EventInfo eventInfo, List<ComponentInspectHistory> dataInfoList)
		throws greenFrameDBErrorSignal
	{
		return super.insert(dataInfoList);
	}
	
	public void remove(EventInfo eventInfo, ComponentInspectHistory dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public ComponentInspectHistory modify(EventInfo eventInfo, ComponentInspectHistory dataInfo)
	{
		super.update(dataInfo);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
}
