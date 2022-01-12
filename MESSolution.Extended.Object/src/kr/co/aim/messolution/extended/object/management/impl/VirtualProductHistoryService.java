package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.VirtualProductHistory;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class VirtualProductHistoryService extends CTORMService<VirtualProductHistory> {
	
	public static Log logger = LogFactory.getLog(VirtualProductHistory.class);
	
	private final String historyEntity = "VIRTUALPRODUCTHISTORY";
	
	public List<VirtualProductHistory> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<VirtualProductHistory> result = super.select(condition, bindSet, VirtualProductHistory.class);
		
		return result;
	}
	
	public VirtualProductHistory selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(VirtualProductHistory.class, isLock, keySet);
	}
	
	public VirtualProductHistory create(EventInfo eventInfo, VirtualProductHistory dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public int[] create(EventInfo eventInfo, List<VirtualProductHistory> dataInfoList)
		throws greenFrameDBErrorSignal
	{
		return super.insert(dataInfoList);
	}
	
	public void remove(EventInfo eventInfo, VirtualProductHistory dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public VirtualProductHistory modify(EventInfo eventInfo, VirtualProductHistory dataInfo)
	{
		super.update(dataInfo);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
}
