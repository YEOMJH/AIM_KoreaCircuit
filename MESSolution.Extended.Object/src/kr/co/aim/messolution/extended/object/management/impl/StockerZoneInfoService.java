package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.StockerZoneInfo;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class StockerZoneInfoService extends CTORMService<StockerZoneInfo> {
	
	public static Log logger = LogFactory.getLog(StockerZoneInfoService.class);
	
	private final String historyEntity = "";
	
	public List<StockerZoneInfo> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<StockerZoneInfo> result = new ArrayList<StockerZoneInfo>();
		try
		{
			result = super.select(condition, bindSet, StockerZoneInfo.class);
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (!ne.getErrorCode().equals("NotFoundSignal"))
				throw ne;
		}
		return result;
	}
	
	public StockerZoneInfo selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(StockerZoneInfo.class, isLock, keySet);
	}
	
	public StockerZoneInfo create(EventInfo eventInfo, StockerZoneInfo dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, StockerZoneInfo dataInfo)
		throws greenFrameDBErrorSignal
	{	
		super.delete(dataInfo);
	}
	
	public StockerZoneInfo modify(EventInfo eventInfo, StockerZoneInfo dataInfo)
	{
		super.update(dataInfo);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
}
