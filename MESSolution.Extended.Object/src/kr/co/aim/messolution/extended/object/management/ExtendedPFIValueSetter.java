package kr.co.aim.messolution.extended.object.management;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import javax.print.attribute.SetOfIntegerSyntax;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.ProductSpecKey;

public class ExtendedPFIValueSetter<DATA extends UdfAccessor>
{
	private String	namePreFix;
	private String  specPreFix;
	private String  flowPreFix;
	private boolean isCTTable = false;
	private DATA		oldData;
	private DATA		newData;
	private Object		productSpec;
	private ProcessFlow		processFlow;

	public ExtendedPFIValueSetter(ExtendedProcessFlowIterator aProcessFlowIterator, DATA aOldData, DATA aNewData,Object productSpecData, boolean IsCTTable)
			throws NotFoundSignal, FrameworkErrorSignal, CustomException
	{
		this.oldData = aOldData;
		this.newData = aNewData;

		setCTTable(IsCTTable);
		setNamePreFix();
	    setProductSpec(productSpecData);
		setProcessFlow(aProcessFlowIterator);
	}

	public boolean isCTTable()
	{
		return isCTTable;
	}


	public void setCTTable(boolean isCTTable)
	{
		this.isCTTable = isCTTable;
	}


	public String getNamePreFix()
	{
		return namePreFix;
	}

	public String getSpecPreFix()
	{
		return specPreFix;
	}

	public String getFlowPreFix()
	{
		return flowPreFix;
	}

	public DATA getOldData()
	{
		return oldData;
	}

	public DATA getNewData()
	{
		return newData;
	}

	public Object getProductSpec()
	{
		return productSpec;
	}

	public ProcessFlow getProcessFlow()
	{
		return processFlow;
	}

	private void setProcessFlow(ExtendedProcessFlowIterator aProcessFlowIterator)
	{
		this.processFlow = (ProcessFlow) aProcessFlowIterator.getProcessFlow();
		this.flowPreFix = this.getProcessFlow().getClass().getSimpleName();
	}

	private void setNamePreFix()
	{
		if (isCTTable())
		{
            this.namePreFix = "CT_" + newData.getClass().getSimpleName().toString();
		}
		else
		{
			this.namePreFix = newData.getClass().getSimpleName();
		}
	}

	private void setProductSpec(Object specData) throws  CustomException
	{
		if(specData!=null)
		{
			this.productSpec =specData;
		    this.specPreFix = specData.getClass().getSimpleName();
		}
		//setProductSpec(getFieldValue(data,"FACTORYNAME"), getFieldValue(data,"PRODUCTSPECNAME"), getFieldValue(data,"PRODUCTSPECVERSION"));
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
			//SYSTEM-0012: ClassNullPointException: class name is null.
			throw new CustomException("SYSTEM-0012");
		}

		try
		{
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
					//SYSTEM-0006: FieldAccessException: {0}
					throw new CustomException("SYSTEM-0006" + e.getMessage());
				}
			}

			if (StringUtil.isEmpty(tempStr))
			{
				//SYSTEM-0009:NotFoundException: Field {0} does not exist in class {1}. 
				throw new CustomException("SYSTEM-0009", fieldName, getNamePreFix());
			}

			return tempStr;
		}
		catch (Exception ex)
		{
			//SYSTEM-0006: FieldAccessException: {0}
			throw new CustomException("SYSTEM-0006" + ex.getMessage());
		}
	}
}

