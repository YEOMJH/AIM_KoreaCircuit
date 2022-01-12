package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.OrganicMapping;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class OrganicMappingService extends CTORMService<OrganicMapping> {
	public static Log logger = LogFactory.getLog(OrganicMappingService.class);

	private final String historyEntity = "OrganicMappingHistory";

	public List<OrganicMapping> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<OrganicMapping> result = super.select(condition, bindSet, OrganicMapping.class);

		return result;
	}

	public OrganicMapping selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(OrganicMapping.class, isLock, keySet);
	}

	public OrganicMapping create(EventInfo eventInfo, OrganicMapping dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, OrganicMapping dataInfo) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public OrganicMapping modify(EventInfo eventInfo, OrganicMapping dataInfo)
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public OrganicMapping getOrganicMappingData(String crucibleName)
	{
		OrganicMapping dataInfo = new OrganicMapping();

		try
		{
			dataInfo = this.selectByKey(false, new Object[] { crucibleName });
		}
		catch (Exception e)
		{
			dataInfo = null;
		}

		return dataInfo;
	}

	public void checkMappingQuantity(String chamberName) throws CustomException
	{
		List<OrganicMapping> dataInfoList = new ArrayList<OrganicMapping>();

		String condition = " CHAMBERNAME = ? ";
		Object[] bindSet = new Object[] { chamberName };

		try
		{
			dataInfoList = this.select(condition, bindSet);
		}
		catch (Exception e)
		{
			dataInfoList = null;
		}

		if (dataInfoList != null && dataInfoList.size() > 6)
			throw new CustomException("CRUCIBLE-0002", chamberName);
	}
}
