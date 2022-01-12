package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.DSPControl;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DSPControlService extends CTORMService<DSPControl> {
	public static Log logger = LogFactory.getLog(DSPControlService.class);

	private final String historyEntity = "DSPControlHistory";

	public List<DSPControl> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<DSPControl> result = super.select(condition, bindSet, DSPControl.class);

		return result;
	}

	public DSPControl selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(DSPControl.class, isLock, keySet);
	}

	public DSPControl create(EventInfo eventInfo, DSPControl dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, DSPControl dataInfo) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public DSPControl modify(EventInfo eventInfo, DSPControl dataInfo)
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	/**
	 * 2021-01-11	dhko	Add portName for DSP
	 */
	public DSPControl createDSPControl(EventInfo eventInfo, String machineName, String processOperationName, String processOperationVersion, String factoryName, String dspFlag, String portName)
	{
		DSPControl dataInfo = new DSPControl();
		dataInfo.setMachineName(machineName);
		dataInfo.setProcessOperationName(processOperationName);
		dataInfo.setProcessOperationVersion(processOperationVersion);
		dataInfo.setFactoryName(factoryName);
		dataInfo.setPortName(portName);
		dataInfo.setDspFlag(dspFlag);
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
		
		dataInfo = this.create(eventInfo, dataInfo);

		return dataInfo;
	}

	public void deleteDSPControlData(EventInfo eventInfo, String machineName, String processOperationName, String processOperationVersion) throws greenFrameDBErrorSignal, CustomException
	{
		DSPControl dataInfo = ExtendedObjectProxy.getDSPControlService().selectByKey(false, new Object[] { machineName, processOperationName, processOperationVersion });
		this.remove(eventInfo, dataInfo);
	}
}
