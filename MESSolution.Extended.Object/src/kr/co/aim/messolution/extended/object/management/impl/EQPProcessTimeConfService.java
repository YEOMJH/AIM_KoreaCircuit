package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.EQPProcessTimeConf;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class EQPProcessTimeConfService extends CTORMService<EQPProcessTimeConf>{

	
	public static Log logger = LogFactory.getLog(EQPProcessTimeConf.class);
	
	private final String historyEntity = "CT_EQPProcessTimeConfHis";
	
	public List<EQPProcessTimeConf> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<EQPProcessTimeConf> result = super.select(condition, bindSet, EQPProcessTimeConf.class);
		
		return result;
	}
	
	public EQPProcessTimeConf selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(EQPProcessTimeConf.class, isLock, keySet);
	}
	
	public EQPProcessTimeConf create(EventInfo eventInfo, EQPProcessTimeConf dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public int[] create(EventInfo eventInfo, List<EQPProcessTimeConf> dataInfoList)
		throws greenFrameDBErrorSignal
	{
		return super.insert(dataInfoList);
	}
	
	public void remove(EventInfo eventInfo, EQPProcessTimeConf dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public EQPProcessTimeConf modify(EventInfo eventInfo, EQPProcessTimeConf dataInfo)
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void modify(EventInfo eventInfo, List<EQPProcessTimeConf> updateList) {
		// TODO Auto-generated method stub
		super.update(updateList);

		super.addHistory(eventInfo, this.historyEntity, updateList, logger);
	} 


}
