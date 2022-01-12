package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.MaterialProduct;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class MaterialProductService extends CTORMService<MaterialProduct> {
	
	public static Log logger = LogFactory.getLog(MaterialProduct.class);
	
	public List<MaterialProduct> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<MaterialProduct> result = super.select(condition, bindSet, MaterialProduct.class);
		
		return result;
	}
	
	public MaterialProduct selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(MaterialProduct.class, isLock, keySet);
	}
	
	public MaterialProduct create(EventInfo eventInfo, MaterialProduct dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, MaterialProduct dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.delete(dataInfo);
	}
	
	public MaterialProduct modify(EventInfo eventInfo, MaterialProduct dataInfo)
	{
		super.update(dataInfo);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
}
