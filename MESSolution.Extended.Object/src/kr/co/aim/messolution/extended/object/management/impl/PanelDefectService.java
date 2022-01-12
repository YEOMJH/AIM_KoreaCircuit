package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.PanelDefect;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class PanelDefectService extends CTORMService<PanelDefect> {
	
	public static Log logger = LogFactory.getLog(PanelDefect.class);
	
	private final String historyEntity = "";
	
	public List<PanelDefect> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<PanelDefect> result = super.select(condition, bindSet, PanelDefect.class);
		
		return result;
	}
	
	public PanelDefect selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(PanelDefect.class, isLock, keySet);
	}
	
	public PanelDefect create(EventInfo eventInfo, PanelDefect dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, PanelDefect dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public PanelDefect modify(EventInfo eventInfo, PanelDefect dataInfo)
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
}
