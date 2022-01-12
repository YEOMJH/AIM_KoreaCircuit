package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMServiceNoCT;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.POSAlterProcessOperation;
import kr.co.aim.messolution.extended.object.management.data.POSQueueTime;
import kr.co.aim.messolution.extended.object.management.data.STKConfig;
import kr.co.aim.messolution.extended.object.management.data.TFOPolicy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class POSQueueTimeService extends CTORMServiceNoCT<POSQueueTime>
{

	public static Log logger = LogFactory.getLog(POSQueueTimeService.class);
	
	private final String historyEntity = "POSQUEUETIMEHISTORY";
	
	public POSQueueTime selectByKey(boolean isLock, Object[] keySet)
	throws greenFrameDBErrorSignal
	{
		return super.selectByKey(POSQueueTime.class, isLock, keySet);
	}	
	
	public List<POSQueueTime> select(String condition, Object[] bindSet)
	throws greenFrameDBErrorSignal
	{
		List<POSQueueTime> result = super.select(condition, bindSet, POSQueueTime.class);
		
		return result;
	}
	
	
	public void create(POSQueueTime dataInfo)
	throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		
	}	
	
	public POSQueueTime create(EventInfo eventInfo, POSQueueTime dataInfo)
			throws CustomException
	{
		super.insert(dataInfo);
			
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
		return dataInfo;
	}
	
	public void remove(EventInfo eventInfo, POSQueueTime dataInfo)
			throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
		super.delete(dataInfo);
	}
		
	
	public void modify(EventInfo eventInfo, POSQueueTime dataInfo)
			throws CustomException
	{
		super.update(dataInfo);
			
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
		//return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
	
}
