package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.extended.object.management.data.R2RFeedbackDEPOHist;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class R2RFeedbackDEPOHistService extends CTORMService<R2RFeedbackDEPOHist> 
{
	public static Log logger = LogFactory.getLog(R2RFeedbackDEPOHistService.class);

	public List<R2RFeedbackDEPOHist> select(String condition, Object[] bindSet) throws CustomException
	{
		List<R2RFeedbackDEPOHist> result = super.select(condition, bindSet, R2RFeedbackDEPOHist.class);

		return result;
	}

	public R2RFeedbackDEPOHist selectByKey(boolean isLock, Object[] keySet) throws CustomException
	{
		return super.selectByKey(R2RFeedbackDEPOHist.class, isLock, keySet);
	}

	public R2RFeedbackDEPOHist create(EventInfo eventInfo, R2RFeedbackDEPOHist dataInfo) throws CustomException
	{
		super.insert(dataInfo);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void insert(EventInfo eventInfo, List<R2RFeedbackDEPOHist> dataInfoList) throws CustomException
	{
		dataInfoList = setDataFromEventInfo(eventInfo, dataInfoList);
		
		super.insert(dataInfoList);
	}

	public void remove(EventInfo eventInfo, R2RFeedbackDEPOHist dataInfo) throws CustomException
	{
		super.delete(dataInfo);
	}

	public R2RFeedbackDEPOHist modify(EventInfo eventInfo, R2RFeedbackDEPOHist dataInfo) throws CustomException
	{
		super.update(dataInfo);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	private R2RFeedbackDEPOHist setDataFromEventInfo(EventInfo eventInfo, R2RFeedbackDEPOHist dataInfo)
	{
		String eventTimekey = null;
		if(StringUtils.isNotEmpty(eventInfo.getLastEventTimekey()))
		{
			eventTimekey = eventInfo.getLastEventTimekey();
		}
		else if(StringUtils.isNotEmpty(eventInfo.getEventTimeKey()))
		{
			eventTimekey = eventInfo.getEventTimeKey();
		}
		else
		{
			eventTimekey = TimeStampUtil.getCurrentEventTimeKey();
		}
		
		dataInfo.setEventName(eventInfo.getEventName());
		dataInfo.setEventTime(eventInfo.getEventTime());
		dataInfo.setEventUser(eventInfo.getEventUser());
		dataInfo.setEventComment(eventInfo.getEventComment());
		
		return dataInfo;
	}
	
	private List<R2RFeedbackDEPOHist> setDataFromEventInfo(EventInfo eventInfo, List<R2RFeedbackDEPOHist> dataInfoList)
	{
		for(R2RFeedbackDEPOHist dataInfo : dataInfoList)
		{
			dataInfo = setDataFromEventInfo(eventInfo, dataInfo);
		}
		
		return dataInfoList;
	}

	public List<R2RFeedbackDEPOHist> tranform(List resultList)
	{
		if (resultList == null || resultList.size() == 0)
		{
			return null;
		}
		Object result = super.ormExecute(CTORMUtil.createDataInfo(R2RFeedbackDEPOHist.class), resultList);

		if ((result instanceof List))
		{
			return (List) result;
		}

		List<R2RFeedbackDEPOHist> resultSet = new ArrayList();
		resultSet.add((R2RFeedbackDEPOHist) result);
		return resultSet;
	}
}
