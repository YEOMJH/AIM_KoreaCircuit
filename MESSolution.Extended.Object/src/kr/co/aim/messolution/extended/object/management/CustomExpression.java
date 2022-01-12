package kr.co.aim.messolution.extended.object.management;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.orm.info.KeyInfo;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;
import kr.co.aim.greentrack.generic.GenericServiceProxy;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.processflow.management.iter.ProcessFlowExpressionUtils;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.Context;
import org.jaxen.Function;
import org.jaxen.FunctionCallException;
import org.jaxen.FunctionContext;
import org.jaxen.XPathFunctionContext;

public class CustomExpression
{
	private static Log				log				= LogFactory.getLog(CustomExpression.class);

	protected FunctionContext		functionContext	= new XPathFunctionContext();
	protected Map<String, Object>	expressionContext;

	public FunctionContext getFunctionContext()
	{
		return functionContext;
	}

	public void setExpressionContext(Map<String, Object> expressionContext)
	{
		this.expressionContext = expressionContext;
	}

	public CustomExpression()
	{
		((XPathFunctionContext) functionContext).registerFunction(null, "getVariable", new GetVariableFunction());
		((XPathFunctionContext) functionContext).registerFunction(null, "getField", new GetField());
		((XPathFunctionContext) functionContext).registerFunction(null, "getUDF", new GetUDF());
		((XPathFunctionContext) functionContext).registerFunction(null, "getAttribute", new GetAttribute());

		((XPathFunctionContext) functionContext).registerFunction(null, "setField", new SetField());
		((XPathFunctionContext) functionContext).registerFunction(null, "setUDF", new SetUDF());
	}

	private class GetVariableFunction implements Function
	{
		public Object call(Context xpath, List list) throws FunctionCallException
		{
			return expressionContext.get((String) list.get(0));
		}
	}

	private class GetField implements Function
	{
		@SuppressWarnings("unchecked")
		public Object call(Context arg0, List arg1) throws FunctionCallException
		{
			Object variable = arg1.get(0);
			if (variable instanceof String)
				variable = expressionContext.get((String) variable);

			String fieldName = String.valueOf(arg1.get(1));
			Object result;
			
			try
			{
				result = getFieldValue(variable, fieldName);
			}
			catch (CustomException e)
			{
				log.warn("Fail to execute getFieldValue method : " + e.getLocalizedMessage());
				return false;
			}

			if (result ==null )
			{
				if (arg1.size() == 3)
					result = arg1.get(2);
			}

			if (log.isDebugEnabled())
				log.debug("[getField: " + fieldName + "] = " + result);
			return result;
			
		}
	}

	private class GetUDF implements Function
	{
		@SuppressWarnings("unchecked")
		public Object call(Context arg0, List arg1) throws FunctionCallException
		{
			Object variable = arg1.get(0);
			if (variable instanceof String)
				variable = expressionContext.get((String) variable);

			String udfName = String.valueOf(arg1.get(1));

			if (!(variable instanceof UdfAccessor))
				return null;

			Object result = ((UdfAccessor) variable).getUdfs().get(udfName);

			if (StringUtils.isEmpty((String) result))
			{
				if (arg1.size() == 3)
					result = arg1.get(2);
			}
			if (log.isDebugEnabled())
				log.debug("[getUDF: " + udfName + "] = " + result);
			return result;
		}
	}

	private class GetAttribute implements Function
	{
		@SuppressWarnings("unchecked")
		public Object call(Context arg0, List arg1) throws FunctionCallException
		{
				Object variable = arg1.get(0);
				if (variable instanceof String)
					variable = expressionContext.get((String) variable);

		        log.info("The 'GetAttribute' expression cannot be used by the custom class." + "["+variable+"]");

				return false;
		}
	}
	
	private class SetField implements Function
	{
		@SuppressWarnings("unchecked")
		public Object call(Context arg0, List arg1) throws FunctionCallException
		{
			try
			{
				Object variable = arg1.get(0);
				if (variable instanceof String)
					variable = expressionContext.get((String) variable);

				String fieldName = String.valueOf(arg1.get(1));
				Object value = arg1.get(2);
				if (arg1.size() == 4)
				{
					String operator = value.toString();
					Number number = (Number) arg1.get(3);

					value = getFieldValue(variable, fieldName);
					if (value == null || StringUtils.isEmpty(value.toString()))
						value = 0;
					else
						value = Double.parseDouble(value.toString());

					value = calculateOperator((Number) value, operator, number);
				}

				setFieldValue(variable, fieldName, value);

				if (log.isDebugEnabled())
					log.debug("[setField: " + fieldName + "] = " + value);

				return true;
			}
			catch (Exception ex)
			{
				if (log.isDebugEnabled())
					log.error(ex, ex);
				else
					log.error(ex);
				return false;
			}
		}
	}

