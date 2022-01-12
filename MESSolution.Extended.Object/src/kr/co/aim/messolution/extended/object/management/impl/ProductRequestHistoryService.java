package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.ProductRequestHistory;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ProductRequestHistoryService extends CTORMService<ProductRequestHistory> {
	
	public static Log logger = LogFactory.getLog(ProductRequestHistoryService.class);
	
	public List<ProductRequestHistory> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<ProductRequestHistory> result = super.select(condition, bindSet, ProductRequestHistory.class);
		
		return result;
	}
	
	public ProductRequestHistory selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(ProductRequestHistory.class, isLock, keySet);
	}
	
	public ProductRequestHistory create(EventInfo eventInfo, ProductRequestHistory dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
			
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, ProductRequestHistory dataInfo)
		throws CustomException
	{

		super.delete(dataInfo);
	}
	
	public ProductRequestHistory modify(EventInfo eventInfo, ProductRequestHistory dataInfo)
		throws CustomException
	{
		super.update(dataInfo);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
