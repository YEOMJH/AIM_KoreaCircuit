package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.MaskLotComponentHistory;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class MaskLotComponentHistoryService extends CTORMService<MaskLotComponentHistory> {
	
	public static Log logger = LogFactory.getLog(MaskLotComponentHistory.class);
	
	private final String historyEntity = "MaskLotComponentHistory";
	
	public List<MaskLotComponentHistory> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<MaskLotComponentHistory> result = super.select(condition, bindSet, MaskLotComponentHistory.class);
		
		return result;
	}
	
	public MaskLotComponentHistory selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(MaskLotComponentHistory.class, isLock, keySet);
	}
	
	public MaskLotComponentHistory create(EventInfo eventInfo, MaskLotComponentHistory dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, MaskLotComponentHistory dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public MaskLotComponentHistory modify(EventInfo eventInfo, MaskLotComponentHistory dataInfo)
	{
		super.update(dataInfo);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
}
