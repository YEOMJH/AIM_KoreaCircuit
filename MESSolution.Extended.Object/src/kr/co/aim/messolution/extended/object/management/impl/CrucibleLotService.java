package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.CrucibleLot;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class CrucibleLotService extends CTORMService<CrucibleLot> {

	public static Log logger = LogFactory.getLog(CrucibleLot.class);

	private final String historyEntity = "CrucibleLotHistory";

	public List<CrucibleLot> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<CrucibleLot> result = super.select(condition, bindSet, CrucibleLot.class);

		return result;
	}

	public CrucibleLot selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(CrucibleLot.class, isLock, keySet);
	}

	public CrucibleLot create(EventInfo eventInfo, CrucibleLot dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, CrucibleLot dataInfo) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public CrucibleLot modify(EventInfo eventInfo, CrucibleLot dataInfo)
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public List<CrucibleLot> getCrucibleLotList(String crucibleName)
	{
		List<CrucibleLot> dataInfoList = new ArrayList<CrucibleLot>();

		String condition = " DURABLENAME = ? ";
		Object[] bindSet = new Object[] { crucibleName };

		try
		{
			dataInfoList = this.select(condition, bindSet);
		}
		catch (Exception e)
		{
			dataInfoList = null;
		}
		
		return dataInfoList;
	}
}
