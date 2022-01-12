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
import kr.co.aim.messolution.extended.object.management.data.SorterSignReserve;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.exception.ErrorSignal;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

public class SorterSignReserveService extends CTORMService<SorterSignReserve> {
	
	private static Log logger = LogFactory.getLog(SorterSignReserveService.class);
	
	private final String historyEntity = "SorterSignReserveHistory";
	
	public List<SorterSignReserve> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<SorterSignReserve> result = super.select(condition, bindSet, SorterSignReserve.class);
		
		return result;
	}
	
	public SorterSignReserve selectByKey(boolean isLock, Object[] keySet)throws CustomException
	{
		return super.selectByKey(SorterSignReserve.class, isLock, keySet);
	}
	
	public SorterSignReserve create(EventInfo eventInfo, SorterSignReserve dataInfo)throws CustomException
	{
		dataInfo = setDataFromEventInfo(eventInfo, dataInfo);
		
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void create(EventInfo eventInfo, List<SorterSignReserve> dataInfoList)throws CustomException
	{
		dataInfoList = this.setDataFromEventInfo(eventInfo, dataInfoList);
		
		super.insert(dataInfoList);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
	}
	
	public void remove(EventInfo eventInfo, SorterSignReserve dataInfo)throws CustomException
	{
		dataInfo = this.setDataFromEventInfo(eventInfo, dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public void remove(EventInfo eventInfo, List<SorterSignReserve> dataInfo)throws CustomException
	{
		dataInfo = this.setDataFromEventInfo(eventInfo, dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public void remove(EventInfo eventInfo, String lotName)throws CustomException
	{
		if (!StringUtil.isNotEmpty(lotName))
		{
			logger.info("SorterSignReserveService.RemoveBatch: Input LotName is null or Empty.");
			return;
		}

		List<SorterSignReserve> dataInfoList = getDataInfoListByLotName(lotName);
		
		if (dataInfoList == null || dataInfoList.size() == 0) return;

		dataInfoList = this.setDataFromEventInfo(eventInfo, dataInfoList);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
		super.delete(dataInfoList);

		//greenFrameServiceProxy.getSqlTemplate().update("DELETE FROM CT_SorterSignReserve WHERE LOTNAME IN (:LOTNAMELIST ) ",bindMap);
	}
	
	public SorterSignReserve modify(EventInfo eventInfo, SorterSignReserve dataInfo)throws CustomException
	{
		dataInfo = setDataFromEventInfo(eventInfo, dataInfo);
		
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
	
	public void modify(EventInfo eventInfo, List<SorterSignReserve> dataInfoList)throws CustomException
	{
		dataInfoList = setDataFromEventInfo(eventInfo, dataInfoList);
		
		super.update(dataInfoList);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
	} 
	
	private SorterSignReserve setDataFromEventInfo(EventInfo eventInfo, SorterSignReserve dataInfo)
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
	
	private List<SorterSignReserve> setDataFromEventInfo(EventInfo eventInfo, List<SorterSignReserve> dataInfoList)
	{
		for(SorterSignReserve dataInfo : dataInfoList)
		{
			dataInfo = setDataFromEventInfo(eventInfo, dataInfo);
		}
		
		return dataInfoList;
	}
	
	
	public List<SorterSignReserve> getDataInfoListByLotName(String lotName) throws CustomException
	{
		logger.info("getDataInfoListByLotName: Input LotName size is " + lotName ==null?0:1);
		
		if(lotName.isEmpty()) return null;
		
		String sql = " SELECT * FROM CT_SORTERSIGNRESERVE WHERE 1=1 AND LOTNAME = :LOTNAME ";
		
		Map<String,Object> bindMap = new HashMap<>();
		bindMap.put("LOTNAME", lotName);
		
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
	
	public List<SorterSignReserve> getDataInfoListByProductName(String productName) throws CustomException
	{
		logger.info("getDataInfoListByLotName: Input productName size is " + productName ==null?0:1);
		
		if(productName.isEmpty()) return null;
		
		String sql = " SELECT * FROM CT_SORTERSIGNRESERVE WHERE 1=1 AND PRODUCTNAME = :PRODUCTNAME ";
		
		Map<String,Object> bindMap = new HashMap<>();
		bindMap.put("PRODUCTNAME", productName);
		
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
	
	public List<SorterSignReserve> getDataInfoListByProductNameAndOper(String productName,String processOperationName) throws CustomException
	{
		if(productName.isEmpty() || processOperationName.isEmpty()) return null;
		
		String sql = " SELECT * FROM CT_SORTERSIGNRESERVE WHERE 1=1 AND PRODUCTNAME = :PRODUCTNAME AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME";
		
		Map<String,Object> bindMap = new HashMap<>();
		bindMap.put("PRODUCTNAME", productName);
		bindMap.put("PROCESSOPERATIONNAME", processOperationName);
		
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
	
	public List<SorterSignReserve> transform (List resultList)
	{
		if (resultList==null || resultList.size() == 0)
		{
			return null;
		}
		
		Object result = super.ormExecute( CTORMUtil.createDataInfo(SorterSignReserve.class), resultList);

		if ((result instanceof List))
		{
			return (List) result;
		}

		List<SorterSignReserve> resultSet = new ArrayList();
		resultSet.add((SorterSignReserve) result);
		return resultSet;
	}
}
