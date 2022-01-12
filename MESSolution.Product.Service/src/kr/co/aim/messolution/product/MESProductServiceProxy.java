package kr.co.aim.messolution.product;

import org.apache.commons.logging.Log;

import kr.co.aim.messolution.generic.MESStackTrace;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.product.service.ProductInfoUtil;
import kr.co.aim.messolution.product.service.ProductServiceImpl;
import kr.co.aim.messolution.product.service.ProductServiceUtil;
import kr.co.aim.greenframe.util.bundle.BundleUtil;

public class MESProductServiceProxy extends MESStackTrace {


	public static Object executeMethod(Log eventLogger, String beanName, String methodName, Object... args)
		throws CustomException
	{
		return executeMethodMonitor(eventLogger, beanName, methodName, args);
	}
	
	public static ProductInfoUtil getProductInfoUtil()
	{
		return (ProductInfoUtil) BundleUtil.getServiceByBeanName(ProductInfoUtil.class.getSimpleName());
	}
	
	public static ProductServiceImpl getProductServiceImpl()
	{
		return (ProductServiceImpl) BundleUtil.getServiceByBeanName(ProductServiceImpl.class.getSimpleName());
	}
	
	public static ProductServiceUtil getProductServiceUtil() 
	{
		return (ProductServiceUtil) BundleUtil.getServiceByBeanName(ProductServiceUtil.class.getSimpleName());
	}
}
