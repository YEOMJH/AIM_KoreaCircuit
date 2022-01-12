package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.MachineAlarmList;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MachineAlarmListService extends CTORMService<MachineAlarmList> {

	public static Log logger = LogFactory.getLog(MachineAlarmList.class);

	private final String historyEntity = "MachineAlarmHistory";

	public List<MachineAlarmList> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<MachineAlarmList> result = super.select(condition, bindSet, MachineAlarmList.class);

		return result;
	}

	public MachineAlarmList selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(MachineAlarmList.class, isLock, keySet);
	}

	public MachineAlarmList create(EventInfo eventInfo, MachineAlarmList dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, MachineAlarmList dataInfo) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public MachineAlarmList modify(EventInfo eventInfo, MachineAlarmList dataInfo)
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public MachineAlarmList getMachineAlarmListData(String machineName, String unitName, String subUnitName, String alarmCode)
	{
		MachineAlarmList machineAlarmList = new MachineAlarmList();

		try
		{
			machineAlarmList = ExtendedObjectProxy.getMachineAlarmListService().selectByKey(false, new Object[] { machineName, unitName, subUnitName, alarmCode });
		}
		catch (Exception ex)
		{
			logger.info("MachineAlarmList data information is not registred.");
			machineAlarmList = null;
		}

		return machineAlarmList;
	}
}
