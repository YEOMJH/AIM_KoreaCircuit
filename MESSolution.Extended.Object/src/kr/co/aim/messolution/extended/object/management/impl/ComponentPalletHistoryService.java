package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.ComponentPalletHistory;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class ComponentPalletHistoryService extends CTORMService<ComponentPalletHistory> {
	
	public static Log logger = LogFactory.getLog(ComponentPalletHistory.class);
	
	private final String historyEntity = "ComponentHistory";
	
	public List<ComponentPalletHistory> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<ComponentPalletHistory> result = super.select(condition, bindSet, ComponentPalletHistory.class);
		
		return result;
	}
	
	public ComponentPalletHistory selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(ComponentPalletHistory.class, isLock, keySet);
	}
	
	public ComponentPalletHistory create(EventInfo eventInfo, ComponentPalletHistory dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public int[] create(EventInfo eventInfo, List<ComponentPalletHistory> dataInfoList)
		throws greenFrameDBErrorSignal
	{
		return super.insert(dataInfoList);
	}
	
	public void remove(EventInfo eventInfo, ComponentPalletHistory dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public ComponentPalletHistory modify(EventInfo eventInfo, ComponentPalletHistory dataInfo)
	{
		super.update(dataInfo);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
}
