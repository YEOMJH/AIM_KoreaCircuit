package kr.co.aim.messolution.extended.object.management.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.IPQCRule;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;


public class IPQCRuleService extends CTORMService<IPQCRule> {
	
	public static Log logger = LogFactory.getLog(IPQCRule.class);
	
	public List<IPQCRule> select(String condition, Object[] bindSet)
			throws greenFrameDBErrorSignal
	{
		List<IPQCRule> result = super.select(condition, bindSet, IPQCRule.class);
		
		return result;
	}
	
	public IPQCRule selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(IPQCRule.class, isLock, keySet);
	}
	
	public IPQCRule create(EventInfo eventInfo, IPQCRule dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void create(EventInfo eventInfo, List<IPQCRule> dataInfoList)
			throws greenFrameDBErrorSignal
	{
		super.insert(dataInfoList);
			
	}
	
	public void remove(EventInfo eventInfo, IPQCRule dataInfo)
		throws greenFrameDBErrorSignal
	{	
		super.delete(dataInfo);
	}

	public IPQCRule modify(EventInfo eventInfo, IPQCRule dataInfo)
	{
		super.update(dataInfo);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void modify(EventInfo eventInfo, List<IPQCRule> dataInfoList)
	{
		super.update(dataInfoList);
	}
	
	public String getIPQCRuleSeq()
	{
		String sql = " SELECT NVL(MAX(SEQ) , 0) + 1 AS SEQ FROM CT_IPQCRULE ";
		String seq = "";
		Map<String, String> args = new HashMap<String, String>();
		
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);
		
		if (result.size() > 0)
		{
			seq = ConvertUtil.getMapValueByName(result.get(0), "SEQ");
		}
		
		return seq;
	}
}
