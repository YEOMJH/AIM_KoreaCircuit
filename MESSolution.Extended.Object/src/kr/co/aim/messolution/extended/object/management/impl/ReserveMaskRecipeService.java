package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.extended.object.management.data.ReserveMaskRecipe;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

public class ReserveMaskRecipeService extends CTORMService<ReserveMaskRecipe>
{
	public static Log logger = LogFactory.getLog(ReserveMaskRecipeService.class);

	private final String historyEntity = "ReserveMaskRecipeHist";

	public List<ReserveMaskRecipe> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		return select(condition, bindSet, ReserveMaskRecipe.class);
	}

	public ReserveMaskRecipe selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return selectByKey(ReserveMaskRecipe.class, isLock, keySet);
	}

	public ReserveMaskRecipe create(EventInfo eventInfo, ReserveMaskRecipe dataInfo) throws greenFrameDBErrorSignal
	{
		dataInfo = setDataFromEventInfo(eventInfo, dataInfo);
		
		addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		insert(dataInfo);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public int[] create(EventInfo eventInfo, List<ReserveMaskRecipe> dataInfoList) throws greenFrameDBErrorSignal
	{
		dataInfoList = setDataFromEventInfo(eventInfo, dataInfoList);
		
		addHistory(eventInfo, this.historyEntity, dataInfoList, logger);

		return insert(dataInfoList);
	}

	public ReserveMaskRecipe modify(EventInfo eventInfo, ReserveMaskRecipe dataInfo) throws greenFrameDBErrorSignal
	{
		dataInfo = setDataFromEventInfo(eventInfo, dataInfo);
		
		addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		update(dataInfo);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public int[] modify(EventInfo eventInfo, List<ReserveMaskRecipe> dataInfoList) throws greenFrameDBErrorSignal
	{
		dataInfoList = setDataFromEventInfo(eventInfo, dataInfoList);
		
		addHistory(eventInfo, this.historyEntity, dataInfoList, logger);

		return update(dataInfoList);
	}

	public void remove(EventInfo eventInfo, ReserveMaskRecipe dataInfo) throws greenFrameDBErrorSignal
	{
		dataInfo = setDataFromEventInfo(eventInfo, dataInfo);
		
		addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		delete(dataInfo);
	}

	public void remove(EventInfo eventInfo, List<ReserveMaskRecipe> dataInfoList) throws greenFrameDBErrorSignal
	{
		dataInfoList = setDataFromEventInfo(eventInfo, dataInfoList);
		
		addHistory(eventInfo, this.historyEntity, dataInfoList, logger);

		delete(dataInfoList);
	}
	
	private ReserveMaskRecipe setDataFromEventInfo(EventInfo eventInfo, ReserveMaskRecipe dataInfo)
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
		dataInfo.setLastEventTimekey(eventTimekey);
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		
		return dataInfo;
	}
	
	private List<ReserveMaskRecipe> setDataFromEventInfo(EventInfo eventInfo, List<ReserveMaskRecipe> dataInfoList)
	{
		for(ReserveMaskRecipe dataInfo : dataInfoList)
		{
			dataInfo = setDataFromEventInfo(eventInfo, dataInfo);
		}
		
		return dataInfoList;
	}

	public static Log getLogger()
	{
		return logger;
	}

	public String getHistoryEntity()
	{
		return historyEntity;
	}
}
