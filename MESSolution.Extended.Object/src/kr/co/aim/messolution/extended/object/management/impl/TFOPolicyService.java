package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMServiceNoCT;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.STKConfig;
import kr.co.aim.messolution.extended.object.management.data.TFOPolicy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class TFOPolicyService extends CTORMServiceNoCT<TFOPolicy>
{

	public static Log logger = LogFactory.getLog(TFOPolicyService.class);
	
	private final String historyEntity = "";
	
	public TFOPolicy selectByKey(boolean isLock, Object keySet)
	throws greenFrameDBErrorSignal
	{
		return super.selectByKey(TFOPolicy.class, isLock, keySet);
	}	
	
	public List<TFOPolicy> select(String condition, Object[] bindSet)
	throws greenFrameDBErrorSignal
	{
		List<TFOPolicy> result = super.select(condition, bindSet, TFOPolicy.class);
		
		return result;
	}
	
	public void modify(TFOPolicy dataInfo)
	throws greenFrameDBErrorSignal
	{
		super.update(dataInfo);
		
	}
	
	public void create(TFOPolicy dataInfo)
	throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		
	}	
	
}
