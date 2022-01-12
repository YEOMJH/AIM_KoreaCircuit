package kr.co.aim.messolution.extended.object.management;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.exception.ErrorSignal;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.support.InvokeUtils;
import kr.co.aim.greentrack.generic.util.StringUtil;

public class CTORMUtil {

	private static Log logger = LogFactory.getLog(CTORMUtil.class);

	public static Log getLogger()
	{
		return logger;
	}

	public static final String servicePath = "kr.co.aim.messolution.extended.object.management.impl";

	public static Object createDataInfo(Class dataObject)
	{
		Object createdInfo = null;

		try
		{
			createdInfo = (Object) Class.forName(dataObject.getName()).newInstance();

		}
		catch (Exception e)
		{
			logger.warn(e, e);
		}

		if (createdInfo != null)
		{
			for (Field column : createdInfo.getClass().getDeclaredFields())
			{
				if (column.isAnnotationPresent(CTORMTemplate.class))
				{
					CTORMTemplate annotation = column.getAnnotation(CTORMTemplate.class);

					String name = annotation.name();
					String type = annotation.type();
					String dataType = annotation.dataType();
					String initial = annotation.initial();

					// specific data type setting
					if (dataType.equalsIgnoreCase("timestamp"))
						ObjectUtil.setFieldValue(createdInfo, name, null);
					else if (!initial.isEmpty())
						ObjectUtil.setFieldValue(createdInfo, name, initial);
				}
			}
		}

		return createdInfo;
	}

	public static String getTableNameByClassName(Class clazz)
	{
		String tableName = clazz.getSimpleName();

		tableName = new StringBuffer().append("CT").append("_").append(tableName).toString();

		return tableName;
	}

	public static String getTableNameByClassNameNoCT(Class clazz)
	{
		String tableName = clazz.getSimpleName();

		return tableName;
	}

	public static String getHistoryTableNameByClassName(Class clazz, boolean isBrief)
	{
		StringBuffer tableNameBuffer = new StringBuffer().append("CT").append("_").append(clazz.getSimpleName());

		if (isBrief)
			tableNameBuffer.append("Hist");
		else
			tableNameBuffer.append("History");

		return tableNameBuffer.toString();
	}

	public static String getConditionSql(String sql, String condition)
	{
		if (condition.toLowerCase().trim().startsWith("where "))
		{
			// sql = sql + " " + condition.trim();
			sql = new StringBuffer(sql).append(" ").append(condition.trim()).toString();
		}
		else
		{
			// sql = sql + " where " + condition;
			sql = new StringBuffer(sql).append(" WHERE ").append(condition).toString();
		}
		return sql;
	}

	public static String getKeySql(String sql, Object dataInfo)
	{
		StringBuffer sCondition = new StringBuffer(sql).append(" WHERE 1=1 ");

		if (dataInfo != null)
		{
			for (Field column : dataInfo.getClass().getDeclaredFields())
			{
				// only by annotation presentation
				if (column.isAnnotationPresent(CTORMTemplate.class))
				{
					CTORMTemplate annotation = column.getAnnotation(CTORMTemplate.class);

					String name = annotation.name();
					String type = annotation.type();

					if (type.equalsIgnoreCase("key"))
						sCondition.append(" AND ").append(name).append("=").append("?");
				}
			}
		}

		return sCondition.toString();
	}

	public static String getUpdateSql(Class dataInfo, String tableName)
	{
		StringBuffer sql = new StringBuffer();
		StringBuffer subSql = new StringBuffer(" ").append("WHERE 1=1").append(" ");

		sql.append("UPDATE").append(" ").append(tableName).append(" ").append("SET").append(" ");

		for (Field column : dataInfo.getDeclaredFields())
		{
			if (column.isAnnotationPresent(CTORMTemplate.class))
			{
				CTORMTemplate annotation = column.getAnnotation(CTORMTemplate.class);

				String name = annotation.name();
				String type = annotation.type();
				// String dataType = annotation.dataType();
				String initial = annotation.initial();

				// never key update
				// never null update
				if (!initial.equalsIgnoreCase("never"))
				{
					if (type.equalsIgnoreCase("key"))
						subSql.append(" AND ").append(name).append("=").append("?");
					else
						sql.append(name).append("=").append("?").append(",");
				}
			}
		}

		String refinedSql = StringUtil.removeEnd(sql.toString(), ",");

		sql = new StringBuffer(refinedSql).append(subSql.toString());

		return sql.toString();
	}

