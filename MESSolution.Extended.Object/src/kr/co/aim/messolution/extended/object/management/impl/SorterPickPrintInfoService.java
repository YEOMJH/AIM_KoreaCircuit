package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.SorterPickPrintInfo;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.exception.ErrorSignal;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class SorterPickPrintInfoService extends CTORMService<SorterPickPrintInfo> {
	
	public static Log logger = LogFactory.getLog(SorterPickPrintInfoService.class);
	
	private final String historyEntity = "SorterPickPrintInfoHist";
	
	public List<SorterPickPrintInfo> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<SorterPickPrintInfo> result = super.select(condition, bindSet, SorterPickPrintInfo.class);
		
		return result;
	}
	
	public SorterPickPrintInfo selectByKey(boolean isLock, Object[] keySet)throws CustomException
	{
		return super.selectByKey(SorterPickPrintInfo.class, isLock, keySet);
	}
	
	public SorterPickPrintInfo create(EventInfo eventInfo, SorterPickPrintInfo dataInfo)throws CustomException
	{
		dataInfo = setDataFromEventInfo(eventInfo, dataInfo);
		
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void create(EventInfo eventInfo, List<SorterPickPrintInfo> dataInfoList)throws CustomException
	{
		dataInfoList = this.setDataFromEventInfo(eventInfo, dataInfoList);
		
		super.insert(dataInfoList);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
	}
	
	public void remove(EventInfo eventInfo, SorterPickPrintInfo dataInfo)throws CustomException
	{
		dataInfo = this.setDataFromEventInfo(eventInfo, dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public void remove(EventInfo eventInfo ,String lotName)throws CustomException
	{
		if (lotName == null || lotName.isEmpty())
		{
			logger.info("SorterPickPrintInfoService.Remove: Input LotName is null or Empty.");
			return ;
		}
			
		SorterPickPrintInfo dataInfo = this.getDataInfoByKey(lotName,false);
		
		if(dataInfo==null) return;
	    this.remove(eventInfo, dataInfo);
		
		//greenFrameServiceProxy.getSqlTemplate().update("DELETE FROM CT_SORTERPICKPRINTINFO WHERE LOTNAME = ? ", new Object[]{lotName});
	}
	
	public void remove(EventInfo eventInfo, List<String> lotNameList)throws CustomException
	{
		if (lotNameList == null || lotNameList.size() == 0)
		{
			logger.info("SorterPickPrintInfoService.RemoveBatch: Input LotNameList is null or Empty.");
			return;
		}

		List<SorterPickPrintInfo> dataInfoList = getDataInfoListByLotNameList(lotNameList);
		
		if (dataInfoList == null || dataInfoList.size() == 0) return;

		dataInfoList = this.setDataFromEventInfo(eventInfo, dataInfoList);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
		super.delete(dataInfoList);

		//greenFrameServiceProxy.getSqlTemplate().update("DELETE FROM CT_SORTERPICKPRINTINFO WHERE LOTNAME IN (:LOTNAMELIST ) ",bindMap);
	}
	
	public SorterPickPrintInfo modify(EventInfo eventInfo, SorterPickPrintInfo dataInfo)throws CustomException
	{
		dataInfo = setDataFromEventInfo(eventInfo, dataInfo);
		
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
	
	public void modify(EventInfo eventInfo, List<SorterPickPrintInfo> dataInfoList)throws CustomException
	{
		dataInfoList = setDataFromEventInfo(eventInfo, dataInfoList);
		
		super.update(dataInfoList);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
	} 
	
	private SorterPickPrintInfo setDataFromEventInfo(EventInfo eventInfo, SorterPickPrintInfo dataInfo)
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
	
	private List<SorterPickPrintInfo> setDataFromEventInfo(EventInfo eventInfo, List<SorterPickPrintInfo> dataInfoList)
	{
		for(SorterPickPrintInfo dataInfo : dataInfoList)
		{
			dataInfo = setDataFromEventInfo(eventInfo, dataInfo);
		}
		
		return dataInfoList;
	}
	
	public SorterPickPrintInfo getDataInfoByKey(String lotName,boolean throwException) throws CustomException
	{
		if (logger.isInfoEnabled())
			logger.info(String.format("getDataInfoByKey: Input argument LotName is [%s] .", lotName));

		if(lotName ==null || lotName.isEmpty()) return null;
		
		SorterPickPrintInfo dataInfo = null;
		try
		{
			dataInfo = super.selectByKey(SorterPickPrintInfo.class, false, new Object[] {lotName });
		}
		catch (greenFrameDBErrorSignal dbError)
		{
			if (dbError.getErrorCode().equals(ErrorSignal.NotFoundSignal))
			{
				if (throwException)
					throw new CustomException("COMM-1000", "SorterPickPrintInfo", " LotName = " + lotName);
				else
					logger.info(String.format("SorterPick Data Information is not registered.condition by LotName = %s].", lotName));
			}
			else
			{
				throw new CustomException(dbError.getCause());
			}
		}

		return dataInfo;
	}
	
	public List<SorterPickPrintInfo> getDataInfoListByLotNameList(List<String> lotNameList) throws CustomException
	{
		logger.info("getDataInfoListByLotNameList: Input LotNameList size is " + lotNameList ==null?0:lotNameList.size());
		
		if(lotNameList ==null || lotNameList.size()==0) return null;
		
		String sql = " SELECT * FROM CT_SORTERPICKPRINTINFO WHERE 1=1 AND LOTNAME IN (:LOTNAMELIST) ";
		
		Map<String,Object> bindMap = new HashMap<>();
		bindMap.put("LOTNAMELIST", lotNameList);
		
		List<Map<String,Object>> resultList = null;
		
		try
		{
			resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		}
		catch (Exception ex)
		{
			throw new CustomException(ex.getCause());
		}
		
	    if(resultList == null || resultList.size()==0) return null;
	    
	    return this.transform(resultList);
	}
	
	public List<SorterPickPrintInfo> transform (List resultList)
	{
		if (resultList==null || resultList.size() == 0)
		{
			return null;
		}
		
		Object result = super.ormExecute( CTORMUtil.createDataInfo(SorterPickPrintInfo.class), resultList);

		if ((result instanceof List))
		{
			return (List) result;
		}

		List<SorterPickPrintInfo> resultSet = new ArrayList();
		resultSet.add((SorterPickPrintInfo) result);
		return resultSet;
	}
}
