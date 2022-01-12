package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.MQCPlanDetail_Extended;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MQCPlanDetail_ExtendedService extends CTORMService<MQCPlanDetail_Extended> {
	public static Log logger = LogFactory.getLog(MQCPlanDetailService.class);

	private final String historyEntity = "";

	public List<MQCPlanDetail_Extended> select(String condition, Object[] bindSet) throws CustomException
	{
		List<MQCPlanDetail_Extended> result = super.select(condition, bindSet, MQCPlanDetail_Extended.class);

		return result;
	}

	public MQCPlanDetail_Extended selectByKey(boolean isLock, Object[] keySet) throws CustomException
	{
		return super.selectByKey(MQCPlanDetail_Extended.class, isLock, keySet);
	}

	public MQCPlanDetail_Extended create(EventInfo eventInfo, MQCPlanDetail_Extended dataInfo) throws CustomException
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void create(EventInfo eventInfo, List<MQCPlanDetail_Extended> dataInfoList) throws CustomException
	{
		super.insert(dataInfoList);

		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
	}

	public void remove(EventInfo eventInfo, MQCPlanDetail_Extended dataInfo) throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public MQCPlanDetail_Extended modify(EventInfo eventInfo, MQCPlanDetail_Extended dataInfo) throws CustomException
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
   public List<Object> tranform (List resultList)
	{
		if (resultList==null || resultList.size() == 0)
		{
			return null;
		}
		
		MQCPlanDetail_Extended dataInfo = null;
		Object result = super.ormExecute(CTORMUtil.createDataInfo(MQCPlanDetail_Extended.class), resultList);

		if ((result instanceof List))
		{
			return (List) result;
		}

		List<Object> resultSet = new ArrayList();
		resultSet.add((MQCPlanDetail_Extended) result);
		return resultSet;
	}
   
   public List<Map<String, Object>> getMQCProduct(String lotName)
	{
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT P.PRODUCTNAME, P.POSITION, E.RECIPENAME, E.MACHINENAME ");
		sql.append("  FROM CT_MQCPLAN M, ");
		sql.append("       CT_MQCPLANDETAIL D, ");
		sql.append("       CT_MQCPLANDETAIL_EXTENDED E, ");
		sql.append("       PRODUCT P ");
		sql.append(" WHERE M.JOBNAME = D.JOBNAME ");
		sql.append("   AND M.JOBNAME = E.JOBNAME ");
		sql.append("   AND M.MQCSTATE = 'Released' ");
		sql.append("   AND E.PRODUCTNAME = P.PRODUCTNAME ");
		sql.append("   AND P.PRODUCTSTATE = 'InProduction' ");
		sql.append("   AND M.LOTNAME = :LOTNAME ");
		sql.append("   AND M.LOTNAME = P.LOTNAME ");
		sql.append("   AND M.PROCESSFLOWNAME = P.PROCESSFLOWNAME ");
		sql.append("   AND M.PROCESSFLOWVERSION = P.PROCESSFLOWVERSION ");
		sql.append("   AND D.PROCESSFLOWNAME = E.PROCESSFLOWNAME ");
		sql.append("   AND D.PROCESSFLOWVERSION = E.PROCESSFLOWVERSION ");
		sql.append("   AND D.PROCESSOPERATIONNAME = E.PROCESSOPERATIONNAME ");
		sql.append("   AND D.PROCESSOPERATIONVERSION = E.PROCESSOPERATIONVERSION ");
		sql.append("   AND D.PROCESSFLOWNAME = P.PROCESSFLOWNAME ");
		sql.append("   AND D.PROCESSFLOWVERSION = P.PROCESSFLOWVERSION ");
		sql.append("   AND D.PROCESSOPERATIONNAME = P.PROCESSOPERATIONNAME ");
		sql.append("   AND D.PROCESSOPERATIONVERSION = P.PROCESSOPERATIONVERSION ");
		sql.append("   AND E.PRODUCTNAME = P.PRODUCTNAME ");
		sql.append("ORDER BY P.POSITION ");

		Map<String, String> args = new HashMap<String, String>();
		args.put("LOTNAME", lotName);

		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		return result;
	}

	public List<Map<String, Object>> getMQCRecycleProduct(String lotName)
	{
		List<Map<String, Object>> result = null;

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT P.PRODUCTNAME, P.POSITION, E.RECIPENAME, E.MACHINENAME  ");
		sql.append("  FROM CT_MQCPLAN M,  ");
		sql.append("       CT_MQCPLANDETAIL D,  ");
		sql.append("       CT_MQCPLANDETAIL_EXTENDED E,  ");
		sql.append("       PRODUCT P  ");
		sql.append(" WHERE M.JOBNAME = D.JOBNAME  ");
		sql.append("   AND M.JOBNAME = E.JOBNAME  ");
		sql.append("   AND M.MQCSTATE = 'Recycling'  ");
		sql.append("   AND E.PRODUCTNAME = P.PRODUCTNAME  ");
		sql.append("   AND P.PRODUCTSTATE = 'InProduction'  ");
		sql.append("   AND M.LOTNAME = :LOTNAME  ");
		sql.append("   AND M.LOTNAME = P.LOTNAME  ");
		sql.append("   AND D.PROCESSFLOWNAME = E.PROCESSFLOWNAME  ");
		sql.append("   AND D.PROCESSFLOWVERSION = E.PROCESSFLOWVERSION  ");
		sql.append("   AND D.PROCESSOPERATIONNAME = E.PROCESSOPERATIONNAME  ");
		sql.append("   AND D.PROCESSOPERATIONVERSION = E.PROCESSOPERATIONVERSION  ");
		sql.append("   AND D.PROCESSFLOWNAME = P.PROCESSFLOWNAME  ");
		sql.append("   AND D.PROCESSFLOWVERSION = P.PROCESSFLOWVERSION  ");
		sql.append("   AND D.PROCESSOPERATIONNAME = P.PROCESSOPERATIONNAME  ");
		sql.append("   AND D.PROCESSOPERATIONVERSION = P.PROCESSOPERATIONVERSION  ");
		sql.append("   AND E.PRODUCTNAME = P.PRODUCTNAME  ");
		sql.append("ORDER BY P.POSITION  ");

		Map<String, String> args = new HashMap<String, String>();
		args.put("LOTNAME", lotName);

		try
		{
			result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

			if (result.size() > 0)
			{
				// Product Position information based on Operation registered in the MQCPlanDetail_EXTENDED Table
			}
			else
			{// Product Position Information Based on Existing MQCMainFlow
				StringBuilder sql2 = new StringBuilder();

				sql2.append("SELECT DISTINCT P.PRODUCTNAME, P.POSITION, E.RECIPENAME, E.MACHINENAME  ");
				sql2.append("  FROM CT_MQCPLAN M,  ");
				sql2.append("       CT_MQCPLANDETAIL D,  ");
				sql2.append("       CT_MQCPLANDETAIL_EXTENDED E, ");
				sql2.append("       PROCESSFLOW F, ");
				sql2.append("       PRODUCT P  ");
				sql2.append(" WHERE M.JOBNAME = D.JOBNAME  ");
				sql2.append("   AND M.JOBNAME = E.JOBNAME  ");
				sql2.append("   AND M.MQCSTATE = 'Recycling'  ");
				sql2.append("   AND E.PRODUCTNAME = P.PRODUCTNAME  ");
				sql2.append("   AND P.PRODUCTSTATE = 'InProduction' ");
				sql2.append("   AND D.PROCESSFLOWNAME = E.PROCESSFLOWNAME ");
				sql2.append("   AND D.PROCESSFLOWVERSION = E.PROCESSFLOWVERSION ");
				sql2.append("   AND E.PROCESSFLOWNAME = F.PROCESSFLOWNAME ");
				sql2.append("   AND E.PROCESSFLOWVERSION = F.PROCESSFLOWVERSION ");
				sql2.append("   AND F.PROCESSFLOWTYPE != 'MQCRecycle' ");
				sql2.append("   AND M.LOTNAME = :LOTNAME  ");
				sql2.append("   AND M.LOTNAME = P.LOTNAME  ");
				sql2.append("   AND E.PRODUCTNAME = P.PRODUCTNAME  ");
				sql2.append("ORDER BY P.POSITION  ");

				Map<String, String> args2 = new HashMap<String, String>();
				args2.put("LOTNAME", lotName);

				result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql2.toString(), args2);

				if (result.size() > 0)
				{
					return result;
				}
			}
		}
		catch (Exception ex)
		{
			logger.info("getMQCRecycleProduct error ");
		}

		return result;
	}

	public List<MQCPlanDetail_Extended> getMQCPlanDetail_ExtendedByJobName(String jobName)
	{
		String condition = "JOBNAME = ?";
		Object[] bindSet = new Object[] { jobName };

		List<MQCPlanDetail_Extended> dataInfoList = new ArrayList<MQCPlanDetail_Extended>();

		try
		{
			dataInfoList = ExtendedObjectProxy.getMQCPlanDetail_ExtendedService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			dataInfoList = null;
		}

		return dataInfoList;
	}

	public List<MQCPlanDetail_Extended> getMQCPlanDetail_ExtendedWithoutProductName(String jobName, String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVersion)
	{
		String condition = "JOBNAME = ? AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ? AND PROCESSOPERATIONNAME = ? AND PROCESSOPERATIONVERSION = ?";
		Object[] bindSet = new Object[] { jobName, processFlowName, processFlowVersion, processOperationName, processOperationVersion };

		List<MQCPlanDetail_Extended> dataInfoList = new ArrayList<MQCPlanDetail_Extended>();

		try
		{
			dataInfoList = ExtendedObjectProxy.getMQCPlanDetail_ExtendedService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			dataInfoList = null;
		}

		return dataInfoList;
	}

	public List<MQCPlanDetail_Extended> getMQCPlanDetail_ExtendedByFlow(String jobName, String processFlowName, String processFlowVersion)
	{
		String condition = "JOBNAME = ? AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ?";
		Object[] bindSet = new Object[] { jobName, processFlowName, processFlowVersion };

		List<MQCPlanDetail_Extended> dataInfoList = new ArrayList<MQCPlanDetail_Extended>();

		try
		{
			dataInfoList = ExtendedObjectProxy.getMQCPlanDetail_ExtendedService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			dataInfoList = null;
		}

		return dataInfoList;
	}

	public List<MQCPlanDetail_Extended> getMQCPlanDetail_ExtendedByProdList(String jobName, List<String> productList)
	{
		String condition = "JOBNAME = ? AND PRODUCTNAME IN (";

		int count = 0;

		if (productList != null && productList.size() > 0)
		{
			for (String productName : productList)
			{
				condition += "'" + productName + "'";
				count += 1;

				if (count != productList.size())
				{
					condition += ",";
				}
			}
		}

		condition += ")";

		Object[] bindSet = new Object[] { jobName };

		List<MQCPlanDetail_Extended> dataInfoList = new ArrayList<MQCPlanDetail_Extended>();

		try
		{
			dataInfoList = ExtendedObjectProxy.getMQCPlanDetail_ExtendedService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			dataInfoList = null;
		}

		return dataInfoList;
	}

	public List<MQCPlanDetail_Extended> getMQCPlanDetail_ExtendedOverCount(String jobName, String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVersion, String dummyUsedCount)
	{
		String condition = "JOBNAME = ? AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ? AND PROCESSOPERATIONNAME = ? AND PROCESSOPERATIONVERSION = ? AND DUMMYUSEDCOUNT >= ? AND ROWNUM = 1";
		Object[] bindSet = new Object[] { jobName, processFlowName, processFlowVersion, processOperationName, processOperationVersion, Integer.parseInt(dummyUsedCount) };

		List<MQCPlanDetail_Extended> dataInfoList = new ArrayList<MQCPlanDetail_Extended>();

		try
		{
			dataInfoList = ExtendedObjectProxy.getMQCPlanDetail_ExtendedService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			dataInfoList = null;
		}

		return dataInfoList;
	}

	public List<MQCPlanDetail_Extended> getMQCPlanDetail_ExtendedByProductName(String jobName, String lotName, String productName)
	{
		String condition = "JOBNAME = ? AND LOTNAME = ? AND PRODUCTNAME = ?";
		Object[] bindSet = new Object[] { jobName, lotName, productName };

		List<MQCPlanDetail_Extended> dataInfoList = new ArrayList<MQCPlanDetail_Extended>();

		try
		{
			dataInfoList = ExtendedObjectProxy.getMQCPlanDetail_ExtendedService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			dataInfoList = null;
		}

		return dataInfoList;
	}

	public void insertMQCPlanDetail_Extended(EventInfo eventInfo, String jobName, String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion,
			String productName, String position, String lotName, String forbiddenCode, String oldForbiddenCode, String dummyUsedCount, String recipeName, String machineName)
			throws greenFrameDBErrorSignal, CustomException
	{
		MQCPlanDetail_Extended dataInfo = new MQCPlanDetail_Extended();
		dataInfo.setJobName(jobName);
		dataInfo.setProcessFlowName(processFlowName);
		dataInfo.setProcessFlowVersion(processFlowVersion);
		dataInfo.setProcessOperationName(processOperationName);
		dataInfo.setProcessOperationVersion(processOperationVersion);
		dataInfo.setProductName(productName);
		dataInfo.setPosition(position);
		dataInfo.setLotName(lotName);
		dataInfo.setForbiddenCode(forbiddenCode);
		dataInfo.setOldForbiddenCode(oldForbiddenCode);
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventTime(eventInfo.getEventTime().toString());
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setDummyUsedCount(Integer.parseInt(dummyUsedCount));
		dataInfo.setRecipeName(recipeName);
		dataInfo.setMachineName(machineName);

		ExtendedObjectProxy.getMQCPlanDetail_ExtendedService().insert(dataInfo);
	}

	public void deleteMQCPlanDetail_ExtendedWithoutProductName(String jobName, String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion)
			throws CustomException, NotFoundSignal
	{
		logger.info(" Start delete from CT_MQCPLANDETAIL_EXTENDED");

		List<MQCPlanDetail_Extended> dataInfoList = ExtendedObjectProxy.getMQCPlanDetail_ExtendedService().getMQCPlanDetail_ExtendedWithoutProductName(jobName, processFlowName, processFlowVersion,
				processOperationName, processOperationVersion);

		if (dataInfoList != null)
		{
			ExtendedObjectProxy.getMQCPlanDetail_ExtendedService().delete(dataInfoList);
		}

		logger.info("End MQCJob: " + jobName + " delete from CT_MQCPLANDETAIL_EXTENDED Sucess");
	}

	public void deleteMQCPlanDetail_ExtendedByFlow(String jobName, String processFlowName, String processFlowVersion) throws CustomException, NotFoundSignal
	{
		logger.info(" Start delete from CT_MQCPLANDETAIL_EXTENDED");

		List<MQCPlanDetail_Extended> dataInfoList = ExtendedObjectProxy.getMQCPlanDetail_ExtendedService().getMQCPlanDetail_ExtendedByFlow(jobName, processFlowName, processFlowVersion);

		if (dataInfoList != null)
		{
			ExtendedObjectProxy.getMQCPlanDetail_ExtendedService().delete(dataInfoList);
		}

		logger.info("End MQCJob: " + jobName + " delete from CT_MQCPLANDETAIL_EXTENDED Sucess");
	}

	public void deleteMQCPlanDetail_ExtendedByProdList(String jobName, List<String> productList) throws CustomException
	{
		List<MQCPlanDetail_Extended> dataInfoList = ExtendedObjectProxy.getMQCPlanDetail_ExtendedService().getMQCPlanDetail_ExtendedByProdList(jobName, productList);

		if (dataInfoList != null)
		{
			ExtendedObjectProxy.getMQCPlanDetail_ExtendedService().delete(dataInfoList);
		}
	}

	public void deleteMQCPlanDetail_ExtendedByJobName(String jobName) throws CustomException, NotFoundSignal
	{
		logger.info(" Start delete from CT_MQCPLANDETAIL_EXTENDED");

		List<MQCPlanDetail_Extended> dataInfoList = ExtendedObjectProxy.getMQCPlanDetail_ExtendedService().getMQCPlanDetail_ExtendedByJobName(jobName);

		if (dataInfoList != null)
		{
			ExtendedObjectProxy.getMQCPlanDetail_ExtendedService().delete(dataInfoList);
		}

		logger.info("End MQCJob: " + jobName + " delete from CT_MQCPLANDETAIL_EXTENDED Sucess");
	}

	public void updateMQCPlanDetail_ExtendedPosition(String jobName, String lotName, String productName, String position) throws CustomException
	{
		List<MQCPlanDetail_Extended> dataInfoList = ExtendedObjectProxy.getMQCPlanDetail_ExtendedService().getMQCPlanDetail_ExtendedByProductName(jobName, lotName, productName);

		if (dataInfoList != null)
		{
			for (MQCPlanDetail_Extended dataInfo : dataInfoList)
			{
				dataInfo.setPosition(position);

				ExtendedObjectProxy.getMQCPlanDetail_ExtendedService().update(dataInfo);
			}
		}
	}

	public void increateDummyUsedCount(MQCPlanDetail_Extended dataInfo) throws greenFrameDBErrorSignal, CustomException
	{
		dataInfo.setDummyUsedCount(dataInfo.getDummyUsedCount().intValue() + 1);
		ExtendedObjectProxy.getMQCPlanDetail_ExtendedService().update(dataInfo);
	}
}