	public static String getAllUpdateSql(Class dataInfo, String tableName)
	{
		StringBuffer sql = new StringBuffer();
		StringBuffer subSql = new StringBuffer(" ").append("WHERE 1=1").append(" ");

		sql.append("UPDATE").append(" ").append(tableName).append(" ").append("SET").append(" ");

		for (Field column : dataInfo.getDeclaredFields())
		{
			if (column.isAnnotationPresent(CTORMTemplate.class))
			{
				CTORMTemplate annotation = column.getAnnotation(CTORMTemplate.class);

				String name = annotation.name();
				String type = annotation.type();
				// String dataType = annotation.dataType();
				String initial = annotation.initial();

				// never key update
				// never null update
				if (!initial.equalsIgnoreCase("never"))
				{
					if (type.equalsIgnoreCase("key"))
					{
						subSql.append(" AND ").append(name).append("=").append("?");
					}
					sql.append(name).append("=").append("?").append(",");
				}
			}
		}

		String refinedSql = StringUtil.removeEnd(sql.toString(), ",");

		sql = new StringBuffer(refinedSql).append(subSql.toString());

		return sql.toString();
	}

	public static String getDeleteSql(Class dataInfo, String tableName)
	{
		StringBuffer sql = new StringBuffer();
		StringBuffer subSql = new StringBuffer(" ").append("WHERE 1=1").append(" ");

		sql.append("DELETE").append(" ").append(tableName).append(" ");

		for (Field column : dataInfo.getDeclaredFields())
		{
			if (column.isAnnotationPresent(CTORMTemplate.class))
			{
				CTORMTemplate annotation = column.getAnnotation(CTORMTemplate.class);

				String name = annotation.name();
				String type = annotation.type();

				// never key update
				if (type.equalsIgnoreCase("key"))
					subSql.append(" AND ").append(name).append("=").append("?");
			}
		}

		String refinedSql = StringUtil.removeEnd(sql.toString(), ",");

		sql = new StringBuffer(refinedSql).append(subSql.toString());

		return sql.toString();
	}

	public static String getInsertSql(Class dataInfo, String tableName)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("INSERT").append(" ").append("INTO").append(" ").append(tableName).append(" ");

		StringBuilder attrBuilder = new StringBuilder("");
		StringBuilder valueBuilder = new StringBuilder("");

		for (Field column : dataInfo.getDeclaredFields())
		{
			if (column.isAnnotationPresent(CTORMTemplate.class))
			{
				CTORMTemplate annotation = column.getAnnotation(CTORMTemplate.class);

				String name = annotation.name();
				String type = annotation.type();
				String dataType = annotation.dataType();
				String initial = annotation.initial();

				if (initial.equalsIgnoreCase("never"))
					continue;

				{// column generation
					if (!attrBuilder.toString().isEmpty())
						attrBuilder.append(",");

					if (IsSQLKeyWord(name))
						attrBuilder.append("\"" + name.toUpperCase() + "\"");
					else
						attrBuilder.append(name);
				}

				{// value generation
					if (!valueBuilder.toString().isEmpty())
						valueBuilder.append(",");

					if ((dataType.equalsIgnoreCase("timestamp") || dataType.equalsIgnoreCase("systemtime")) && initial.equalsIgnoreCase("current"))
					{
						valueBuilder.append("SYSDATE");
					}
					else
					{
						valueBuilder.append("?");
					}
				}

			}
		}

		sql.append("(").append(attrBuilder).append(")").append(" VALUES ").append("(").append(valueBuilder).append(")");

