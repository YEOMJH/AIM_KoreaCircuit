package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.ScrapCrate;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class ScrapCrateService extends CTORMService<ScrapCrate> {
	
	public static Log logger = LogFactory.getLog(ScrapCrate.class);
	
	private final String historyEntity = "";
	
	public List<ScrapCrate> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<ScrapCrate> result = super.select(condition, bindSet, ScrapCrate.class);
		
		return result;
	}
	
	public ScrapCrate selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(ScrapCrate.class, isLock, keySet);
	}
	
	public ScrapCrate create(EventInfo eventInfo, ScrapCrate dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, ScrapCrate dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public ScrapCrate modify(EventInfo eventInfo, ScrapCrate dataInfo)
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
}
