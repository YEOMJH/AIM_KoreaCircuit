package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMServiceNoCT;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.POSAlterProcessOperation;
import kr.co.aim.messolution.extended.object.management.data.STKConfig;
import kr.co.aim.messolution.extended.object.management.data.TFOPolicy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class POSAlterProcessOperationService extends CTORMServiceNoCT<POSAlterProcessOperation>
{

	public static Log logger = LogFactory.getLog(POSAlterProcessOperationService.class);
	
	private final String historyEntity = "POSALTEROPERATIONHISTORY";
	
	public POSAlterProcessOperation selectByKey(boolean isLock, Object keySet)
	throws greenFrameDBErrorSignal
	{
		return super.selectByKey(POSAlterProcessOperation.class, isLock, keySet);
	}	
	
	public List<POSAlterProcessOperation> select(String condition, Object[] bindSet)
	throws greenFrameDBErrorSignal
	{
		List<POSAlterProcessOperation> result = super.select(condition, bindSet, POSAlterProcessOperation.class);
		
		return result;
	}
	
	
	public void create(POSAlterProcessOperation dataInfo)
	throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		
	}	
	
	public POSAlterProcessOperation create(EventInfo eventInfo, POSAlterProcessOperation dataInfo)
			throws CustomException
	{
		super.insert(dataInfo);
			
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
		return dataInfo;
	}
	
	public void remove(EventInfo eventInfo, POSAlterProcessOperation dataInfo)
			throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
		super.delete(dataInfo);
	}
		
	
	public POSAlterProcessOperation modify(EventInfo eventInfo, POSAlterProcessOperation dataInfo)
			throws CustomException
	{
		super.update(dataInfo);
			
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
	
}
