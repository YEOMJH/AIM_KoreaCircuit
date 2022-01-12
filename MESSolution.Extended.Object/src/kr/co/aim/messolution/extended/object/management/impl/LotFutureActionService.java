package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.LotFutureAction;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LotFutureActionService extends CTORMService<LotFutureAction> {

	public static Log logger = LogFactory.getLog(LotFutureActionService.class);

	private final String historyEntity = "LotFutureActionHist";

	public List<LotFutureAction> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<LotFutureAction> result = super.select(condition, bindSet, LotFutureAction.class);

		return result;
	}

	public LotFutureAction selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(LotFutureAction.class, isLock, keySet);
	}

	public LotFutureAction create(EventInfo eventInfo, LotFutureAction dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, LotFutureAction dataInfo) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public LotFutureAction modify(EventInfo eventInfo, LotFutureAction dataInfo)
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public LotFutureAction getLotFutureActionData(String lotName, String factoryName, String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion,
			int position, String reasonCode) throws CustomException
	{
		Object[] keySet = new Object[] { lotName, factoryName, processFlowName, processFlowVersion, processOperationName, processOperationVersion, position, reasonCode };

		LotFutureAction dataInfo = new LotFutureAction();

		try
		{
			dataInfo = ExtendedObjectProxy.getLotFutureActionService().selectByKey(false, keySet);
		}
		catch (Exception e)
		{
			dataInfo = null;
		}

		return dataInfo;
	}

	public List<LotFutureAction> getLotFutureActionDataWithLotList(List<Lot> lotList) throws CustomException
	{
		if (lotList == null || lotList.size() == 0)
			return null;

		StringBuffer sql = new StringBuffer();
		sql.append("SELECT * ");
		sql.append("  FROM CT_LOTFUTUREACTION ");
		sql.append(" WHERE 1 = 1 ");
		sql.append("   AND LOTNAME IN (:LOTLIST) ");
		sql.append("   AND FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("   AND PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
		sql.append("   AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
		sql.append("   AND PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION ");

		Map<String, Object> bindMap = new HashMap<>();
		bindMap.put("LOTLIST", CommonUtil.makeToStringList(lotList));
		bindMap.put("FACTORYNAME", lotList.get(0).getFactoryName());
		bindMap.put("PROCESSFLOWNAME", lotList.get(0).getProcessFlowName());
		bindMap.put("PROCESSFLOWVERSION", lotList.get(0).getProcessFlowVersion());
		bindMap.put("PROCESSOPERATIONNAME", lotList.get(0).getProcessOperationName());
		bindMap.put("PROCESSOPERATIONVERSION", lotList.get(0).getProcessOperationVersion());

		List<Map<String, Object>> resultList = null;
		try
		{
			resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
		}
		catch (Exception e)
		{
			throw new CustomException(e.getCause());
		}

		if (resultList == null || resultList.size() == 0)
			return null;

		return ExtendedObjectProxy.getLotFutureActionService().tranform(resultList);
	}

	public List<LotFutureAction> getLotFutureActionList(String lotName)
	{
		String condition = " LOTNAME = ? ";
		Object[] bindSet = new Object[] { lotName };

		List<LotFutureAction> dataInfoList = new ArrayList<LotFutureAction>();

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

	public void deleteLotFutureActionWithLotList(EventInfo eventInfo, List<LotFutureAction> futureActionList)
	{
		if (futureActionList != null)
		{
			this.remove(eventInfo, futureActionList);
		}
	}

	public List<LotFutureAction> tranform(List resultList)
	{
		if (resultList == null || resultList.size() == 0)
		{
			return null;
		}

		Object result = super.ormExecute(CTORMUtil.createDataInfo(LotFutureAction.class), resultList);

		if ((result instanceof List))
		{
			return (List) result;
		}

		List<LotFutureAction> resultSet = new ArrayList();
		resultSet.add((LotFutureAction) result);
		return resultSet;
	}

	public void remove(EventInfo eventInfo, List<LotFutureAction> dataInfoList) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);

		super.deleteBatch(dataInfoList);
		
		//super.delete(dataInfoList);
	}

	public List<LotFutureAction> getLotFutureActionDataList(String lotName, String factoryName, String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVersion, int position, String reasonCode)
	{
		String condition = "LOTNAME = ? AND FACTORYNAME = ? AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ? AND PROCESSOPERATIONNAME = ? AND PROCESSOPERATIONVERSION = ? AND POSITION = ? AND REASONCODE = ?";
		Object[] bindSet = new Object[] { lotName, factoryName, processFlowName, processFlowVersion, processOperationName, processOperationVersion, position, reasonCode };

		List<LotFutureAction> dataList = new ArrayList<LotFutureAction>();
		try
		{
			dataList = ExtendedObjectProxy.getLotFutureActionService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			dataList = null;
		}

		return dataList;
	}

	public List<LotFutureAction> getLotFutureActionDataList(String lotName, String factoryName, String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVersion, int position, String reasonCode, String beforeAction, String afterAction)
	{
		String condition = "LOTNAME = ? AND FACTORYNAME = ? AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ? AND PROCESSOPERATIONNAME = ? AND PROCESSOPERATIONVERSION = ? AND POSITION = ? AND REASONCODE = ? AND BEFOREACTION = ? AND AFTERACTION = ?";

		if (StringUtils.isEmpty(beforeAction))
			beforeAction = "False";

		if (StringUtils.isEmpty(afterAction))
			afterAction = "False";

		Object[] bindSet = new Object[] { lotName, factoryName, processFlowName, processFlowVersion, processOperationName, processOperationVersion, position, reasonCode, beforeAction, afterAction };

		List<LotFutureAction> dataList = new ArrayList<LotFutureAction>();
		try
		{
			dataList = ExtendedObjectProxy.getLotFutureActionService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			dataList = null;
		}

		return dataList;
	}

	public List<LotFutureAction> getLotFutureActionDataListByLotName(String lotName, String factoryName)
	{
		String condition = "LOTNAME = ? AND FACTORYNAME = ?";
		Object[] bindSet = new Object[] { lotName, factoryName };

		List<LotFutureAction> dataList = new ArrayList<LotFutureAction>();
		try
		{
			dataList = ExtendedObjectProxy.getLotFutureActionService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			dataList = null;
		}

		return dataList;
	}

	public List<LotFutureAction> getLotFutureActionDataListWithReasonCodeType(String lotName, String factoryName, String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVersion, int position, String reasonCode, String reasonCodeType)
	{
		String condition = "LOTNAME = ? AND FACTORYNAME = ? AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ? AND PROCESSOPERATIONNAME = ? AND PROCESSOPERATIONVERSION = ? AND POSITION = ? AND REASONCODE = ? AND REASONCODETYPE = ?";
		Object[] bindSet = new Object[] { lotName, factoryName, processFlowName, processFlowVersion, processOperationName, processOperationVersion, position, reasonCode, reasonCodeType };

		List<LotFutureAction> dataList = new ArrayList<LotFutureAction>();
		try
		{
			dataList = ExtendedObjectProxy.getLotFutureActionService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			dataList = null;
		}

		return dataList;
	}

	public List<LotFutureAction> getLotFutureActionDataListWithoutPosition(String lotName, String factoryName, String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVersion, String reasonCode)
	{
		String condition = "LOTNAME = ? AND FACTORYNAME = ? AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ? AND PROCESSOPERATIONNAME = ? AND PROCESSOPERATIONVERSION = ? AND REASONCODE = ?";
		Object[] bindSet = new Object[] { lotName, factoryName, processFlowName, processFlowVersion, processOperationName, processOperationVersion, reasonCode };

		List<LotFutureAction> dataList = new ArrayList<LotFutureAction>();
		try
		{
			dataList = ExtendedObjectProxy.getLotFutureActionService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			dataList = null;
		}

		return dataList;
	}

	public List<LotFutureAction> getLotFutureActionDataListWithoutReasonCode(String lotName, String factoryName, String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVersion, int position)
	{
		String condition = "LOTNAME = ? AND FACTORYNAME = ? AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ? AND PROCESSOPERATIONNAME = ? AND PROCESSOPERATIONVERSION = ? AND POSITION = ?";
		Object[] bindSet = new Object[] { lotName, factoryName, processFlowName, processFlowVersion, processOperationName, processOperationVersion, position };

		List<LotFutureAction> dataList = new ArrayList<LotFutureAction>();
		try
		{
			dataList = ExtendedObjectProxy.getLotFutureActionService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			dataList = null;
		}

		return dataList;
	}

	public List<LotFutureAction> getLotFutureActionDataWithActionName(String lotName, String factoryName, String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVersion, int position, String actionName)
	{
		String condition = "LOTNAME = ? AND FACTORYNAME = ? AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ? AND PROCESSOPERATIONNAME = ? AND PROCESSOPERATIONVERSION = ? AND POSITION = ? AND ACTIONNAME = ?";
		Object[] bindSet = new Object[] { lotName, factoryName, processFlowName, processFlowVersion, processOperationName, processOperationVersion, position, actionName };

		List<LotFutureAction> dataInfoList = new ArrayList<LotFutureAction>();

		try
		{
			dataInfoList = ExtendedObjectProxy.getLotFutureActionService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			dataInfoList = null;
		}

		return dataInfoList;
	}

	public List<LotFutureAction> getLotFutureActionDataWithActionName(String lotName, String factoryName, String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVersion, int position, String reasonCode, String actionName)
	{
		String condition = "LOTNAME = ? AND FACTORYNAME = ? AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ? AND PROCESSOPERATIONNAME = ? AND PROCESSOPERATIONVERSION = ? AND POSITION = ? AND REASONCODE = ? AND ACTIONNAME = ?";
		Object[] bindSet = new Object[] { lotName, factoryName, processFlowName, processFlowVersion, processOperationName, processOperationVersion, position, reasonCode, actionName };

		List<LotFutureAction> dataInfoList = new ArrayList<LotFutureAction>();

		try
		{
			dataInfoList = ExtendedObjectProxy.getLotFutureActionService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			dataInfoList = null;
		}

		return dataInfoList;
	}

	public List<LotFutureAction> getLotFutureActionDataWithActionType(String lotName, String factoryName, String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVersion, int position, String reasonCode, String beforeAction, String afterAction)
	{
		String condition = "LOTNAME = ? AND FACTORYNAME = ? AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ? AND PROCESSOPERATIONNAME = ? AND PROCESSOPERATIONVERSION = ? AND POSITION = ? AND REASONCODE = ? AND BEFOREACTION = ? AND AFTERACTION = ?";
		Object[] bindSet = new Object[] { lotName, factoryName, processFlowName, processFlowVersion, processOperationName, processOperationVersion, position, reasonCode, beforeAction, afterAction };

		List<LotFutureAction> dataInfoList = new ArrayList<LotFutureAction>();

		try
		{
			dataInfoList = ExtendedObjectProxy.getLotFutureActionService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			dataInfoList = null;
		}

		return dataInfoList;
	}

	public List<LotFutureAction> getLotFutureActionDataWithLotList(List<String> lotList, String factoryName, String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVerstion)
	{
		String condition = "LOTNAME IN (";
		if (lotList != null)
		{
			int count = 0;

			for (String lotName : lotList)
			{
				count += 1;
				condition += "'" + lotName + "'";

				if (count != lotList.size())
				{
					condition += ",";
				}
			}

			condition += ")";
		}
		condition += "AND FACTORYNAME = ? AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ? AND PROCESSOPERATIONNAME = ? AND PROCESSOPERATIONVERSION = ?";

		Object[] bindSet = new Object[] { factoryName, processFlowName, processFlowVersion, processOperationName, processOperationVerstion };

		List<LotFutureAction> dataInfoList = new ArrayList<LotFutureAction>();

		try
		{
			dataInfoList = ExtendedObjectProxy.getLotFutureActionService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			dataInfoList = null;
		}

		return dataInfoList;
	}

	public List<LotFutureAction> getLotFutureActionDataByLotList(List<String> lotList)
	{
		String condition = "LOTNAME IN (";
		int count = 0;

		for (String lotName : lotList)
		{
			condition += "'" + lotName + "'";
			count += 1;

			if (count != lotList.size())
			{
				condition += ",";
			}
		}

		condition += ")";

		Object[] bindSet = new Object[] {};

		List<LotFutureAction> dataInfoList = new ArrayList<LotFutureAction>();

		try
		{
			dataInfoList = ExtendedObjectProxy.getLotFutureActionService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			dataInfoList = null;
		}

		return dataInfoList;
	}

	public List<LotFutureAction> getLotFutureActionDataListWithLotNameActionName(String lotName, String actionName)
	{
		String condition = "LOTNAME = ? AND ACTIONNAME = ?";
		Object[] bindSet = new Object[] { lotName, actionName };

		List<LotFutureAction> dataList = new ArrayList<LotFutureAction>();
		try
		{
			dataList = ExtendedObjectProxy.getLotFutureActionService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			dataList = null;
		}

		return dataList;
	}

	public List<LotFutureAction> getLotFutureActionDataListByFlow(String lotName, String factoryName, String processFlowName, String processFlowVersion, int position)
	{
		String condition = "LOTNAME = ? AND FACTORYNAME = ? AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ? AND POSITION = ?";
		Object[] bindSet = new Object[] { lotName, factoryName, processFlowName, processFlowVersion, position };

		List<LotFutureAction> dataList = new ArrayList<LotFutureAction>();
		try
		{
			dataList = ExtendedObjectProxy.getLotFutureActionService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			dataList = null;
		}

		return dataList;
	}

	public List<LotFutureAction> getLotFutureActionDataListPostCell(String lotName, String factoryName, String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVersion)
	{
		String condition = "LOTNAME = ? AND FACTORYNAME = ? AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ? AND PROCESSOPERATIONNAME = ? AND PROCESSOPERATIONVERSION = ?";
		Object[] bindSet = new Object[] { lotName, factoryName, processFlowName, processFlowVersion, processOperationName, processOperationVersion };

		List<LotFutureAction> dataList = new ArrayList<LotFutureAction>();
		try
		{
			dataList = ExtendedObjectProxy.getLotFutureActionService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			dataList = null;
		}

		return dataList;
	}

	public void deleteLotFutureActionWithLotList(EventInfo eventInfo, List<String> lotList, String factoryName, String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVerstion) throws CustomException
	{
		List<LotFutureAction> dataInfoList = ExtendedObjectProxy.getLotFutureActionService().getLotFutureActionDataWithLotList(lotList, factoryName, processFlowName, processFlowVersion,
				processOperationName, processOperationVerstion);

		if (dataInfoList != null)
		{
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			for (LotFutureAction dataInfo : dataInfoList)
			{
				ExtendedObjectProxy.getLotFutureActionService().remove(eventInfo, dataInfo);
			}
		}
	}

	public void deleteLotFutureActionWithActionName(EventInfo eventInfo, String lotName, String factoryName, String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVersion, int position, String actionName) throws CustomException
	{
		List<LotFutureAction> dataInfoList = ExtendedObjectProxy.getLotFutureActionService().getLotFutureActionDataWithActionName(lotName, factoryName, processFlowName, processFlowVersion,
				processOperationName, processOperationVersion, position, actionName);

		if (dataInfoList != null)
		{
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			for (LotFutureAction dataInfo : dataInfoList)
			{
				ExtendedObjectProxy.getLotFutureActionService().remove(eventInfo, dataInfo);
			}
		}
	}

	public void deleteLotFutureActionWithReasonCodeType(EventInfo eventInfo, String lotName, String factoryName, String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVersion, int position, String reasonCode, String reasonCodeType) throws CustomException
	{
		List<LotFutureAction> dataInfoList = ExtendedObjectProxy.getLotFutureActionService().getLotFutureActionDataListWithReasonCodeType(lotName, factoryName, processFlowName, processFlowVersion,
				processOperationName, processOperationVersion, position, reasonCode, reasonCodeType);

		if (dataInfoList != null)
		{
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			for (LotFutureAction dataInfo : dataInfoList)
			{
				ExtendedObjectProxy.getLotFutureActionService().remove(eventInfo, dataInfo);
			}
		}
	}

	public void deleteLotFutureActionWithoutReaconCode(EventInfo eventInfo, String lotName, String factoryName, String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVersion, int position) throws CustomException
	{
		List<LotFutureAction> dataInfoList = ExtendedObjectProxy.getLotFutureActionService().getLotFutureActionDataListWithoutReasonCode(lotName, factoryName, processFlowName, processFlowVersion,
				processOperationName, processOperationVersion, position);

		if (dataInfoList != null)
		{
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			for (LotFutureAction dataInfo : dataInfoList)
			{
				if(!dataInfo.getAttribute3().equals("True"))
				{
					ExtendedObjectProxy.getLotFutureActionService().remove(eventInfo, dataInfo);
				}
				
			}
		}
	}

	public void deleteLotFutureActionData(EventInfo eventInfo, String lotName, String factoryName, String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVersion, int position, String reasonCode) throws CustomException
	{
		LotFutureAction dataInfo = ExtendedObjectProxy.getLotFutureActionService().getLotFutureActionData(lotName, factoryName, processFlowName, processFlowVersion, processOperationName,
				processOperationVersion, position, reasonCode);

		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		ExtendedObjectProxy.getLotFutureActionService().remove(eventInfo, dataInfo);
	}

	public void deleteLotFutureActionByFlow(EventInfo eventInfo, String lotName, String factoryName, String processFlowName, String processFlowVersion, int position) throws CustomException
	{
		List<LotFutureAction> dataInfoList = ExtendedObjectProxy.getLotFutureActionService().getLotFutureActionDataListByFlow(lotName, factoryName, processFlowName, processFlowVersion, position);

		if (dataInfoList != null)
		{
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			for (LotFutureAction dataInfo : dataInfoList)
			{
				ExtendedObjectProxy.getLotFutureActionService().remove(eventInfo, dataInfo);
			}
		}
	}

	public void deleteLotFutureActionDataPostCell(EventInfo eventInfo, String lotName, String factoryName, String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVersion) throws CustomException
	{
		List<LotFutureAction> dataInfoList = ExtendedObjectProxy.getLotFutureActionService().getLotFutureActionDataListPostCell(lotName, factoryName, processFlowName, processFlowVersion,
				processOperationName, processOperationVersion);

		if (dataInfoList != null)
		{
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			for (LotFutureAction dataInfo : dataInfoList)
			{
				ExtendedObjectProxy.getLotFutureActionService().remove(eventInfo, dataInfo);
			}
		}
	}

	public LotFutureAction insertLotFutureAction(EventInfo eventInfo, String lotName, String factoryName, String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVersion, int position, String reasonCode, String reasonCodeType, String actionName, String actionType, String attribute1, String attribute2, String attribute3,
			String beforeAction, String afterAction, String beforeActionComment, String afterActionComment, String beforeActionUser, String afterActionUser) throws CustomException
	{
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		LotFutureAction dataInfo = new LotFutureAction();
		dataInfo.setLotName(lotName);
		dataInfo.setFactoryName(factoryName);
		dataInfo.setProcessFlowName(processFlowName);
		dataInfo.setProcessFlowVersion(processFlowVersion);
		dataInfo.setProcessOperationName(processOperationName);
		dataInfo.setProcessOperationVersion(processOperationVersion);
		dataInfo.setPosition(position);
		dataInfo.setReasonCode(reasonCode);
		dataInfo.setReasonCodeType(reasonCodeType);
		dataInfo.setActionName(actionName);
		dataInfo.setActionType(actionType);
		dataInfo.setAttribute1(attribute1);
		dataInfo.setAttribute2(attribute2);
		dataInfo.setBeforeAction(beforeAction);
		dataInfo.setAfterAction(afterAction);
		dataInfo.setBeforeActionComment(beforeActionComment);
		dataInfo.setAfterActionComment(afterActionComment);
		dataInfo.setBeforeActionUser(beforeActionUser);
		dataInfo.setAfterActionUser(afterActionUser);
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
		dataInfo.setLastEventTime(eventInfo.getEventTime());

		LotFutureAction lotFutureActionData = ExtendedObjectProxy.getLotFutureActionService().create(eventInfo, dataInfo);

		return lotFutureActionData;
	}

	public LotFutureAction insertLotFutureAction(EventInfo eventInfo, String lotName, String factoryName, String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVersion, int position, String reasonCode, String reasonCodeType, String actionName, String actionType, String attribute1, String attribute2, String attribute3,
			String beforeAction, String afterAction, String beforeActionComment, String afterActionComment, String beforeActionUser, String afterActionUser, String beforeMailFlag,
			String afterMailFlag, String requestDepartment, String releaseType, String alarmCode) throws CustomException
	{
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		LotFutureAction dataInfo = new LotFutureAction();
		dataInfo.setLotName(lotName);
		dataInfo.setFactoryName(factoryName);
		dataInfo.setProcessFlowName(processFlowName);
		dataInfo.setProcessFlowVersion(processFlowVersion);
		dataInfo.setProcessOperationName(processOperationName);
		dataInfo.setProcessOperationVersion(processOperationVersion);
		dataInfo.setPosition(position);
		dataInfo.setReasonCode(reasonCode);
		dataInfo.setReasonCodeType(reasonCodeType);
		dataInfo.setActionName(actionName);
		dataInfo.setActionType(actionType);
		dataInfo.setAttribute1(attribute1);
		dataInfo.setAttribute2(attribute2);
		dataInfo.setBeforeAction(beforeAction);
		dataInfo.setAfterAction(afterAction);
		dataInfo.setBeforeActionComment(beforeActionComment);
		dataInfo.setAfterActionComment(afterActionComment);
		dataInfo.setBeforeActionUser(beforeActionUser);
		dataInfo.setAfterActionUser(afterActionUser);
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setBeforeMailFlag(beforeMailFlag);
		dataInfo.setAfterMailFlag(afterMailFlag);
		dataInfo.setRequestDepartment(requestDepartment);
		dataInfo.setReleaseType(releaseType);
		dataInfo.setAlarmCode(alarmCode);

		LotFutureAction lotFutureActionData = ExtendedObjectProxy.getLotFutureActionService().create(eventInfo, dataInfo);

		return lotFutureActionData;
	}

	public LotFutureAction insertLotFutureAction(EventInfo eventInfo, String lotName, String factoryName, String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVersion, int position, String reasonCode, String reasonCodeType, String actionName, String actionType, String attribute1, String attribute2, String attribute3,
			String beforeAction, String afterAction, String beforeActionComment, String afterActionComment, String beforeActionUser, String afterActionUser, String beforeMailFlag,
			String afterMailFlag, String requestDepartment, String releaseType) throws CustomException
	{
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		LotFutureAction dataInfo = new LotFutureAction();
		dataInfo.setLotName(lotName);
		dataInfo.setFactoryName(factoryName);
		dataInfo.setProcessFlowName(processFlowName);
		dataInfo.setProcessFlowVersion(processFlowVersion);
		dataInfo.setProcessOperationName(processOperationName);
		dataInfo.setProcessOperationVersion(processOperationVersion);
		dataInfo.setPosition(position);
		dataInfo.setReasonCode(reasonCode);
		dataInfo.setReasonCodeType(reasonCodeType);
		dataInfo.setActionName(actionName);
		dataInfo.setActionType(actionType);
		dataInfo.setAttribute1(attribute1);
		dataInfo.setAttribute2(attribute2);
		dataInfo.setBeforeAction(beforeAction);
		dataInfo.setAfterAction(afterAction);
		dataInfo.setBeforeActionComment(beforeActionComment);
		dataInfo.setAfterActionComment(afterActionComment);
		dataInfo.setBeforeActionUser(beforeActionUser);
		dataInfo.setAfterActionUser(afterActionUser);
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setBeforeMailFlag(beforeMailFlag);
		dataInfo.setAfterMailFlag(afterMailFlag);
		dataInfo.setRequestDepartment(requestDepartment);
		dataInfo.setReleaseType(releaseType);

		LotFutureAction lotFutureActionData = ExtendedObjectProxy.getLotFutureActionService().create(eventInfo, dataInfo);

		return lotFutureActionData;
	}

	public LotFutureAction insertLotFutureAction(EventInfo eventInfo, String lotName, String factoryName, String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVersion, int position, String reasonCode, String reasonCodeType, String actionName, String actionType, String attribute1, String attribute2, String attribute3,
			String beforeAction, String afterAction, String beforeActionComment, String afterActionComment, String beforeActionUser, String afterActionUser, String beforeMailFlag,
			String afterMailFlag) throws CustomException
	{
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		LotFutureAction dataInfo = new LotFutureAction();
		dataInfo.setLotName(lotName);
		dataInfo.setFactoryName(factoryName);
		dataInfo.setProcessFlowName(processFlowName);
		dataInfo.setProcessFlowVersion(processFlowVersion);
		dataInfo.setProcessOperationName(processOperationName);
		dataInfo.setProcessOperationVersion(processOperationVersion);
		dataInfo.setPosition(position);
		dataInfo.setReasonCode(reasonCode);
		dataInfo.setReasonCodeType(reasonCodeType);
		dataInfo.setActionName(actionName);
		dataInfo.setActionType(actionType);
		dataInfo.setAttribute1(attribute1);
		dataInfo.setAttribute2(attribute2);
		dataInfo.setAttribute3(attribute3);
		dataInfo.setBeforeAction(beforeAction);
		dataInfo.setAfterAction(afterAction);
		dataInfo.setBeforeActionComment(beforeActionComment);
		dataInfo.setAfterActionComment(afterActionComment);
		dataInfo.setBeforeActionUser(beforeActionUser);
		dataInfo.setAfterActionUser(afterActionUser);
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setBeforeMailFlag(beforeMailFlag);
		dataInfo.setAfterMailFlag(afterMailFlag);

		LotFutureAction lotFutureActionData = ExtendedObjectProxy.getLotFutureActionService().create(eventInfo, dataInfo);

		return lotFutureActionData;
	}
	
	public LotFutureAction insertLotFutureAction(EventInfo eventInfo, String lotName, String factoryName, String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVersion, int position, String reasonCode, String reasonCodeType, String actionName, String actionType, String attribute1, String attribute2, String attribute3,
			String beforeAction, String afterAction, String beforeActionComment, String afterActionComment, String beforeActionUser, String afterActionUser, String beforeMailFlag,
			String afterMailFlag, String alarmCode) throws CustomException
	{
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		LotFutureAction dataInfo = new LotFutureAction();
		dataInfo.setLotName(lotName);
		dataInfo.setFactoryName(factoryName);
		dataInfo.setProcessFlowName(processFlowName);
		dataInfo.setProcessFlowVersion(processFlowVersion);
		dataInfo.setProcessOperationName(processOperationName);
		dataInfo.setProcessOperationVersion(processOperationVersion);
		dataInfo.setPosition(position);
		dataInfo.setReasonCode(reasonCode);
		dataInfo.setReasonCodeType(reasonCodeType);
		dataInfo.setActionName(actionName);
		dataInfo.setActionType(actionType);
		dataInfo.setAttribute1(attribute1);
		dataInfo.setAttribute2(attribute2);
		dataInfo.setBeforeAction(beforeAction);
		dataInfo.setAfterAction(afterAction);
		dataInfo.setBeforeActionComment(beforeActionComment);
		dataInfo.setAfterActionComment(afterActionComment);
		dataInfo.setBeforeActionUser(beforeActionUser);
		dataInfo.setAfterActionUser(afterActionUser);
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setBeforeMailFlag(beforeMailFlag);
		dataInfo.setAfterMailFlag(afterMailFlag);
		dataInfo.setAlarmCode(alarmCode);

		LotFutureAction lotFutureActionData = ExtendedObjectProxy.getLotFutureActionService().create(eventInfo, dataInfo);

		return lotFutureActionData;
	}

	public LotFutureAction insertLotFutureActionForSkip(EventInfo eventInfo, String lotName, String factoryName, String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVersion, int position, String reasonCode, String reasonCodeType, String actionName, String actionType, String attribute1, String attribute2, String attribute3,
			String beforeAction, String afterAction, String beforeActionComment, String afterActionComment, String beforeActionUser, String afterActionUser, String beforeMailFlag,
			String afterMailFlag, String eventComment) throws CustomException
	{
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		LotFutureAction dataInfo = new LotFutureAction();
		dataInfo.setLotName(lotName);
		dataInfo.setFactoryName(factoryName);
		dataInfo.setProcessFlowName(processFlowName);
		dataInfo.setProcessFlowVersion(processFlowVersion);
		dataInfo.setProcessOperationName(processOperationName);
		dataInfo.setProcessOperationVersion(processOperationVersion);
		dataInfo.setPosition(position);
		dataInfo.setReasonCode(reasonCode);
		dataInfo.setReasonCodeType(reasonCodeType);
		dataInfo.setActionName(actionName);
		dataInfo.setActionType(actionType);
		dataInfo.setAttribute1(attribute1);
		dataInfo.setAttribute2(attribute2);
		dataInfo.setBeforeAction(beforeAction);
		dataInfo.setAfterAction(afterAction);
		dataInfo.setBeforeActionComment(beforeActionComment);
		dataInfo.setAfterActionComment(afterActionComment);
		dataInfo.setBeforeActionUser(beforeActionUser);
		dataInfo.setAfterActionUser(afterActionUser);
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventComment(eventComment);
		dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setBeforeMailFlag(beforeMailFlag);
		dataInfo.setAfterMailFlag(afterMailFlag);

		LotFutureAction lotFutureActionData = ExtendedObjectProxy.getLotFutureActionService().create(eventInfo, dataInfo);

		return lotFutureActionData;
	}

	public LotFutureAction insertLotFutureActionForSkip(EventInfo eventInfo, String lotName, String factoryName, String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVersion, int position, String reasonCode, String reasonCodeType, String actionName, String actionType, String attribute1, String attribute2, String attribute3)
			throws CustomException
	{
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		LotFutureAction dataInfo = new LotFutureAction();
		dataInfo.setLotName(lotName);
		dataInfo.setFactoryName(factoryName);
		dataInfo.setProcessFlowName(processFlowName);
		dataInfo.setProcessFlowVersion(processFlowVersion);
		dataInfo.setProcessOperationName(processOperationName);
		dataInfo.setProcessOperationVersion(processOperationVersion);
		dataInfo.setPosition(position);
		dataInfo.setReasonCode(reasonCode);
		dataInfo.setReasonCodeType(reasonCodeType);
		dataInfo.setActionName(actionName);
		dataInfo.setActionType(actionType);
		dataInfo.setAttribute1(attribute1);
		dataInfo.setAttribute2(attribute2);
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
		dataInfo.setLastEventTime(eventInfo.getEventTime());

		LotFutureAction lotFutureActionData = ExtendedObjectProxy.getLotFutureActionService().create(eventInfo, dataInfo);

		return lotFutureActionData;
	}

	public LotFutureAction updateLotFutureAction(EventInfo eventInfo, String lotName, String factoryName, String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVersion, int position, String reasonCode, String reasonCodeType, String actionName, String actionType, String attribute1, String attribute2, String attribute3,
			String beforeAction, String afterAction, String beforeActionComment, String afterActionComment, String beforeActionUser, String afterActionUser) throws CustomException
	{
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		LotFutureAction dataInfo = ExtendedObjectProxy.getLotFutureActionService().getLotFutureActionData(lotName, factoryName, processFlowName, processFlowVersion, processOperationName,
				processOperationVersion, position, reasonCode);

		LotFutureAction lotFutureActionData = new LotFutureAction();

		if (dataInfo != null)
		{
			dataInfo.setReasonCodeType(reasonCodeType);
			dataInfo.setActionName(actionName);
			dataInfo.setActionType(actionType);
			dataInfo.setAttribute1(attribute1);
			dataInfo.setAttribute2(attribute2);
			dataInfo.setBeforeAction(beforeAction);
			dataInfo.setAfterAction(afterAction);
			dataInfo.setBeforeActionComment(beforeActionComment);
			dataInfo.setAfterActionComment(afterActionComment);
			dataInfo.setBeforeActionUser(beforeActionUser);
			dataInfo.setAfterActionUser(afterActionUser);

			dataInfo.setLastEventUser(eventInfo.getEventUser());
			dataInfo.setLastEventName(eventInfo.getEventName());
			dataInfo.setLastEventComment(eventInfo.getEventComment());
			dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
			dataInfo.setLastEventTime(eventInfo.getEventTime());

			lotFutureActionData = ExtendedObjectProxy.getLotFutureActionService().modify(eventInfo, dataInfo);
		}
		else
		{
			lotFutureActionData = null;
		}

		return lotFutureActionData;
	}

	public List<LotFutureAction> updateLotFutureActionWithReasonCodeType(EventInfo eventInfo, String lotName, String factoryName, String processFlowName, String processFlowVersion,
			String processOperationName, String processOperationVersion, int position, String reasonCode, String reasonCodeType, String actionName, String actionType, String attribute1,
			String attribute2, String attribute3, String beforeAction, String afterAction, String beforeActionComment, String afterActionComment, String beforeActionUser, String afterActionUser)
			throws CustomException
	{
		List<LotFutureAction> dataInfoList = ExtendedObjectProxy.getLotFutureActionService().getLotFutureActionDataListWithReasonCodeType(lotName, factoryName, processFlowName, processFlowVersion,
				processOperationName, processOperationVersion, position, reasonCode, reasonCodeType);

		List<LotFutureAction> lotFutureActionDataList = new ArrayList<LotFutureAction>();

		if (dataInfoList != null)
		{
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			for (LotFutureAction dataInfo : dataInfoList)
			{
				dataInfo.setReasonCodeType(reasonCodeType);
				dataInfo.setActionName(actionName);
				dataInfo.setActionType(actionType);
				dataInfo.setAttribute1(attribute1);
				dataInfo.setAttribute2(attribute2);
				dataInfo.setBeforeAction(beforeAction);
				dataInfo.setAfterAction(afterAction);
				dataInfo.setBeforeActionComment(beforeActionComment);
				dataInfo.setAfterActionComment(afterActionComment);
				dataInfo.setBeforeActionUser(beforeActionUser);
				dataInfo.setAfterActionUser(afterActionUser);

				dataInfo.setLastEventUser(eventInfo.getEventUser());
				dataInfo.setLastEventName(eventInfo.getEventName());
				dataInfo.setLastEventComment(eventInfo.getEventComment());
				dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
				dataInfo.setLastEventTime(eventInfo.getEventTime());

				LotFutureAction lotFutureActionData = ExtendedObjectProxy.getLotFutureActionService().modify(eventInfo, dataInfo);

				lotFutureActionDataList.add(lotFutureActionData);
			}
		}

		return lotFutureActionDataList;
	}

	public List<LotFutureAction> updateLotFutureActionWithReasonCodeType(EventInfo eventInfo, String lotName, String factoryName, String processFlowName, String processFlowVersion,
			String processOperationName, String processOperationVersion, int position, String reasonCode, String reasonCodeType, String actionName, String actionType, String attribute1,
			String attribute2, String attribute3, String beforeAction, String afterAction, String beforeActionComment, String afterActionComment, String beforeActionUser, String afterActionUser,
			String beforeMailFlag, String afterMailFlag, String requestDepartment, String releaseType, String alarmCode) throws CustomException
	{
		List<LotFutureAction> dataInfoList = ExtendedObjectProxy.getLotFutureActionService().getLotFutureActionDataListWithReasonCodeType(lotName, factoryName, processFlowName, processFlowVersion,
				processOperationName, processOperationVersion, position, reasonCode, reasonCodeType);

		List<LotFutureAction> lotFutureActionDataList = new ArrayList<LotFutureAction>();

		if (dataInfoList != null)
		{
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			for (LotFutureAction dataInfo : dataInfoList)
			{
				dataInfo.setReasonCodeType(reasonCodeType);
				dataInfo.setActionName(actionName);
				dataInfo.setActionType(actionType);
				dataInfo.setAttribute1(attribute1);
				dataInfo.setAttribute2(attribute2);
				dataInfo.setBeforeAction(beforeAction);
				dataInfo.setAfterAction(afterAction);
				dataInfo.setBeforeActionComment(beforeActionComment);
				dataInfo.setAfterActionComment(afterActionComment);
				dataInfo.setBeforeActionUser(beforeActionUser);
				dataInfo.setAfterActionUser(afterActionUser);
				dataInfo.setLastEventUser(eventInfo.getEventUser());
				dataInfo.setLastEventName(eventInfo.getEventName());
				dataInfo.setLastEventComment(eventInfo.getEventComment());
				dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
				dataInfo.setLastEventTime(eventInfo.getEventTime());
				dataInfo.setBeforeMailFlag(beforeMailFlag);
				dataInfo.setAfterMailFlag(afterMailFlag);
				dataInfo.setRequestDepartment(requestDepartment);
				dataInfo.setReleaseType(releaseType);
				dataInfo.setAlarmCode(alarmCode);

				LotFutureAction lotFutureActionData = ExtendedObjectProxy.getLotFutureActionService().modify(eventInfo, dataInfo);

				lotFutureActionDataList.add(lotFutureActionData);
			}
		}

		return lotFutureActionDataList;
	}

	public List<LotFutureAction> updateLotFutureActionWithReasonCodeType(EventInfo eventInfo, String lotName, String factoryName, String processFlowName, String processFlowVersion,
			String processOperationName, String processOperationVersion, int position, String reasonCode, String reasonCodeType, String actionName, String actionType, String attribute1,
			String attribute2, String attribute3, String beforeAction, String afterAction, String beforeActionComment, String afterActionComment, String beforeActionUser, String afterActionUser,
			String beforeMailFlag, String afterMailFlag, String requestDepartment, String releaseType) throws CustomException
	{
		List<LotFutureAction> dataInfoList = ExtendedObjectProxy.getLotFutureActionService().getLotFutureActionDataListWithReasonCodeType(lotName, factoryName, processFlowName, processFlowVersion,
				processOperationName, processOperationVersion, position, reasonCode, reasonCodeType);

		List<LotFutureAction> lotFutureActionDataList = new ArrayList<LotFutureAction>();

		if (dataInfoList != null)
		{
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			for (LotFutureAction dataInfo : dataInfoList)
			{
				dataInfo.setReasonCodeType(reasonCodeType);
				dataInfo.setActionName(actionName);
				dataInfo.setActionType(actionType);
				dataInfo.setAttribute1(attribute1);
				dataInfo.setAttribute2(attribute2);
				dataInfo.setBeforeAction(beforeAction);
				dataInfo.setAfterAction(afterAction);
				dataInfo.setBeforeActionComment(beforeActionComment);
				dataInfo.setAfterActionComment(afterActionComment);
				dataInfo.setBeforeActionUser(beforeActionUser);
				dataInfo.setAfterActionUser(afterActionUser);
				dataInfo.setLastEventUser(eventInfo.getEventUser());
				dataInfo.setLastEventName(eventInfo.getEventName());
				dataInfo.setLastEventComment(eventInfo.getEventComment());
				dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
				dataInfo.setLastEventTime(eventInfo.getEventTime());
				dataInfo.setBeforeMailFlag(beforeMailFlag);
				dataInfo.setAfterMailFlag(afterMailFlag);
				dataInfo.setRequestDepartment(requestDepartment);
				dataInfo.setReleaseType(releaseType);

				LotFutureAction lotFutureActionData = ExtendedObjectProxy.getLotFutureActionService().modify(eventInfo, dataInfo);

				lotFutureActionDataList.add(lotFutureActionData);
			}
		}

		return lotFutureActionDataList;
	}

	public List<LotFutureAction> updateLotFutureActionWithReasonCodeType(EventInfo eventInfo, String lotName, String factoryName, String processFlowName, String processFlowVersion,
			String processOperationName, String processOperationVersion, int position, String reasonCode, String reasonCodeType, String actionName, String actionType, String attribute1,
			String attribute2, String attribute3, String beforeAction, String afterAction, String beforeActionComment, String afterActionComment, String beforeActionUser, String afterActionUser,
			String beforeMailFlag, String afterMailFlag) throws CustomException
	{
		List<LotFutureAction> dataInfoList = ExtendedObjectProxy.getLotFutureActionService().getLotFutureActionDataListWithReasonCodeType(lotName, factoryName, processFlowName, processFlowVersion,
				processOperationName, processOperationVersion, position, reasonCode, reasonCodeType);

		List<LotFutureAction> lotFutureActionDataList = new ArrayList<LotFutureAction>();

		if (dataInfoList != null)
		{
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			for (LotFutureAction dataInfo : dataInfoList)
			{
				dataInfo.setReasonCodeType(reasonCodeType);
				dataInfo.setActionName(actionName);
				dataInfo.setActionType(actionType);
				dataInfo.setAttribute1(attribute1);
				dataInfo.setAttribute2(attribute2);
				dataInfo.setAttribute3(attribute3);
				dataInfo.setBeforeAction(beforeAction);
				dataInfo.setAfterAction(afterAction);
				dataInfo.setBeforeActionComment(beforeActionComment);
				dataInfo.setAfterActionComment(afterActionComment);
				dataInfo.setBeforeActionUser(beforeActionUser);
				dataInfo.setAfterActionUser(afterActionUser);
				dataInfo.setLastEventUser(eventInfo.getEventUser());
				dataInfo.setLastEventName(eventInfo.getEventName());
				dataInfo.setLastEventComment(eventInfo.getEventComment());
				dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
				dataInfo.setLastEventTime(eventInfo.getEventTime());
				dataInfo.setBeforeMailFlag(beforeMailFlag);
				dataInfo.setAfterMailFlag(afterMailFlag);

				LotFutureAction lotFutureActionData = ExtendedObjectProxy.getLotFutureActionService().modify(eventInfo, dataInfo);

				lotFutureActionDataList.add(lotFutureActionData);
			}
		}

		return lotFutureActionDataList;
	}

	/**
	 * 
	 * AR-Photo-0027-01,AR-Photo-0032-01 ReserveHold sends an e-mail when it arrives at the process.
	 * 
	 * @author aim_dhko
	 * @return
	 */
	public List<LotFutureAction> getReserveHoldDataList(String lotName, String factoryName, String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVersion, int position)
	{
		String condition = "ACTIONNAME = 'hold' AND LOTNAME = ? AND FACTORYNAME = ? AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ? AND PROCESSOPERATIONNAME = ? AND PROCESSOPERATIONVERSION = ? AND POSITION = ?";
		Object[] bindSet = new Object[] { lotName, factoryName, processFlowName, processFlowVersion, processOperationName, processOperationVersion, position };

		List<LotFutureAction> dataList = new ArrayList<LotFutureAction>();
		try
		{
			dataList = ExtendedObjectProxy.getLotFutureActionService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			dataList = null;
		}

		return dataList;
	}
}
