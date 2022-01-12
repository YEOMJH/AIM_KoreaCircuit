package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMServiceNoCT;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.MachineHistory;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MachineHistoryService extends CTORMService<MachineHistory>{
	public static Log logger = LogFactory.getLog(MachineHistoryService.class);
		
		private final String historyEntity = "";
		
		public List<MachineHistory> select(String condition, Object[] bindSet)
			throws greenFrameDBErrorSignal
		{
			List<MachineHistory> result = super.select(condition, bindSet, MachineHistory.class);
			
			return result;
		}
		
		public MachineHistory selectByKey(boolean isLock, Object[] keySet)
			throws greenFrameDBErrorSignal
		{
			return super.selectByKey(MachineHistory.class, isLock, keySet);
		}
		
		public MachineHistory create(EventInfo eventInfo, MachineHistory dataInfo)
			throws greenFrameDBErrorSignal
		{
			super.insert(dataInfo);
			
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
		}
		
		public void remove(EventInfo eventInfo, MachineHistory dataInfo)
			throws greenFrameDBErrorSignal
		{
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			super.delete(dataInfo);
		}
		
		public MachineHistory modify(EventInfo eventInfo, MachineHistory dataInfo)
		{
			super.update(dataInfo);
			
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
		}
}