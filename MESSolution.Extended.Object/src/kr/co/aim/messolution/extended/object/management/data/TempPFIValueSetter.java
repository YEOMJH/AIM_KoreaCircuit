
package kr.co.aim.messolution.extended.object.management.data;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import kr.co.aim.messolution.extended.object.CTORMTemplate;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.iter.PFIValueSetter;
import kr.co.aim.greentrack.processflow.management.iter.ProcessFlowIterator;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.data.ProductSpecKey;

public class TempPFIValueSetter<DATA extends UdfAccessor>
{
	private String	namePreFix;
	private DATA		oldData;
	private DATA		newData;
	private ProductSpec		productSpec;
	private ProcessFlow		processFlow;

	public TempPFIValueSetter(ProcessFlowIterator aProcessFlowIterator, DATA aOldData, DATA aNewData)
			throws NotFoundSignal, FrameworkErrorSignal, CustomException
	{
		this.oldData = aOldData;
		this.newData = aNewData;

		setNamePreFix();
		setProductSpec(newData);
		setProcessFlow(aProcessFlowIterator);
	}

	public String getNamePreFix()
	{
		return namePreFix;
	}

	public DATA getOldData()
	{
		return oldData;
	}

	public DATA getNewData()
	{
		return newData;
	}

	public ProductSpec getProductSpec()
	{
		return productSpec;
	}

	public ProcessFlow getProcessFlow()
	{
		return processFlow;
	}

	private void setProcessFlow(ProcessFlowIterator aProcessFlowIterator)
	{
		this.processFlow = (ProcessFlow) aProcessFlowIterator.getProcessFlow();
	}

	private void setNamePreFix()
	{
		this.namePreFix = newData.getClass().getSimpleName() ;
	}

	private void setProductSpec(DATA data) throws  CustomException
	{
		setProductSpec(getFieldValue(data,"FACTORYNAME"), getFieldValue(data,"PRODUCTSPECNAME"), getFieldValue(data,"PRODUCTSPECVERSION"));
	}

	private void setProductSpec(String aFactoryName, String aProductSpecName, String aProductSpecVersion) throws NotFoundSignal, FrameworkErrorSignal
	{
		ProductSpecKey productSpecKey = new ProductSpecKey();
		productSpecKey.setFactoryName(aFactoryName);
		productSpecKey.setProductSpecName(aProductSpecName);
		productSpecKey.setProductSpecVersion(aProductSpecVersion);

		this.productSpec = ProductServiceProxy.getProductSpecService().selectByKey(productSpecKey);
	}

	private String getFieldValue(Object dataInfo, String fieldName) throws CustomException
	{
		if (dataInfo == null)
		{
			//SYSTEM-0004:ClassNullPointException:{0}
			throw new CustomException("SYSTEM-0004" , getNamePreFix());
		}

		try
		{
			if (!dataInfo.getClass().isAnnotationPresent(CTORMTemplate.class))
			{
				//SYSTEM-0005: ClassInvalidTypeException:{0} is not annotation present of CTORMTemplate.
				throw new CustomException("SYSTEM-0005" , getNamePreFix());
			}

			Field[] fields = dataInfo.getClass().getDeclaredFields();
			String tempStr = "";
			for (Field field : fields)
			{
				if (field.getName().toUpperCase().equals(fieldName))
				{
					field.setAccessible(true);
					tempStr = field.get(dataInfo).toString();
				}
			}
			
			if (StringUtil.isEmpty(tempStr) && dataInfo instanceof UdfAccessor)
			{
				try
				{
					Method getMethod = dataInfo.getClass().getMethod("getUdfs");
					Map<String,String> udfValue = (Map<String, String>) getMethod.invoke(dataInfo, null);
					for (String keyName : udfValue.keySet())
					{
						if (keyName.toUpperCase().equals(fieldName))
						{
							tempStr = udfValue.get(keyName);
						}
					}
				}
				catch (Exception e)
				{
					//SYSTEM-0006:FieldAccessException: {0}
					throw new CustomException("SYSTEM-0006" , e.getMessage());
				}
			}

			if (StringUtil.isEmpty(tempStr))
			{
				//SYSTEM-0009: NotFoundException: Field {0} does not exist in class {1}.
				throw new CustomException("SYSTEM-0009", fieldName, getNamePreFix());
			}

			return tempStr;
		}
		catch (Exception ex)
		{
			//SYSTEM-0006: FieldAccessException: {0}
			throw new CustomException("SYSTEM-0006" , ex.getMessage());
		}
	}
}
