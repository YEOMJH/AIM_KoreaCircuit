package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMServiceNoCT;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.ProductIdleTime;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class ProductIdleTimeService  extends CTORMServiceNoCT<ProductIdleTime>
{
	public static Log logger = LogFactory.getLog(ProductIdleTimeService.class);
	
	private final String historyEntity = "ProductIdleTimeHist";
	
	public List<ProductIdleTime> select(String condition, Object[] bindSet)
	throws greenFrameDBErrorSignal
	{
		List<ProductIdleTime> result = super.select(condition, bindSet, ProductIdleTime.class);
		
		return result;
	}
	
	public ProductIdleTime selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(ProductIdleTime.class, isLock, keySet);
	}
	
	public ProductIdleTime create(EventInfo eventInfo, ProductIdleTime dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, ProductIdleTime dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public ProductIdleTime modify(EventInfo eventInfo, ProductIdleTime dataInfo)
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
