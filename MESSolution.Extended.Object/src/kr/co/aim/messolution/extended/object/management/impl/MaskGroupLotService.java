package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.MaskGroupLot;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class MaskGroupLotService extends CTORMService<MaskGroupLot> {
	
	public static Log logger = LogFactory.getLog(MaskGroupLotService.class);
	
	private final String historyEntity = "MaskGroupLotHistory";
	
	public List<MaskGroupLot> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<MaskGroupLot> result = super.select(condition, bindSet, MaskGroupLot.class);
		
		return result;
	}
	
	public MaskGroupLot selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(MaskGroupLot.class, isLock, keySet);
	}
	
	public boolean create(EventInfo eventInfo, MaskGroupLot dataInfo)
		throws CustomException
	{
		
		super.insert(dataInfo);
				
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return true;
	}
	
	public void remove(EventInfo eventInfo, MaskGroupLot dataInfo)
		throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public MaskGroupLot modify(EventInfo eventInfo, MaskGroupLot dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
