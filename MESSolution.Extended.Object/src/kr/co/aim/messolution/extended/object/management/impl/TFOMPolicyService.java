package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMServiceNoCT;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.STKConfig;
import kr.co.aim.messolution.extended.object.management.data.TFOMPolicy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class TFOMPolicyService extends CTORMServiceNoCT<TFOMPolicy>
{

	public static Log logger = LogFactory.getLog(TFOMPolicyService.class);
	
	private final String historyEntity = "";
	
	public TFOMPolicy selectByKey(boolean isLock, Object keySet)
	throws greenFrameDBErrorSignal
	{
		return super.selectByKey(TFOMPolicy.class, isLock, keySet);
	}	
	
	public List<TFOMPolicy> select(String condition, Object[] bindSet)
	throws greenFrameDBErrorSignal
	{
		List<TFOMPolicy> result = super.select(condition, bindSet, TFOMPolicy.class);
		
		return result;
	}
	
	public void modify(TFOMPolicy dataInfo)
	throws greenFrameDBErrorSignal
	{
		super.update(dataInfo);
		
	}
	
	public void create(TFOMPolicy dataInfo)
	throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		
	}
}