		return sql.toString();
	}

	public static boolean IsSQLKeyWord(String name)
	{
		return SQLKeyWord.validate(name);
	}
	
	public enum SQLKeyWord 
	{
		MODE;
		
		public static boolean validate(String inputArg)
		{
			if (inputArg == null || StringUtil.isEmpty(inputArg))
				return false;

			SQLKeyWord[] values = SQLKeyWord.values();

			for (SQLKeyWord var : values)
			{
				if (var.name().equals(inputArg.toUpperCase()))
					return true;
			}

			return false;
		}
	}
	
	public static String getSql(Class dataInfo, String tableName)
	{
		StringBuffer sql = new StringBuffer();

		sql.append("SELECT").append(" ");

		for (Field column : dataInfo.getDeclaredFields())
		{
			if (column.isAnnotationPresent(CTORMTemplate.class))
			{
				CTORMTemplate annotation = column.getAnnotation(CTORMTemplate.class);

				String name = annotation.name();

				sql.append(name);
				sql.append(",");
			}
		}

		String refinedSql = StringUtil.removeEnd(sql.toString(), ",");

		sql = new StringBuffer(refinedSql).append(" ").append("FROM").append(" ").append(tableName);

		return sql.toString();
	}

	public static Object loadServiceProxy(Class clazz) throws CustomException
	{
		String serviceName = new StringBuffer(servicePath).append(".").append(clazz.getSimpleName()).append("Service").toString();

		Object serviceClass = InvokeUtils.newInstance(serviceName, new Class[0], new Object[0]);

		if (serviceClass != null)
		{
			return serviceClass;
		}
		else
		{
			throw new CustomException("SYS-8001", serviceName);
		}
	}

	public static String validateKeyParam(Object dataInfo, Object[] args)
	{
		// key info validating
		try
		{
			if (dataInfo == null)
				throw new Exception("dataInfo is null");

			int keyCnt = 0;

			for (Field column : dataInfo.getClass().getDeclaredFields())
			{
				// only by annotation presentation
				if (column.isAnnotationPresent(CTORMTemplate.class))
				{
					CTORMTemplate annotation = column.getAnnotation(CTORMTemplate.class);

					String name = annotation.name();
					String type = annotation.type();
					String dataType = annotation.dataType();
					String initial = annotation.initial();

					if (type.equalsIgnoreCase("key"))
						keyCnt++;
				}
			}

			if (keyCnt != args.length)
				throw new Exception("key value is null");
		}
		catch (Exception ex)
		{
			return ErrorSignal.NullPointKeySignal;
		}

		return "";
	}

	public static List<Object> makeKeyParam(Object dataInfo)
	{
		List<Object> temp = new ArrayList<Object>();

		if (dataInfo == null)
			return temp;

		int idx = 0;

		// find non-key binding at first
		for (Field column : dataInfo.getClass().getDeclaredFields())
		{
			// only by annotation presentation
			if (column.isAnnotationPresent(CTORMTemplate.class))
			{
				CTORMTemplate annotation = column.getAnnotation(CTORMTemplate.class);

				String name = annotation.name();
				String type = annotation.type();

				if (type.equalsIgnoreCase("key"))
				{
					temp.add(idx, ObjectUtil.getFieldValue(dataInfo, name));
					idx++;
				}
			}
		}

		return temp;
	}

	public static List<Object> makeNonKeyParam(Object dataInfo)
	{
		List<Object> temp = new ArrayList<Object>();

		if (dataInfo == null)
			return temp;

		int idx = 0;

		// find non-key binding at first
		for (Field column : dataInfo.getClass().getDeclaredFields())
		{
			// only by annotation presentation
			if (column.isAnnotationPresent(CTORMTemplate.class))
			{
				CTORMTemplate annotation = column.getAnnotation(CTORMTemplate.class);

				String name = annotation.name();
				String type = annotation.type();
				String initial = annotation.initial();

				if (!type.equalsIgnoreCase("key") && !initial.equalsIgnoreCase("never"))
				{
					temp.add(idx, ObjectUtil.getFieldValue(dataInfo, name));
					idx++;
				}
			}
		}

		return temp;
	}
}
