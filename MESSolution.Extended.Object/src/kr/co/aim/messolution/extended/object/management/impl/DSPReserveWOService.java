package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.DSPReserveWO;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class DSPReserveWOService extends CTORMService<DSPReserveWO> {
	
	public static Log logger = LogFactory.getLog(DSPReserveWOService.class);
	
	private final String historyEntity = "DSPRESERVEWOHIST";
	
	public List<DSPReserveWO> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<DSPReserveWO> result = super.select(condition, bindSet, DSPReserveWO.class);
		
		return result;
	}
	
	public DSPReserveWO selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(DSPReserveWO.class, isLock, keySet);
	}
	
	public DSPReserveWO create(EventInfo eventInfo, DSPReserveWO dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, DSPReserveWO dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public DSPReserveWO modify(EventInfo eventInfo, DSPReserveWO dataInfo)
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
}
