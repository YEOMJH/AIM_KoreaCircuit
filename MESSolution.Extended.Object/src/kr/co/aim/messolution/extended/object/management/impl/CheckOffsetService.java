package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.CheckOffset;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class CheckOffsetService  extends CTORMService<CheckOffset>{
public static Log logger = LogFactory.getLog(CheckOffset.class);
	
	private final String historyEntity = "CheckOffsetHistory";
	
	public List<CheckOffset> select(String condition, Object[] bindSet)
			throws greenFrameDBErrorSignal
		{
			List<CheckOffset> result = super.select(condition, bindSet, CheckOffset.class);
			
			return result;
		}
		
		public CheckOffset selectByKey(boolean isLock, Object[] keySet)
			throws greenFrameDBErrorSignal
		{
			return super.selectByKey(CheckOffset.class, isLock, keySet);
		}
		
		public CheckOffset create(EventInfo eventInfo, CheckOffset dataInfo)
			throws greenFrameDBErrorSignal
		{
			super.insert(dataInfo);
			
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
		}
		
		public void remove(EventInfo eventInfo, CheckOffset dataInfo)
			throws greenFrameDBErrorSignal
		{
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			super.delete(dataInfo);
		}
		
		public CheckOffset modify(EventInfo eventInfo, CheckOffset dataInfo)
		{
			super.update(dataInfo);
			
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
		} 
}
