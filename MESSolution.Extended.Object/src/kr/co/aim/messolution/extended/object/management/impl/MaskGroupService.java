package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.MaskGroup;
import kr.co.aim.messolution.extended.object.management.data.MaskGroupList;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class MaskGroupService extends CTORMService<MaskGroup>{
	

	public static Log logger = LogFactory.getLog(MaskGroupService.class);
	
	private final String historyEntity = "MaskGroupHistory";
	
	public List<MaskGroup> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<MaskGroup> result = super.select(condition, bindSet, MaskGroup.class);
		
		return result;
	}
	
	public MaskGroup selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(MaskGroup.class, isLock, keySet);
	}
	
	public boolean create(EventInfo eventInfo, MaskGroup dataInfo)
		throws CustomException
	{
		
		super.insert(dataInfo);
				
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return true;
	}
	
	public void remove(EventInfo eventInfo, MaskGroup dataInfo)
		throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public MaskGroup modify(EventInfo eventInfo, MaskGroup dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

}