	private class SetUDF implements Function
	{
		@SuppressWarnings("unchecked")
		public Object call(Context arg0, List arg1) throws FunctionCallException
		{
			try
			{
				Object variable = arg1.get(0);
				if (variable instanceof String)
					variable = expressionContext.get((String) variable);

				String udfName = String.valueOf(arg1.get(1));
				Object value = arg1.get(2).toString();

				if (!(variable instanceof UdfAccessor))
					return false;

				if (arg1.size() == 4)
				{
					String operator = value.toString();
					Number number = (Number) arg1.get(3);

					value = ((UdfAccessor) variable).getUdfs().get(udfName);
					if (value == null || StringUtils.isEmpty(value.toString()))
						value = 0;
					else
						value = Double.parseDouble(value.toString());

					value = calculateOperator((Number) value, operator, number);
				}

				((UdfAccessor) variable).getUdfs().put(udfName, String.valueOf(value));

				if (log.isDebugEnabled())
					log.debug("[setUDF: " + udfName + "] = " + value);

				return true;
			} catch (Throwable ex)
			{
				if (log.isDebugEnabled())
					log.error(ex, ex);
				else
					log.error(ex);
				return false;
			}
		}
	}

	private static Object  getFieldValue(Object dataInfo, String fieldName) throws CustomException
	{
		if (dataInfo == null)
		{
			//SYSTEM-0012:ClassNullPointException: class name is null.
			throw new CustomException("SYSTEM-0012");
		}

		try
		{
			Object objTemp = null;
			Field field = getField(dataInfo.getClass(), fieldName);

			objTemp = field.get(dataInfo);

			if (objTemp == null)
			{
				//SYSTEM-0009: NotFoundException: Field {0} does not exist in class {1}.
				throw new CustomException("SYSTEM-0009", fieldName, dataInfo.getClass().getSimpleName());
			}

			return objTemp;
		}
		catch (Exception ex)
		{
			//SYSTEM-0006: FieldAccessException: {0}
			throw new CustomException("SYSTEM-0006" + ex.getMessage());
		}
	}
	
	private static Object setFieldValue(Object dataInfo, String fieldName,Object value) throws CustomException, IllegalArgumentException, IllegalAccessException
	{
		if (dataInfo == null)
		{
			//SYSTEM-0012:ClassNullPointException: class name is null.
			throw new CustomException("SYSTEM-0012");
		}

		Field field;

		try
		{
			field = getField(dataInfo.getClass(), fieldName);

			if (value instanceof Number)
			{
				value = getFixedNumberValue(field.getType(), value);
			}
			field.set(dataInfo, value);
		}
		catch (Exception e)
		{
			//SYSTEM-0006: FieldAccessException: {0}
			throw new CustomException("SYSTEM-0006" + e.getMessage());
		}

		return field.get(dataInfo);
	}
	
	private static Object getFixedNumberValue(Class clazz, Object value)
	{
		if (value == null || !(value instanceof Number))
			return value;

		Number numV = (Number) value;
		if (clazz == int.class || clazz == Integer.class)
			return new Integer(numV.intValue());
		if (clazz == long.class || clazz == Long.class)
			return new Long(numV.longValue());
		if (clazz == float.class || clazz == Float.class)
			return new Float(numV.floatValue());
		if (clazz == byte.class || clazz == Byte.class)
			return new Byte(numV.byteValue());
		if (clazz == short.class || clazz == Short.class)
			return new Short(numV.shortValue());
		if (clazz == double.class || clazz == Double.class)
			return new Double(numV.doubleValue());
		return value;
	}

	public static Field getField(Class cls, String fieldName) throws NoSuchFieldException
	{
		Field field = null;
		try
		{
			field = cls.getDeclaredField(fieldName);
		} catch (NoSuchFieldException e)
		{
			field = cls.getField(fieldName);
		}

		field.setAccessible(true);
		return field;
	}
	private static Number calculateOperator(Number value, String operator, Number number)
	{
		if ("+".equals(operator))
			return value.doubleValue() + number.doubleValue();
		else if ("-".equals(operator))
			return value.doubleValue() - number.doubleValue();
		else if ("*".equals(operator))
			return value.doubleValue() * number.doubleValue();
		else if ("/".equals(operator))
			return value.doubleValue() / number.doubleValue();
		else if ("%".equals(operator))
			return value.doubleValue() % number.doubleValue();
		else
			throw new RuntimeException("Not Support Operator : " + operator);
	}
}
