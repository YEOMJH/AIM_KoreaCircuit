package kr.co.aim.messolution.extended.object.management.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.AVIPanelJudge;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class AVIPanelJudgeService extends CTORMService<AVIPanelJudge> {
	
	public static Log logger = LogFactory.getLog(AVIPanelJudge.class);
	
	private final String historyEntity = "AVIPanelJudgeHistory";
	
	public List<AVIPanelJudge> select(String condition, Object[] bindSet)
			throws greenFrameDBErrorSignal
	{
		List<AVIPanelJudge> result = super.select(condition, bindSet, AVIPanelJudge.class);
		
		return result;
	}
	
	public AVIPanelJudge selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(AVIPanelJudge.class, isLock, keySet);
	}
	
	public AVIPanelJudge create(EventInfo eventInfo, AVIPanelJudge dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void create(EventInfo eventInfo, List<AVIPanelJudge> dataInfoList)
			throws greenFrameDBErrorSignal
		{
			super.insert(dataInfoList);
			
			super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
		}
	
	public void remove(EventInfo eventInfo, AVIPanelJudge dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}

	public AVIPanelJudge modify(EventInfo eventInfo, AVIPanelJudge dataInfo)
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void modify(EventInfo eventInfo, List<AVIPanelJudge> dataInfoList)
	{
		super.update(dataInfoList);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
	}
	
	public boolean isExist(String panelName)
	{
		boolean isExist = false;
		
		String sql = "SELECT PANELNAME FROM CT_AVIPanelJudge WHERE PANELNAME = :PANELNAME ";
		Map<String, String> args = new HashMap<String, String>();
		args.put("PANELNAME", panelName);
		
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);
		
		if (result.size() > 0)
		{
			isExist = true;
		}
		
		return isExist;
	}
}
