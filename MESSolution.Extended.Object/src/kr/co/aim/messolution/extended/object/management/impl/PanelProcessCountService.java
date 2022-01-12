package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.PanelProcessCount;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.greenframe.exception.ErrorSignal;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.GenericServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.master.EnumDefValue;
import kr.co.aim.greentrack.lot.management.data.Lot;

public class PanelProcessCountService extends CTORMService<PanelProcessCount> {
	
	public static Log logger = LogFactory.getLog(PanelProcessCountService.class);
	
	private final String historyEntity = "PanelProcessCountHistory";
	
	public List<PanelProcessCount> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<PanelProcessCount> result = super.select(condition, bindSet, PanelProcessCount.class);
		
		return result;
	}
	
	public PanelProcessCount selectByKey(boolean isLock, Object[] keySet)throws CustomException
	{
		return super.selectByKey(PanelProcessCount.class, isLock, keySet);
	}
	
	public PanelProcessCount create(EventInfo eventInfo, PanelProcessCount dataInfo)throws CustomException
	{
		dataInfo = setDataFromEventInfo(eventInfo, dataInfo);
		
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, PanelProcessCount dataInfo)throws CustomException
	{
		dataInfo = setDataFromEventInfo(eventInfo, dataInfo);
		
		super.delete(dataInfo);
		
		super.addHistory(eventInfo, historyEntity, dataInfo, logger);
	}
	
	public PanelProcessCount modify(EventInfo eventInfo, PanelProcessCount dataInfo)throws CustomException
	{
		dataInfo = setDataFromEventInfo(eventInfo, dataInfo);
		
		super.update(dataInfo);

		super.addHistory(eventInfo, historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
	
	private PanelProcessCount setDataFromEventInfo(EventInfo eventInfo, PanelProcessCount dataInfo)
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
	
	private List<PanelProcessCount> setDataFromEventInfo(EventInfo eventInfo, List<PanelProcessCount> dataInfoList)
	{
		for(PanelProcessCount dataInfo : dataInfoList)
		{
			dataInfo = setDataFromEventInfo(eventInfo, dataInfo);
		}
		
		return dataInfoList;
	}
	
	public List<PanelProcessCount> getDataListByPanelName(String panelName,boolean throwException) throws CustomException
	{
		if (logger.isInfoEnabled())
			logger.info("getDataListByPanelName: Input argument PanelName is " + panelName);

		List<PanelProcessCount> resultList = null;
		try
		{
			resultList = super.select("WHERE 1=1 AND LOTNAME = ? ", new Object[] { panelName }, PanelProcessCount.class);
		}
		catch (greenFrameDBErrorSignal dbError)
		{
			if (dbError.getErrorCode().equals(ErrorSignal.NotFoundSignal))
			{
				if (throwException)
					throw new CustomException("COMM-1000", "PhotoProcessCount", " oldLotData.getKey().getLotName() = " + panelName);
				else
					logger.info(String.format("PhotoProcessCount Data Information is not registered.condition by [oldLotData.getKey().getLotName() = %s].", panelName));
			}
			else
			{
				throw new CustomException(dbError.getCause());
			}
		}

		return resultList;
	}
	
	public PanelProcessCount getDataInfoByKey(String panelName,String detailProcessOperationType,boolean throwException) throws CustomException
	{
		if (logger.isInfoEnabled())
			logger.info(String.format("getDataInfoByKey: Input argument PanelName is [%s] , DetailProcessOperationType is [%s].", panelName, detailProcessOperationType));

		if(panelName.isEmpty() || detailProcessOperationType.isEmpty()) return null;
		
		PanelProcessCount dataInfo = null;
		try
		{
			dataInfo = super.selectByKey(PanelProcessCount.class, false, new Object[] { panelName, detailProcessOperationType });
		}
		catch (greenFrameDBErrorSignal dbError)
		{
			if (dbError.getErrorCode().equals(ErrorSignal.NotFoundSignal))
			{
				if (throwException)
					throw new CustomException("COMM-1000", "PhotoProcessCount", " oldLotData.getKey().getLotName() = " + panelName);
				else
					logger.info(String.format("PhotoProcessCount Data Information is not registered.condition by [oldLotData.getKey().getLotName() = %s].", panelName));
			}
			else
			{
				throw new CustomException(dbError.getCause());
			}
		}

		return dataInfo;
	}
	
	public  Map<String,String> getProcessLimitConfiguration(String enumName) throws CustomException
	{
		List<Map<String, Object>> resultList = null;

		try
		{
			resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(" SELECT ENUMVALUE,SEQ FROM ENUMDEFVALUE WHERE ENUMNAME = ? ORDER BY SEQ ", new Object[] { enumName });
		}
		catch (Exception ex)
		{
			throw new CustomException(ex.getCause());
		}

		if (resultList == null || resultList.size() == 0) return null;

		Map<String, String> returnMap = new HashMap<String, String>();

		for (Map<String, Object> resultMap : resultList)
		{
			returnMap.put(ConvertUtil.getMapValueByName(resultMap, "ENUMVALUE"), ConvertUtil.getMapValueByName(resultMap, "SEQ"));
		}

		return returnMap;
	}
	
	// Req ID. PC-Tech-0026-01: Panel Process count management
	public void setPanelProcessCount(EventInfo eventInfo,Lot oldLotData,String detailProcessOperationType,String processLimit) throws CustomException
	{
		if (oldLotData == null)
		{
			logger.info("setPanelProcessCount: Input Lot data is null.");
			return;
		}

		PanelProcessCount ppcData = this.getDataInfoByKey(oldLotData.getKey().getLotName(), detailProcessOperationType, false);

		if (ppcData != null)
		{
			// modify process limit by enumvalue
			if (CommonValidation.isNumeric(processLimit) && ppcData.getProcessLimit().intValue() != Integer.parseInt(processLimit))
			{
				logger.info(String.format("Update ProcessLimit: Lot [%s] , DetailProcessOperationType [%s] , ProcessLimit [EnumValue(%s) To CurrentData(%s)]", oldLotData.getKey().getLotName(),
																																							   detailProcessOperationType, processLimit, ppcData.getProcessLimit()));
				eventInfo.setEventName("Update");

				ppcData.setProcessLimit(Integer.parseInt(processLimit));
				this.modify(eventInfo, ppcData);
			}
			else
			{
				logger.info(String.format("Get EnumDefValue: EnumName[%s] , EnumValue[%s] ,SEQ [%s]", "ProcessLimit", detailProcessOperationType, processLimit));
			}

			// udpate process count when processLimit > processCount
			if (ppcData.getProcessLimit().intValue() > ppcData.getProcessCount().intValue())
			{
				eventInfo.setEventName("Update");

				ppcData.setProcessCount(ppcData.getProcessCount().intValue() + 1);
				this.modify(eventInfo, ppcData);
			}
			else
			{
				logger.info(String.format("Lot[%s|%s]: ProcessLimit(%s) , ProcessCount(%s).", oldLotData.getKey().getLotName(), detailProcessOperationType, 
																							   ppcData.getProcessLimit(),ppcData.getProcessCount()));
			}
		}
		else
		{
			ppcData = new PanelProcessCount();
			ppcData.setLotName(oldLotData.getKey().getLotName());
			ppcData.setDetailProcessOperationType(detailProcessOperationType);
			ppcData.setFactoryName(oldLotData.getFactoryName());
			ppcData.setProductSpecName(oldLotData.getProductSpecName());
			ppcData.setProductSpecVersion(oldLotData.getProductSpecVersion());
			ppcData.setProcessFlowName(oldLotData.getProcessFlowName());
			ppcData.setProcessFlowVersion(oldLotData.getProcessFlowVersion());
			ppcData.setProcessCount(1);
			ppcData.setProcessLimit(0);

			if (CommonValidation.isNumeric(processLimit))
				ppcData.setProcessLimit(Integer.parseInt(processLimit));
			else
				logger.info(String.format("Invalid Process limit.[EnumName = %s , EnumValue = %s , SEQ = %S ]", "ProcessLimit", detailProcessOperationType, processLimit));

			eventInfo.setEventName("Create");
			this.create(eventInfo, ppcData);
		}
	}
	
	public void checkPanelProcessCount(String lotName,String detailProcessOperationType) throws CustomException
	{
		PanelProcessCount dataInfo =  this.getDataInfoByKey(lotName, detailProcessOperationType, false);
		
		if(dataInfo == null) return ;
		
		// LOT-0153:ProcessCount of the Inspection is out of limit.[Lot:%s,DetailOperType:%s,ProcessLimit:%s,ProcessCount:%s]
		if(dataInfo.getProcessLimit().intValue()<= dataInfo.getProcessCount().intValue())
			throw new CustomException("LOT-0153",lotName,detailProcessOperationType,dataInfo.getProcessLimit(),dataInfo.getProcessCount());
	}
}
