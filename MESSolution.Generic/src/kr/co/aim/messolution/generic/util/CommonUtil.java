
package kr.co.aim.messolution.generic.util;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ProductRequestPlan;
import kr.co.aim.messolution.extended.webinterface.ExtendedWebInterfaceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.exception.ErrorSignal;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.exception.greenFrameErrorSignal;
import kr.co.aim.greenframe.orm.ObjectAttributeDef;
import kr.co.aim.greenframe.orm.ObjectAttributeMap;
import kr.co.aim.greenframe.orm.SQLLogUtil;
import kr.co.aim.greenframe.orm.info.DataInfo;
import kr.co.aim.greenframe.orm.info.KeyInfo;
import kr.co.aim.greenframe.orm.support.OrmStandardEngineUtil;
import kr.co.aim.greenframe.util.file.CollectionUtil;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greenframe.util.xml.JdomUtils;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.data.ConsumableKey;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpec;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpecKey;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.durable.management.data.DurableSpec;
import kr.co.aim.greentrack.durable.management.data.DurableSpecKey;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineKey;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.machine.management.data.MachineSpecKey;
import kr.co.aim.greentrack.name.NameServiceProxy;
import kr.co.aim.greentrack.name.management.data.NameGeneratorRuleAttrDef;
import kr.co.aim.greentrack.name.management.data.NameGeneratorRuleDef;
import kr.co.aim.greentrack.name.management.data.NameGeneratorRuleDefKey;
import kr.co.aim.greentrack.name.management.util.GenerateUtil;
import kr.co.aim.greentrack.port.PortServiceProxy;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.data.PortKey;
import kr.co.aim.greentrack.port.management.data.PortSpec;
import kr.co.aim.greentrack.port.management.data.PortSpecKey;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Arc;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.greentrack.processoperationspec.ProcessOperationSpecServiceProxy;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpecKey;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.data.ProductSpecKey;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGSRC;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.dao.DataAccessException;
import org.springframework.util.CollectionUtils;


public class CommonUtil implements ApplicationContextAware
{
	private static Log log = LogFactory.getLog(CommonUtil.class);
	private ApplicationContext	applicationContext;


	@Override
	public void setApplicationContext( ApplicationContext arg0 ) throws BeansException
	{
		// TODO Auto-generated method stub
		applicationContext = arg0;
	}
	
	public static Map<String,ConsumableSpec> generateConsumableSpecDataMap(final List<String> crateNameList) throws CustomException
	{
        List<Map<String,Object>> resultList = null;
        Map<String,ConsumableSpec> consumableSpecDataMap = new HashMap<>();
        
        String sql = " SELECT DISTINCT C.CONSUMABLENAME,S.* FROM CONSUMABLE C , CONSUMABLESPEC S "
        		   + " WHERE 1=1 "
        		   + " AND C.CONSUMABLENAME IN (:CONSUMABLENAMELIST) "
        		   + " AND C.FACTORYNAME = S.FACTORYNAME "
        		   + " AND C.CONSUMABLESPECNAME = S.CONSUMABLESPECNAME "
        		   + " AND C.CONSUMABLESPECVERSION = S.CONSUMABLESPECVERSION ";
        
        resultList = greenFrameServiceProxy.getSqlTemplate().queryForList(sql, new HashMap<String,Object>(){{this.put("CONSUMABLENAMELIST", crateNameList);}});

		if (resultList == null || resultList.size() == 0) return consumableSpecDataMap;

		for (Map<String, Object> resultData : resultList)
		{
			String crateName = ConvertUtil.getMapValueByName(resultData, "CONSUMABLENAME");
			resultData.remove("CONSUMABLENAME");

			List<Map<String, Object>> tempMapList = new ArrayList<>();
			tempMapList.add(resultData);

			ConsumableSpec specData = ConsumableServiceProxy.getConsumableSpecService().transform(tempMapList).get(0);
			consumableSpecDataMap.put(crateName, specData);
		}

		return consumableSpecDataMap;
	}
	
	public static void SendMail(String alarmType, String subJectName, String message) throws CustomException
	{
		String[] mailList = getEmailList(alarmType);
		
		if (mailList != null && mailList.length > 0)
		{
			sendEmail(mailList, subJectName, message);
		}
		else
		{
			log.info("Recipients information is empty.");
		}
	}
	
	public static void sendEmail(String[] mailList, String subJectName,String message) throws CustomException
	{
		log.info(String.format("sendEmail: Start mail send. [Line = %s]", Thread.currentThread().getStackTrace()[1].getLineNumber()));

		if (mailList != null && mailList.length > 0)
		{
			try
			{
				GenericServiceProxy.getMailSerivce().postMail(mailList, subJectName, message);
			}
			catch (Exception ex)
			{
				if (ex instanceof CustomException)
				{
					log.info(((CustomException) ex).errorDef.getEng_errorMessage());
					sendSMSWhenPostMailFail(message);
				}
				else
				{
					throw new CustomException(ex.getCause());
				}
			}
		}
		else
		{
			log.info("Recipients information is empty.");
			return;
		}

		log.info("sendEmail: End of mail send.");
	}
	
	@SuppressWarnings("unchecked")
	public static int[] executeBatch(String queryType, List dataObjectList, boolean newVersion) throws Exception
	{
		if (!newVersion)
		{
			log.info("Execute the old version of the method(Fucntion Name:executeBatch , Arguments: String queryType, List<?> dataObjectList )");
			return executeBatch(queryType, dataObjectList);
		}
		if (CollectionUtils.isEmpty(dataObjectList))
		{
			log.warn("There is no Object to batch update.");
			return null;
		}
		String sql = isAbleToUseSameQuery(queryType, dataObjectList);
		int resultSizes[] = new int[dataObjectList.size()];
		if (StringUtils.isEmpty(sql))
		{
			for (int i = 0; i < dataObjectList.size(); i++)
				resultSizes[i] = doExecute(queryType, (DataInfo) dataObjectList.get(i));

		}
		else if (queryType.equalsIgnoreCase("delete"))
			resultSizes = greenFrameServiceProxy.getSqlTemplate().updateBatch(sql, getBatchDeleteBindObjects(dataObjectList));
		else if (queryType.equalsIgnoreCase("update"))
			resultSizes = greenFrameServiceProxy.getSqlTemplate().updateBatch(sql, getBatchUpdateBindObjects(dataObjectList));
		else if (queryType.equalsIgnoreCase("insert"))
			resultSizes = greenFrameServiceProxy.getSqlTemplate().updateBatch(sql, getBatchInsertBindObjects(dataObjectList));
		else
			throw new greenFrameDBErrorSignal("InvalidQueryType", queryType);
		return resultSizes;
	}

	private static String isAbleToUseSameQuery(String queryType, List dataObjectList)
	{
		String oldSql = "";
		for (Iterator iterator = dataObjectList.iterator(); iterator.hasNext();)
		{
			Object data = iterator.next();
			String sql = "";
			sql = OrmStandardEngineUtil.generateSqlStatement(queryType, data);
			if (StringUtils.isEmpty(oldSql))
				oldSql = sql;
			else if (!oldSql.equals(sql))
				return "";
		}

		return oldSql;
	}

	private static int doExecute(String queryType, DataInfo dataObject)
	{
		String sql = OrmStandardEngineUtil.generateSqlStatement(queryType, dataObject);
		int Result = 0;
		if (queryType.equalsIgnoreCase("delete"))
			Result = update(sql, OrmStandardEngineUtil.getSelectOrDeleteBindObjects(dataObject), ObjectUtil.getString(OrmStandardEngineUtil.getKeyInfo(dataObject)));
		else if (queryType.equalsIgnoreCase("update"))
			Result = update(sql, OrmStandardEngineUtil.getUpdateBindObjects(dataObject), ObjectUtil.getString(OrmStandardEngineUtil.getKeyInfo(dataObject)));
		else if (queryType.equalsIgnoreCase("insert"))
			Result = update(sql, OrmStandardEngineUtil.getInsertBindObjects(dataObject), ObjectUtil.getString(OrmStandardEngineUtil.getKeyInfo(dataObject)));
		else
			throw new greenFrameDBErrorSignal("InvalidQueryType", queryType);
		if (Result == 0)
		{
			if (queryType.equalsIgnoreCase("delete") || queryType.equalsIgnoreCase("update"))
				throw new greenFrameDBErrorSignal("NotFoundSignal", ObjectUtil.getString(OrmStandardEngineUtil.getKeyInfo(dataObject)),
						SQLLogUtil.getLogFormatSqlStatement(sql, ((Object) (OrmStandardEngineUtil.getSelectOrDeleteBindObjects(dataObject))), log));
			else
				throw new greenFrameDBErrorSignal("InvalidQueryState", ObjectUtil.getString(OrmStandardEngineUtil.getKeyInfo(dataObject)),
						SQLLogUtil.getLogFormatSqlStatement(sql, ((Object) (OrmStandardEngineUtil.getSelectOrDeleteBindObjects(dataObject))), log));
		}
		else
		{
			return Result;
		}
	}

	private static int update(String sql, Object args[], String keyString) throws greenFrameDBErrorSignal
	{
		String logFormatSql = SQLLogUtil.getLogFormatSqlStatement(sql, ((Object) (args)), log);
		int result = 0;
		try
		{
			SQLLogUtil.logBeforeExecutingQuery(logFormatSql, log);
			if (args == null)
				result = greenFrameServiceProxy.getSqlTemplate().getJdbcTemplate().update(sql);
			else
				result = greenFrameServiceProxy.getSqlTemplate().getJdbcTemplate().update(sql, args);
		}
		catch (DataAccessException e)
		{
			if (keyString != null)
				throw ErrorSignal.getNotifyException(e, keyString, logFormatSql);
			else
				throw ErrorSignal.getNotifyException(e, ObjectUtil.getString(args), logFormatSql);
		}
		SQLLogUtil.logAfterExecutingUpdate(result, log);
		return result;
	}

	public static List getBatchDeleteBindObjects(List keyInfoList)
	{
		List bindSetList = new ArrayList();
		Object obj;
		for (Iterator iterator = keyInfoList.iterator(); iterator.hasNext(); bindSetList.add(((Object) (getSelectOrDeleteBindObjects(obj)))))
			obj = iterator.next();

		return bindSetList;
	}

	public static List getBatchUpdateBindObjects(List dataInfoList)
	{
		List bindSetList = new ArrayList();
		Object dataInfo;
		for (Iterator iterator = dataInfoList.iterator(); iterator.hasNext(); bindSetList.add(((Object) (getUpdateBindObjects(dataInfo)))))
			dataInfo = iterator.next();

		return bindSetList;
	}

	public static List getBatchInsertBindObjects(List dataInfoList)
	{
		List bindSetList = new ArrayList();
		Object dataInfo;
		for (Iterator iterator = dataInfoList.iterator(); iterator.hasNext(); bindSetList.add(((Object) (getInsertBindObjects(dataInfo)))))
			dataInfo = iterator.next();

		return bindSetList;
	}
	 
	public static void sendSMSWhenPostMailFail(String message) throws CustomException
	{
		String AlarmCode="Failedmail";
	    String AlarmType="MES";
		String smsMessage = alarmSmsText(AlarmCode,AlarmType,message);
		//GenericServiceProxy.getSMSInterface().AlarmSmsSend(AlarmCode, AlarmType, smsMessage);
	}
	
	public static String[] getEmailList(String alarmType) throws CustomException
	{
		String sql = " SELECT DISTINCT  UP.EMAIL "
				   + " FROM CT_ALARMGROUP AG , CT_ALARMUSERGROUP AU  , USERPROFILE UP"
				   + " WHERE AG.ALARMTYPE = :ALARMTYPE"
				   + " AND AG.ALARMGROUPNAME = AU.ALARMGROUPNAME "
				   + " AND AU.USERID = UP.USERID "
				   + " AND UP.EMAIL IS NOT NULL ";
		
		List<Map<String, Object>> resultList = null;
		try
		{
			resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, new String[] { alarmType });
		}
		catch (Exception ex)
		{
			throw new CustomException(ex.getCause());
		}

		if(resultList ==null || resultList.size()==0) return null;
		
