package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.ComponentInChamberHist;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class ComponentInChamberHistService extends CTORMService<ComponentInChamberHist> {
public static Log logger = LogFactory.getLog(ComponentInChamberHist.class);
	
	private final String historyEntity = "ComponentInChamberHist";
	
	public List<ComponentInChamberHist> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<ComponentInChamberHist> result = super.select(condition, bindSet, ComponentInChamberHist.class);
		
		return result;
	}
	
	public ComponentInChamberHist selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(ComponentInChamberHist.class, isLock, keySet);
	}
	
	public ComponentInChamberHist create(EventInfo eventInfo, ComponentInChamberHist dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public int[] create(EventInfo eventInfo, List<ComponentInChamberHist> dataInfoList)
		throws greenFrameDBErrorSignal
	{
		return super.insert(dataInfoList);
	}
	
	public void remove(EventInfo eventInfo, ComponentInChamberHist dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public ComponentInChamberHist modify(EventInfo eventInfo, ComponentInChamberHist dataInfo)
	{
		super.update(dataInfo);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
}
