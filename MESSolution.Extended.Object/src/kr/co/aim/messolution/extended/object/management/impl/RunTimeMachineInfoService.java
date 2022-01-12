package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.RunTimeMachineInfo;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.exception.ErrorSignal;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.management.data.Lot;

public class RunTimeMachineInfoService extends CTORMService<RunTimeMachineInfo> {
	
	public static Log logger = LogFactory.getLog(RunTimeMachineInfoService.class);
	
	private final String historyEntity = "";
	
	public List<RunTimeMachineInfo> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<RunTimeMachineInfo> result = super.select(condition, bindSet, RunTimeMachineInfo.class);
		
		return result;
	}
	
	public RunTimeMachineInfo selectByKey(boolean isLock, Object[] keySet)throws CustomException
	{
		return super.selectByKey(RunTimeMachineInfo.class, isLock, keySet);
	}
	
	public RunTimeMachineInfo create(EventInfo eventInfo, RunTimeMachineInfo dataInfo)throws CustomException
	{
		dataInfo = setDataFromEventInfo(eventInfo, dataInfo);
		
		super.insert(dataInfo);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(RunTimeMachineInfo dataInfo)throws CustomException
	{
		super.delete(dataInfo);
	}
	
	public void remove(String machineName)throws CustomException
	{
		if (machineName == null || machineName.isEmpty())
		{
			logger.info("RunTimeMachineInfoService.Remove: Input machineName is null or Empty.");
			return ;
		}
			
		greenFrameServiceProxy.getSqlTemplate().update("DELETE FROM CT_RUNTIMEMACHINEINFO WHERE MACHINENAME = ? ", new Object[]{machineName});
	}
	
	public RunTimeMachineInfo modify(EventInfo eventInfo, RunTimeMachineInfo dataInfo)throws CustomException
	{
		dataInfo = setDataFromEventInfo(eventInfo, dataInfo);
		
		super.update(dataInfo);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
	
	private RunTimeMachineInfo setDataFromEventInfo(EventInfo eventInfo, RunTimeMachineInfo dataInfo)
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
	
	private List<RunTimeMachineInfo> setDataFromEventInfo(EventInfo eventInfo, List<RunTimeMachineInfo> dataInfoList)
	{
		for(RunTimeMachineInfo dataInfo : dataInfoList)
		{
			dataInfo = setDataFromEventInfo(eventInfo, dataInfo);
		}
		
		return dataInfoList;
	}
	
	public RunTimeMachineInfo getDataInfoByKey(String machineName,boolean throwException) throws CustomException
	{
		if (logger.isInfoEnabled())
			logger.info(String.format("getDataInfoByKey: Input argument MachineName is [%s] .", machineName));

		if(machineName ==null || machineName.isEmpty()) return null;
		
		RunTimeMachineInfo dataInfo = null;
		try
		{
			dataInfo = super.selectByKey(RunTimeMachineInfo.class, false, new Object[] {machineName });
		}
		catch (greenFrameDBErrorSignal dbError)
		{
			if (dbError.getErrorCode().equals(ErrorSignal.NotFoundSignal))
			{
				if (throwException)
					throw new CustomException("COMM-1000", "RunTimeMachineInfo", " MachineName = " + machineName);
				else
					logger.info(String.format("RunTimeMachineInfo Data Information is not registered.condition by MachineName = %s].", machineName));
			}
			else
			{
				throw new CustomException(dbError.getCause());
			}
		}

		return dataInfo;
	}
	
	public void checkRunTimeMachineInfo(EventInfo eventInfo,String machineName,String portName,Lot lotData) throws CustomException
	{
		RunTimeMachineInfo runTimeInfo = ExtendedObjectProxy.getRunTimeMachineInfoService().getDataInfoByKey(machineName, false);

		if (MESPortServiceProxy.getPortServiceUtil().checkMachinePortIsEmpty(machineName, portName) || runTimeInfo == null)
		{
			if (runTimeInfo != null)
			{
				runTimeInfo.setProductRequestName(lotData.getProductRequestName());
				runTimeInfo.setProductSpecName(lotData.getProductSpecName());
				runTimeInfo.setProductSpecVersion(lotData.getProductSpecVersion());
				runTimeInfo.setProductSpecVersion(lotData.getProductSpecVersion());
				runTimeInfo.setProcessFlowName(lotData.getProcessFlowName());
				runTimeInfo.setProcessFlowVersion(lotData.getProcessFlowVersion());
				runTimeInfo.setProcessOperationName(lotData.getProcessOperationName());
				runTimeInfo.setProcessOperationVersion(lotData.getProcessOperationVersion());
			
				eventInfo.setEventName("Update");
				this.modify(eventInfo, runTimeInfo);
				
				logger.info(String.format("Update RunTimeInfo: ProductRequestName = %s,ProductSpecName = %s ,ProcessFlowName = %s ,OperationName = %s",
										  lotData.getProductRequestName(),lotData.getProductSpecName(),lotData.getProcessFlowName(),lotData.getProcessOperationName()));
			}
			else
			{
				RunTimeMachineInfo dataInfo = new RunTimeMachineInfo();
				dataInfo.setMachineName(machineName);
				dataInfo.setProductRequestName(lotData.getProductRequestName());
				dataInfo.setProductSpecName(lotData.getProductSpecName());
				dataInfo.setProductSpecVersion(lotData.getProductSpecVersion());
				dataInfo.setProductSpecVersion(lotData.getProductSpecVersion());
				dataInfo.setProcessFlowName(lotData.getProcessFlowName());
				dataInfo.setProcessFlowVersion(lotData.getProcessFlowVersion());
				dataInfo.setProcessOperationName(lotData.getProcessOperationName());
				dataInfo.setProcessOperationVersion(lotData.getProcessOperationVersion());

				eventInfo.setEventName("Create");
				this.create(eventInfo, dataInfo);
				
				logger.info(String.format("Create RunTimeInfo: ProductRequestName = %s,ProductSpecName = %s ,ProcessFlowName = %s ,OperationName = %s",
						  				   lotData.getProductRequestName(),lotData.getProductSpecName(),lotData.getProcessFlowName(),lotData.getProcessOperationName()));
			}
		}
		else
		{
           if(lotData.getProductRequestName()!= runTimeInfo.getProductRequestName() 
             || lotData.getProcessFlowName() != runTimeInfo.getProcessFlowName() || lotData.getProcessFlowVersion()!= runTimeInfo.getProcessFlowVersion()
             || lotData.getProcessOperationName() !=runTimeInfo.getProcessOperationName() || lotData.getProcessOperationVersion() != runTimeInfo.getProcessOperationVersion())
           {
        	   throw new CustomException("SORTER-0008",lotData.getProductRequestName(),lotData.getProductSpecName(),lotData.getProcessFlowName(),lotData.getProcessOperationName());
           }
		}
	}
}
