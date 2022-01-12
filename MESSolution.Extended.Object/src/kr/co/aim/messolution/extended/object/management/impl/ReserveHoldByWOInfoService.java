package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.ReserveHoldByWOInfo;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ReserveHoldByWOInfoService extends CTORMService<ReserveHoldByWOInfo> {
	public static Log logger = LogFactory.getLog(ReserveHoldByWOInfoService.class);

	private final String historyEntity = "ReserveHoldByWOInfoHistory"; 
	
	public List<ReserveHoldByWOInfo> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<ReserveHoldByWOInfo> result = super.select(condition, bindSet, ReserveHoldByWOInfo.class);

		return result;
	}

	public ReserveHoldByWOInfo selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(ReserveHoldByWOInfo.class, isLock, keySet);
	}

	public ReserveHoldByWOInfo create(EventInfo eventInfo, ReserveHoldByWOInfo dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, ReserveHoldByWOInfo dataInfo) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public ReserveHoldByWOInfo modify(EventInfo eventInfo, ReserveHoldByWOInfo dataInfo)
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public ReserveHoldByWOInfo getReserveHoldByWOInfoData(String productRequest,String processOperation,String panelGrade )
	{
		Object[] keySet = new Object[] {productRequest,processOperation,panelGrade};
		ReserveHoldByWOInfo dataInfo = new ReserveHoldByWOInfo();

		try
		{
			dataInfo = this.selectByKey(false, keySet);
		}
		catch (Exception e)
		{
			dataInfo = null;
		}

		return dataInfo;
	}

}
