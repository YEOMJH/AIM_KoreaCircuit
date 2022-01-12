package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.RecipeParameter;
import kr.co.aim.messolution.extended.object.management.data.ShieldChamberMap;
import kr.co.aim.messolution.extended.object.management.data.ShieldLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;

public class ShieldChamberMapService extends CTORMService<ShieldChamberMap> {
	public static Log logger = LogFactory.getLog(ShieldChamberMapService.class);

	private final String historyEntity = "ShieldChamberMapHistory";

	public List<ShieldChamberMap> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<ShieldChamberMap> result = super.select(condition, bindSet, ShieldChamberMap.class);

		return result;
	}

	public ShieldChamberMap selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(ShieldChamberMap.class, isLock, keySet);
	}

	public ShieldChamberMap create(EventInfo eventInfo, ShieldChamberMap dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void create(EventInfo eventInfo, List<ShieldChamberMap> dataInfoList)
			throws greenFrameDBErrorSignal
	{		
		super.insert(dataInfoList);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
	}

	public void remove(EventInfo eventInfo, ShieldChamberMap dataInfo) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}
	
	public void remove(EventInfo eventInfo, List<ShieldChamberMap> dataInfoList)
			throws greenFrameDBErrorSignal
	{	
		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
		
		super.delete(dataInfoList);
	}

	public ShieldChamberMap modify(EventInfo eventInfo, ShieldChamberMap dataInfo)
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void modify(EventInfo eventInfo, List<ShieldChamberMap> dataInfoList)
			throws greenFrameDBErrorSignal
	{		
		super.update(dataInfoList);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
	}
}
