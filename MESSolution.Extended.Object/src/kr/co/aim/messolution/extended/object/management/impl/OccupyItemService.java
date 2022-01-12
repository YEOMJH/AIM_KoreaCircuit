package kr.co.aim.messolution.extended.object.management.impl;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.OccupyItem;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.greenframe.exception.ErrorSignal;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

public class OccupyItemService extends CTORMService<OccupyItem> {
	private static Log logger = LogFactory.getLog(OccupyItemService.class);
	
	private final String historyEntity = "OccupyItemHistory";

	ConstantMap constantMap = GenericServiceProxy.getConstantMap();
	
	public List<OccupyItem> select(String condition, Object[] bindSet)
			throws greenFrameDBErrorSignal
	{
		List<OccupyItem> result = super.select(condition, bindSet, OccupyItem.class);
		
		return result;
	}
	
	public OccupyItem selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(OccupyItem.class, isLock, keySet);
	}
	
	public OccupyItem create(EventInfo eventInfo, OccupyItem dataInfo)
		throws greenFrameDBErrorSignal
	{
		dataInfo = setDataFromEventInfo(eventInfo, dataInfo);
		
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void create(EventInfo eventInfo, List<OccupyItem> dataInfoList)
			throws greenFrameDBErrorSignal
	{		
		dataInfoList = setDataFromEventInfo(eventInfo, dataInfoList);
		
		super.insert(dataInfoList);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
	}
	
	public void remove(EventInfo eventInfo, OccupyItem dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}

	public OccupyItem modify(EventInfo eventInfo, OccupyItem dataInfo)
	{
		dataInfo = setDataFromEventInfo(eventInfo, dataInfo);
		
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void modify(EventInfo eventInfo, List<OccupyItem> dataInfoList)
	{
		dataInfoList = setDataFromEventInfo(eventInfo, dataInfoList);
		
		super.update(dataInfoList);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
	}
	
	private OccupyItem setDataFromEventInfo(EventInfo eventInfo, OccupyItem dataInfo)
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
	
	private List<OccupyItem> setDataFromEventInfo(EventInfo eventInfo, List<OccupyItem> dataInfoList)
	{
		for(OccupyItem dataInfo : dataInfoList)
		{
			dataInfo = setDataFromEventInfo(eventInfo, dataInfo);
		}
		
		return dataInfoList;
	}
	
	private OccupyItem setOccupyItem(String state, OccupyItem itemData, EventInfo eventInfo, Timestamp currentTime, String uiName, String menuName)
	{
		//set expire time
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(currentTime.getTime());
		calendar.add(Calendar.HOUR, 1);	//set occupation time to 1 hour
		Timestamp expireTime = new java.sql.Timestamp(calendar.getTime().getTime());
		
		itemData.setUserId(eventInfo.getEventUser());
		itemData.setState(state);
		itemData.setStartTime(currentTime);
		itemData.setExpireTime(expireTime);
		itemData.setUiName(uiName);
		itemData.setMenuName(menuName);
		itemData.setLastEventTime(eventInfo.getEventTime());
		itemData.setLastEventTimeKey(eventInfo.getEventTimeKey());
		itemData.setLastEventName(eventInfo.getEventName());
		itemData.setLastEventUser(eventInfo.getEventUser());
		itemData.setLastEventComment(eventInfo.getEventComment());
		
		return itemData;
	}
	
	public OccupyItem createOccupyItem(String state, OccupyItem itemData, 
		EventInfo eventInfo, Timestamp currentTime, String uiName, String menuName)
	{
		itemData = setOccupyItem(state, itemData, eventInfo, currentTime, uiName, menuName);
		
		create(eventInfo, itemData);
				
		return itemData;
	}

	public List<OccupyItem> createOccupyItem(String state, List<OccupyItem> itemList, 
		EventInfo eventInfo, Timestamp currentTime, String uiName, String menuName)
	{		
		for(OccupyItem itemData : itemList)
		{
			itemData = setOccupyItem(state, itemData, eventInfo, currentTime, uiName, menuName);
		}
		
		create(eventInfo, itemList);
				
		return itemList;
	}
	
	public OccupyItem renewOccupyItem(String state, OccupyItem itemData, 
		EventInfo eventInfo, Timestamp currentTime, String uiName, String menuName)
	{
		itemData = setOccupyItem(state, itemData, eventInfo, currentTime, uiName, menuName);
		
		modify(eventInfo, itemData);
		
		return itemData;
	}
	
	public List<OccupyItem> renewOccupyItem(String state, List<OccupyItem> itemList, 
		EventInfo eventInfo, Timestamp currentTime, String uiName, String menuName)
	{
		for(OccupyItem itemData : itemList)
		{
			itemData = setOccupyItem(state, itemData, eventInfo, currentTime, uiName, menuName);
		}
		
		modify(eventInfo, itemList);
				
		return itemList;
	}
	
	public int freeOccupiedItemsByUserAndMenuName(EventInfo eventInfo, String state, String uiName, String menuName)
	{
		String condition = " WHERE USERID = ? AND STATE = ? AND UINAME = ? AND MENUNAME = ?";
		try
		{
			List<OccupyItem> itemList = select(condition, new Object[] { eventInfo.getEventUser(), state, uiName, menuName });
			for(OccupyItem itemData : itemList)
			{
				itemData.setState("Free");
			}
			
			modify(eventInfo, itemList);
			
			return itemList.size();
		}
		catch(Exception e)
		{
			return 0;
		}
	}
}
