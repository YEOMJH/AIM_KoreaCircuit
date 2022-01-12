package kr.co.aim.messolution.port;

import org.apache.commons.logging.Log;

import kr.co.aim.messolution.generic.MESStackTrace;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.port.service.PortInfoUtil;
import kr.co.aim.messolution.port.service.PortServiceImpl;
import kr.co.aim.messolution.port.service.PortServiceUtil;
import kr.co.aim.greenframe.util.bundle.BundleUtil;

public class MESPortServiceProxy extends MESStackTrace {

	
	public static Object executeMethod(Log eventLogger, String beanName, String methodName, Object... args)
		throws CustomException
	{
		return executeMethodMonitor(eventLogger, beanName, methodName, args);
	}
	
	public static PortInfoUtil getPortInfoUtil()
	{
		return (PortInfoUtil) BundleUtil.getServiceByBeanName(PortInfoUtil.class.getSimpleName());
	}
	
	public static PortServiceImpl getPortServiceImpl()
	{
		return (PortServiceImpl) BundleUtil.getServiceByBeanName(PortServiceImpl.class.getSimpleName());
	} 

	public static PortServiceUtil getPortServiceUtil() 
	{
		return (PortServiceUtil) BundleUtil.getServiceByBeanName(PortServiceUtil.class.getSimpleName());
	}
}
