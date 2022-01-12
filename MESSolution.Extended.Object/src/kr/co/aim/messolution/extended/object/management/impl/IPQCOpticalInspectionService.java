package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.IPQCOpticalInspection;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;


public class IPQCOpticalInspectionService extends CTORMService<IPQCOpticalInspection> {
	
	public static Log logger = LogFactory.getLog(IPQCOpticalInspection.class);
	
	private final String historyEntity = "";
	
	public List<IPQCOpticalInspection> select(String condition, Object[] bindSet)
			throws greenFrameDBErrorSignal
	{
		List<IPQCOpticalInspection> result = super.select(condition, bindSet, IPQCOpticalInspection.class);
		
		return result;
	}
	
	public IPQCOpticalInspection selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(IPQCOpticalInspection.class, isLock, keySet);
	}
	
	public IPQCOpticalInspection create(EventInfo eventInfo, IPQCOpticalInspection dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		
		//super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void create(EventInfo eventInfo, List<IPQCOpticalInspection> dataInfoList)
			throws greenFrameDBErrorSignal
		{
			super.insert(dataInfoList);
			
			//super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
		}
	
	public void remove(EventInfo eventInfo, IPQCOpticalInspection dataInfo)
		throws greenFrameDBErrorSignal
	{
		//super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}

	public IPQCOpticalInspection modify(EventInfo eventInfo, IPQCOpticalInspection dataInfo)
	{
		super.update(dataInfo);
		
		//super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void modify(EventInfo eventInfo, List<IPQCOpticalInspection> dataInfoList)
	{
		super.update(dataInfoList);
		
		//super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
	}
	
	
}
