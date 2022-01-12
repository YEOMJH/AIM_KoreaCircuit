package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.MaskGroupList;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class MaskGroupListService extends CTORMService<MaskGroupList> {
	
	public static Log logger = LogFactory.getLog(MaskGroupListService.class);
	
	private final String historyEntity = "MaskGroupListHistory";
	
	public List<MaskGroupList> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<MaskGroupList> result = super.select(condition, bindSet, MaskGroupList.class);
		
		return result;
	}
	
	public MaskGroupList selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(MaskGroupList.class, isLock, keySet);
	}
	
	public boolean create(EventInfo eventInfo, MaskGroupList dataInfo)
		throws CustomException
	{
		
		super.insert(dataInfo);
				
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return true;
	}
	
	public void remove(EventInfo eventInfo, MaskGroupList dataInfo)
		throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public MaskGroupList modify(EventInfo eventInfo, MaskGroupList dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
