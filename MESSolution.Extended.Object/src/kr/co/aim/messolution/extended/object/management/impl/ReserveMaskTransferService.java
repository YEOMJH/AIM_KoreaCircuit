package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.ReserveMaskTransfer;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class ReserveMaskTransferService extends CTORMService<ReserveMaskTransfer> {

	public static Log logger = LogFactory.getLog(ReserveMaskTransfer.class);

	private final String historyEntity = "ReserveMaskTransferHist";

	public List<ReserveMaskTransfer> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<ReserveMaskTransfer> result = super.select(condition, bindSet, ReserveMaskTransfer.class);

		return result;
	}

	public ReserveMaskTransfer selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(ReserveMaskTransfer.class, isLock, keySet);
	}

	public ReserveMaskTransfer create(EventInfo eventInfo, ReserveMaskTransfer dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, ReserveMaskTransfer dataInfo) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public void remove(EventInfo eventInfo, List<ReserveMaskTransfer> dataInfoList) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);

		super.delete(dataInfoList);
	}

	public ReserveMaskTransfer modify(EventInfo eventInfo, ReserveMaskTransfer dataInfo)
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
