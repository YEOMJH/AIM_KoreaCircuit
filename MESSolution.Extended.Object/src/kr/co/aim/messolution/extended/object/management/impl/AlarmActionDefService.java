package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.AlarmActionDef;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AlarmActionDefService extends CTORMService<AlarmActionDef> {

	public static Log logger = LogFactory.getLog(AlarmActionDefService.class);

	private final String historyEntity = "";

	public List<AlarmActionDef> select(String condition, Object[] bindSet) throws CustomException
	{
		List<AlarmActionDef> result = super.select(condition, bindSet, AlarmActionDef.class);

		return result;
	}

	public AlarmActionDef selectByKey(boolean isLock, Object[] keySet) throws CustomException
	{
		return super.selectByKey(AlarmActionDef.class, isLock, keySet);
	}

	public AlarmActionDef create(EventInfo eventInfo, AlarmActionDef dataInfo) throws CustomException
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, AlarmActionDef dataInfo) throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public AlarmActionDef modify(EventInfo eventInfo, AlarmActionDef dataInfo) throws CustomException
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public List<Object> tranform(List resultList)
	{
		if (resultList == null || resultList.size() == 0)
		{
			return null;
		}
		Object result = super.ormExecute(CTORMUtil.createDataInfo(AlarmActionDef.class), resultList);

		if ((result instanceof List))
		{
			return (List) result;
		}

		List<Object> resultSet = new ArrayList();
		resultSet.add((AlarmActionDef) result);
		return resultSet;
	}

	public List<AlarmActionDef> getAlarmActionDefData(String alarmCode, String machineName, String unitName)
	{
		List<AlarmActionDef> alarmActionList = new ArrayList<AlarmActionDef>();

		try
		{
			alarmActionList = ExtendedObjectProxy.getAlarmActionDefService().select(" WHERE 1=1 AND ALARMCODE =? AND MACHINENAME = ? AND UNITNAME =?  ORDER BY SEQ ",
					new Object[] { alarmCode, machineName, unitName });
		}
		catch (Exception ex)
		{
			logger.info("AlarmActionDef data information is not registred.");
			alarmActionList = null;
		}

		return alarmActionList;
	}
}
