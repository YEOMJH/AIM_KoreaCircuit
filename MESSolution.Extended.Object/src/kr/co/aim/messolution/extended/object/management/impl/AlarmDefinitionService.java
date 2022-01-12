package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.AlarmDefinition;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AlarmDefinitionService extends CTORMService<AlarmDefinition> {

	public static Log logger = LogFactory.getLog(AlarmDefinitionService.class);

	private final String historyEntity = "";

	public List<AlarmDefinition> select(String condition, Object[] bindSet) throws CustomException
	{
		List<AlarmDefinition> result = super.select(condition, bindSet, AlarmDefinition.class);

		return result;
	}

	public AlarmDefinition selectByKey(boolean isLock, Object[] keySet) throws CustomException
	{
		return super.selectByKey(AlarmDefinition.class, isLock, keySet);
	}

	public AlarmDefinition create(EventInfo eventInfo, AlarmDefinition dataInfo) throws CustomException
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, AlarmDefinition dataInfo) throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public AlarmDefinition modify(EventInfo eventInfo, AlarmDefinition dataInfo) throws CustomException
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
		Object result = super.ormExecute(CTORMUtil.createDataInfo(AlarmDefinition.class), resultList);

		if ((result instanceof List))
		{
			return (List) result;
		}

		List<Object> resultSet = new ArrayList();
		resultSet.add((AlarmDefinition) result);
		return resultSet;
	}

	public List<AlarmDefinition> getAlarmData(String alarmCode, String machineName, String unitName)
	{
		List<AlarmDefinition> alarmDefList = new ArrayList<AlarmDefinition>();

		try
		{
			alarmDefList = ExtendedObjectProxy.getAlarmDefinitionService().select(" WHERE 1=1 and alarmCode = ? and machineName = ? and unitName = ? ",
					new Object[] { alarmCode, machineName, unitName });
		}
		catch (Exception e)
		{
			logger.info("AlarmDefinition data information is not registred.");
			alarmDefList = null;
		}

		return alarmDefList;
	}
}
