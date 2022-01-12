package kr.co.aim.messolution.dispatch;

import org.apache.commons.logging.Log;

import kr.co.aim.messolution.dispatch.service.DSPServiceImpl;
import kr.co.aim.messolution.dispatch.service.DSPServiceUtil;
import kr.co.aim.messolution.generic.MESStackTrace;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.util.bundle.BundleUtil;

public class MESDSPServiceProxy extends MESStackTrace {

	public static Object executeMethod(Log eventLogger, String beanName, String methodName, Object... args) throws CustomException
	{
		return executeMethodMonitor(eventLogger, beanName, methodName, args);
	}

	public static DSPServiceImpl getDSPServiceImpl()
	{
		return (DSPServiceImpl) BundleUtil.getServiceByBeanName(DSPServiceImpl.class.getSimpleName());
	}

	public static DSPServiceUtil getDSPServiceUtil()
	{
		return (DSPServiceUtil) BundleUtil.getServiceByBeanName(DSPServiceUtil.class.getSimpleName());
	}
}
