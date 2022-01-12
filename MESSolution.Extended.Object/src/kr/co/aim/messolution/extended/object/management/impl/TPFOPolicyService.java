package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMServiceNoCT;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.TPFOPolicy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class TPFOPolicyService extends CTORMServiceNoCT<TPFOPolicy>
{

	public static Log logger = LogFactory.getLog(TPFOPolicyService.class);
	
	private final String historyEntity = "";
	
	public TPFOPolicy selectByKey(boolean isLock, Object keySet)
	throws greenFrameDBErrorSignal
	{
		return super.selectByKey(TPFOPolicy.class, isLock, keySet);
	}	
	
	public List<TPFOPolicy> select(String condition, Object[] bindSet)
	throws greenFrameDBErrorSignal
	{
		List<TPFOPolicy> result = super.select(condition, bindSet, TPFOPolicy.class);
		
		return result;
	}
	
	public void modify(TPFOPolicy dataInfo)
	throws greenFrameDBErrorSignal
	{
		super.update(dataInfo);
		
	}
	
	public void create(TPFOPolicy dataInfo)
	throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		
	}
}
