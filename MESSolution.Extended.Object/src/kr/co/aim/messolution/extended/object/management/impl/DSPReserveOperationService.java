package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.DSPReserveOperation;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class DSPReserveOperationService extends CTORMService<DSPReserveOperation> {
	
	public static Log logger = LogFactory.getLog(DSPReserveOperationService.class);
	
	private final String historyEntity = "DSPRESERVEOPERATIONHIST";
	
	public List<DSPReserveOperation> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<DSPReserveOperation> result = super.select(condition, bindSet, DSPReserveOperation.class);
		
		return result;
	}
	
	public DSPReserveOperation selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(DSPReserveOperation.class, isLock, keySet);
	}
	
	public DSPReserveOperation create(EventInfo eventInfo, DSPReserveOperation dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, DSPReserveOperation dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public DSPReserveOperation modify(EventInfo eventInfo, DSPReserveOperation dataInfo)
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
}
