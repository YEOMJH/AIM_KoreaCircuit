package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.ReserveMaskToEQP;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class ReserveMaskToEQPService extends CTORMService<ReserveMaskToEQP> {
	
	public static Log logger = LogFactory.getLog(ReserveMaskToEQP.class);
	
	private final String historyEntity = "ReserveMaskToEQPHistory";
	
	public List<ReserveMaskToEQP> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<ReserveMaskToEQP> result = super.select(condition, bindSet, ReserveMaskToEQP.class);
		
		return result;
	}
	
	public ReserveMaskToEQP selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(ReserveMaskToEQP.class, isLock, keySet);
	}
	
	public ReserveMaskToEQP create(EventInfo eventInfo, ReserveMaskToEQP dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, ReserveMaskToEQP dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public ReserveMaskToEQP modify(EventInfo eventInfo, ReserveMaskToEQP dataInfo)
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
	

	public void remove(EventInfo eventInfo, List<ReserveMaskToEQP> dataInfoList) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);

		super.delete(dataInfoList);
	}

}