		return makeListBySqlResult(resultList, "EMAIL").toArray(new String[] {});
	}
	
	public static String[]  getEmailList(String alarmCode , String alarmType, String machineName,String unitName) throws CustomException
	{
		if (log.isInfoEnabled())
			log.info(String.format("getEmailList: AlarmCode = %s , AlarmType = %s , MachineName = %s , UnitNaem = %s ", alarmCode, alarmType, machineName, unitName));

		if (StringUtil.in(StringUtil.EMPTY, alarmCode, alarmType, machineName))
			return null;
		
		if (unitName ==null || unitName.isEmpty()) unitName = "-";

		Object[] bindSet = new Object[] { alarmCode, alarmType, machineName,unitName ,GenericServiceProxy.getConstantMap().AlarmAction_Mail };

		try
		{
			ExtendedObjectProxy.getAlarmActionDefService().select("WHERE ALARMCODE = ? AND ALARMTYPE = ? AND MACHINENAME = ? AND ( UNITNAME IS NULL OR UNITNAME = ? ) AND ACTIONNAME = ? ", bindSet);
		}
		catch (Exception ex)
		{
			if (ex instanceof greenFrameDBErrorSignal && ((greenFrameDBErrorSignal) ex).getErrorCode().equals(ErrorSignal.NotFoundSignal))
				return null;
			else
				throw new CustomException(ex.getCause());
		}

		String sql = " SELECT DISTINCT  UP.EMAIL "
				   + " FROM CT_ALARMGROUP AG , CT_ALARMUSERGROUP AU  , USERPROFILE UP"
				   + " WHERE AG.ALARMTYPE = :ALARMTYPE"
				   + " AND AG.ALARMGROUPNAME = AU.ALARMGROUPNAME "
				   + " AND AU.USERID = UP.USERID "
				   + " AND UP.EMAIL IS NOT NULL ";
		
		List<Map<String, Object>> resultList = null;
		try
		{
			resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, new String[] { alarmType });
		}
		catch (Exception ex)
		{
			throw new CustomException(ex.getCause());
		}

		if(resultList ==null || resultList.size()==0) return null;
		
		return makeListBySqlResult(resultList, "EMAIL").toArray(new String[] {});
	}
	
	public static List<String> getEmailList(String alarmCode , String alarmType, String machineName) throws CustomException
	{
		if (log.isInfoEnabled())
			log.info(String.format("getEmailList: AlarmCode = %s , AlarmType = %s , MachineName = %s ", alarmCode, alarmType, machineName));

		if (StringUtil.in(StringUtil.EMPTY, alarmCode, alarmType, machineName))
			return null;

		Object[] bindSet = new Object[] { alarmCode, alarmType, machineName, GenericServiceProxy.getConstantMap().AlarmAction_Mail };

		try
		{
			ExtendedObjectProxy.getAlarmActionDefService().select("WHERE ALARMCODE = ? AND ALARMTYPE = ? AND MACHINENAME = ? AND ACTIONNAME = ? ", bindSet);
		}
		catch (Exception ex)
		{
			if (ex instanceof greenFrameDBErrorSignal && ((greenFrameDBErrorSignal) ex).getErrorCode().equals(ErrorSignal.NotFoundSignal))
				return null;
			else
				throw new CustomException(ex.getCause());
		}

		String sql = " SELECT DISTINCT  UP.EMAIL "
				   + " FROM CT_ALARMGROUP AG , CT_ALARMUSERGROUP AU  , USERPROFILE UP"
				   + " WHERE AG.ALARMTYPE = :ALARMTYPE"
				   + " AND AG.ALARMGROUPNAME = AU.ALARMGROUPNAME "
				   + " AND AU.USERID = UP.USERID "
				   + " AND UP.EMAIL IS NOT NULL ";
		
		List<Map<String, Object>> resultList = null;
		try
		{
			resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, new String[] { alarmType });
		}
		catch (Exception ex)
		{
			throw new CustomException(ex.getCause());
		}

		if(resultList ==null || resultList.size()==0) return null;
		
		return makeListBySqlResult(resultList, "EMAIL");
	}
	
	public static String[] getEmailListByAlarmGroup(String alarmGroupName) throws CustomException
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT DISTINCT UP.EMAIL ");
		sql.append("  FROM CT_ALARMGROUP AG, CT_ALARMUSERGROUP AU, USERPROFILE UP ");
		sql.append(" WHERE AG.ALARMGROUPNAME = AU.ALARMGROUPNAME ");
		sql.append("   AND AU.USERID = UP.USERID ");
		sql.append("   AND UP.EMAIL IS NOT NULL ");
		sql.append("   AND AG.ALARMGROUPNAME = :ALARMGROUPNAME ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("ALARMGROUPNAME", alarmGroupName);
		
		List<Map<String, Object>> resultList = null;
		try
		{
			resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		}
		catch (NotFoundSignal notFound)
		{
			return null;
		}
		catch (Exception ex)
		{
			throw new CustomException(ex.getCause());
		}

		if (resultList == null || resultList.size() == 0)
			return null;

		return makeListBySqlResult(resultList, "EMAIL").toArray(new String[] {});
	}
	
	//houxk add
	public static String[] getEmailListByDepart(String department) throws CustomException
	{
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT DISTINCT M.EMAIL  FROM USERPROFILE M ");
		sql.append("  WHERE M.DEPARTMENT =:DEPARTMENT ");
		sql.append("   AND M.EMAIL IS NOT NULL ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("DEPARTMENT", department);
		
		List<Map<String, Object>> resultList = null;
		try
		{
			resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		}
		catch (NotFoundSignal notFound)
		{
			return null;
		}
		catch (Exception ex)
		{
			throw new CustomException(ex.getCause());
		}

		if (resultList == null || resultList.size() == 0)
			return null;

		return makeListBySqlResult(resultList, "EMAIL").toArray(new String[] {});
	}
	
	public static List<String> makeToStringList (List<?> valueList)
	{
		if(valueList == null || valueList.size() == 0)
		{
			return new ArrayList<String>();
		}
		
		String strTemp =  makeListByKey(valueList);
		if(StringUtils.isEmpty(strTemp)) return new ArrayList<String>();
		
		return Arrays.asList(org.springframework.util.StringUtils.commaDelimitedListToStringArray(strTemp.replaceAll("'", "")));
		
	}
	
	public static String makeListByKey (List<?> valueList)
	{
		
		if(valueList == null || valueList.size() == 0)
		{
			return StringUtils.EMPTY;
		}
		
		int valueCount = 0;
		if(valueList.size() <= 100)
		{
			String sqlStatement = null;
			
			for (Object object : valueList) 
			{
				String value = StringUtils.EMPTY;
				if(object instanceof Lot)
					value = ((Lot)object).getKey().getLotName();
				else if(object instanceof Product)
					value = ((Product)object).getKey().getProductName();
				else if(object instanceof Consumable)
					value = ((Consumable)object).getKey().getConsumableName();
				else if(object instanceof String)
					value = (String)object;
				else if(object instanceof Durable)
					value = ((Durable)object).getKey().getDurableName();
				else if (object instanceof ProductPGSRC)
					value = ((ProductPGSRC) object).getProductName();
				else  return ""; 
					
				if(StringUtils.isEmpty(value))
				{
					continue;
				}
				
				if(valueCount==0) sqlStatement ="'";
				
				sqlStatement += value + "','";
				valueCount++;
			}
			
			if(valueCount == 0)
			{
				return StringUtils.EMPTY;
			}
			
			return sqlStatement.substring(0, sqlStatement.length() - 2) ;
		}
		else
		{
			StringBuilder sqlStatement = new StringBuilder();
			for (Object object : valueList) 
			{
				String value = StringUtils.EMPTY;
				if(object instanceof Lot)
					value = ((Lot)object).getKey().getLotName();
				else if(object instanceof Product)
					value = ((Product)object).getKey().getProductName();
				else if(object instanceof Consumable)
					value = ((Consumable)object).getKey().getConsumableName();
				else if(object instanceof String)
					value = (String)object;
				else if (object instanceof ProductPGSRC)
					value = ((ProductPGSRC) object).getProductName();
				else  return "";
				
				if(StringUtils.isEmpty(value))
				{
					continue;
				}
				if(valueCount==0) sqlStatement.append("'");
				sqlStatement.append(value).append("','");
				valueCount++;
			}
			
			if(valueCount == 0)
			{
				return StringUtils.EMPTY;
			}
			
			return (sqlStatement.delete(sqlStatement.length() - 2 , sqlStatement.length())).toString();
		}
	}
	public static String makeInStringByList(String key, List<?> valueList, boolean isStart)
	{
		if(valueList == null || valueList.size() == 0)
		{
			return StringUtils.EMPTY;
		}
		
		int valueCount = 0;
		if(valueList.size() <= 100)
		{
			String sqlStatement = null;
			if(isStart)
				sqlStatement = " " + key + " in ('";
			else
				sqlStatement = " and " + key + " in ('";
			
			for (Object object : valueList) 
			{
				String value = StringUtils.EMPTY;
				if(object instanceof Lot)
					value = ((Lot)object).getKey().getLotName();
				else if(object instanceof Product)
					value = ((Product)object).getKey().getProductName();
				else if(object instanceof Consumable)
					value = ((Consumable)object).getKey().getConsumableName();
				else if(object instanceof String)
					value = (String)object;
				else if(object instanceof Element)
				{
					if(key.indexOf(".") != -1)
						value = ((Element)object).getChildText(key.split(".")[1]);
					else
						value = ((Element)object).getChildText(key);
				}
				
				if(StringUtils.isEmpty(value))
				{
					continue;
				}
				
				sqlStatement += value + "','";
				valueCount++;
			}
			
			if(valueCount == 0)
			{
				return StringUtils.EMPTY;
			}
			
			return sqlStatement.substring(0, sqlStatement.length() - 2) + ") ";
		}
		else
		{
			StringBuilder sqlStatement = new StringBuilder();
			if(isStart)
				sqlStatement.append(" ").append(key).append( " in ('");
			else
				sqlStatement.append(" and ").append(key).append( " in ('");
			
			for (Object object : valueList) 
			{
				String value = StringUtils.EMPTY;
				if(object instanceof Lot)
					value = ((Lot)object).getKey().getLotName();
				else if(object instanceof Product)
					value = ((Product)object).getKey().getProductName();
				else if(object instanceof Consumable)
					value = ((Consumable)object).getKey().getConsumableName();
				else if(object instanceof String)
					value = (String)object;
				else if(object instanceof Element)
				{
					if(key.indexOf(".") != -1)
						value = ((Element)object).getChildText(key.split(".")[1]);
					else
						value = ((Element)object).getChildText(key);
				}
				
				if(StringUtils.isEmpty(value))
				{
					continue;
				}
				
				sqlStatement.append(value).append("','");
				valueCount++;
			}
			
			if(valueCount == 0)
			{
				return StringUtils.EMPTY;
			}
			
			return (sqlStatement.delete(sqlStatement.length() - 2 , sqlStatement.length())).append(") ").toString();
		}
	}
	
	public static String makeInString(String key, String[] values, boolean isStart)
	{
		if(values == null || values.length == 0)
			return "";
		int valueCount = 0;
		if(values.length <= 100)
		{
			String sqlStatement = null;
			if(isStart)
				sqlStatement = " " + key + " in ('";
			else
				sqlStatement = " and " + key + " in ('";
			for(String value : values)
			{
				if(value == null || value.isEmpty())
					continue;
				sqlStatement += ( value + "','");
				valueCount++;
			}
			if(valueCount == 0)
				return "";
			return sqlStatement.substring(0, sqlStatement.length()-2) + ") ";
		}
		else
		{
			StringBuilder sqlStatement = new StringBuilder();
			if(isStart)
				sqlStatement.append(" ").append(key).append( " in ('");
			else
				sqlStatement.append(" and ").append(key).append( " in ('");
	
			for(String value : values)
			{
				if(value == null || value.isEmpty())
					continue;
				sqlStatement.append(value).append("','");
				valueCount++;
			}
			if(valueCount == 0)
				return "";
			return (sqlStatement.delete(sqlStatement.length() - 2 , sqlStatement.length())).append(") ").toString();
			
		}
	}
	
	public static String getRecipeByProduct( List<Map<String, Object>> reserveRecipeByProduct, String productName, String lotRecipeName ){
		String recipename = "";
		
		for( int i = 0; i < reserveRecipeByProduct.size(); i++ ){
			String inputProductName = reserveRecipeByProduct.get(i).get("PRODUCTNAME").toString();
			if( StringUtils.equals(inputProductName, productName)){
				recipename = reserveRecipeByProduct.get(i).get("RECIPENAME").toString();
				break;
			}
		}
		if(StringUtils.isEmpty(recipename))
			recipename = lotRecipeName;
		
		return recipename;
	}
	
	public static String getValue(Document doc, String itemName)	
	{
		String value = "";
		Element root = doc.getDocument().getRootElement();
		try
		{
			value  = root.getChild("Body").getChild(itemName).getText();
		}
		catch(Exception e1)
		{
			log.info("Cannot Find Item Name: " + itemName);
		}
		return value;
	}
	
	public static String getIp()
	{
		String ip = "";
		try 
		{
			ip = InetAddress.getLocalHost().getHostAddress();
		} catch (Exception e) {}
		return ip;
	}
	
	public static String getEnumDefValueStringByEnumName( String enumName ){
		String enumValue = "";
		String sql = "SELECT ENUMVALUE FROM ENUMDEFVALUE WHERE ENUMNAME = :enumName ";
		
		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("enumName", enumName);
		
		List<Map<String, Object>> sqlResult = 
				GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		
		if(sqlResult.size() > 0){
			enumValue = sqlResult.get(0).get("ENUMVALUE").toString();
		}
		
		return enumValue;
	}

	public static List<Map<String, Object>> getEnumDefValueByEnumName( String enumName ){
		String sql = "SELECT ENUMVALUE FROM ENUMDEFVALUE "
					+ "WHERE ENUMNAME = :enumName ";
		
		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("enumName", enumName);
		
		List<Map<String, Object>> sqlResult = 
				GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		
		return sqlResult;
	}

	public static List<Map<String, Object>> getEnumDefByEnumName( String enumName ){
		String sql = "SELECT ENUMNAME, CONSTANTFLAG FROM ENUMDEF "
					+ "WHERE ENUMNAME = :enumName ";
		
		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("enumName", enumName);
		
		List<Map<String, Object>> sqlResult = 
				GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		
		return sqlResult;
	}
	
	public static List<Product> getProductList (String carrierName) throws CustomException
	{
		String condition = "WHERE lotName in ( select lotname from lot where carrierName = :carrierName ) "
				 + "AND PRODUCTSTATE = :productState " + "ORDER BY POSITION ";

		Object[] bindSet = new Object[] { carrierName, GenericServiceProxy.getConstantMap().Prod_InProduction };
		
		List<Product> productList = ProductServiceProxy.getProductService().select(condition, bindSet);
		
		return productList;
	}
	
	public static Machine getMachineInfo( String machineName ) throws CustomException{
		MachineKey machineKey = new MachineKey();
		machineKey.setMachineName(machineName);
		
		Machine machineData = null;
		
		try {
			machineData = MachineServiceProxy.getMachineService().selectByKey(machineKey);
		}catch (Exception e){
			throw new CustomException("MACHINE-9000", machineName);
		}
		
		return machineData; 
	}
	
	public static Lot getLotInfoByLotName ( String lotName ) throws CustomException{
		try {
			LotKey lotKey = new LotKey();
			lotKey.setLotName(lotName);
			Lot lotData = LotServiceProxy.getLotService().selectByKey(lotKey);
			 
			return lotData;
			
		} catch (Exception e) {
			throw new CustomException("LOT-9000", lotName);	
		}
	}

	public static Port getPortInfo(String machineName, String portName) throws CustomException
	{
		try
		{
			PortKey portKey = new PortKey();
			portKey.setMachineName(machineName);
			portKey.setPortName(portName);

			Port portData = null;

			portData = PortServiceProxy.getPortService().selectByKey(portKey);

			return portData;
		}
		catch (Exception e)
		{
			throw new CustomException("PORT-9000", machineName, portName);
		}
	}

	public static MachineSpec getMachineSpecByMachineName(String machineName){
	MachineSpecKey machineSpecKey = new MachineSpecKey();
	machineSpecKey.setMachineName(machineName);

	MachineSpec machineSpecData = MachineServiceProxy.getMachineSpecService()
			.selectByKey(machineSpecKey);
	
	return machineSpecData;
	}
	
	public static Lot getLotInfoBydurableName(String carrierName)
		throws CustomException
	{ 
		String condition = "WHERE carrierName = ? AND lotState = ?";
						
		Object[] bindSet = new Object[] {carrierName, GenericServiceProxy.getConstantMap().Lot_Released};
		
		List<Lot> lotList;
		
		try
		{
			lotList = LotServiceProxy.getLotService().select(condition, bindSet);
		}
		catch(NotFoundSignal ne)
		{
			lotList = new ArrayList<Lot>();
			
			return null;
		}
		catch (Exception ex)
		{
			throw new CustomException("", "");
		}

		//one Lot to a carrier
		return lotList.get(0);
	}
	
	public static ProductSpec getProductSpecByLotName ( String lotName){
		LotKey lotKey = new LotKey();
		lotKey.setLotName(lotName);
		
		Lot lotData = null;
		
		lotData = LotServiceProxy.getLotService().selectByKey(lotKey);
		
		ProductSpecKey productSpecKey = new ProductSpecKey();
		productSpecKey.setFactoryName(lotData.getFactoryName());
		productSpecKey.setProductSpecName(lotData.getProductSpecName());
		productSpecKey.setProductSpecVersion(lotData.getProductSpecVersion());
		
		ProductSpec productSpecData = null;
		productSpecData = ProductServiceProxy.getProductSpecService().selectByKey(productSpecKey);
		
		return productSpecData;
	}
	
	public static ConsumableSpec getConsumableSpec ( String crateName) throws CustomException{
		
		ConsumableKey consumableKey = new ConsumableKey();
		consumableKey.setConsumableName(crateName);
		
		Consumable consumableData = ConsumableServiceProxy.getConsumableService().selectByKey(consumableKey);
		
		ConsumableSpecKey consumableSpecKey = new ConsumableSpecKey();
		consumableSpecKey.setFactoryName(consumableData.getFactoryName());
		consumableSpecKey.setConsumableSpecName(consumableData.getConsumableSpecName());
		consumableSpecKey.setConsumableSpecVersion(consumableData.getConsumableSpecVersion());
		
		ConsumableSpec consumableSpecData = null;
		consumableSpecData = ConsumableServiceProxy.getConsumableSpecService().selectByKey(consumableSpecKey);
		 
		return consumableSpecData; 
	}
	
	public static ProcessOperationSpec getProcessOperationSpec( String factoryName, String processOperationName, String processOperationVersion) throws CustomException{

		ProcessOperationSpec processOperationData = new ProcessOperationSpec();
		try{
			ProcessOperationSpecKey processOperationKey = new ProcessOperationSpecKey();
			
			processOperationKey.setFactoryName(factoryName);
			processOperationKey.setProcessOperationName(processOperationName);
			processOperationKey.setProcessOperationVersion(processOperationVersion);
			
			processOperationData 
				= ProcessOperationSpecServiceProxy.getProcessOperationSpecService().selectByKey(processOperationKey);
			
		} catch( Exception e ){
			throw new CustomException("PROCESSOPERATION-9001", processOperationName);
		}
		
		return processOperationData;
	}
	
	public static DurableSpec getDurableSpecByDurableName( String durableName ){
		DurableKey durableKey = new DurableKey();
		durableKey.setDurableName(durableName);

		Durable durableData = null;
		
		durableData = DurableServiceProxy.getDurableService().selectByKey(durableKey);
		
		DurableSpecKey durableSpecKey = new DurableSpecKey();
		durableSpecKey.setFactoryName(durableData.getFactoryName());
		durableSpecKey.setDurableSpecName(durableData.getDurableSpecName());
		durableSpecKey.setDurableSpecVersion(durableData.getDurableSpecVersion());
		
		DurableSpec durableSpecData = null;
		durableSpecData = DurableServiceProxy.getDurableSpecService().selectByKey(durableSpecKey);
		
		return durableSpecData;
	}

	public static PortSpec getPortSpecInfo(String machineName, String portName) throws CustomException
	{
		try
		{
			PortSpecKey portSpecKey = new PortSpecKey();
			portSpecKey.setMachineName(machineName);
			portSpecKey.setPortName(portName);

			PortSpec portSpecData = null;

			portSpecData = PortServiceProxy.getPortSpecService().selectByKey(portSpecKey);

			return portSpecData;
		}
		catch (Exception e)
		{
			throw new CustomException("PORT-9000", machineName, portName);
		}
	}

	public static String getAreaName(String factoryName, String processOperationName, String processOperationVersion)
			throws FrameworkErrorSignal, NotFoundSignal
	{
		ProcessOperationSpecKey key = new ProcessOperationSpecKey();
		key.setFactoryName(factoryName);
		key.setProcessOperationName(processOperationName);
		key.setProcessOperationVersion(processOperationVersion);

		ProcessOperationSpec spec = ProcessOperationSpecServiceProxy.getProcessOperationSpecService().selectByKey(key);
		return spec.getDefaultAreaName();
	}
	
	public static boolean hasText(String targetText, String searchText)
	{
		boolean hasText = false;
		if (targetText.length() > 0) {
			if (targetText.indexOf(searchText) > 0)
				hasText = true;
		}
		return hasText;
	}
	
	public static String getPrefix(String targetText, String delimeter)
	{
		if (targetText.length() > 0)
			targetText = targetText.substring(0, targetText.indexOf(delimeter));
		return targetText;
	}
	
	public static int getIsSameCharactorCount( String context, String compareCharator, int compareCLength ){
		int iSameCount = 0;
		try{
			for( int i = 0; i < context.length(); i++ ){
				String tempContext = context.substring(i, compareCLength + i);
				
				if( StringUtils.equals(tempContext, compareCharator)){
					iSameCount++;
				}
			}
		}
		catch( Exception e ){
			
		}
		return iSameCount;
	}
	
	public static List<String> makeListForQueryAndCount(Element element, String listName, String Name) throws CustomException
	{
		int i = 0;
		String list= "'";
		List<String> argSeq = new ArrayList<String>();
		
		Element ListElement = element.getChild(listName);
		
		if( ListElement.getChildren().size() > 0 ){
			for( Iterator iterator = ListElement.getChildren().iterator(); iterator.hasNext(); ){
				Element product = (Element) iterator.next();
				String productName = product.getChild(Name).getText();
				list =  list + productName + "', '";
				i = i + 1;
			}
		}
		list = list + "'";
		try{
			if( list.length() > 4){
				list = list.substring(0, list.length() - 4);
			}
			argSeq.add(list);
			argSeq.add(Integer.toString(i));
		}
		catch(Exception e){
			//throw new Exception();
		}
		return argSeq;
	}

	public static List<String> makeList(Element element, String listName, String name)
    {
		// Caution.
		// This list size should not exceed 1000 when it uses for SQL BindSet without SQL Exception.
		String childText = "";
		List<String> list = new ArrayList<String>();
		Element childElement = null;
		Element listElement = element.getChild(listName);

		if (listElement != null)
		{
			for (Iterator<?> iterator = listElement.getChildren().iterator(); iterator.hasNext();)
			{
				childElement = (Element) iterator.next();
				childText = childElement.getChildText(name);
				list.add(childText);
			}
		}
		else
		{
			listElement = element;

			if (listElement != null)
			{
				for (Iterator<?> iterator = listElement.getChildren().iterator(); iterator.hasNext();)
				{
					childElement = (Element) iterator.next();
					childText = childElement.getChildText(name);
					list.add(childText);
				}
			}
		}
		log.debug(list);

		return list;
	}

	public static List<String> makeListExceptNull(Element element, String listName, String name)
	{
		// Caution.
		// This list size should not exceed 1000 when it uses for SQL BindSet without SQL Exception.
		String childText = "";
		List<String> list = new ArrayList<String>();
		Element childElement = null;
		Element listElement = element.getChild(listName);

		if (listElement != null)
		{
			for (Iterator<?> iterator = listElement.getChildren().iterator(); iterator.hasNext();)
			{
				childElement = (Element) iterator.next();
				childText = childElement.getChildText(name);
				if (childText != null)
				{
					list.add(childText);
				}
			}
		}
		else
		{
			listElement = element;

			if (listElement != null)
			{
				for (Iterator<?> iterator = listElement.getChildren().iterator(); iterator.hasNext();)
				{
					childElement = (Element) iterator.next();
					childText = childElement.getChildText(name);
					if (childText != null)
					{
						list.add(childText);
					}
				}
			}
		}
		log.debug(list);

		return list;
	}

	public static int strConvent(String id)
	{
		int num = 0;
		for(int i = 65; i < 91; i++)
		{
			if(id.charAt(0) >= 86)
			{
				if(id.charAt(0) == i)
				{
					num = i - 57;
					break;
				}
			}
			else
			{
				if(id.charAt(0) == i)
				{
					num = i - 55;
					break;
				}
			}
		}
		
		return num;
	}
	
	public static List<String> getNameByNamingRule(String ruleName, Map<String, Object> nameRuleAttrMap, int quantity,
														String sendSubjectName)
		throws CustomException
	{
		List<String> lstResult = new ArrayList<String>();
		
		try
		{
			//set param for query
			HashMap<String, Object> paraMap = new HashMap<String, Object>();
			paraMap.put("RULENAME", ruleName);
			paraMap.put("QUANTITY", String.valueOf(quantity));
			
			Document doc = SMessageUtil.generateQueryMessage("GetNameList",
																paraMap,
																(HashMap<String, Object>) nameRuleAttrMap,
																"", "MES", "");
			doc = GenericServiceProxy.getESBServive().sendRequestBySender(sendSubjectName, doc, "QRYSender"); 
			if (doc != null)
			{
				//parsing result
				Element resultList = JdomUtils.getNode(doc, "//" + SMessageUtil.Message_Tag + "/" + SMessageUtil.Body_Tag + "/" + "DATALIST");
				for (Iterator iterator = resultList.getChildren().iterator(); iterator.hasNext();) {
					Element resultData = (Element) iterator.next();
					
					String name = resultData.getChildText("NAMEVALUE");
					
					lstResult.add(name);
				}
			}
		}
		catch (Exception ex)
		{
			throw new CustomException("SYS-0000", ex.getMessage());
		}
		
		return lstResult;
	}

	public static List<String> getNameByPPBoxNamingRule(String ruleName, Map<String, Object> argSeqp, int quantity,
														String sendSubjectName)
		throws CustomException
	{
		List<String> lstResult = new ArrayList<String>();
		
		try
		{
			//set param for query
			HashMap<String, Object> paraMap = new HashMap<String, Object>();
			paraMap.put("RULENAME", ruleName);
			paraMap.put("QUANTITY", String.valueOf(quantity));
			
			Document doc = SMessageUtil.generateQueryMessage("GetNameList",
																paraMap,
																(HashMap<String, Object>) argSeqp,
																"", "MES", "");
			
			doc = GenericServiceProxy.getESBServive().sendRequestBySender(sendSubjectName, doc, "QRYSender"); 
			
			if (doc != null)
			{
				//parsing result
				Element resultList = JdomUtils.getNode(doc, "//" + SMessageUtil.Message_Tag + "/" + SMessageUtil.Body_Tag + "/" + "DATALIST");
				
				for (Iterator iterator = resultList.getChildren().iterator(); iterator.hasNext();) {
					Element resultData = (Element) iterator.next();
					
					String name = resultData.getChildText("NAMEVALUE");
					
					lstResult.add(name);
				}
			}
		}
		catch (Exception ex)
		{
			throw new CustomException("SYS-0000", ex.getMessage());
		}
		
		return lstResult;
	}
	
	public static List<String> generateNameByNamingRule(String ruleName, Map<String, Object> nameRuleAttrMap, int quantity)
	{
		if(log.isInfoEnabled()){
			log.debug("ruleName = " + ruleName);
			log.debug("demand = " + quantity);
		}
		
		//get single Lot ID
		//${location}.${factory}.${cim}.${mode}.${svr}
		StringBuffer nameServerSubjectName = new StringBuffer(GenericServiceProxy.getESBServive().makeCustomServerLocalSubject("QRYsvr"));
		List<String> lstLotName;
		try {
			lstLotName = CommonUtil.getNameByNamingRule(ruleName, nameRuleAttrMap, quantity, nameServerSubjectName.toString());
			log.info("lotName = " + lstLotName);
		} catch (CustomException e) {
			lstLotName = new ArrayList<String>();
		}
		
		return lstLotName;
	}

	public static List<String> generateNameByPPBoxNamingRule(String ruleName, Map<String, Object> argSeq, int quantity)
	{
		if(log.isInfoEnabled()){
			log.debug("ruleName = " + ruleName);
			log.debug("demand = " + quantity);
		}
		
		//get single Lot ID
		//${location}.${factory}.${cim}.${mode}.${svr}
		StringBuffer nameServerSubjectName = new StringBuffer(GenericServiceProxy.getESBServive().makeCustomServerLocalSubject("QRYsvr"));
			
		List<String> lstLotName;
		
		try {
			lstLotName = CommonUtil.getNameByPPBoxNamingRule(ruleName,argSeq, quantity, nameServerSubjectName.toString());
		} catch (CustomException e) {
			lstLotName = new ArrayList<String>();
		}
		
		return lstLotName;
	}
	
	public static List<String> generateNameByNamingRule(String ruleName, Map<String, Object> nameRuleAttrMap, long quantity) throws CustomException
	{
		List<String> argSeq = new ArrayList<String>();

		try
		{
			NameGeneratorRuleDef ruleDef;

			try
			{
				ruleDef = NameServiceProxy.getNameGeneratorRuleDefService().selectByKey(new NameGeneratorRuleDefKey(ruleName));
			}
			catch (NotFoundSignal ne)
			{
				throw new CustomException("SYS-9001", NameGeneratorRuleDef.class.getSimpleName());
			}

			String namingRuleSql = CommonUtil.getValue(ruleDef.getUdfs(), "SQL");

			List<NameGeneratorRuleAttrDef> nameGeneratorRuleAttrDefList;

			try
			{
				nameGeneratorRuleAttrDefList = NameServiceProxy.getNameGeneratorRuleAttrDefService().getAllRuleAttrDefs(ruleName);
			}
			catch (NotFoundSignal ne)
			{
				throw new CustomException("SYS-9001", NameGeneratorRuleAttrDef.class.getSimpleName());
			}
			
			long sectionLength = 2;
			StringBuilder prefixBuilder = new StringBuilder();
			StringBuilder serialNoBuilder = new StringBuilder();
			
			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(namingRuleSql, nameRuleAttrMap);

			for (Map<String, Object> resultMap : sqlResult)
			{
				argSeq.add(ConvertUtil.getMapValueByName(resultMap, "SECTIONVALUE"));
				prefixBuilder.append(ConvertUtil.getMapValueByName(resultMap, "SECTIONVALUE"));
			}
			
			if (StringUtils.equals(ruleName, "ProductionLotNaming"))
			{
				String exceptionalCharacter = nameGeneratorRuleAttrDefList.get(5).getExceptionalCharacter();

				sectionLength = 3;

				List<Map<String, Object>> sqlForResult = null;
				String sqlForQ = " SELECT * FROM NAMEGENERATORSERIAL WHERE RULENAME = :RULENAME AND PREFIX = :PREFIX  ";
				Map<String, Object> bindMapQ = new HashMap<String, Object>();
				bindMapQ.put("RULENAME", ruleName);
				bindMapQ.put("PREFIX", prefixBuilder.toString());
				sqlForResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlForQ, bindMapQ);

				log.info("RULENAME:" + bindMapQ.get("RULENAME") + " PREFIX:" + bindMapQ.get("PREFIX"));

				if (sqlForResult.size() < 1)
				{
					serialNoBuilder.append("0");
					if (sectionLength > 1)
					{
						for (int i = 0; i < sectionLength - 1; i++)
						{
							serialNoBuilder.append("0");
						}
					}

					bindMapQ.put("LASTSERIALNO", serialNoBuilder.toString());
					String sqlForU = "INSERT INTO NAMEGENERATORSERIAL VALUES(:RULENAME , :PREFIX , :LASTSERIALNO) ";
					GenericServiceProxy.getSqlMesTemplate().update(sqlForU, bindMapQ);

					sqlForResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlForQ, bindMapQ);
				}

				if (sqlForResult.size() > 0)
				{
					// MaxSerial Validation
					if (StringUtils.equals(ConvertUtil.getMapValueByName(sqlForResult.get(0), "LASTSERIALNO"), "9ZZ"))
					{
						throw new CustomException("NAMING-0001", NameGeneratorRuleAttrDef.class.getSimpleName());
					}
					
					String firstSerial = ConvertUtil.getMapValueByName(sqlForResult.get(0), "LASTSERIALNO").substring(0, 1);
					int iFirstSerial = Integer.parseInt(firstSerial);

					String exSerial = ConvertUtil.getMapValueByName(sqlForResult.get(0), "LASTSERIALNO").substring(1, 3);

					if (StringUtils.equals(exSerial, "ZZ"))
					{
						iFirstSerial += 1;
						
						firstSerial = Integer.toString(iFirstSerial);
						exSerial = "00";
					}
					else
					{
						char cSerial[] = exSerial.toCharArray();
						
						for (int i = exSerial.length() - 1; i >= 0; i--)
						{
							if (cSerial[i] == 'Z')
							{
								cSerial[i] = '0';
							}
							else if (cSerial[i] == '9')
							{
								cSerial[i] = 'A';
								break;
							}
							else
							{
								cSerial[i] += 1;
								cSerial[i] = GenerateUtil.checkSerialException(cSerial[i], exceptionalCharacter);
								break;
							}
						}
						
						exSerial = new String(cSerial);
					}

					exSerial = firstSerial + exSerial;

					argSeq.add(exSerial);
					
					StringBuffer updateSql = new StringBuffer();
					updateSql.append("UPDATE NAMEGENERATORSERIAL ");
					updateSql.append("   SET LASTSERIALNO = :LASTSERIALNO ");
					updateSql.append(" WHERE RULENAME = :RULENAME ");
					updateSql.append("   AND PREFIX = :PREFIX ");

					Map<String, Object> args = new HashMap<String, Object>();
					args.put("LASTSERIALNO", exSerial);
					args.put("RULENAME", ruleName);
					args.put("PREFIX", prefixBuilder.toString());

					GenericServiceProxy.getSqlMesTemplate().update(updateSql.toString(), args);

				}
			}
			
			if (StringUtils.equals(ruleName, "LotNaming"))
			{
//				if ("F3MA14E".equals(prefixBuilder.toString()))
//				{
//					List<String> names = fixPrefix_F3MA14E();
//					if (names != null && names.size() > 0)
//					{
//						return names;
//					}
//				}
				
				String sectionValue = nameGeneratorRuleAttrDefList.get(6).getSectionValue();
				if (StringUtils.equals(sectionValue, "AlphaNumeric1st36Notation") ||
					StringUtils.equals(sectionValue, "AlphaNumeric1st36NotationExA00") ||
					StringUtils.equals(sectionValue, "AlphaNumericNotation") ||
					StringUtils.equals(sectionValue, "AlphaNumericNotationExA00"))
				{
					List<Map<String, Object>> sqlForResult = null;
					String sqlForQ = " SELECT * FROM NAMEGENERATORSERIAL WHERE RULENAME = :RULENAME AND PREFIX = :PREFIX  ";
					Map<String, Object> bindMapQ = new HashMap<String, Object>();
					bindMapQ.put("RULENAME", ruleName);
					bindMapQ.put("PREFIX", prefixBuilder.toString());
					sqlForResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlForQ, bindMapQ);

					log.info("RULENAME:" + bindMapQ.get("RULENAME") + " PREFIX:" + bindMapQ.get("PREFIX"));

					if (sqlForResult.size() < 1)
					{
						serialNoBuilder.append("0");
						if (sectionLength > 1)
						{
							for (int i = 0; i < sectionLength - 1; i++)
							{
								serialNoBuilder.append("0");
							}
						}

						bindMapQ.put("LASTSERIALNO", serialNoBuilder.toString());
						String sqlForU = "INSERT INTO NAMEGENERATORSERIAL VALUES(:RULENAME , :PREFIX , :LASTSERIALNO) ";
						GenericServiceProxy.getSqlMesTemplate().update(sqlForU, bindMapQ);
					}
				}
			}

			List<String> names = new ArrayList<String>();

			if (StringUtils.equals(ruleName, "SplitLotNaming"))
			{
				for (long l = 0; quantity > l; l++)
				{
					List<String> tempName = NameServiceProxy.getNameGeneratorRuleDefService().generateName(ruleName, argSeq, 1);

					String name = tempName.get(0);

					if (StringUtils.indexOf(name, "Z") == 10)
					{
						quantity = quantity + 1;
						continue;
					}

					names.add(name);
				}
			}
			else
			{
				names = NameServiceProxy.getNameGeneratorRuleDefService().generateName(ruleName, argSeq, quantity);
			}

			for (int i = 0; i < names.size(); i++)
			{
				log.info("ID LIST = " + i + " : " + names.get(i));
			}
			return names;

		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("SYS-9999", "NameGeneratorRuleDefService", fe.getMessage());
		}
	}

	private static List<String> fixPrefix_F3MA14E()
	{
		List<Map<String, Object>> sqlForResult = null;
		String sqlForQ = " SELECT * FROM NAMEGENERATORSERIAL WHERE RULENAME = :RULENAME AND PREFIX = :PREFIX  ";
		Map<String, Object> bindMapQ = new HashMap<String, Object>();
		bindMapQ.put("RULENAME", "LotNaming");
		bindMapQ.put("PREFIX", "F3MA14E");
		sqlForResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlForQ, bindMapQ);

		log.info("RULENAME:" + bindMapQ.get("RULENAME") + " PREFIX:" + bindMapQ.get("PREFIX"));
		
		if (sqlForResult == null || sqlForResult.size() == 0)
		{
			return null;
		}
		
		Map<String,Object> resultMap = sqlForResult.get(0);
		String lastSerialNo = resultMap.get("LASTSERIALNO").toString();
		
		char cSerial[] = lastSerialNo.toCharArray();
		
		if (cSerial[1] == 'Z')
		{
			if (cSerial[0] == '0')
			{
				return null;
			}
			else if (cSerial[0] == 'A')
			{
				cSerial[0] = '9';
			}
			else
			{
				cSerial[0] -= 1;
			}
		}
		
		if (cSerial[1] == 'Z')
		{
			cSerial[1] = 'A';
		}
		else
		{
			cSerial[1] += 1;
		}
		
		String serialNo = String.valueOf(cSerial);
		
		String sql = "UPDATE NAMEGENERATORSERIAL "
				   + "SET LASTSERIALNO = :LASTSERIALNO "
				   + "WHERE RULENAME = :RULENAME "
				   + "  AND PREFIX = :PREFIX ";
		
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("LASTSERIALNO", serialNo);
		bindMap.put("RULENAME", "LotNaming");
		bindMap.put("PREFIX", "F3MA14E");
		
		greenFrameServiceProxy.getSqlTemplate().update(sql, bindMap);
		
		List<String> names = new ArrayList<String>();
		names.add("F3MA14E" + serialNo + "AA");
		
		return names;
	}
	
	public static String getSlotMapInfo(Durable durableData, Lot lotData)
	{
		String normalSlotInfo = "";
		StringBuffer normalSlotInfoBuffer = new StringBuffer();

		// Get Durable's Capacity
		long iCapacity = durableData.getCapacity();

		// Get Product's Slot , These are not Scrapped Product.
		List<Product> productDatas = new ArrayList<Product>();

		if (StringUtils.equals(durableData.getDurableType(), GenericServiceProxy.getConstantMap().CST_TYPE_BUFFER))
		{
			productDatas = ProductServiceProxy.getProductService().select(" WHERE carrierName = ? AND productState != ? AND productState != ? ORDER BY position ",
					new Object[] { durableData.getKey().getDurableName(), GenericServiceProxy.getConstantMap().Prod_Scrapped, GenericServiceProxy.getConstantMap().Prod_Consumed });
		}
		else if (StringUtils.isNotEmpty(lotData.getUdfs().get("JOBNAME")))
		{
			productDatas = ProductServiceProxy.getProductService().select(" WHERE carrierName = ? AND productState != ? AND productState != ? ORDER BY position ",
					new Object[] { durableData.getKey().getDurableName(), GenericServiceProxy.getConstantMap().Prod_Scrapped, GenericServiceProxy.getConstantMap().Prod_Consumed });
		}
		else
		{
			productDatas = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotData.getKey().getLotName().toString());
		}

		// Make Durable Normal SlotMapInfo
		for (int i = 0; i < iCapacity; i++)
		{
			normalSlotInfoBuffer.append(GenericServiceProxy.getConstantMap().PRODUCT_NOT_IN_SLOT);
		}
		log.debug("Normal Slot Map : " + normalSlotInfoBuffer);

		for (int i = 0; i < productDatas.size(); i++)
		{
			int index = (int) productDatas.get(i).getPosition() - 1;

			if (iCapacity > 30 && StringUtils.isNotEmpty(productDatas.get(i).getUdfs().get("SLOTPOSITION")))
			{
				if (StringUtils.equals(productDatas.get(i).getUdfs().get("SLOTPOSITION"), "A"))
				{
					index = ((int) productDatas.get(i).getPosition() * 2) - 1;
				}
				else if (StringUtils.equals(productDatas.get(i).getUdfs().get("SLOTPOSITION"), "B"))
				{
					index = ((int) productDatas.get(i).getPosition() * 2);
				}
				
				index = index - 1;
			}

			normalSlotInfoBuffer.replace(index, index + 1, GenericServiceProxy.getConstantMap().PRODUCT_IN_SLOT);
		}
		log.debug("Completed Slot Map : " + normalSlotInfoBuffer);

		normalSlotInfo = normalSlotInfoBuffer.toString();

		return normalSlotInfo;
	}

	public static String convertReceivedNaming(String ruleName, String name)
	{
		String convertedName = name;
		
		try
		{
			if (ruleName.equals("LotNaming"))
			{
				int length = 2;
				
				//parse only serial partition
				String serial = StringUtils.substring(name, name.length() - length);
				
				if (!StringUtils.isEmpty(serial) && serial.length() == length)
				{
					//must be numeric with single digit
					String target = StringUtils.substring(serial, 0, 1);
					String suffix = StringUtils.substring(serial, 1, length);
					String alpha = "";
					
					switch (Integer.parseInt(target))
					{
						case 0: alpha = "A";break;
						case 1: alpha = "B";break;
						case 2: alpha = "C";break;
						case 3: alpha = "D";break;
						case 4: alpha = "E";break;
						case 5: alpha = "F";break;
						case 6: alpha = "G";break;
						case 7: alpha = "H";break;
						case 8: alpha = "J";break;
						case 9: alpha = "K";break;
						default: alpha = target;
					}
					
					convertedName = new StringBuilder()
										.append(StringUtils.removeEnd(name, serial))
										.append(alpha).append(suffix).toString();
				}
			}
		}
		catch (Exception ex)
		{
			//ignore
		}
		
		return convertedName;
	}
	
	public static String getValue(Map map, String keyName)	
	{
		try
		{
			Object value = map.get(keyName);
			
			if (value != null && value instanceof String)
				return value.toString();
			else if (value != null && value instanceof BigDecimal)
				return value.toString();
		}
		catch(Exception ex)
		{
			log.debug(ex.getMessage());
		}
		
		return "";
	}

	public static Date getDateValue(Map<Object, Object> map, String keyName)
	{
		Date value = null;
		try
		{
			value = (Date) map.get(keyName);
		}
		catch (Exception ex)
		{
			log.debug(ex.getMessage());
		}

		return value;
	}

	public static Map<String, String> setNamedValueSequence(Element element, String typeName) throws FrameworkErrorSignal, NotFoundSignal
	{
		Map<String, String> namedValuemap = new HashMap<String, String>();
		List<ObjectAttributeDef> objectAttributeDefs = greenFrameServiceProxy.getObjectAttributeMap().getAttributeNames(typeName, "ExtendedC");

		log.info("UDF SIZE=" + objectAttributeDefs.size());

		if (objectAttributeDefs != null)
		{
			for (int i = 0; i < objectAttributeDefs.size(); i++)
			{
				String name = "";
				String value = "";

				if (element != null)
				{
					for (int j = 0; j < element.getContentSize(); j++)
					{
						if (element.getChildText(objectAttributeDefs.get(i).getAttributeName()) != null)
						{
							name = objectAttributeDefs.get(i).getAttributeName();
							value = element.getChildText(objectAttributeDefs.get(i).getAttributeName());

							break;
						}
						else
						{
							name = objectAttributeDefs.get(i).getAttributeName();
						}
					}
				}

				if (StringUtils.isNotEmpty(value))
				{
					namedValuemap.put(name, value);
				}
			}
		}

		log.info("UDF SIZE=" + namedValuemap.size());
		return namedValuemap;
	}

	public static Map<String, String> setNamedValueSequence(Map<String, String> namedValuemap, Element element, String typeName) throws FrameworkErrorSignal, NotFoundSignal
	{
		List<ObjectAttributeDef> objectAttributeDefs = greenFrameServiceProxy.getObjectAttributeMap().getAttributeNames(typeName, "ExtendedC");
		log.info("UDF SIZE=" + objectAttributeDefs.size());

		if (objectAttributeDefs != null)
		{
			for (int i = 0; i < objectAttributeDefs.size(); i++)
			{
				String name = "";
				String value = "";

				if (element != null)
				{
					for (int j = 0; j < element.getContentSize(); j++)
					{
						if (element.getChildText(objectAttributeDefs.get(i).getAttributeName()) != null)
						{
							name = objectAttributeDefs.get(i).getAttributeName();
							value = element.getChildText(objectAttributeDefs.get(i).getAttributeName());

							break;
						}
						else
						{
							name = objectAttributeDefs.get(i).getAttributeName();
						}
					}
				}

				// empty value could not be modified in UDF
				if (StringUtils.isNotEmpty(value))
				{
					namedValuemap.put(name, value);
				}
			}
		}

		log.info("UDF SIZE=" + namedValuemap.size());
		return namedValuemap;
	}

	public static List<String> makeList(List<Element> listElement, String name)
	{
		List<String> list = new ArrayList<String>();

		if (listElement != null)
		{
			for (int i = 0; i < listElement.size(); i++)
			{
				Element element = listElement.get(i);
				String value = element.getChildText(name);

				list.add(value);
			}
		}

		return list;
	}

	public static String makeListForQuery(List<Element> listElement, String Name)
	{
		String list= "'";

		if( listElement != null )
		{
			for( int i=0; i<listElement.size(); i++ )
			{
				Element product = listElement.get(i);
				String productName = product.getChild(Name).getText();
				list =  list + productName;
				if(i+1 < listElement.size())
					list = list + "', '";
			}
			list = list + "'";
		}
		
		return list;
	}
	

	public static String getProductAttributeByLength(String machineName, String productName, String fieldName, String itemValue) throws CustomException
	{
		String sql = "SELECT LENGTH(" + fieldName + ") AS LEN, " + fieldName + " FROM PRODUCT WHERE PRODUCTNAME = :productName ";
		
		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("productName", productName);
		
		try{
			
			List<Map<String,Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
			
			if(sqlResult.size() > 0)
			{
				if(!(StringUtils.isEmpty((String)sqlResult.get(0).get(fieldName))))
				{
					if(Integer.parseInt(sqlResult.get(0).get("LEN").toString()) != itemValue.length() )
					{
						itemValue = (String)sqlResult.get(0).get(fieldName);
						log.debug("Product Info Different In Field: " + fieldName + ". Reported Value: " + itemValue + ". Machine ID: " + machineName);
					}
				}
				else
				{
					sql = "UPDATE PRODUCT SET " + fieldName +  " = :itemValue WHERE PRODUCTNAME = :productName";
					bindMap.put("itemValue", itemValue);
					
					GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
				}
			}
		}
		catch(Exception e)
		{
			throw new CustomException("COM-9001", fieldName);
		}
		return itemValue;
	}
	
	public static String toStringFromCollection(Object obj)
	{
		StringBuffer temp = new StringBuffer("");

		if (obj instanceof Object[])
		{
			for (Object element : (Object[]) obj)
			{
				if (!temp.toString().isEmpty())
					temp.append(",");

				if (element != null)
				{
					try
					{
						temp.append(element.toString());
					}
					catch (Exception ex)
					{
						if (log.isDebugEnabled())
							log.debug(ex.getMessage());
					}
				}
			}
		}
		else if (obj instanceof List)
		{
			for (Object element : (List<Object>) obj)
			{
				if (!temp.toString().isEmpty())
					temp.append(",");

				if (element != null)
				{
					try
					{
						temp.append(element.toString());
					}
					catch (Exception ex)
					{
						if (log.isDebugEnabled())
							log.debug(ex.getMessage());
					}
				}
			}
		}

		return temp.toString();
	}
	
	public static String getNodeStack(String factoryName, String processFlowName, String processFlowVersion ,String processOperationName, String processOperationVersion) throws CustomException
	{
		String nodeStack = "";
		
		String sql = "SELECT NODEID FROM NODE WHERE "
			+ " FACTORYNAME = :factoryName "
			+ " AND PROCESSFLOWNAME = :processFlowName "
			+ " AND PROCESSFLOWVERSION = :processFlowVersion "
			+ " AND NODEATTRIBUTE1 = :processOperationName "
			+ " AND NODEATTRIBUTE2 = :processOperationVersion ";
		
		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("factoryName", factoryName);
		bindMap.put("processFlowName", processFlowName);
		bindMap.put("processFlowVersion", processFlowVersion);
		bindMap.put("processOperationName", processOperationName);
		bindMap.put("processOperationVersion", processOperationVersion);
		
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
	
		if( sqlResult.size() == 1 )
		{
			nodeStack = sqlResult.get(0).get("NODEID").toString();
		} 
		else
		{
			nodeStack = "";
		}
		return nodeStack;
	}
	
	public static String[][] getNodeInfo(String nodeID) throws CustomException
	{
		String sql = "SELECT NODEID, PROCESSFLOWNAME, NODEATTRIBUTE1, NODEATTRIBUTE2, FACTORYNAME FROM NODE WHERE NODEID = ? AND NODETYPE = 'ProcessOperation' ";
		Object[] bind = new Object[] { nodeID };

		String[][] nodeResult = null;
		try
		{
			nodeResult = GenericServiceProxy.getSqlMesTemplate().queryForStringArray(sql, bind);
		}
		catch (Exception e)
		{
			
		}
		
		return nodeResult;
	}
	
	
	public static List<Map<String, Object>> getNextSkipInfo(Lot lotData) throws CustomException
	{		
		List<Map<String, Object>> skipInfo = new ArrayList<Map<String, Object>>();

		String SkipSql = "SELECT N.NODETYPE,N.NODEID,N.NODEATTRIBUTE1 " +
						 "FROM ARC A, NODE N " +
						 "WHERE 1=1 " +
						 "AND A.FROMNODEID = :nodeStack " +
						 "AND A.PROCESSFLOWNAME = :processFlowName " +
						 "AND A.PROCESSFLOWVERSION = :processFlowVersion " +
						 "AND A.TONODEID = N.NODEID " +
						 "AND A.PROCESSFLOWNAME = N.PROCESSFLOWNAME " ;
						 
		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("nodeStack", lotData.getNodeStack());
		bindMap.put("processFlowName", lotData.getProcessFlowName());
		bindMap.put("processFlowVersion", lotData.getProcessFlowVersion());

		skipInfo = GenericServiceProxy.getSqlMesTemplate().queryForList(SkipSql, bindMap);

		if(!(skipInfo.size() > 0))
			throw new CustomException("Node-0003");
		
		return skipInfo;
	}
	
	public static List<String> getOperList(String factoryName, String processFlowName, String processFlowVersion)
	{
		String sql = "SELECT N.NODEATTRIBUTE1 FROM NODE N, ARC A " +
					 " WHERE N.FACTORYNAME = :factoryName " +
					 " AND A.FROMNODEID = N.NODEID " +
					 " AND N.PROCESSFLOWNAME = :processFlowName " +
					 " AND N.PROCESSFLOWVERSION = :processFlowVersion " +
					 " AND N.NODETYPE = 'ProcessOperation' ";
			
		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("factoryName", factoryName);
		bindMap.put("processFlowName", processFlowName);
		bindMap.put("processFlowVersion", processFlowVersion);
		
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		
		List<String> operList = new ArrayList<String>();
		for(int i=0; i<sqlResult.size(); i++)
		{
			operList.add(sqlResult.get(i).get("NODEATTRIBUTE1").toString());
		}
		
		return operList;
	}
	
	public static boolean checkProductGradeInProductList(String lotName, String checkProductGrade) throws CustomException
	{
		boolean isExistGrade = false;
		
		List<Product> productList = new ArrayList<Product>();
		productList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotName);
		
		for(Product productData : productList)
		{
			if(StringUtils.equals(productData.getProductGrade(), checkProductGrade))
				isExistGrade = true;
		}
		
		return isExistGrade;
	}
	
	public static boolean checkProductGradeInProductList(List<ProductPGSRC> productPGSRCSequence, String checkProductGrade) throws CustomException
	{
		boolean isExistGrade = false;
		
		for(ProductPGSRC productPGSRC : productPGSRCSequence)
		{
			if(StringUtils.equals(productPGSRC.getProductGrade(), checkProductGrade))
				isExistGrade = true;
		}
		
		return isExistGrade;
	}
	
	public static List<String> splitString(String regex, String str) throws CustomException
	{
		List<String> strList = new ArrayList<String>();
		String[] strArray = str.split(regex);
		
		for(String strTemp : strArray)
		{
			strList.add(strTemp);
		}
		
		return strList;
	}

	public static List<String> splitStringDistinct(String regex, String str) throws CustomException
	{
		List<String> strList = new ArrayList<String>();
		String[] strArray = str.split(regex);

		for (String strTemp : strArray)
		{
			if (StringUtils.isNotEmpty(strTemp) && !strList.contains(strTemp))
			{
				strList.add(strTemp);
			}
		}
		
		return strList;
	}

	public static String makeProductSamplingPositionList(int productSamplingCount) throws CustomException
	{
		String productSamplePositionList = "";
		
		for(int i=1; i<productSamplingCount+1; i++)
		{
			productSamplePositionList = productSamplePositionList + String.valueOf(i);
			
			if(i != productSamplingCount)
				productSamplePositionList = productSamplePositionList + ",";
		}
		
		return productSamplePositionList;
	}
	
	public static String StringValueOf(double d) throws CustomException
	{
		String str = "";
		
		if(d == 0)
			str = "0";
		else
		{
			str = String.valueOf(d);
			
			if(str.indexOf(".") > 0)
			{
				str = str.substring(0, str.indexOf("."));
			}
		}
		
		return str;
	}
	
	public static List<String> copyToStringList(List<String> strList) throws CustomException
	{
		List<String> returnStrList = new ArrayList<String>();
		
		for(String str : strList)
		{
			returnStrList.add(str);
		}
		
		return returnStrList;
	}
	
	public static String toStringWithoutBrackets(List<String> strList) throws CustomException
	{
		String str = strList.toString();
		
		str = str.replace("[", "");
		str = str.replace("]", "");
		
		return str;
	}
	
	public static boolean isLotInEndOperation(Lot lotData, String factoryName, String processFlowName)
		throws CustomException
	{
		try
		{
			Node endNode = ProcessFlowServiceProxy.getProcessFlowService().getEndNode(new ProcessFlowKey(factoryName, processFlowName, "00001"));
			
			List<Arc> connectArc = ProcessFlowServiceProxy.getArcService().select("toNodeId = ?", new Object[] {endNode.getKey().getNodeId()});
			
			Node lastOperationNode = ProcessFlowServiceProxy.getNodeService().getNode(connectArc.get(0).getKey().getFromNodeId());
			
			String targetFactoryName = lastOperationNode.getFactoryName();
			String targetFlowName = lastOperationNode.getProcessFlowName();
			String targetOperationName = lastOperationNode.getNodeAttribute1();
			
			if (lotData.getFactoryName().equals(targetFactoryName)
					&& lotData.getProcessFlowName().equals(targetFlowName)
					&& lotData.getProcessOperationName().equals(targetOperationName))
			{
				return true;
			}
		}
		catch (Exception ex)
		{
			//all components are mandatory
			log.warn("this Lot might not be located anywhere");
		}
		
		return false;
	}

	public static ProcessFlow getProcessFlowData(String FactoryName, String processFlowName, String processFlowVersion)
	{
		ProcessFlowKey processFlowKey = new ProcessFlowKey();
		processFlowKey.setFactoryName(FactoryName);
		processFlowKey.setProcessFlowName(processFlowName);
		processFlowKey.setProcessFlowVersion(processFlowVersion);
		ProcessFlow processFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);

		return processFlowData;
	}

	public static String getEnumDefValueStringByEnumNameAndEnumValue(
			String enumName, String carrierName) {
		String enumValue = "";
		String sql = "SELECT ENUMVALUE FROM ENUMDEFVALUE WHERE " +
				"ENUMNAME = :enumName AND ENUMVALUE = :ENUMVALUE ";
		
		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("enumName", enumName);
		bindMap.put("ENUMVALUE", carrierName);
		
		List<Map<String, Object>> sqlResult = 
				GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		
		if(sqlResult.size() > 0){
			enumValue = sqlResult.get(0).get("ENUMVALUE").toString();
		}
		
		return enumValue;
	}
	
	public static ProcessOperationSpec getLastOperation(String factoryName, String processFlowName)throws CustomException
	{
		try
		{
			String sql = " SELECT A.FROMNODEID FROM ARC A , NODE N " 
					   + " WHERE A.PROCESSFLOWNAME = N.PROCESSFLOWNAME AND A.PROCESSFLOWNAME = :PROCESSFLOWNAME "
					   + " AND A.FACTORYNAME = N.FACTORYNAME AND A.FACTORYNAME = :FACTORYNAME " 
					   + " AND N.NODEID = A.TONODEID AND N.NODETYPE ='End' AND N.NODEATTRIBUTE1 IS NULL "
					   + " AND A.ARCTYPE ='Normal' ";

			Map<String, Object> bindMap = new HashMap<>();
			bindMap.put("FACTORYNAME", factoryName);
			bindMap.put("PROCESSFLOWNAME", processFlowName);

			List<Map<String,Object>> resultList = greenFrameServiceProxy.getSqlTemplate().queryForList(sql, bindMap);
			
			if(resultList == null || resultList.size() == 0)
			{
				log.info(String.format("getLastOperation: NodeId does not exist[FactoryName = %s ,ProcessFlowName = %s]",factoryName,processFlowName));
				throw new CustomException("LOT-0146");
			}

			Node currentNode = ProcessFlowServiceProxy.getNodeService().getNode(ConvertUtil.getMapValueByName(resultList.get(0), "FROMNODEID"));

			String processOperationName = currentNode.getNodeAttribute1();
			String processOperationVer = currentNode.getNodeAttribute2();

			ProcessOperationSpec processOperation = getProcessOperationSpec(factoryName, processOperationName, processOperationVer);

			return processOperation;
		}
		catch (CustomException ce)
		{
			throw ce;
		}
		catch (Exception ex)
		{
			throw new CustomException("SYS-9999", "ProcessOperation", ex.getMessage());
		}
	}
	
	public static ProcessOperationSpec getFirstOperation(String factoryName, String processFlowName)
	throws CustomException
	{
		try
		{
			ProcessFlowKey pfKey = new ProcessFlowKey(factoryName, processFlowName, GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);
			
			String startNodeStack = ProcessFlowServiceProxy.getProcessFlowService().getStartNode(pfKey).getKey().getNodeId();
			String nodeId = ProcessFlowServiceProxy.getProcessFlowService().getNextNode(startNodeStack, "Normal", "").getKey().getNodeId();
			
			Node currentNode = ProcessFlowServiceProxy.getNodeService().getNode(nodeId);
			
			String processOperationName = currentNode.getNodeAttribute1();
			String processOperationVer  = currentNode.getNodeAttribute2();
			
			ProcessOperationSpec processOperation = getProcessOperationSpec(factoryName, processOperationName, processOperationVer);
			
			return processOperation;
		}
		catch (CustomException ce)
		{
			throw ce;
		}
		catch (Exception ex)
		{
			throw new CustomException("SYS-9999", "ProcessOperation", ex.getMessage());
		}
	}
	
	public static boolean isInitialInputUnpacker(Lot lotData)
			throws CustomException 
	{
		boolean isInitialInput = false;
		
		try
		{
			ProcessOperationSpecKey processOperationKey = new ProcessOperationSpecKey();
					
			processOperationKey.setFactoryName(lotData.getFactoryName());
			processOperationKey.setProcessOperationName(lotData.getProcessOperationName());
			processOperationKey.setProcessOperationVersion(lotData.getProcessOperationVersion());
			
			ProcessOperationSpec processOperationSpecData 
				= ProcessOperationSpecServiceProxy.getProcessOperationSpecService().selectByKey(processOperationKey);
			
			if(processOperationSpecData.getDetailProcessOperationType().equals("UPK"))
			{
				isInitialInput = true;
			}
		}
		catch (Exception ex)
		{
			log.error(ex.getMessage());
		}
		
		return isInitialInput;
	}
	
	public static boolean isInitialInputPacker(String machineName)
			throws CustomException 
	{
		boolean isInitialInput = false;
		
		try
		{
			MachineSpecKey mSpecKey = new MachineSpecKey(machineName);
			MachineSpec mSpec = MachineServiceProxy.getMachineSpecService().selectByKey(mSpecKey);
			String machineGroup = mSpec.getMachineGroupName();
			
			MachineKey mKey = new MachineKey();
			mKey.setMachineName(machineName);
			Machine mData = MachineServiceProxy.getMachineService().selectByKey(mKey);
			
			if(machineGroup.equals("CTL") && mData.getUdfs().get("OPERATIONMODE").equals("PPK"))
			{
				isInitialInput = true;
			}
		}
		catch (Exception ex)
		{
			log.error(ex.getMessage());
		}
		
		return isInitialInput;
	}
	
	public static List<String> makeListBySqlResult(List<Map<String, Object>> sqlResult, String objectName)
	{
		String childText = "";
		List<String> list = new ArrayList<String>();

		try
		{
			for (int i = 0; sqlResult.size() > i; i++)
			{
				childText = sqlResult.get(i).get(objectName).toString();
				list.add(childText);
			}
		}
		catch (Exception e)
		{

		}

		return list;
	}
	
	public static String judgeLotGradeByProductList(List<Element> productListEl, String productGradeElementName)
	{
		String lotJudge = "";
		
		//	grade[0] : N, grade[1] : R, grade[2] : P, grade[3] : S, grade[4] : G
		//	Priority : S(all) > N > R > P > G(all)
		int[] grade = {0,0,0,0,0};
		
		for (Element element : productListEl) {
			String productGrade = element.getChildText(productGradeElementName);
		
			if(productGrade.equals("N"))
			{
				grade[0] += 1;
			}
			else if (productGrade.equals("R"))
			{
				grade[1] += 1;
			}
			else if (productGrade.equals("P"))
			{
				grade[2] += 1;
			}
			else if (productGrade.equals("S"))
			{
				grade[3] += 1;
			}
			else if (productGrade.equals("G"))
			{
				grade[4] += 1;
			}
		
		}
		
		if(grade[0] > 0)
		{
			lotJudge = "N";
		}
		else if (grade[1] > 0)
		{
			lotJudge = "R";
		}
		else if(grade[2] > 0)
		{
			lotJudge = "P";
		}
		else if(grade[3] > 0)
		{
			lotJudge = "S";
		}
		else 
		{
			lotJudge = "G";
		}
		
		return lotJudge;
		
	}
	
	public static String judgeLotGradeByProductList(List<Product> productList)
	{
		String lotJudge = "";
		
		//	grade[0] : N, grade[1] : R, grade[2] : P, grade[3] : S, grade[4] : G
		//	Priority : S(all) > N > R > P > G(all)
		int[] grade = {0,0,0,0,0};
		
		for (Product product : productList) {
			String productGrade = product.getProductGrade();
		
			if(productGrade.equals("N"))
			{
				grade[0] += 1;
			}
			else if (productGrade.equals("R"))
			{
				grade[1] += 1;
			}
			else if (productGrade.equals("P"))
			{
				grade[2] += 1;
			}
			else if (productGrade.equals("S"))
			{
				grade[3] += 1;
			}
			else if (productGrade.equals("G"))
			{
				grade[4] += 1;
			}
		
		}
		
		if(grade[0] > 0)
		{
			lotJudge = "N";
		}
		else if (grade[1] > 0)
		{
			lotJudge = "R";
		}
		else if(grade[2] > 0)
		{
			lotJudge = "P";
		}
		else if(grade[3] > 0)
		{
			lotJudge = "S";
		}
		else 
		{
			lotJudge = "G";
		}
		
		return lotJudge;
		
	}
	public static String judgeProductGradeByHalfCutSubProductGrade(String subProductGrade)
	{
		String sql = "SELECT ENUMVALUE FROM ENUMDEFVALUE WHERE ENUMNAME = :ENUMNAME";
		
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("ENUMNAME", "GlassJudgeStd" );

		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);

		double judgeStd = 100.0;
		
		if (result.size() > 0)
		{
			judgeStd = Long.parseLong(ConvertUtil.getMapValueByName(result.get(0), "ENUMVALUE"));
		}

        int iCount = 0;

        for (int i = 0; i < subProductGrade.length(); i++)
        {
            if (subProductGrade.substring(i, i + 1).equals("N") || subProductGrade.substring(i, i + 1).equals("S"))
            {
            	iCount++;
            }
        }

        long judgeCal = 0;
        if (!StringUtil.isEmpty(subProductGrade))
        {
        	judgeCal = Long.parseLong(String.valueOf(iCount)) * 100 / Long.parseLong(String.valueOf(subProductGrade.length()));
        }

        String glassJudge = "G";

        if (judgeCal < judgeStd)
        {
        	if (subProductGrade.contains("P"))
        		glassJudge = "P";
        	else
        		glassJudge = "G";
        }
        else glassJudge = "N";

		return glassJudge;
		
	}
	
	public static String judgeProductGradeBySubProductGrade(String subProductGrade)
	{
		String sql = "SELECT ENUMVALUE FROM ENUMDEFVALUE WHERE ENUMNAME = :ENUMNAME";
		
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("ENUMNAME", "GlassJudgeStd" );

		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);

		double judgeStd = 100.0;
		
		if (result.size() > 0)
		{
			judgeStd = Long.parseLong(ConvertUtil.getMapValueByName(result.get(0), "ENUMVALUE"));
		}

        int countN1 = 0;
        int countN2 = 0;

        for (int i = 0; i < subProductGrade.length(); i++)
        {
            if (subProductGrade.substring(i, i + 1).equals("N"))
            {
                if (i < subProductGrade.length() / 2)
                    countN1++;
                else
                    countN2++;
            }
        }

        String glassJudge1 = "G";
        String glassJudge2 = "G";

        if (Long.parseLong(String.valueOf(countN1)) * 100 / Long.parseLong(String.valueOf(subProductGrade.length() / 2)) < judgeStd) glassJudge1 = "G";
        else glassJudge1 = "N";

        if (Long.parseLong(String.valueOf(countN2)) * 100 / Long.parseLong(String.valueOf(subProductGrade.length() / 2)) < judgeStd) glassJudge2 = "G";
        else glassJudge2 = "N";
		
        String ProductJudge = "G";
        
        if (glassJudge1.equals("N") && glassJudge2.equals("N")) ProductJudge = "N";
        
		return ProductJudge;
		
	}

	public static List<Lot> getLotListByLotNameList(List<String> lotNameList)
	{
		List<Lot> lotDataList = new ArrayList<Lot>();

		try
		{
			if (lotNameList != null && lotNameList.size() > 0)
			{
				List<String> bindLotNameList = new ArrayList<String>();
				StringBuilder sqlLotNameList = new StringBuilder();

				for (int i = 0; i < lotNameList.size(); i++)
				{
					if (bindLotNameList.size() <= 0)
					{
						sqlLotNameList = new StringBuilder();
						sqlLotNameList.append(" where lotName in (");
					}

					if (bindLotNameList.size() > 0)
					{
						sqlLotNameList.append(", ?");
						bindLotNameList.add(lotNameList.get(i));
					}
					else
					{
						sqlLotNameList.append("?");
						bindLotNameList.add(lotNameList.get(i));
					}

					if (bindLotNameList.size() >= 1000 || i + 1 >= lotNameList.size())
					{
						sqlLotNameList.append(") ");
						Object[] bindObject = new Object[bindLotNameList.size()];

						for (int j = 0; j < bindLotNameList.size(); j++)
						{
							bindObject[j] = bindLotNameList.get(j);
						}
						log.info(bindLotNameList);

						List<Lot> tmpLotDataList = LotServiceProxy.getLotService().select(sqlLotNameList.toString(), bindObject);

						for (Lot lotData : tmpLotDataList)
						{
							lotDataList.add(lotData);
						}

						sqlLotNameList = new StringBuilder();
						bindLotNameList = new ArrayList<String>();
					}
				}
			}
			else
			{
				lotDataList = new ArrayList<Lot>();
			}
		}
		catch (NotFoundSignal nfs)
		{
			log.warn(nfs);
			lotDataList = new ArrayList<Lot>();
		}

		return lotDataList;
	}

	public static List<Lot> getReleasedLotListByLotNameList(List<String> lotNameList)
	{
		List<Lot> lotDataList = new ArrayList<Lot>();

		try
		{
			if (lotNameList != null && lotNameList.size() > 0)
			{
				List<String> bindLotNameList = new ArrayList<String>();
				StringBuilder sqlLotNameList = new StringBuilder();

				for (int i = 0; i < lotNameList.size(); i++)
				{
					if (bindLotNameList.size() <= 0)
					{
						sqlLotNameList = new StringBuilder();
						sqlLotNameList.append(" where lotName in (");
					}

					if (bindLotNameList.size() > 0)
					{
						sqlLotNameList.append(", ?");
						bindLotNameList.add(lotNameList.get(i));
					}
					else
					{
						sqlLotNameList.append("?");
						bindLotNameList.add(lotNameList.get(i));
					}

					if (bindLotNameList.size() >= 1000 || i + 1 >= lotNameList.size())
					{
						sqlLotNameList.append(") and lotState = ? ");
						Object[] bindObject = new Object[bindLotNameList.size() + 1];

						for (int j = 0; j < bindLotNameList.size(); j++)
						{
							bindObject[j] = bindLotNameList.get(j);
						}

						log.info(bindLotNameList);
						bindObject[bindLotNameList.size()] = GenericServiceProxy.getConstantMap().Lot_Released;

						List<Lot> tmpLotDataList = LotServiceProxy.getLotService().select(sqlLotNameList.toString(), bindObject);

						for (Lot lotData : tmpLotDataList)
						{
							lotDataList.add(lotData);
						}

						sqlLotNameList = new StringBuilder();
						bindLotNameList = new ArrayList<String>();
					}
				}
			}
			else
			{
				lotDataList = new ArrayList<Lot>();
			}
		}
		catch (NotFoundSignal nfs)
		{
			log.warn(nfs);
			lotDataList = new ArrayList<Lot>();
		}

		return lotDataList;
	}

	public static List<Lot> getLotListByCarrier(String carrierName, boolean isMandatory) throws CustomException
	{
		String condition = "WHERE carrierName = ? AND lotState = ?";
		
		Object[] bindSet = new Object[] {carrierName, GenericServiceProxy.getConstantMap().Lot_Released};
		
		List<Lot> lotList;
		
		try
		{
			lotList = LotServiceProxy.getLotService().select(condition, bindSet);
		}
		catch (Exception ex)
		{
			if (isMandatory)
				throw new CustomException("SYS-9999", "Product", "Nothing Lot");
			else
				return lotList = new ArrayList<Lot>();
		}

		return lotList;
	}
	
	public static ProcessOperationSpec getProcessOperationSpec( String factoryName, String processOperationName ) throws CustomException{

		ProcessOperationSpec processOperationData = new ProcessOperationSpec();
		try{
			ProcessOperationSpecKey processOperationKey = new ProcessOperationSpecKey();
			
			processOperationKey.setFactoryName(factoryName);
			processOperationKey.setProcessOperationName(processOperationName);
			processOperationKey.setProcessOperationVersion("00001");
			
			processOperationData 
				= ProcessOperationSpecServiceProxy.getProcessOperationSpecService().selectByKey(processOperationKey);
			
		} catch( Exception e ){
			throw new CustomException("PROCESSOPERATION-9001", processOperationName);
		}
		
		return processOperationData;
	}

	public static boolean equalsIn(String source, String... values)
	{
		for (String value : values)
		{
			if (StringUtils.equals(source, value))
			{
				return true;
			}
		}
		return false;
	}

	public static ProductRequestPlan getFirstPlanByMachine(String machineName, boolean flag)
			throws CustomException
	{
		try
		{
			ProductRequestPlan pPlan = new ProductRequestPlan();
				
			if(flag)
			{
					
				List<ProductRequestPlan> pPlanList = ExtendedObjectProxy.getProductRequestPlanService().select("machineName = ? and planState IN (?, ?) "
						+ "and position = (select min(position) from CT_PRODUCTREQUESTPLAN Where machineName = ? and planState IN (?, ?))", 
						new Object[]{machineName, "Released", "Started", machineName, "Released", "Started"});

				if(pPlanList.size() > 1)
				{
					throw new CustomException("PRODUCTREQUEST-0022", machineName);
				}
				
				pPlan = pPlanList.get(0);
			}
			
			return pPlan;
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("PRODUCTREQUEST-0021", machineName);
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("PRODUCTREQUEST-0021", machineName);
		}
	}

	
	public static List<Map<String, Object>> getOperationList(String factoryName, String processFlowName, String processFlowVersion)
	{
		String sql = "SELECT N.NODEATTRIBUTE1, N.NODEATTRIBUTE2 FROM NODE N, ARC A " +
					 " WHERE N.FACTORYNAME = :factoryName " +
					 " AND A.FROMNODEID = N.NODEID " +
					 " AND N.PROCESSFLOWNAME = :processFlowName " +
					 " AND N.PROCESSFLOWVERSION = :processFlowVersion " +
					 " AND N.NODETYPE = 'ProcessOperation' ";
			
		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("factoryName", factoryName);
		bindMap.put("processFlowName", processFlowName);
		bindMap.put("processFlowVersion", processFlowVersion);
		
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		
	
		
		return sqlResult;
	}

	/**
	 * 
	 * AR-Photo-0027-01,AR-Photo-0032-01
	 * ReserveHold sends an e-mail when it arrives at the process.
	 * 
	 * @author aim_dhko
	 * @return 
	 */
	public static void sendEmail(String alarmGroupName, String alarmType, String message) throws CustomException
	{
		List<String> emailList = getUserEmailList(alarmGroupName, alarmType);

		if (emailList.size() > 0)
		{
			try
			{
				EMailInterface ei = new EMailInterface(GenericServiceProxy.getConstantMap().MailServerAddress, "25", "1000", "1000");

				ei.postMail(emailList, alarmType + "AlarmReport", message, "AlarmReport", "mesalarmreport@visionox.com", "mesalarmreport@visionox.com", "2wsx@qaz");
				String emailAction="Y";
				addAlarmemailActionHistory(alarmGroupName, alarmType, message, emailAction);
			}
			catch (Exception e)
			{
				log.error("Failed to send mail.");
				e.printStackTrace();
			}
		}
	}
	
	public static void sendAlarmEmail(String alarmCode, String alarmType, String message) throws CustomException
	{
		
		String[] mailList = getEmailList(alarmType);

		if (mailList != null && mailList.length > 0)
		{
			sendEmail(mailList, alarmType + "AlarmReport", message);
		}
		else
		{
			log.info("[sendAlarmEmail] Recipients information is empty.");
		}
	}

	public static void sendAlarmEmailToborrowUsername(String nSendEmailFlag,String alarmCode, String alarmType, String message,String borrowCentrality,String borrowDepartment, String borrowUsername, String borrowDate) throws CustomException
	{
		String[] mailList = getborrowUsername(alarmCode,alarmType,borrowCentrality,borrowDepartment,borrowUsername,borrowDate);

		if (mailList != null && mailList.length > 0)
		{
			sendEmail(mailList, borrowCentrality+"-" +borrowDepartment+"-" +borrowUsername + "BorrowOvertime", message);
			updateborrowFlag(nSendEmailFlag, borrowCentrality,borrowDepartment, borrowUsername, borrowDate);
		}
		else
		{
			log.info("[sendAlarmEmailToborrowUsername] Recipients information is empty.");
		}
	}
	
	public static String[] getborrowUsername(String alarmCode,String alarmType,String borrowCentrality,String borrowDepartment,String borrowUsername,String borrowDate)
	{
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT B.BORROWEMAIL  FROM CT_BORROWPANEL B ");
		sql.append("  WHERE B.BORROWCENTRALITY =:BORROWCENTRALITY  ");
		sql.append("  AND B.BORROWDEPARTMENT =:BORROWDEPARTMENT ");
		sql.append("  AND B.BORROWUSERNAME =:BORROWUSERNAME ");
		sql.append("  AND B.BORROWSTATE='Borrowed' ");
		sql.append("   AND B.BORROWEMAIL IS NOT NULL ");
		sql.append("   GROUP by B.BORROWEMAIL ");
		Map<String, Object> userArgs = new HashMap<String, Object>();
		userArgs.put("BORROWCENTRALITY", borrowCentrality);
		userArgs.put("BORROWDEPARTMENT", borrowDepartment);
		userArgs.put("BORROWUSERNAME", borrowUsername);
		List<Map<String, Object>>userResult1 = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), userArgs);//BORROWEMAIL
	    sql = new StringBuffer();
		sql.append(" SELECT DISTINCT  UP.EMAIL  ");
		sql.append(" FROM CT_ALARMGROUP AG , CT_ALARMUSERGROUP AU  , USERPROFILE UP ");
		sql.append(" WHERE AG.ALARMTYPE = :ALARMTYPE ");
		sql.append(" AND AG.ALARMGROUPNAME = AU.ALARMGROUPNAME ");
		sql.append(" AND AU.USERID = UP.USERID ");
		sql.append(" AND UP.EMAIL IS NOT NULL ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("ALARMTYPE", alarmType);

		List<Map<String, Object>> userResult2 = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);//USERGROUPNAME

		Set mailSet = new TreeSet();
		if (userResult1.size() > 0 || userResult2.size() > 0)
		{
			if (userResult1.size() > 0)
			{
				for (Map<String, Object> user : userResult1)
				{
					String eMail = ConvertUtil.getMapValueByName(user, "BORROWEMAIL");
					mailSet.add(eMail);
				}
			}
			if (userResult2.size() > 0)
			{
				for (Map<String, Object> group : userResult2)
				{
					String eMail = ConvertUtil.getMapValueByName(group, "EMAIL");
					mailSet.add(eMail);
				}
			}
		}
		return CollectionUtil.toStringArray(mailSet);
	}

	public static void updateborrowFlag(String nSendEmailFlag,String borrowCentrality,String borrowDepartment, String borrowUsername, String borrowDate )throws CustomException
	{
		try
		 {
			String SQL = "UPDATE CT_BORROWPANEL  SET SENDEMAILFLG = :SENDEMAILFLG  WHERE BORROWUSERNAME = :BORROWUSERNAME AND BORROWDEPARTMENT=:BORROWDEPARTMENT AND BORROWCENTRALITY=:BORROWCENTRALITY  AND  BORROWDATE LIKE to_date(:BORROWDATE, 'yyyy-mm-dd HH24:MI:SS') ";
			  Map<String, Object> bindMap = new HashMap<String, Object>();
			  bindMap.put("BORROWCENTRALITY",borrowCentrality);
			  bindMap.put("BORROWDEPARTMENT",borrowDepartment);
			  bindMap.put("BORROWUSERNAME", borrowUsername);
			  bindMap.put("BORROWDATE", borrowDate);
			  bindMap.put("SENDEMAILFLG", nSendEmailFlag);
		      GenericServiceProxy.getSqlMesTemplate().update(SQL, bindMap);	
			  log.info("UPDATE SENDEMAILFLAG Sucess");
		  }
		  catch(Exception ex)
		  {
			  log.info("UPDATE SENDEMAILFLAG Failed");
		  }
		  	
		  
	}

	public static void sendTransportEmail(String alarmCode, String alarmType, String message, String transportjobName, String transportjobType, String jobState, String sendemailFlag)
			throws CustomException
	{

		String[] mailList = getEmailList(alarmType);

		if (mailList != null && mailList.length > 0)
		{
			sendEmail(mailList, alarmType + "AlarmReport", message);
			updateFlag(transportjobName, transportjobType, jobState, sendemailFlag);
		}
		else
		{
			log.info("[sendTransportEmail] Recipients information is empty.");
		}
	}

	public static void updateFlag(String transportjobName, String transportjobType, String jobState, String sendemailFlag) throws CustomException
	{
		try
		{
			String SQL = "UPDATE CT_TRANSPORTJOBCOMMAND SET SENDEMAILFLAG = :SENDEMAILFLAG  WHERE TRANSPORTJOBNAME = :TRANSPORTJOBNAME  AND TRANSPORTJOBTYPE=:TRANSPORTJOBTYPE AND JOBSTATE=:JOBSTATE";
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("TRANSPORTJOBNAME", transportjobName);
			bindMap.put("TRANSPORTJOBTYPE", transportjobType);
			bindMap.put("JOBSTATE", jobState);
			bindMap.put("SENDEMAILFLAG", sendemailFlag);
			GenericServiceProxy.getSqlMesTemplate().update(SQL, bindMap);
			log.info("UPDATE SENDEMAILFLAG Sucess");
		}
		catch (Exception ex)
		{
			log.info("UPDATE SENDEMAILFLAG Failed");
		}

	}
	
	public static void sendAlarmEmailwithSubject(String alarmCode, String alarmType, String message, String subject) throws CustomException
	{
		String[] mailList = getEmailList(alarmType);

		if (mailList != null && mailList.length > 0)
		{
			sendEmail(mailList, subject, message);
			// GenericServiceProxy.getSMSInterface().AlarmSmsSend(AlarmCode, AlarmType, smsMessage);
		}
		else
		{
			log.info("[sendAlarmEmail] Recipients information is empty.");
		}
	}
	public static void sendAlarmEmail(String alarmCode, String alarmType, String factoryName, String machineName, String unitName, String message) throws CustomException
	{
		String[] mailList = getEmailList(alarmCode, alarmType, machineName, unitName);

		if (mailList != null && mailList.length > 0)
		{
			sendEmail(mailList, alarmType + "AlarmReport", message);
		}
		else
		{
			log.info("[sendAlarmEmail] Recipients information is empty.");
		}
	}

	/**
	 * 
	 * AR-Photo-0027-01,AR-Photo-0032-01
	 * ReserveHold sends an e-mail when it arrives at the process.
	 * 
	 * @author aim_dhko
	 * @return 
	 */
	@SuppressWarnings("unchecked")
	public static List<String> getUserEmailList(String alarmGroupName, String alarmType)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT U.ALARMGROUPNAME, G.ALARMTYPE, U.USERID, U.USERLEVEL, U.RANGE, G.DESCRIPTION, F.EMAIL ");
		sql.append("  FROM CT_ALARMUSERGROUP U, CT_ALARMGROUP G, USERPROFILE F ");
		sql.append(" WHERE U.ALARMGROUPNAME = :ALARMGROUPNAME ");
		sql.append("   AND G.ALARMTYPE = :ALARMTYPE ");
		sql.append("   AND G.ALARMGROUPNAME = U.ALARMGROUPNAME ");
		sql.append("   AND F.USERID(+) = U.USERID ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("ALARMGROUPNAME", alarmGroupName);
		args.put("ALARMTYPE", alarmType);

		List<Map<String, Object>> resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);
		if(resultList == null || resultList.size() == 0)
		{
			return new ArrayList<String>();
		}
		
		List<String> emailList = new ArrayList<String>();
		
		for (Map<String, Object> result : resultList) 
		{
			String userId = ConvertUtil.getMapValueByName(result, "USERID");
			String eMail = ConvertUtil.getMapValueByName(result, "EMAIL");
			if(StringUtils.isEmpty(eMail))
			{
				log.info("getUserEmailList : Email is Empty. UserID = " + userId);
				continue;
			}
			
			emailList.add(eMail);
		}
		
		return emailList;
	}
	
	public static void sendAlarmEmailForFactory(String factoryName, String alarmType, String message) throws CustomException
	{
		String[] mailList = getEmailList(alarmType);

		if (mailList != null && mailList.length > 0)
		{
			sendEmail(mailList, alarmType + "AlarmReport", message);
		}
		else
		{
			log.info("[sendAlarmEmailForFactory] Recipients information is empty.");
		}
	}

	public static void sendAlarmEmailForFactoryAndEQP(String machineName, String factoryName, String alarmType, String message) throws CustomException
	{
		//List<String> emailList = getEmailListForFactoryAndEQP(machineName, factoryName, alarmType);

		String[] mailList = getEmailList(alarmType);

		if (mailList != null && mailList.length > 0)
		{
			sendEmail(mailList, alarmType + "AlarmReport", message);
		}
		else
		{
			log.info("[sendAlarmEmailForFactoryAndEQP] Recipients information is empty.");
		}
	}
	
	public static void sendTFEAOIEmailForFactoryAndEQP(String machineName, String factoryName, String alarmType, String message) throws CustomException
	{
		//List<String> emailList = getEmailListForFactoryAndEQP(machineName, factoryName, alarmType);
		
		String[] mailList = getEmailList(alarmType);

		if (mailList != null && mailList.length > 0)
		{
			sendEmail(mailList, alarmType + ": " + "TFE AOI WSI OutOfSpec Alarm", message);
		}
		else
		{
			log.info("[sendTFEAOIEmailForFactoryAndEQP] Recipients information is empty.");
		}
	}

	@SuppressWarnings("unchecked")
	public static List<String> getEmailListForFactoryAndEQP(String machineName, String factoryName, String alarmType)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT AU.EMAIL ");
		sql.append("  FROM CT_ALARMUSERGROUP AG, CT_ALARMMAILACTION AM, CT_ALARMUSER AU ");
		sql.append(" WHERE AG.USERGROUPNAME = AM.USERGROUPNAME ");
		sql.append("   AND AM.ALARMTYPE = :ALARMTYPE ");
		sql.append("   AND AM.MACHINENAME = :MACHINENAME ");
		sql.append("   AND AG.USERGROUPNAME = AU.USERGROUPNAME ");
		sql.append("   AND AM.FACTORYNAME IN (NULL, :FACTORYNAME) ");
		sql.append("   AND AU.EMAIL IS NOT NULL ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("ALARMTYPE", alarmType);
		args.put("MACHINENAME", machineName);
		args.put("FACTORYNAME", factoryName);

		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		List<String> emailList = new ArrayList<String>();

		if (sqlResult.size() > 0)
		{
			emailList = makeListBySqlResult(sqlResult, "EMAIL");
		}
		else
		{
			args.put("MACHINENAME", "-");
			sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

			if (sqlResult.size() > 0)
			{
				emailList = makeListBySqlResult(sqlResult, "EMAIL");
			}
		}

		return emailList;
	}

	public static int[] executeBatch(String queryType, List<?> dataObjectList) throws Exception
	{
		return executeBatch(queryType, dataObjectList, true);
		
//		if (CollectionUtils.isEmpty(dataObjectList))
//		{
//			log.warn("There is no Object to batch update.");
//			return null;
//		}
//
//		String sql = generateSqlStatement(queryType, dataObjectList.get(0));
//
//		List<Object[]> bindSetList = new ArrayList<Object[]>();
//		for (Object dataInfo : dataObjectList)
//		{
//			bindSetList.add(getBindSetObject(queryType, dataInfo));
//		}
//
//		int[] resultSizes;
//		if (queryType.equalsIgnoreCase("delete"))
//		{
//			resultSizes = GenericServiceProxy.getSqlMesTemplate().updateBatch(sql, bindSetList);
//		}
//		else
//		{
//			// int[] resultSizes;
//			if (queryType.equalsIgnoreCase("update"))
//			{
//				log.info("LogCheck");
//				log.info(sql);
//				for(int i = 0 ; i < bindSetList.size() ; i++)
//				{
//					String bindInfo = "";
//					Object[] info = bindSetList.get(i);
//					for(int j = 0 ; j < info.length ; j++)
//					{
//						bindInfo += info[j].toString();
//						bindInfo += ", ";
//					}
//					log.info(bindInfo.substring(0, bindInfo.length()-2));
//				}
//				
//				resultSizes = GenericServiceProxy.getSqlMesTemplate().updateBatch(sql, bindSetList);
//			}
//			else
//			{
//				// int[] resultSizes;
//				if (queryType.equalsIgnoreCase("insert"))
//				{
//					resultSizes = GenericServiceProxy.getSqlMesTemplate().updateBatch(sql, bindSetList);
//				}
//				else
//				{
//					throw new greenFrameDBErrorSignal("InvalidQueryType", queryType);
//				}
//			}
//		}
//
//		return resultSizes;
	}

	public static Object[] getBindSetObject(String queryType, Object dataKeyObject) throws Exception
	{
		Object[] objectValue = new Object[] {};

		if ((queryType.equalsIgnoreCase("select")) || (queryType.equalsIgnoreCase("select.for.update")) || (queryType.equalsIgnoreCase("delete")))
		{
			objectValue = getSelectOrDeleteBindObjects(dataKeyObject);
		}
		else
		{
			if (queryType.equalsIgnoreCase("update"))
			{
				objectValue = getUpdateBindObjects(dataKeyObject);
			}
			else if (queryType.equalsIgnoreCase("insert"))
			{
				objectValue = getInsertBindObjects(dataKeyObject);
			}
			else if (queryType.equalsIgnoreCase("delete"))
			{
				objectValue = getSelectOrDeleteBindObjects(dataKeyObject);
			}
			else
			{
				throw new greenFrameErrorSignal("InvalidQueryType", queryType);
			}
		}
		return objectValue;
	}
	
	public static List<Map<String,Object>> getWorkOrderListFromPallet(String processGroupName)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT L.PRODUCTREQUESTNAME, L.LOTDETAILGRADE, COUNT (L.PRODUCTREQUESTNAME) AS QUANTITY ");
		sql.append("  FROM LOT L, ");
		sql.append("       (SELECT PROCESSGROUPNAME ");
		sql.append("          FROM PROCESSGROUP ");
		sql.append("         WHERE SUPERPROCESSGROUPNAME IN (SELECT PROCESSGROUPNAME ");
		sql.append("                                           FROM PROCESSGROUP ");
		sql.append("                                          WHERE SUPERPROCESSGROUPNAME = :PROCESSGROUPNAME)) PG ");
		sql.append(" WHERE L.PROCESSGROUPNAME = PG.PROCESSGROUPNAME ");
		sql.append("GROUP BY PRODUCTREQUESTNAME, LOTDETAILGRADE ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("PROCESSGROUPNAME", processGroupName);
		
		List<Map<String,Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);
		
		return sqlResult;
	}
	
	public static List<String> sortActualSamplePosition(List<String> actualSamplePositionList) throws CustomException
	{
		Collections.sort(actualSamplePositionList, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2)
			{
				Integer d1 = Integer.parseInt(o1);
				Integer d2 = Integer.parseInt(o2);

				return d1.compareTo(d2);
			}
		});
		
		return actualSamplePositionList;
	}
	
	public static List<String> sort(List<String> sortList) throws CustomException
	{
		Collections.sort(sortList, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2)
			{
				Integer d1 = Integer.parseInt(o1);
				Integer d2 = Integer.parseInt(o2);

				return d1.compareTo(d2);
			}
		});
		
		return sortList;
	}
	
	public static String generateSqlStatement(String queryType, Object dataKeyObject) throws greenFrameErrorSignal
	{
		StringBuilder strBuilder = new StringBuilder();

		String tableName = OrmStandardEngineUtil.getTableName(dataKeyObject);
		if (queryType.toLowerCase().equalsIgnoreCase("select"))
		{
			strBuilder.append("select * from " + tableName);
		}
		else if (queryType.toLowerCase().equalsIgnoreCase("delete"))
		{
			strBuilder.append("delete " + tableName);
		}
		else if (queryType.toLowerCase().equalsIgnoreCase("update"))
		{
			strBuilder.append("update " + tableName).append(" set ");
		}
		else if (queryType.toLowerCase().equalsIgnoreCase("insert"))
		{
			strBuilder.append("insert ").append("into ").append(tableName);
		}

		return generateCondition(strBuilder, queryType, tableName, dataKeyObject).toString();
	}
	
	public static StringBuilder generateCondition(StringBuilder strBuilder, String queryType, String tableName, Object dataKeyObject) throws greenFrameErrorSignal
	{

		List<ObjectAttributeDef> ObjectAttributeDefs = greenFrameServiceProxy.getObjectAttributeMap().getAttributeNames(tableName, ObjectAttributeMap.Standard_Type);
		if (ObjectAttributeDefs == null)
			throw new greenFrameErrorSignal(ErrorSignal.NotDefineObjectAttributeSignal, tableName);

		if (queryType.equalsIgnoreCase("select") || queryType.equalsIgnoreCase("delete"))
		{
			strBuilder.append(" where ");
			for (int i = 0 ; i < ObjectAttributeDefs.size() ; i++ )
			{
				ObjectAttributeDef attributeDef = ObjectAttributeDefs.get(i);
				
				if (attributeDef.getPrimaryKeyFlag().equalsIgnoreCase("y"))
				{
					strBuilder.append(attributeDef.getAttributeName()).append("=").append("?");
					strBuilder.append(" and ");
				}
			}
			
			if (strBuilder.toString().endsWith(" and "))
				strBuilder.delete(strBuilder.length() - 5, strBuilder.length());
			return strBuilder;
		}
		else if (queryType.equalsIgnoreCase("update"))
		{
			String where = " where ";
			List<ObjectAttributeDef> ObjectAttributeCDefs =
					greenFrameServiceProxy.getObjectAttributeMap().getAttributeNames(tableName, ObjectAttributeMap.ExtendedC_Type);
			Object[] attributes = new Object[] { ObjectAttributeDefs, ObjectAttributeCDefs };

			for (int h = 0; h < attributes.length; h++)
			{
				if (attributes[h] == null)
					continue;
				List<ObjectAttributeDef> oads = (List<ObjectAttributeDef>) attributes[h];
				for (int i = 0 ; i < oads.size() ; i++ )
				{
					ObjectAttributeDef attributeDef = oads.get(i);
					
					if (attributeDef.getPrimaryKeyFlag().equalsIgnoreCase("y"))
						where = where + attributeDef.getAttributeName() + "=" + "?" + " and ";
					else
					{
						if (h == 1)
						{ // udfs
							if (checkNotNullValue(dataKeyObject, attributeDef, ObjectUtil.getUdfsValue(dataKeyObject)))
								strBuilder.append(attributeDef.getAttributeName()).append("=").append("?").append(",");
							else if (attributeDef.getDataType().equalsIgnoreCase("timestamp"))
								strBuilder.append(attributeDef.getAttributeName()).append("=").append("null").append(",");
						}
						else
						{
							if (checkNotNullValue(dataKeyObject, attributeDef, null))
								strBuilder.append(attributeDef.getAttributeName()).append("=").append("?").append(",");
							else if (attributeDef.getDataType().equalsIgnoreCase("timestamp"))
								strBuilder.append(attributeDef.getAttributeName()).append("=").append("null").append(",");
						}
					}
				}
			}
			
			where = where.substring(0, where.length() - 5);
			strBuilder.delete(strBuilder.length() - 1, strBuilder.length()).append(where);
			
			return strBuilder;
		}
		else if (queryType.equalsIgnoreCase("insert"))
		{
			strBuilder.append(" (");

			StringBuilder strBuilder2 = new StringBuilder();
			List<ObjectAttributeDef> ObjectAttributeCDefs =
					greenFrameServiceProxy.getObjectAttributeMap().getAttributeNames(tableName, ObjectAttributeMap.ExtendedC_Type);
			Object[] attributes = new Object[] { ObjectAttributeDefs, ObjectAttributeCDefs };

			for (int h = 0; h < attributes.length; h++)
			{
				if (attributes[h] == null)
					continue;
				
				List<ObjectAttributeDef> oads = (List<ObjectAttributeDef>) attributes[h];
				
				for (int i = 0 ; i < oads.size() ; i++ )
				{
					ObjectAttributeDef attributeDef = oads.get(i);
					
					if (h == 1)
					{
						if (checkNotNullValue(dataKeyObject, attributeDef, ObjectUtil.getUdfsValue(dataKeyObject)))
						{
							if (attributeDef.getDataType().equalsIgnoreCase("timestamp")
								&& attributeDef.getAttributeName().equalsIgnoreCase("systemtime"))
							{
								strBuilder.append(attributeDef.getAttributeName()).append(",");
								strBuilder2.append("SYSDATE").append(",");
							}
							else
							{
								strBuilder.append(attributeDef.getAttributeName()).append(",");
								strBuilder2.append("?").append(",");
							}
						}
						else if (StringUtils.isNotEmpty(attributeDef.getDefaultValue()))
						{
							strBuilder.append(attributeDef.getAttributeName()).append(",");
							strBuilder2.append("?").append(",");
						}
					}
					else
					{
						if (checkNotNullValue(dataKeyObject, attributeDef, null))
						{
							if (attributeDef.getDataType().equalsIgnoreCase("timestamp") && attributeDef.getAttributeName().equalsIgnoreCase("systemtime"))
							{
								strBuilder.append(attributeDef.getAttributeName()).append(",");
								strBuilder2.append("SYSDATE").append(",");
							}
							else
							{
								strBuilder.append(attributeDef.getAttributeName()).append(",");
								strBuilder2.append("?").append(",");
							}
						}
						else if (StringUtils.isNotEmpty(attributeDef.getDefaultValue()))
						{
							strBuilder.append(attributeDef.getAttributeName()).append(",");
							strBuilder2.append("?").append(",");
						}
					}
				}
			}

			strBuilder.delete(strBuilder.length() - 1, strBuilder.length()).append(")").append(" values (");
			strBuilder.append(strBuilder2.delete(strBuilder2.length() - 1, strBuilder2.length()));
			strBuilder.append(")");

			return strBuilder;
		}
		else
			throw new greenFrameErrorSignal(ErrorSignal.InvalidQueryType, queryType);
	}

	private static boolean checkNotNullValue(Object dataKeyObject, ObjectAttributeDef objectAttributeDef, Map<String, String> udfs)
	{
		if (objectAttributeDef.getDataType().equalsIgnoreCase("timestamp") && objectAttributeDef.getAttributeName().equalsIgnoreCase("systemtime"))
			return true;
		Object value = null;

		if (objectAttributeDef.getPrimaryKeyFlag().equalsIgnoreCase("y"))
			value = ObjectUtil.getFieldValue(OrmStandardEngineUtil.getKeyInfo(dataKeyObject), objectAttributeDef.getAttributeName());
		else if (udfs == null)
			value = ObjectUtil.getFieldValue(dataKeyObject, objectAttributeDef.getAttributeName());
		else
			value = udfs.get(objectAttributeDef.getAttributeName());

		if (value == null)
			return false;

		if (objectAttributeDef.getDataType().equalsIgnoreCase("timestamp"))
		{
			if (value.equals(new Timestamp(0)) || value.toString().length() == 0)
				return false;
		}
		else if (value != null && (objectAttributeDef.getDataType().equalsIgnoreCase("Long") || objectAttributeDef.getDataType().equalsIgnoreCase("Double")))
		{
			if (value.toString().length() == 0)
			{
				return false;
			}
		}
		return true;
	}
	
	public static Object[] getSelectOrDeleteBindObjects(Object dataKeyInfo)
	{
		String tableName = getTableName(dataKeyInfo);

		KeyInfo keyInfo = null;
		if (dataKeyInfo instanceof DataInfo)
		{
			keyInfo = getKeyInfo(dataKeyInfo);
		}
		else if (dataKeyInfo instanceof KeyInfo)
		{
			keyInfo = (KeyInfo) dataKeyInfo;
		}

		List<Object> objectValue = new ArrayList<Object>();
		List<ObjectAttributeDef> ObjectAttributeDefs =
				greenFrameServiceProxy.getObjectAttributeMap().getAttributeNames(tableName,
					ObjectAttributeMap.Standard_Type);
		for (int i = 0 ; i < ObjectAttributeDefs.size() ; i++ )
		{
			ObjectAttributeDef attributeDef = ObjectAttributeDefs.get(i);
			if (attributeDef.getPrimaryKeyFlag().equalsIgnoreCase("y"))
			{
				objectValue.add(ObjectUtil.getFieldValue(keyInfo, attributeDef.getAttributeName()));
			}
			else
			{
				if (dataKeyInfo instanceof KeyInfo)
					break;
			}
		}
		return objectValue.toArray();
	}
	
	public static String getTableName(Object dataKeyInfo)
	{
		String tableName = dataKeyInfo.getClass().getSimpleName();
		if (dataKeyInfo instanceof KeyInfo)
		{
			tableName = tableName.substring(0, tableName.length() - 3);
		}
		return tableName;
	}
	
	public static KeyInfo getKeyInfo(Object dataInfo)
	{
		if (dataInfo instanceof DataInfo)
		{
			try
			{
				return ((DataInfo) dataInfo).getKey();
			} catch (Exception e)
			{
				log.warn(e, e);
			}
			return null;
		}
		else if (dataInfo instanceof KeyInfo)
			return (KeyInfo) dataInfo;
		else
			return null;
	}
	
	public static Object[] getUpdateBindObjects(Object dataKeyInfo) throws greenFrameErrorSignal
	{
		String tableName = getTableName(dataKeyInfo);
		
		String attributeValue = "";

		KeyInfo keyInfo = null;
		if (dataKeyInfo instanceof DataInfo)
		{
			keyInfo = getKeyInfo(dataKeyInfo);
		}
		else if (dataKeyInfo instanceof KeyInfo)
		{
			keyInfo = (KeyInfo) dataKeyInfo;
		}

		List<Object> objectValue = new ArrayList<Object>();
		List<Object> objectPrimaryValue = new ArrayList<Object>();

		List<ObjectAttributeDef> ObjectAttributeDefs =
				greenFrameServiceProxy.getObjectAttributeMap().getAttributeNames(tableName,
					ObjectAttributeMap.Standard_Type);
		for (int i = 0 ; i < ObjectAttributeDefs.size() ; i++ )
		{
			 ObjectAttributeDef attributeDef = ObjectAttributeDefs.get(i);
			
			if (attributeDef.getPrimaryKeyFlag().equalsIgnoreCase("y"))
			{
				objectPrimaryValue.add(ObjectUtil.getFieldValue(keyInfo, attributeDef.getAttributeName()));
				attributeValue += ObjectUtil.getFieldValue(keyInfo, attributeDef.getAttributeName()) + ", ";
			}
			else
			{
				if (checkNotNullValue(dataKeyInfo, attributeDef, null))
				{
					objectValue.add(ObjectUtil.getFieldValue(dataKeyInfo, attributeDef.getAttributeName()));
					attributeValue += ObjectUtil.getFieldValue(dataKeyInfo, attributeDef.getAttributeName()) + ", ";
				}
			}
		}

		ObjectAttributeDefs =
				greenFrameServiceProxy.getObjectAttributeMap().getAttributeNames(tableName,
					ObjectAttributeMap.ExtendedC_Type);
		if (ObjectAttributeDefs != null && ObjectAttributeDefs.size() > 0)
		{
			Map<String, String> udfs = ObjectUtil.getUdfsValue(dataKeyInfo);
			for (int i = 0 ; i < ObjectAttributeDefs.size() ; i++ )
			{
				ObjectAttributeDef attributeDef = ObjectAttributeDefs.get(i);
				
				if (checkNotNullValue(dataKeyInfo, attributeDef, udfs))
				{
					objectValue.add(getValueDataType(attributeDef, (String) ObjectUtil.getUdfFieldValue(dataKeyInfo,
						attributeDef.getAttributeName())));
					attributeValue += (String) ObjectUtil.getUdfFieldValue(dataKeyInfo, attributeDef.getAttributeName()) + ", ";;
				}
			}
		}
		log.debug("Attribute");
		log.debug(attributeValue.substring(0, attributeValue.length()-2));
		
		for (int i = 0; i < objectPrimaryValue.size(); i++)
		{
			objectValue.add(objectPrimaryValue.get(i));
		}
		return objectValue.toArray();
	}
	
	public static Object getValueDataType(ObjectAttributeDef objectAttributeDef, String value)
	{
		if (objectAttributeDef.getDataType().equalsIgnoreCase("String"))
			return value;
		else if (objectAttributeDef.getDataType().equalsIgnoreCase("Double"))
			return Double.parseDouble(value);
		else if (objectAttributeDef.getDataType().equalsIgnoreCase("Long"))
		{
			int index = StringUtils.indexOf(value, ".");
			if (index >= 0)
				value = StringUtils.substring(value, 0, index);
			return Long.parseLong(value);
		}
		else if (objectAttributeDef.getDataType().equalsIgnoreCase("TimeStamp"))
			return TimeStampUtil.getTimestamp(value);
		return "";
	}
	
	public static Object[] getInsertBindObjects(Object dataKeyInfo) throws greenFrameErrorSignal
	{
		String tableName = getTableName(dataKeyInfo);

		KeyInfo keyInfo = null;
		if (dataKeyInfo instanceof DataInfo)
		{
			keyInfo = getKeyInfo(dataKeyInfo);
		}
		else if (dataKeyInfo instanceof KeyInfo)
		{
			keyInfo = (KeyInfo) dataKeyInfo;
			throw new greenFrameErrorSignal(ErrorSignal.InvalidArgumentSignal, dataKeyInfo.getClass().getSimpleName());
		}

		List<Object> objectValue = new ArrayList<Object>();

		List<ObjectAttributeDef> ObjectAttributeDefs =
				greenFrameServiceProxy.getObjectAttributeMap().getAttributeNames(tableName,
					ObjectAttributeMap.Standard_Type);
		for ( int i = 0 ; i < ObjectAttributeDefs.size() ; i++)
		{
			ObjectAttributeDef attributeDef = ObjectAttributeDefs.get(i);
			
			if (attributeDef.getAttributeName().equalsIgnoreCase("systemtime")
				&& attributeDef.getDataType().equalsIgnoreCase("timestamp"))
				continue;

			if (attributeDef.getPrimaryKeyFlag().equalsIgnoreCase("y"))
			{
				Object value = ObjectUtil.getFieldValue(keyInfo, attributeDef.getAttributeName());
				if (value == null)
					throw new greenFrameErrorSignal(ErrorSignal.InvalidArgumentSignal, String.format(
						"Input Object[%s] is not have attribute(%s).", ObjectUtil.getString(keyInfo),
						attributeDef.getAttributeName()));
				else
					objectValue.add(value);
			}
			else
			{
				if (checkNotNullValue(dataKeyInfo, attributeDef, null))
					objectValue.add(ObjectUtil.getFieldValue(dataKeyInfo, attributeDef.getAttributeName()));
				else if (StringUtils.isNotEmpty(attributeDef.getDefaultValue()))
					objectValue.add(getValueDataType(attributeDef, attributeDef.getDefaultValue()));
			}
		}

		ObjectAttributeDefs =
				greenFrameServiceProxy.getObjectAttributeMap().getAttributeNames(tableName,
					ObjectAttributeMap.ExtendedC_Type);
		if (ObjectAttributeDefs != null && ObjectAttributeDefs.size() > 0)
		{
			Map<String, String> udfs = ObjectUtil.getUdfsValue(dataKeyInfo);
			for (int i = 0 ; i < ObjectAttributeDefs.size() ; i++)
			{
				ObjectAttributeDef attributeDef = ObjectAttributeDefs.get(i);
				
				if (checkNotNullValue(dataKeyInfo, attributeDef, udfs))
				{
					objectValue.add(getValueDataType(attributeDef, (String) ObjectUtil.getUdfFieldValue(dataKeyInfo,
						attributeDef.getAttributeName())));
				}
				else if (StringUtils.isNotEmpty(attributeDef.getDefaultValue()))
					objectValue.add(getValueDataType(attributeDef, attributeDef.getDefaultValue()));
			}
		}
		return objectValue.toArray();
	}
	
	public static List<String> getPositionList(Lot lotData) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		List<String> sProductList = new ArrayList<String>();
		List<Product> prodList = MESProductServiceProxy.getProductServiceUtil().getProductListByLotName(lotData.getKey().getLotName());
		
		for (Product prod : prodList)
		{
			sProductList.add(Long.toString(prod.getPosition()));
		}
		
		return sProductList;
	}
	
	private static String alarmSmsText(String alarmCode,String alarmType,String message)
	{
		StringBuilder smsMessage = new StringBuilder();
		smsMessage.append("\n");
		smsMessage.append("AlarmCode: ");
		smsMessage.append(alarmCode);
		smsMessage.append("\n");
		smsMessage.append("AlarmType: ");
		smsMessage.append(alarmType);
		smsMessage.append("\n");
		smsMessage.append("message: ");
		smsMessage.append(message);
		smsMessage.append("\n");


		return smsMessage.toString();
	}
	
	public static  void  addAlarmemailActionHistory(String alarmCode,String alarmType,String message,String emailAction)throws CustomException 
	{
		
		String timeKey=TimeStampUtil.getCurrentTime(TimeStampUtil.FORMAT_TIMEKEY) ;
		String alarmTimekey=TimeStampUtil.getCurrentTime(TimeStampUtil.FORMAT_TIMEKEY) ;
		String sql = "";

		
		Map<String, Object> bindMap = new HashMap<String, Object>();
		sql = " INSERT INTO ALARMACTIONHISTORY "
		+ " ( ALARMID, ALARMTIMEKEY, TIMEKEY, MESSAGELOG, EMAILACTION,ALARMTYPE) " + " VALUES "
		+ " (  :ALARMID, :ALARMTIMEKEY, :TIMEKEY, :MESSAGELOG, :EMAILACTION,:ALARMTYPE  )";
		
		bindMap.put("ALARMID", alarmCode);
		bindMap.put("ALARMTYPE",alarmType);
		bindMap.put("MESSAGELOG", message);
		bindMap.put("EMAILACTION", emailAction);
		bindMap.put("TIMEKEY", timeKey);
		bindMap.put("ALARMTIMEKEY", alarmTimekey);
		GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
	}
	
	public static void PortDownSendEmailByMachineGroup(String alarmCode, String alarmType, String machineName,String portName,String portStateName,String message) throws CustomException
	{
		log.info("PortDownSendEmailByMachineGroup start.");
		String[] mailList = getEmailList(alarmCode, alarmType, machineName, "");

		if (mailList != null && mailList.length > 0)
		{
			try
			{
				GenericServiceProxy.getMailSerivce().postMail(mailList, machineName + "-" + portName + "-PortDownReport-重要&紧急", message);
			}
			catch (Exception e)
			{
				log.error("Failed to send mail.");
				return;
			}
		}
		else
		{
			log.info("[PortDownSendEmailByMachineGroup] Recipients information is empty.");
			return;
		}

		log.info("PortDownSendEmailByMachineGroup End.");
	}
	
