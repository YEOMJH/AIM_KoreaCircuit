package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.FirstOnlineProduct;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class FirstOnlineProductService extends CTORMService<FirstOnlineProduct> {
	
	public static Log logger = LogFactory.getLog(FirstOnlineProductService.class);
	
	private final String historyEntity = " ";
	
	public List<FirstOnlineProduct> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<FirstOnlineProduct> result = super.select(condition, bindSet, FirstOnlineProduct.class);
		
		return result;
	}
	
	public FirstOnlineProduct selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(FirstOnlineProduct.class, isLock, keySet);
	}
	
	public FirstOnlineProduct create(EventInfo eventInfo, FirstOnlineProduct dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, FirstOnlineProduct dataInfo)
		throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public FirstOnlineProduct modify(EventInfo eventInfo, FirstOnlineProduct dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}