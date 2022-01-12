package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.BufferCSTTransInfo;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class BufferCSTTransInfoService extends CTORMService<BufferCSTTransInfo> {
	public static Log logger = LogFactory.getLog(BufferCSTTransInfo.class);

	private final String historyEntity = "BufferCSTTransInfoHistory";

	public List<BufferCSTTransInfo> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<BufferCSTTransInfo> result = super.select(condition, bindSet, BufferCSTTransInfo.class);

		return result;
	}

	public BufferCSTTransInfo selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(BufferCSTTransInfo.class, isLock, keySet);
	}

	public BufferCSTTransInfo create(EventInfo eventInfo, BufferCSTTransInfo dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, BufferCSTTransInfo dataInfo) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public BufferCSTTransInfo modify(EventInfo eventInfo, BufferCSTTransInfo dataInfo)
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public BufferCSTTransInfo createBufferCSTTransInfo(EventInfo eventInfo, String machineName, String toMachineName, String toPortName, String factoryName) throws greenFrameDBErrorSignal,
			CustomException
	{
		BufferCSTTransInfo dataInfo = new BufferCSTTransInfo();
		dataInfo.setMachineName(machineName);
		dataInfo.setToMachineName(toMachineName);
		dataInfo.setToPortName(toPortName);
		dataInfo.setFactoryName(factoryName);
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());

		ExtendedObjectProxy.getBufferCSTTransInfoService().create(eventInfo, dataInfo);

		return dataInfo;
	}

	public BufferCSTTransInfo getBufferCSTTransInfo(String machineName, String toMachineName, String toPortName) throws CustomException
	{
		BufferCSTTransInfo dataInfo = new BufferCSTTransInfo();

		Object[] keySet = new Object[] { machineName, toMachineName, toPortName };

		try
		{
			dataInfo = this.selectByKey(false, keySet);
		}
		catch (Exception e)
		{
			throw new CustomException("MACHINE-0112", machineName, toMachineName, toPortName);
		}

		return dataInfo;
	}
}