//  SMS not used.
//	public static void sendPortDownSms(String alarmCode,String alarmType,String eventName,String machineName,  String portName,String portStateName)
//	{
//		
//		List<String> emailList = getPortDownEmailList(alarmCode, alarmType,machineName);
//		
//		if (emailList.size() > 0)
//		{
//			log.info("sendPortDownSms start");
//			StringBuilder smsMessage = new StringBuilder();
//			smsMessage.append("\n");
//			smsMessage.append("EventName: ");
//			smsMessage.append(eventName);
//			smsMessage.append("\n");
//			smsMessage.append("MachineName: ");
//			smsMessage.append(machineName);
//			smsMessage.append("\n");
//			smsMessage.append("PortName: ");
//			smsMessage.append(portName);
//			smsMessage.append("\n");
//			smsMessage.append("PortState: ");
//			smsMessage.append(portStateName);
//			smsMessage.append("\n");
//			smsMessage.append("Message: ");
//			smsMessage.append("当前Port已经宕机，请尽快处理!!!");
//						
//			// Send SMS
//			GenericServiceProxy.getSMSInterface().AlarmSmsSend(alarmCode, alarmType,smsMessage.toString());
//			log.info("sendPortDownSms end");
//		}
//	}
	
	public static void maskBatchJobCancelSendAlarmEmail(String alarmCode, String alarmType, String message,String machine,EventInfo eventInfo,String carrierName,String batchJobName,String transaction,String cancelMessage) throws CustomException
	{
		log.info("maskBatchJobCancelSendAlarmEmail Start");

		String[] mailList = getEmailList(alarmType);
		
		if (mailList != null && mailList.length > 0)
		{
			try
			{
				GenericServiceProxy.getMailSerivce().postMail(mailList, machine + "MaskBatchJobCancelAlarmReport-重要&紧急", message);
			}
			catch (Exception ex)
			{
				log.error("Failed to send mail.");
				return;
			}
		}
		else
		{
			log.info("[maskBatchJobCancelSendAlarmEmail] Recipients information is empty.");
			return ;
		}

		log.info("maskBatchJobCancelSendAlarmEmail End");
	}
	
