package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.SVIPickInfo;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.exception.ErrorSignal;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

public class SVIPickInfoService extends CTORMService<SVIPickInfo> {
	
	public static Log logger = LogFactory.getLog(SVIPickInfoService.class);
	
	private final String historyEntity = "SVIPickInfoHist";
	
	public List<SVIPickInfo> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<SVIPickInfo> result = super.select(condition, bindSet, SVIPickInfo.class);
		
		return result;
	}
	
	public SVIPickInfo selectByKey(boolean isLock, Object[] keySet)throws CustomException
	{
		return super.selectByKey(SVIPickInfo.class, isLock, keySet);
	}
	
	public SVIPickInfo create(EventInfo eventInfo, SVIPickInfo dataInfo)throws CustomException
	{
		dataInfo = setDataFromEventInfo(eventInfo, dataInfo);
		
		super.insert(dataInfo);

		super.addHistory(eventInfo, historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo ,SVIPickInfo dataInfo)throws CustomException
	{
		super.delete(dataInfo);
		
		super.addHistory(eventInfo, historyEntity, dataInfo, logger);
	}
	
	public SVIPickInfo modify(EventInfo eventInfo, SVIPickInfo dataInfo)throws CustomException
	{
		dataInfo = setDataFromEventInfo(eventInfo, dataInfo);
		
		super.update(dataInfo);
		
		super.addHistory(eventInfo, historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void modify(EventInfo eventInfo, List<SVIPickInfo> dataInfoList)
	{
		dataInfoList = setDataFromEventInfo(eventInfo, dataInfoList);
		
		super.update(dataInfoList);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
	}
	
	private SVIPickInfo setDataFromEventInfo(EventInfo eventInfo, SVIPickInfo dataInfo)
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
	
	private List<SVIPickInfo> setDataFromEventInfo(EventInfo eventInfo, List<SVIPickInfo> dataInfoList)
	{
		for(SVIPickInfo dataInfo : dataInfoList)
		{
			dataInfo = setDataFromEventInfo(eventInfo, dataInfo);
		}
		
		return dataInfoList;
	}
	
	public SVIPickInfo getDataInfoByKey(String timeKey,boolean throwException) throws CustomException
	{
		if (logger.isInfoEnabled())
			logger.info(String.format("getDataInfoByKey: Input argument timeKey is [%s] .", timeKey));

		if(timeKey ==null || timeKey.isEmpty()) return null;
		
		SVIPickInfo dataInfo = null;
		try
		{
			dataInfo = super.selectByKey(SVIPickInfo.class, false, new Object[] {timeKey });
		}
		catch (greenFrameDBErrorSignal dbError)
		{
			if (dbError.getErrorCode().equals(ErrorSignal.NotFoundSignal))
			{
				if (throwException)
					throw new CustomException("COMM-1000", "SVIPickInfo", " TimeKey = " + timeKey);
				else
					logger.info(String.format("SorterPick Data Information is not registered.condition by TimeKey = %s].", timeKey));
			}
			else
			{
				throw new CustomException(dbError.getCause());
			}
		}

		return dataInfo;
	}
	
	public List<SVIPickInfo> getAvailableDataListByMO(String machineName,String operationName,String operationVersion, boolean throwException) throws CustomException
	{
		if (logger.isInfoEnabled())
			logger.info(String.format("getDataInfoByKey: Input argument is [Machine:%s|Oper:%s|OperVer:%s] .", machineName,operationName,operationVersion));

		if (StringUtil.in(StringUtil.EMPTY, machineName,operationName,operationVersion))
			return null;

		List<SVIPickInfo> dataList = null;
		try
		{
			dataList = super.select(" WHERE 1=1 AND MACHINENAME = ? AND PROCESSOPERATIONNAME = ? AND PROCESSOPERATIONVERSION = ? AND DOWNLOADFLAG = 'N' ORDER BY TIMEKEY ASC  ", new Object[] { machineName,operationName,operationVersion }, SVIPickInfo.class);
		}
		catch (greenFrameDBErrorSignal dbError)
		{
			if (dbError.getErrorCode().equals(ErrorSignal.NotFoundSignal))
			{
				if (throwException)
					throw new CustomException("COMM-1000", "SVIPickInfo", " MachineName = " + machineName);
				else
					logger.info(String.format("SorterPick Data Information is not registered.condition by MachineName = %s].", machineName));
			}
			else
			{
				throw new CustomException(dbError.getCause());
			}
		}

		return dataList;
	}
	
	public List<SVIPickInfo> transform (List resultList)
	{
		if (resultList==null || resultList.size() == 0)
		{
			return null;
		}
		
		Object result = super.ormExecute( CTORMUtil.createDataInfo(SVIPickInfo.class), resultList);

		if ((result instanceof List))
		{
			return (List) result;
		}

		List<SVIPickInfo> resultSet = new ArrayList();
		resultSet.add((SVIPickInfo) result);
		return resultSet;
	}
}
