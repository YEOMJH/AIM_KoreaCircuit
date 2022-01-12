package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.MaskReserveSpec;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class MaskReserveSpecService extends CTORMService<MaskReserveSpec> {
	
	public static Log logger = LogFactory.getLog(MaskReserveSpec.class);
	
	private final String historyEntity = "";
	
	public List<MaskReserveSpec> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<MaskReserveSpec> result = super.select(condition, bindSet, MaskReserveSpec.class);
		
		return result;
	}
	
	public MaskReserveSpec selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(MaskReserveSpec.class, isLock, keySet);
	}
	
	public MaskReserveSpec create(EventInfo eventInfo, MaskReserveSpec dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, MaskReserveSpec dataInfo)
		throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public MaskReserveSpec modify(EventInfo eventInfo, MaskReserveSpec dataInfo)
			throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
}
