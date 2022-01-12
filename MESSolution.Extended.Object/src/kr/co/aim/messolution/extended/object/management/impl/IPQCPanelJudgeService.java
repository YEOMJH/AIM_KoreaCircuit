package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.IPQCPanelJudge;
import kr.co.aim.messolution.extended.object.management.data.IPQCPanelJudge;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;


public class IPQCPanelJudgeService extends CTORMService<IPQCPanelJudge> {
	
	public static Log logger = LogFactory.getLog(IPQCPanelJudge.class);
	
	private final String historyEntity = "";
	
	public List<IPQCPanelJudge> select(String condition, Object[] bindSet)
			throws greenFrameDBErrorSignal
	{
		List<IPQCPanelJudge> result = super.select(condition, bindSet, IPQCPanelJudge.class);
		
		return result;
	}
	
	public IPQCPanelJudge selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(IPQCPanelJudge.class, isLock, keySet);
	}
	
	public IPQCPanelJudge create(EventInfo eventInfo, IPQCPanelJudge dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		
		//super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void create(EventInfo eventInfo, List<IPQCPanelJudge> dataInfoList)
			throws greenFrameDBErrorSignal
		{
			super.insert(dataInfoList);
			
			//super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
		}
	
	public void remove(EventInfo eventInfo, IPQCPanelJudge dataInfo)
		throws greenFrameDBErrorSignal
	{
		//super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}

	public IPQCPanelJudge modify(EventInfo eventInfo, IPQCPanelJudge dataInfo)
	{
		super.update(dataInfo);
		
		//super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void modify(EventInfo eventInfo, List<IPQCPanelJudge> dataInfoList)
	{
		super.update(dataInfoList);
		
		//super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
	}
	
	
}
