package kr.co.aim.messolution.extended.object.management;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.greenframe.exception.ErrorSignal;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.exception.greenFrameErrorSignal;
import kr.co.aim.greenframe.orm.SQLLogUtil;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;
import kr.co.aim.greenframe.orm.support.OrmStandardEngineUtil;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greentrack.generic.GenericServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

public class CTORMServiceNoCT<DATA> 
{
	public ArrayList<Long> StringToLong (String[] slist)
	throws NumberFormatException
	{
		try 
		{
			ArrayList<Long> list = new ArrayList<Long>();
			for (String s : slist)
			{
				Long l = Long.parseLong(s); 
				list.add(l);
			}
			return list;
		}
		catch (NumberFormatException n)
		{
			throw n;
		}
	}
	
	public List<DATA> select(String condition, Object[] bindSet, Class clazz)
		throws greenFrameDBErrorSignal
	{
		String tableName = CTORMUtil.getTableNameByClassNameNoCT(clazz);
		
		String sql = CTORMUtil.getSql(clazz, tableName);
		
		if (StringUtils.isNotEmpty(condition))
		{
			sql = CTORMUtil.getConditionSql(sql, condition);
		}
		
		//query
		List resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindSet);
		
		//case not found
		if (resultList.size() == 0)
		{
			throw new greenFrameDBErrorSignal(ErrorSignal.NotFoundSignal, ObjectUtil.getString(bindSet),
						SQLLogUtil.getLogFormatSqlStatement(sql, bindSet, CTORMUtil.getLogger()));
		}
		
		//refine result data
		Object result = ormExecute(CTORMUtil.createDataInfo(clazz), resultList);
		
