package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.EvaLineWo;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;


public class EvaLineWoService  extends CTORMService<EvaLineWo>{
	
	private static Log log = LogFactory.getLog(EvaLineWoService.class);
	
	public static Log logger = LogFactory.getLog(EvaLineWo.class);
	
	private final String historyEntity = "EvaLineWoHistory";

	ConstantMap constantMap = GenericServiceProxy.getConstantMap();
	
	public List<EvaLineWo> select(String condition, Object[] bindSet)
			throws greenFrameDBErrorSignal
	{
		List<EvaLineWo> result = super.select(condition, bindSet, EvaLineWo.class);
		
		return result;
	}
	
	public EvaLineWo selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(EvaLineWo.class, isLock, keySet);
	}
	
	public EvaLineWo create(EventInfo eventInfo, EvaLineWo dataInfo)
		throws greenFrameDBErrorSignal
	{
		dataInfo = setDataFromEventInfo(eventInfo, dataInfo);
		
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void create(EventInfo eventInfo, List<EvaLineWo> dataInfoList)
			throws greenFrameDBErrorSignal
	{		
		dataInfoList = setDataFromEventInfo(eventInfo, dataInfoList);
		
		super.insert(dataInfoList);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
	}
	
	public void remove(EventInfo eventInfo, EvaLineWo dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}

	public EvaLineWo modify(EventInfo eventInfo, EvaLineWo dataInfo)
	{
		dataInfo = setDataFromEventInfo(eventInfo, dataInfo);
		
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void modify(EventInfo eventInfo, List<EvaLineWo> dataInfoList)
	{
		dataInfoList = setDataFromEventInfo(eventInfo, dataInfoList);
		
		super.update(dataInfoList);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
	}
	
	private EvaLineWo setDataFromEventInfo(EventInfo eventInfo, EvaLineWo dataInfo)
	{
		String eventTimekey = null;
		if(StringUtil.isNotEmpty(eventInfo.getLastEventTimekey()))
		{
			eventTimekey = eventInfo.getLastEventTimekey();
		}
		else if(StringUtil.isNotEmpty(eventInfo.getEventTimeKey()))
		{
			eventTimekey = eventInfo.getEventTimeKey();
		}
		else
		{
			eventTimekey = TimeStampUtil.getCurrentEventTimeKey();
		}
		
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventTimeKey(eventTimekey);
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		
		return dataInfo;
	}
	
	private List<EvaLineWo> setDataFromEventInfo(EventInfo eventInfo, List<EvaLineWo> dataInfoList)
	{
		for(EvaLineWo dataInfo : dataInfoList)
		{
			dataInfo = setDataFromEventInfo(eventInfo, dataInfo);
		}
		
		return dataInfoList;
	}
	
	


}
