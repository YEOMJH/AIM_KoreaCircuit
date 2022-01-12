package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.MaskFrame;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class MaskFrameService extends CTORMService<MaskFrame> {
	
	public static Log logger = LogFactory.getLog(MaskFrame.class);
	
	private final String historyEntity = "MaskFrameHistory";
	
	public List<MaskFrame> select(String condition, Object[] bindSet)
			throws greenFrameDBErrorSignal
		{
			List<MaskFrame> result = super.select(condition, bindSet, MaskFrame.class);
			
			return result;
		}
		
		public MaskFrame selectByKey(boolean isLock, Object[] keySet)
			throws greenFrameDBErrorSignal
		{
			return super.selectByKey(MaskFrame.class, isLock, keySet);
		}
		
		public MaskFrame create(EventInfo eventInfo, MaskFrame dataInfo)
			throws greenFrameDBErrorSignal
		{
			super.insert(dataInfo);
			
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
		}
		
		public void create(EventInfo eventInfo, List<MaskFrame> dataInfoList)
			throws greenFrameDBErrorSignal
		{
			super.insert(dataInfoList);
			
			super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
		}
		
		public void remove(EventInfo eventInfo, MaskFrame dataInfo)
			throws greenFrameDBErrorSignal
		{
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			super.delete(dataInfo);
		}
		
		public MaskFrame modify(EventInfo eventInfo, MaskFrame dataInfo)
		{
			super.update(dataInfo);
			
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
		} 
}
