package kr.co.aim.messolution.extended.object.management.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.IPQCLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;


public class IPQCLotService extends CTORMService<IPQCLot> {
	
	public static Log logger = LogFactory.getLog(IPQCLot.class);
	
	private final String historyEntity = "IPQCLotHistory";
	
	public List<IPQCLot> select(String condition, Object[] bindSet)
			throws greenFrameDBErrorSignal
	{
		List<IPQCLot> result = super.select(condition, bindSet, IPQCLot.class);
		
		return result;
	}
	
	public IPQCLot selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(IPQCLot.class, isLock, keySet);
	}
	
	public IPQCLot create(EventInfo eventInfo, IPQCLot dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void create(EventInfo eventInfo, List<IPQCLot> dataInfoList)
			throws greenFrameDBErrorSignal
		{
			super.insert(dataInfoList);
			
			super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
		}
	
	public void remove(EventInfo eventInfo, IPQCLot dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}

	public IPQCLot modify(EventInfo eventInfo, IPQCLot dataInfo)
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void modify(EventInfo eventInfo, List<IPQCLot> dataInfoList)
	{
		super.update(dataInfoList);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
	}
	
	public String getIPQCLotSeq(String lotName)
	{
		String sql = " SELECT NVL(MAX(SEQ) , 0) + 1 AS SEQ FROM CT_IPQCLOT WHERE IPQCLOTNAME = :IPQCLOTNAME ";
		String seq = "";
		Map<String, String> args = new HashMap<String, String>();
		args.put("IPQCLOTNAME", lotName);
		
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);
		
		if (result.size() > 0)
		{
			seq = ConvertUtil.getMapValueByName(result.get(0), "SEQ");
		}
		
		return seq;
	}
	
}
