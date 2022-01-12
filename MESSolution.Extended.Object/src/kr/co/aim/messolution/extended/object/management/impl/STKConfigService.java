package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.STKConfig;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class STKConfigService extends CTORMService<STKConfig>
{
	public static Log logger = LogFactory.getLog(STKConfigService.class);
	
	private final String historyEntity = "";
	
	public STKConfig selectByKey(boolean isLock, Object keySet)
	throws greenFrameDBErrorSignal
	{
		return super.selectByKey(STKConfig.class, isLock, keySet);
	}	
	
	public List<STKConfig> select(String condition, Object[] bindSet)
	throws greenFrameDBErrorSignal
	{
		List<STKConfig> result = super.select(condition, bindSet, STKConfig.class);
		
		return result;
	}
	
	public void modify(EventInfo eventInfo, STKConfig dataInfo)
	throws greenFrameDBErrorSignal
	{
		super.update(dataInfo);	
	}
	
	public void create(EventInfo eventInfo, STKConfig dataInfo)
	throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);		
	}	
	
	
}
