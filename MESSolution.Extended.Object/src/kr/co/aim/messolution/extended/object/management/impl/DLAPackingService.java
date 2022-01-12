package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.DLAPacking;
import kr.co.aim.messolution.extended.object.management.data.MaterialProduct;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class DLAPackingService extends CTORMService<DLAPacking> {
	
	public static Log logger = LogFactory.getLog(DLAPacking.class);
	
	public List<DLAPacking> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<DLAPacking> result = super.select(condition, bindSet, DLAPacking.class);
		
		return result;
	}
	
	public DLAPacking selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(DLAPacking.class, isLock, keySet);
	}
	
	public DLAPacking create(EventInfo eventInfo, DLAPacking dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, DLAPacking dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.delete(dataInfo);
	}
	
	public DLAPacking modify(EventInfo eventInfo, DLAPacking dataInfo)
	{
		super.update(dataInfo);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
}
