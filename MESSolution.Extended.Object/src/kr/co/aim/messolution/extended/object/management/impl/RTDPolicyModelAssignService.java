package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import kr.co.aim.messolution.extended.object.management.CTORMServiceNoCT;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.RTDPolicyModelAssign;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class RTDPolicyModelAssignService extends CTORMServiceNoCT<RTDPolicyModelAssign>
{
	public static Log logger = LogFactory.getLog(RTDPolicyModelAssignService.class);
	
	private final String historyEntity = "";
	
	public RTDPolicyModelAssign selectByKey(boolean isLock, Object keySet)
	throws greenFrameDBErrorSignal
	{
		return super.selectByKey(RTDPolicyModelAssign.class, isLock, keySet);
	}	
	
	public List<RTDPolicyModelAssign> select(String condition, Object[] bindSet)
	throws greenFrameDBErrorSignal
	{
		List<RTDPolicyModelAssign> result = super.select(condition, bindSet, RTDPolicyModelAssign.class);
		
		return result;
	}
	
	public void modify(RTDPolicyModelAssign dataInfo)
	throws greenFrameDBErrorSignal
	{
		super.update(dataInfo);
		
	}
	
	public void create(RTDPolicyModelAssign dataInfo)
	throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		
	}
}
