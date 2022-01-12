package kr.co.aim.messolution.generic;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.util.bundle.BundleUtil;
import kr.co.aim.greenframe.util.support.InvokeUtils;

import org.apache.commons.logging.Log;

public abstract class MESStackTrace {
	
	protected static Object executeMethodMonitor(Log outterLog, String beanName, String methodName, Object... args)
		throws CustomException
	{
		
		Object targetClass = BundleUtil.getServiceByBeanName(beanName);
		Object result = null;
		
		if (targetClass != null)
		{
			//acquired as singleton
			long begin = 0;
			long end = 0;
			
			try
			{
				if (outterLog.isInfoEnabled())
				{
					begin = System.nanoTime();
					outterLog.info(String.format("���� START %s.%s ParameterCnt=[%d]", beanName, methodName, args.length));
				}
				
				result = InvokeUtils.invokeMethod(targetClass, methodName, args);
				
				if (outterLog.isInfoEnabled())
				{
					end = System.nanoTime();
					outterLog.info(String.format("���� ENDOK %s.%s ParameterCnt=[%d] %s ms", beanName, methodName, args.length, (end - begin) / 1000000));
				}
			}
			catch (Exception e)
			{
				if (outterLog.isInfoEnabled())
				{
					end = System.nanoTime();
					outterLog.info(String.format("���� ENDNG %s.%s ParameterCnt=[%d] %s ms", beanName, methodName, args.length, (end - begin) / 1000000));
				}
				
				if (e.getCause() instanceof CustomException)
					throw (CustomException) e.getCause();
				else
					throw new CustomException(e);
			}
		}
		else
		{
			throw new CustomException(new Exception("spring bean not found"));
		}
		
		return result;
	}
}
