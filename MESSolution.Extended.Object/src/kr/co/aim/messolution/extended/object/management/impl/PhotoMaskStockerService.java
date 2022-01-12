package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.PhotoMaskStocker;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.greenframe.exception.ErrorSignal;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class PhotoMaskStockerService extends CTORMService<PhotoMaskStocker> {
	private static Log log = LogFactory.getLog(PhotoMaskStockerService.class);
	
	public static Log logger = LogFactory.getLog(PhotoMaskStocker.class);
	
	private final String historyEntity = "PhotoMaskStockerHistory";

	ConstantMap constantMap = GenericServiceProxy.getConstantMap();
	
	public List<PhotoMaskStocker> select(String condition, Object[] bindSet)
			throws greenFrameDBErrorSignal
	{
		List<PhotoMaskStocker> result = super.select(condition, bindSet, PhotoMaskStocker.class);
		
		return result;
	}
	
	public PhotoMaskStocker selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(PhotoMaskStocker.class, isLock, keySet);
	}
	
	public PhotoMaskStocker create(EventInfo eventInfo, PhotoMaskStocker dataInfo)
		throws greenFrameDBErrorSignal
	{
		dataInfo = setDataFromEventInfo(eventInfo, dataInfo);
		
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void create(EventInfo eventInfo, List<PhotoMaskStocker> dataInfoList)
			throws greenFrameDBErrorSignal
	{		
		dataInfoList = setDataFromEventInfo(eventInfo, dataInfoList);
		
		super.insert(dataInfoList);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
	}
	
	public void remove(EventInfo eventInfo, PhotoMaskStocker dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}

	public PhotoMaskStocker modify(EventInfo eventInfo, PhotoMaskStocker dataInfo)
	{
		dataInfo = setDataFromEventInfo(eventInfo, dataInfo);
		
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void modify(EventInfo eventInfo, List<PhotoMaskStocker> dataInfoList)
	{
		dataInfoList = setDataFromEventInfo(eventInfo, dataInfoList);
		
		super.update(dataInfoList);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
	}
	
	public void modify(List<PhotoMaskStocker> dataInfoList)
	{
		super.update(dataInfoList);
		super.addHistory(new EventInfo(), this.historyEntity, dataInfoList, logger);
	}
	
	private PhotoMaskStocker setDataFromEventInfo(EventInfo eventInfo, PhotoMaskStocker dataInfo)
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
		
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventTimeKey(eventTimekey);
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		
		return dataInfo;
	}
	
	private List<PhotoMaskStocker> setDataFromEventInfo(EventInfo eventInfo, List<PhotoMaskStocker> dataInfoList)
	{
		for(PhotoMaskStocker dataInfo : dataInfoList)
		{
			dataInfo = setDataFromEventInfo(eventInfo, dataInfo);
		}
		
		return dataInfoList;
	}
	
	public PhotoMaskStocker getPhotoMaskStockerData(String stockerName,String slotId) throws CustomException
	{
		if (log.isInfoEnabled())
			log.info(String.format("getPhotoMaskStockerData: Input StockerName [%s] , SlotId [%s] .", stockerName, slotId));

		PhotoMaskStocker dataInfo = null;
		
		try
		{
			dataInfo = this.selectByKey(false, new Object[] { stockerName, slotId });
		}
		catch (greenFrameDBErrorSignal dbError)
		{
			if (dbError.getErrorCode().equals(ErrorSignal.NotFoundSignal))
				throw new CustomException("COMM-0010", "PhotoMaskStocker", String.format("StockerName = %s , SlotId = %s", stockerName, slotId));
			else
				throw new CustomException(dbError.getCause());
		}
		
		return dataInfo;
	}
	
	public PhotoMaskStocker getPhotoMaskStockerDataForUpdate(String stockerName,String slotId) throws CustomException
	{
		if (log.isInfoEnabled())
			log.info(String.format("getPhotoMaskStockerData: Input StockerName [%s] , SlotId [%s] .", stockerName, slotId));

		PhotoMaskStocker dataInfo = null;
		
		try
		{
			dataInfo = this.selectByKey(true, new Object[] { stockerName, slotId });
		}
		catch (greenFrameDBErrorSignal dbError)
		{
			if (dbError.getErrorCode().equals(ErrorSignal.NotFoundSignal))
				throw new CustomException("COMM-0010", "PhotoMaskStocker", String.format("StockerName = %s , SlotId = %s", stockerName, slotId));
			else
				throw new CustomException(dbError.getCause());
		}
		
		return dataInfo;
	}
	
	public List<PhotoMaskStocker> getDataInfoByLineName(String lineName) throws CustomException
	{
		if (log.isInfoEnabled())
			log.info("getDataInfoByLineName: LineName is " + lineName);

		List<PhotoMaskStocker> resultList = null;

		try
		{
			resultList = this.select(" WHERE 1=1 AND LINENAME = ? ", new Object[] { lineName });
		}
		catch (greenFrameDBErrorSignal dbError)
		{
			if (dbError.getErrorCode().equals(ErrorSignal.NotFoundSignal))
				throw new CustomException("COMM-0010", "PhotoMaskStocker", "LineName = " + lineName);
			else
				throw new CustomException(dbError.getCause());
		}

		return resultList;
	}
}
