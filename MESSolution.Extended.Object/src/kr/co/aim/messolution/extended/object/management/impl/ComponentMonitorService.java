package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.ComponentMonitor;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class ComponentMonitorService extends CTORMService<ComponentMonitor>{

	
	public static Log logger = LogFactory.getLog(ComponentMonitor.class);
	
	private final String historyEntity = "";
	
	public List<ComponentMonitor> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<ComponentMonitor> result = super.select(condition, bindSet, ComponentMonitor.class);
		
		return result;
	}
	
	public ComponentMonitor selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(ComponentMonitor.class, isLock, keySet);
	}
	
	public ComponentMonitor create(EventInfo eventInfo, ComponentMonitor dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public int[] create(EventInfo eventInfo, List<ComponentMonitor> dataInfoList)
		throws greenFrameDBErrorSignal
	{
		return super.insert(dataInfoList);
	}
	
	public void remove(EventInfo eventInfo, ComponentMonitor dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public ComponentMonitor modify(EventInfo eventInfo, ComponentMonitor dataInfo)
	{
		super.update(dataInfo);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void modify(EventInfo eventInfo, List<ComponentMonitor> updateList) {
		// TODO Auto-generated method stub
		super.update(updateList);

		super.addHistory(eventInfo, this.historyEntity, updateList, logger);
	} 


}
