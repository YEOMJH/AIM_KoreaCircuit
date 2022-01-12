package kr.co.aim.messolution.productrequest;

import org.apache.commons.logging.Log;

import kr.co.aim.messolution.generic.MESStackTrace;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.productrequest.service.ProductRequestInfoUtil;
import kr.co.aim.messolution.productrequest.service.ProductRequestServiceImpl;
import kr.co.aim.messolution.productrequest.service.ProductRequestServiceUtil;
import kr.co.aim.greenframe.util.bundle.BundleUtil;

public class MESWorkOrderServiceProxy extends MESStackTrace {

	public static Object executeMethod(Log eventLogger, String beanName, String methodName, Object... args) throws CustomException
	{
		return executeMethodMonitor(eventLogger, beanName, methodName, args);
	}

	public static ProductRequestInfoUtil getProductRequestInfoUtil()
	{
		return (ProductRequestInfoUtil) BundleUtil.getServiceByBeanName("ProductRequestInfoUtil");
	}

	public static ProductRequestServiceImpl getProductRequestServiceImpl()
	{
		return (ProductRequestServiceImpl) BundleUtil.getServiceByBeanName("ProductRequestServiceImpl");
	}

	public static ProductRequestServiceUtil getProductRequestServiceUtil()
	{
		return (ProductRequestServiceUtil) BundleUtil.getServiceByBeanName("ProductRequestServiceUtil");
	}
}