//  SMS not used.
//	public static void maskBatchJobCancelSendSms(String alarmCode, String alarmType,EventInfo eventInfo,String carrierName,String batchJobName,String transaction,String cancelMessage) throws CustomException
//	{
//		log.info("maskBatchJobCancelSendSms Start");
//		// Set SMS Texts
//		StringBuilder smsMessage = new StringBuilder();
//		smsMessage.append("\n");
//		smsMessage.append("EventName: ");
//		smsMessage.append("\n");
//		smsMessage.append(eventInfo.getEventComment());
//		smsMessage.append("\n");
//		smsMessage.append("MaskSTKName: ");
//		smsMessage.append(eventInfo.getEventUser());
//		smsMessage.append("\n");
//		smsMessage.append("CST ID: ");
//		smsMessage.append(carrierName);
//		smsMessage.append("\n");
//		smsMessage.append("BatchJobName: ");
//		smsMessage.append(batchJobName);
//		smsMessage.append("\n");
//		smsMessage.append("Transaction: ");
//		smsMessage.append(transaction);
//		smsMessage.append("\n");
//		smsMessage.append("CancelReason: ");
//		smsMessage.append(cancelMessage);
//		
//		// Send SMS
//		GenericServiceProxy.getSMSInterface().AlarmSmsSend(alarmCode, "MCS",  smsMessage.toString());
//		
//		log.info("maskBatchJobCancelSendSms End");
//	}
	
	public static Double doubleAdd(Double oriQty, Double addQty)
	{
		BigDecimal qty = BigDecimal.valueOf(oriQty);
		BigDecimal dQty = BigDecimal.valueOf(addQty);
		BigDecimal resultQty = qty.add(dQty);
		
		return resultQty.doubleValue();
	}
	
	public static Double doubleSubtract(Double oriQty, Double subQty)
	{
		BigDecimal qty = BigDecimal.valueOf(oriQty);
		BigDecimal dQty = BigDecimal.valueOf(subQty);
		BigDecimal resultQty = qty.subtract(dQty);
		
		return resultQty.doubleValue();
	}
	
	public static void sendACPDEmail(String alarmCode, String alarmType, String factoryName, String machineName, String unitName, String message) throws CustomException
	{
		String[] mailList = getEmailList(alarmCode, alarmType, machineName, unitName);

		if (mailList != null && mailList.length > 0)
		{
			sendEmail(mailList, alarmType + "AlarmReport", message);
		}
		else
		{
			log.info("[sendACPDEmail] Recipients information is empty.");
		}
	}
	
	public static String judgeLotGradeByProducGrade(String productGrade)
	{
		String lotJudge = "";

		if (productGrade.equals("N"))
		{
			lotJudge = "N";
		}
		else if (productGrade.equals("R"))
		{
			lotJudge = "R";
		}
		else if (productGrade.equals("P"))
		{
			lotJudge = "P";
		}
		else if (productGrade.equals("S"))
		{
			lotJudge = "S";
		}
		else if (productGrade.equals("G"))
		{
			lotJudge = "G";
		}

		return lotJudge;
	}
	
	public static void sendabnormalEmail(String alarmCode, String alarmType, String message) throws CustomException
	{
		
		log.info("sendabnormalEmail Start");

		String[] mailList = getEmailList(alarmType);

		if (mailList != null && mailList.length > 0)
		{
			try
			{
				GenericServiceProxy.getMailSerivce().postMail(mailList, alarmType + "AlarmReport", message);
			}
			catch (Exception ex)
			{
				log.error("Failed to send mail.");
				return;
			}
		}
		else
		{
			log.info("[sendabnormalEmail] Recipients information is empty.");
			return;
		}

		log.info("sendabnormalEmail End");
	}

	public static List<Element> makeElementProdListForDummy(List<Element> productList) throws CustomException
	{
		log.info("Filter DummyGlass Product");
		List<Element> elementProductList = new ArrayList<Element>();

		for (Element product : productList)
		{
			String productName = SMessageUtil.getChildText(product, "PRODUCTNAME", true);

			Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
			String dummyGlassFlag = productData.getUdfs().get("DUMMYGLASSFLAG");

			if (!StringUtils.equals(dummyGlassFlag, "Y"))
			{
				elementProductList.add(product);
			}
			else
			{
				log.info("Product : " + productName + " / DummyGlassFlag = Y");
			}
		}

		return elementProductList;
	}
	
	public static List<Element> makeElementProdListForFirstGlass(List<Element> productList,String lotName) throws CustomException
	{
		log.info("Filter ChindLot Product");
		List<Element> elementProductList = new ArrayList<Element>();

		for (Element product : productList)
		{
			String productName = SMessageUtil.getChildText(product, "PRODUCTNAME", true);

			Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);

			if (StringUtils.equals(productData.getLotName(), lotName))
			{
				elementProductList.add(product);
			}
			else
			{
				log.info("Product : " + productName + " / is ChindLot");
			}
		}

		return elementProductList;
	}
	
	public static List<Product> makeProdListForDummy(List<Product> productList) throws CustomException
	{
		log.info("Filter DummyGlass Product");
		List<Product> newProductList = new ArrayList<Product>();

		for (Product product : productList)
		{
			String dummyGlassFlag = product.getUdfs().get("DUMMYGLASSFLAG");

			if (!StringUtils.equals(dummyGlassFlag, "Y"))
			{
				newProductList.add(product);
			}
			else
			{
				log.info("Product : " + product.getKey().getProductName() + " / DummyGlassFlag = Y");
			}
		}

		return newProductList;
	}
	
	public static List<Map<String, Object>> getOperationLevelList(String factoryName, String processFlowName, String processFlowVersion)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT LEVEL LV, ");
		sql.append("       N.FACTORYNAME, ");
		sql.append("       N.NODEATTRIBUTE1 PROCESSOPERATIONNAME, ");
		sql.append("       N.NODEATTRIBUTE2 PROCESSOPERATIONVERSION, ");
		sql.append("       N.PROCESSFLOWNAME, ");
		sql.append("       N.PROCESSFLOWVERSION, ");
		sql.append("       N.NODEID ");
		sql.append("  FROM ARC A, NODE N, PROCESSFLOW PF ");
		sql.append(" WHERE 1 = 1 ");
		sql.append("   AND N.NODETYPE = 'ProcessOperation' ");
		sql.append("   AND N.FACTORYNAME = PF.FACTORYNAME ");
		sql.append("   AND N.PROCESSFLOWNAME = PF.PROCESSFLOWNAME ");
		sql.append("   AND N.PROCESSFLOWVERSION = PF.PROCESSFLOWVERSION ");
		sql.append("   AND N.PROCESSFLOWNAME = A.PROCESSFLOWNAME ");
		sql.append("   AND N.PROCESSFLOWVERSION = A.PROCESSFLOWVERSION ");
		sql.append("   AND A.FROMNODEID = N.NODEID ");
		sql.append("   AND N.FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND PF.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("   AND PF.PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
		sql.append("START WITH N.NODETYPE = 'Start' ");
		sql.append("CONNECT BY NOCYCLE A.FROMNODEID = PRIOR A.TONODEID ");
		sql.append("               AND A.FACTORYNAME = :FACTORYNAME ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("FACTORYNAME", factoryName);
		args.put("PROCESSFLOWNAME", processFlowName);
		args.put("PROCESSFLOWVERSION", processFlowVersion);

		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		return sqlResult;
	}
	
	public static int getNodeStackCount(String str, char c)
	{
		int count = 0;
		
		for (int i = 0; i < str.length(); i++)
		{
			if (str.charAt(i) == c)
				count++;
		}
		
		return count;
	}
	
	public static void resultSendToMDM(String mdID, String mdCode, String mdDesc, String mdTypeCode, EventInfo eventInfo, String serviceName, String errorMessage, String status)
	{
		String requestDate = eventInfo.getEventTime().toString();
		String requestSystem = "MES";
		
		List<Map<String, String>> MDMReportList = new ArrayList<Map<String, String>>();
		Map<String, String> MDMInfo = new HashMap<>();
		
		MDMInfo.put("mdId", mdID);
		MDMInfo.put("mdCode", mdCode);
		MDMInfo.put("mdDescription", mdDesc);
		MDMInfo.put("mdTypeCode", mdTypeCode);
		MDMInfo.put("requestDate", requestDate);
		MDMInfo.put("serverName", serviceName);
		MDMInfo.put("requestSystem", requestSystem);
		MDMInfo.put("errorMessage", errorMessage);
		if(StringUtil.equals(status, "Y")) status="S";
		else status="E";
		MDMInfo.put("serviceStatus", status);
		MDMReportList.add(MDMInfo);
		
		//Send
		try
		{
			ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().MDMResultSend(eventInfo, MDMReportList, 1);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	//20210/1/22 jinlj add start
	public static void sendAlarmEmail(String userGroup, String message, ByteArrayOutputStream baos) {
		// TODO Auto-generated method stub
		try 
		{
			List<String> emailList = new ArrayList<String>();
			StringBuffer sql1 = new StringBuffer();
			sql1.append(
					"SELECT B.* FROM CT_ALARMUSERGROUP A, CT_ALARMUSER B WHERE A.USERID = B.USERID  AND A.ALARMGROUPNAME = :ALARMGROUPNAME");
			Map<String, Object> args1 = new HashMap<String, Object>();
			args1.put("ALARMGROUPNAME", userGroup);
			List<Map<String, Object>> sqlResult1 = GenericServiceProxy.getSqlMesTemplate().queryForList(sql1.toString(), args1);
			if (sqlResult1.size() > 0) 
			{
				for (Map<String, Object> user : sqlResult1)
				{
					String eMail = ConvertUtil.getMapValueByName(user, "EMAIL");
					emailList.add(eMail);
				}
			}
			if (emailList.size() > 0)
			{
				try
				{
					EMailInterfaceExcel ei = new EMailInterfaceExcel("mail.visionox.com", "587", "1000", "1000");
					ei.postMail(emailList, "OperationChangedReport ", message, "OperationChange", baos, "V3MES", "V3MES@visionox.com", "V3MES", "vis@2019");					
				}
				catch (Exception e)
				{
					log.error("Failed to send mail.");
				}
			}
		}
		catch (Exception e)
		{
			log.error("Failed to send mailExcel.");
			log.error("Failed to send mailExcel.");
			String AlarmCode="Failedmail";
		    String AlarmType="MES";
			String smsMessage = alarmSmsText(AlarmCode,AlarmType,message);
			String emailAction="N";

		}
		
	}
	//20210/1/22 jinlj add end
	
	public static String getColumnByEnumNameAndEnumValue( String enumName,String enumValue,String columnName )
	{
		String columnValue="";
		String sql = "SELECT * FROM ENUMDEFVALUE WHERE ENUMNAME = :ENUMNAME AND ENUMVALUE=:ENUMVALUE";
		
		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("ENUMNAME", enumName);
		bindMap.put("ENUMVALUE", enumValue);
		
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		
		if(sqlResult.size() > 0){
			columnValue = sqlResult.get(0).get(columnName).toString();
		}
		
		return columnValue;
	}
	
}