package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.MachineIdleByChamber;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class MachineIdleByChamberService extends CTORMService<MachineIdleByChamber> {
	public static Log logger = LogFactory.getLog(MachineIdleByChamberService.class);

	private final String historyEntity = "";

	public List<MachineIdleByChamber> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<MachineIdleByChamber> result = super.select(condition, bindSet, MachineIdleByChamber.class);

		return result;
	}

	public MachineIdleByChamber selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(MachineIdleByChamber.class, isLock, keySet);
	}

	public MachineIdleByChamber create(EventInfo eventInfo, MachineIdleByChamber dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, MachineIdleByChamber dataInfo) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public MachineIdleByChamber modify(EventInfo eventInfo, MachineIdleByChamber dataInfo)
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public MachineIdleByChamber getMachineIdleByChamber(String machineName, String chamberName, String processOperationName, String processOperationVersion)
	{
		MachineIdleByChamber dataInfo = new MachineIdleByChamber();

		Object[] keySet = new Object[] { machineName, chamberName, processOperationName, processOperationVersion };

		try
		{
			dataInfo = ExtendedObjectProxy.getMachineIdleByChamberService().selectByKey(false, keySet);
		}
		catch (Exception e)
		{
			dataInfo = null;
		}

		return dataInfo;
	}

	public void createMachineIdleByChamber(EventInfo eventInfo, String machineName, String chamberName, String processOperationName, String processOperationVersion, String idleGroupName,
			String recipeName, String recipeGroupName, String countRolSwitch, String lotCount, String lotCountLimit) throws greenFrameDBErrorSignal, CustomException
	{
		MachineIdleByChamber dataInfo = new MachineIdleByChamber();
		dataInfo.setMachineName(machineName);
		dataInfo.setChamberName(chamberName);
		dataInfo.setProcessOperationName(processOperationName);
		dataInfo.setProcessOperationVersion(processOperationVersion);
		dataInfo.setIdleGroupName(idleGroupName);
		dataInfo.setRecipeName(recipeName);
		dataInfo.setRecipeGroupName(recipeGroupName);
		dataInfo.setControlSwitch(countRolSwitch);
		dataInfo.setLotCount(lotCount);
		dataInfo.setLotCountLimit(lotCountLimit);
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventTime(TimeStampUtil.toTimeString(eventInfo.getEventTime()));
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());

		ExtendedObjectProxy.getMachineIdleByChamberService().insert(dataInfo);
	}

	public void deleteMachineIdleByChamber(EventInfo eventInfo, String machineName, String chamberName, String processOperationName, String processOperationVersion) throws CustomException
	{
		MachineIdleByChamber dataInfo = ExtendedObjectProxy.getMachineIdleByChamberService().getMachineIdleByChamber(machineName, chamberName, processOperationName, processOperationVersion);

		if (dataInfo != null)
		{
			ExtendedObjectProxy.getMachineIdleByChamberService().remove(eventInfo, dataInfo);
		}
	}
}
