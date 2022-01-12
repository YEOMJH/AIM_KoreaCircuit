package kr.co.aim.messolution.processgroup;

import org.apache.commons.logging.Log;

import kr.co.aim.messolution.generic.MESStackTrace;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.processgroup.service.ProcessGroupInfoUtil;
import kr.co.aim.messolution.processgroup.service.ProcessGroupServiceImpl;
import kr.co.aim.messolution.processgroup.service.ProcessGroupServiceUtil;
import kr.co.aim.greenframe.util.bundle.BundleUtil;

public class MESProcessGroupServiceProxy extends MESStackTrace {


	public static Object executeMethod(Log eventLogger, String beanName, String methodName, Object... args)
		throws CustomException
	{
		return executeMethodMonitor(eventLogger, beanName, methodName, args);
	}	
	
	public static ProcessGroupInfoUtil getProcessGroupInfoUtil()
	{
		return (ProcessGroupInfoUtil) BundleUtil.getServiceByBeanName(ProcessGroupInfoUtil.class.getSimpleName());
	}
	
	public static ProcessGroupServiceImpl getProcessGroupServiceImpl()
	{
		return (ProcessGroupServiceImpl) BundleUtil.getServiceByBeanName(ProcessGroupServiceImpl.class.getSimpleName());
	}
	
	public static ProcessGroupServiceUtil getProcessGroupServiceUtil() 
	{
		return (ProcessGroupServiceUtil) BundleUtil.getServiceByBeanName(ProcessGroupServiceUtil.class.getSimpleName());
	}
}
