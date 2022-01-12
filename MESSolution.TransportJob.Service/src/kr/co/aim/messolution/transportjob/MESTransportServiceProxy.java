package kr.co.aim.messolution.transportjob;

import org.apache.commons.logging.Log;

import kr.co.aim.messolution.generic.MESStackTrace;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.transportjob.service.TransportJobInfoUtil;
import kr.co.aim.messolution.transportjob.service.TransportJobServiceImpl;
import kr.co.aim.messolution.transportjob.service.TransportJobServiceUtil;
import kr.co.aim.greenframe.util.bundle.BundleUtil;

public class MESTransportServiceProxy extends MESStackTrace {
	
	public static Object executeMethod(Log eventLogger, String beanName, String methodName, Object... args)
		throws CustomException
	{
		return executeMethodMonitor(eventLogger, beanName, methodName, args);
	}
	
	public static TransportJobInfoUtil getTransportJobInfoUtil()
	{
		return (TransportJobInfoUtil) BundleUtil.getServiceByBeanName(TransportJobInfoUtil.class.getSimpleName());
	}
	
	public static TransportJobServiceImpl getTransportJobServiceImpl()
	{
		return (TransportJobServiceImpl) BundleUtil.getServiceByBeanName(TransportJobServiceImpl.class.getSimpleName());
	} 

	public static TransportJobServiceUtil getTransportJobServiceUtil() 
	{
		return (TransportJobServiceUtil) BundleUtil.getServiceByBeanName(TransportJobServiceUtil.class.getSimpleName());
	} 
}
