package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.MaskMaterials;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class MaskMaterialsService extends CTORMService<MaskMaterials>
{
public static Log logger = LogFactory.getLog(MaskMaterials.class);
	
	private final String historyEntity = "MaskMaterialsHistory";
	
	public List<MaskMaterials> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<MaskMaterials> result = super.select(condition, bindSet, MaskMaterials.class);
		
		return result;
	}
	
	public MaskMaterials selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(MaskMaterials.class, isLock, keySet);
	}
	
	public MaskMaterials create(EventInfo eventInfo, MaskMaterials dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, MaskMaterials dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public MaskMaterials modify(EventInfo eventInfo, MaskMaterials dataInfo)
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
}