		//return
		if (result instanceof List)
		{
			return (List<DATA>) result;
		}
		else if (result instanceof UdfAccessor)
		{
			List<DATA> resultSet = new ArrayList<DATA>();
			resultSet.add((DATA) result);
			
			return resultSet;
		}
		else if (clazz.isInstance(result))
		{
			List<DATA> resultSet = new ArrayList<DATA>();
			resultSet.add((DATA) result);
			
			return resultSet;
		}
		else
		{
			throw new greenFrameDBErrorSignal(ErrorSignal.CouldNotMatchData, ObjectUtil.getString(bindSet),
						SQLLogUtil.getLogFormatSqlStatement(sql, bindSet, CTORMUtil.getLogger()), condition);
		}
	}
	
	public DATA selectByKey(Class clazz, boolean isLock, Object... keyValue)
		throws greenFrameDBErrorSignal
	{
		//create base already
		Object dataInfo = CTORMUtil.createDataInfo(clazz);
		
		String tableName = CTORMUtil.getTableNameByClassNameNoCT(clazz);
		
		String sql = CTORMUtil.getSql(clazz, tableName);
		sql = CTORMUtil.getKeySql(sql, dataInfo);
		
		//suffix for lock
		if (isLock)
			sql = new StringBuffer(sql).append(" FOR UPDATE").toString();
		
		String param = CommonUtil.toStringFromCollection(keyValue);
		
		if (!CTORMUtil.validateKeyParam(dataInfo, keyValue).isEmpty())
			throw new greenFrameDBErrorSignal(ErrorSignal.NullPointKeySignal, param,
						SQLLogUtil.getLogFormatSqlStatement(sql, param, CTORMUtil.getLogger()));
		
		
		//query with just one
		List resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, keyValue);
		
		//case not found
		if (resultList.size() == 0)
		{
			throw new greenFrameDBErrorSignal(ErrorSignal.NotFoundSignal, param,
						SQLLogUtil.getLogFormatSqlStatement(sql, param, CTORMUtil.getLogger()));
		}
		
		//refine result data
		Object result = ormExecute(CTORMUtil.createDataInfo(clazz), resultList);
		
		//return
		if (result instanceof UdfAccessor)
		{
			return (DATA) result;
		}
		else
		{
			throw new greenFrameDBErrorSignal(ErrorSignal.CouldNotMatchData, param,
						SQLLogUtil.getLogFormatSqlStatement(sql, param, CTORMUtil.getLogger()), keyValue.toString());
		}
	}
	
	public void update(DATA dataInfo) throws greenFrameDBErrorSignal
	{
		String tableName = CTORMUtil.getTableNameByClassNameNoCT(dataInfo.getClass());
		
		String sql = CTORMUtil.getUpdateSql(dataInfo.getClass(), tableName);
		
		//generate bind parameter
		//by sequence
		List<Object> keySet = CTORMUtil.makeKeyParam(dataInfo);
		String param = CommonUtil.toStringFromCollection(keySet.toArray());
		if (!CTORMUtil.validateKeyParam(dataInfo, keySet.toArray()).isEmpty())
			throw new greenFrameDBErrorSignal(ErrorSignal.NullPointKeySignal, param,
						SQLLogUtil.getLogFormatSqlStatement(sql, param, CTORMUtil.getLogger()));
		
		List<Object> bindSet = CTORMUtil.makeNonKeyParam(dataInfo);
		bindSet.addAll(keySet);
		param = CommonUtil.toStringFromCollection(bindSet.toArray());
		
		//query with just one
		int result = GenericServiceProxy.getSqlMesTemplate().update(sql, bindSet.toArray());
		
		//case not found
		if (result == 0)
		{
			throw new greenFrameDBErrorSignal(ErrorSignal.NotFoundSignal, param,
						SQLLogUtil.getLogFormatSqlStatement(sql, param, CTORMUtil.getLogger()));
		}
	}
	
	public void delete(DATA dataInfo) throws greenFrameDBErrorSignal
	{
		List<Object> keySet = CTORMUtil.makeKeyParam(dataInfo);
		
		delete(dataInfo.getClass(), keySet.toArray());
	}
	
	public void delete(Class clazz, Object... keyValue) throws greenFrameDBErrorSignal
	{
		//create base already
		Object dataInfo = CTORMUtil.createDataInfo(clazz);
		
		String tableName = CTORMUtil.getTableNameByClassNameNoCT(dataInfo.getClass());
		
		String sql = CTORMUtil.getDeleteSql(dataInfo.getClass(), tableName);
		
		String param = CommonUtil.toStringFromCollection(keyValue);
		
		if (!CTORMUtil.validateKeyParam(dataInfo, keyValue).isEmpty())
			throw new greenFrameDBErrorSignal(ErrorSignal.NullPointKeySignal, param,
						SQLLogUtil.getLogFormatSqlStatement(sql, param, CTORMUtil.getLogger()));
		
		//query with just one
		int result = GenericServiceProxy.getSqlMesTemplate().update(sql, keyValue);
		
		//case not found
		if (result == 0)
		{
			throw new greenFrameDBErrorSignal(ErrorSignal.NotFoundSignal, param,
						SQLLogUtil.getLogFormatSqlStatement(sql, param, CTORMUtil.getLogger()));
		}
	}
	
	public void insert(DATA dataInfo) throws greenFrameDBErrorSignal
	{
		String tableName = CTORMUtil.getTableNameByClassNameNoCT(dataInfo.getClass());
		
		String sql = CTORMUtil.getInsertSql(dataInfo.getClass(), tableName);
		
		//generate bind parameter
		//by sequence
		List<Object> keySet = CTORMUtil.makeKeyParam(dataInfo);
		String param = CommonUtil.toStringFromCollection(keySet.toArray());
		if (!CTORMUtil.validateKeyParam(dataInfo, keySet.toArray()).isEmpty())
			throw new greenFrameDBErrorSignal(ErrorSignal.NullPointKeySignal, param,
						SQLLogUtil.getLogFormatSqlStatement(sql, param, CTORMUtil.getLogger()));
		
		List<Object> bindSet = CTORMUtil.makeNonKeyParam(dataInfo);
		keySet.addAll(bindSet);
		param = CommonUtil.toStringFromCollection(bindSet.toArray());
		
		//query with just one
		int result = GenericServiceProxy.getSqlMesTemplate().update(sql, keySet.toArray());
		
		//case not found
		if (result == 0)
		{
			throw new greenFrameDBErrorSignal(ErrorSignal.InvalidQueryState, param,
						SQLLogUtil.getLogFormatSqlStatement(sql, param, CTORMUtil.getLogger()));
		}
	}
	
	public static Object ormExecute(Object dataObject, List<ListOrderedMap> resultList)
		throws greenFrameErrorSignal
	{
		Object dataInfo = null;
		List<Object> resultDataInfos = null;
		
		for (int j = 0; j < resultList.size(); j++)
		{
			//with pure data
			dataInfo = CTORMUtil.createDataInfo(dataObject.getClass());
			setProperties(dataInfo, (Map<String, Object>) resultList.get(j));
			
			if (resultList.size() > 1)
			{
				if (resultDataInfos == null)
					resultDataInfos = new ArrayList<Object>();
				resultDataInfos.add(dataInfo);
			}
		}
		
		resultList.clear();
		
		if (resultDataInfos != null)
			return resultDataInfos;
		else
			return dataInfo;
	}
	
	private static void setProperties(Object dataInfo, Map<String, Object> dataMap)
	{
		for (Field column : dataInfo.getClass().getDeclaredFields())
		{
			if (column.isAnnotationPresent(CTORMTemplate.class))
			{
				CTORMTemplate annotation = column.getAnnotation(CTORMTemplate.class);
				
				String name = annotation.name();
				String type = annotation.type();
				String dataType = annotation.dataType();
				String initial = annotation.initial();
				
				Object resultValue = dataMap.get(name);
				
				//class to object converting
				if (resultValue != null)
					ObjectUtil.setFieldValue(dataInfo, name, resultValue);
				else if (resultValue == null && dataType.equalsIgnoreCase("timestamp"))
					ObjectUtil.setFieldValue(dataInfo, name, null);
			}
		}
	}
		
	public void addHistory(EventInfo eventInfo, String historyEntityName, DATA dataInfo, Log logger)
	{
		try
		{
			if (StringUtil.isEmpty(historyEntityName))
				return;
			
			insertHistory(eventInfo, historyEntityName, dataInfo);
		}
		catch (greenFrameDBErrorSignal ne)
		{
			if (logger.isDebugEnabled()) logger.debug(ne);
		}
		catch (Exception ex)
		{
			if (logger.isDebugEnabled()) logger.debug(ex);
		}
	}
	
	private void insertHistory(EventInfo eventInfo, String historyEntityName, DATA dataInfo)
		throws greenFrameDBErrorSignal
	{
		List<Object> bindList = new ArrayList<Object>();
		List<String> attrList = new ArrayList<String>();
		
		//make attribute list
		for (Field column : dataInfo.getClass().getDeclaredFields())
		{
			if (column.isAnnotationPresent(CTORMTemplate.class))
			{
				CTORMTemplate annotation = column.getAnnotation(CTORMTemplate.class);
				
				String name = annotation.name();
				String type = annotation.type();
				String dataType = annotation.dataType();
				String initial = annotation.initial();
				//mirror table must be consistent with data type
				String mirror = annotation.history();
				
				Object value = ObjectUtil.getFieldValue(dataInfo, column.getName());
				
				if (value == null
						|| initial.equalsIgnoreCase("never")
						|| mirror.equalsIgnoreCase(kr.co.aim.messolution.generic.GenericServiceProxy.getConstantMap().Flag_N))
					continue;
			
				if (StringUtil.isNotEmpty(mirror))
				{
					if (!StringUtil.equals(mirror, "N"))
					{
						attrList.add(mirror);
					}
				}
				else
				{
					attrList.add(name);
				}
				
				//set value, reflect all at current and assure sequence
				bindList.add(value);
			}
		}
		
		{//prerequisite for trace
			//who
			if(!StringUtil.equals(historyEntityName, "CT_RecipeParameterHistory"))
			{
				
				//when
				if(!StringUtil.equals(historyEntityName, "CT_RecipeHistory"))
				{
					attrList.add("eventTime");bindList.add(eventInfo.getEventTime());
				}
				//where(in mirror)
				//what
				
				//how, why
				attrList.add("eventComment");bindList.add(eventInfo.getEventComment());
			}
			
			//trace ID
			attrList.add("eventUser");bindList.add(eventInfo.getEventUser());
			attrList.add("eventName");bindList.add(eventInfo.getEventName());
			
			if (!StringUtil.equals(historyEntityName, "ProductIdleTimeHist"))
			{
				attrList.add("timeKey");
				bindList.add(StringUtil.isEmpty(eventInfo.getEventTimeKey())?TimeUtils.getCurrentEventTimeKey():eventInfo.getEventTimeKey());
			}
			
		}
		
		//query generation
		StringBuffer sql = new StringBuffer();
		{
			StringBuffer bindBuilder = new StringBuffer();
			
			sql.append("INSERT").append(" ").append("INTO").append(" ").append(historyEntityName).append(" ");
			sql.append("(");
			
			for (int idx=0;idx<attrList.size();idx++)
			{
				sql.append(attrList.get(idx));
				
				if (idx<attrList.size()-1)
					sql.append(",");
				
				//bind value is equal with size
				bindBuilder.append("?");
				if (idx<attrList.size()-1)
					bindBuilder.append(",");
			}
			
			sql.append(")");
			sql.append(" VALUES ").append("(").append(bindBuilder).append(")");
		}
		
		//radical validation
		List<Object> keySet = CTORMUtil.makeKeyParam(dataInfo);
		String param = CommonUtil.toStringFromCollection(keySet.toArray());
		if (!CTORMUtil.validateKeyParam(dataInfo, keySet.toArray()).isEmpty())
			throw new greenFrameDBErrorSignal(ErrorSignal.NullPointKeySignal, param,
						SQLLogUtil.getLogFormatSqlStatement(sql.toString(), param, CTORMUtil.getLogger()));
		
		//generate bind parameter
		//by sequence
		//only DDL as form of INSERT
		int result = GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), bindList.toArray());
		
		//case not found
		if (result == 0)
		{
			param = CommonUtil.toStringFromCollection(bindList.toArray());
			throw new greenFrameDBErrorSignal(ErrorSignal.InvalidQueryState, param,
						SQLLogUtil.getLogFormatSqlStatement(sql.toString(), param, CTORMUtil.getLogger()));
		}
	}
	
	public void addHistoryN(EventInfo eventInfo, String historyEntityName, DATA dataInfo, 
			DATA oldDataInfo, Log logger)
	{
		try
		{
			if (StringUtil.isEmpty(historyEntityName))
				return;
			
			insertHistoryN(eventInfo, historyEntityName, dataInfo ,oldDataInfo);
		}
		catch (greenFrameDBErrorSignal ne)
		{
			if (logger.isDebugEnabled()) logger.debug(ne);
		}
		catch (Exception ex)
		{
			if (logger.isDebugEnabled()) logger.debug(ex);
		}
	}
	
	private void insertHistoryN(EventInfo eventInfo, String historyEntityName, DATA dataInfo ,DATA oldDataInfo)
	throws greenFrameDBErrorSignal
	{
		List<Object> bindList = new ArrayList<Object>();
		List<String> attrList = new ArrayList<String>();
		
		//make attribute list
		for (Field column : dataInfo.getClass().getDeclaredFields())
		{
			if (column.isAnnotationPresent(CTORMTemplate.class))
			{
				CTORMTemplate annotation = column.getAnnotation(CTORMTemplate.class);
				
				String name = annotation.name();
				String type = annotation.type();
				String dataType = annotation.dataType();
				String initial = annotation.initial();
				//mirror table must be consistent with data type
				String mirror = annotation.history();
				
				Object value = ObjectUtil.getFieldValue(dataInfo, column.getName());
				Object oldvalue = ObjectUtil.getFieldValue(oldDataInfo,column.getName());
				
				if (value == null
						|| initial.equalsIgnoreCase("never")
						|| mirror.equalsIgnoreCase(kr.co.aim.messolution.generic.GenericServiceProxy.getConstantMap().Flag_N))
					continue;
			
				if (StringUtil.isNotEmpty(mirror))
				{
					attrList.add(name);bindList.add(value);
					attrList.add("old" + name);bindList.add(oldvalue);
					continue;
				}					
				else
					attrList.add(name);
				
				//set value, reflect all at current and assure sequence
				bindList.add(value);
			}
		}
		
		{//prerequisite for trace
		    //trace ID
			attrList.add("eventTime");bindList.add(eventInfo.getEventTime());
			attrList.add("eventUser");bindList.add(eventInfo.getEventUser());
			attrList.add("eventName");bindList.add(eventInfo.getEventName());
			attrList.add("eventComment");bindList.add(eventInfo.getEventComment());
			attrList.add("timeKey");
			bindList.add(StringUtil.isEmpty(eventInfo.getEventTimeKey())?TimeUtils.getCurrentEventTimeKey():eventInfo.getEventTimeKey());					
		}
		
		//query generation
		StringBuffer sql = new StringBuffer();
		{
			StringBuffer bindBuilder = new StringBuffer();
			
			sql.append("INSERT").append(" ").append("INTO").append(" ").append(historyEntityName).append(" ");
			sql.append("(");
			
			for (int idx=0;idx<attrList.size();idx++)
			{
				sql.append(attrList.get(idx));
				
				if (idx<attrList.size()-1)
					sql.append(",");
				
				//bind value is equal with size
				bindBuilder.append("?");
				if (idx<attrList.size()-1)
					bindBuilder.append(",");
			}
			
			sql.append(")");
			sql.append(" VALUES ").append("(").append(bindBuilder).append(")");
		}
		
		//radical validation
		List<Object> keySet = CTORMUtil.makeKeyParam(dataInfo);
		String param = CommonUtil.toStringFromCollection(keySet.toArray());
		if (!CTORMUtil.validateKeyParam(dataInfo, keySet.toArray()).isEmpty())
			throw new greenFrameDBErrorSignal(ErrorSignal.NullPointKeySignal, param,
						SQLLogUtil.getLogFormatSqlStatement(sql.toString(), param, CTORMUtil.getLogger()));
		
		//generate bind parameter
		//by sequence
		//only DDL as form of INSERT
		int result = GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), bindList.toArray());
		
		//case not found
		if (result == 0)
		{
			param = CommonUtil.toStringFromCollection(bindList.toArray());
			throw new greenFrameDBErrorSignal(ErrorSignal.InvalidQueryState, param,
						SQLLogUtil.getLogFormatSqlStatement(sql.toString(), param, CTORMUtil.getLogger()));
		}
	}
}
