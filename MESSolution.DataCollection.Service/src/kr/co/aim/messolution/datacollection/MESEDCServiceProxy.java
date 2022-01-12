package kr.co.aim.messolution.datacollection;

import org.apache.commons.logging.Log;

import kr.co.aim.messolution.datacollection.service.DataCollectionServiceUtil;
import kr.co.aim.messolution.generic.MESStackTrace;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.util.bundle.BundleUtil;

public class MESEDCServiceProxy extends MESStackTrace {

	public static Object executeMethod(Log eventLogger, String beanName, String methodName, Object... args)
		throws CustomException
	{
		return executeMethodMonitor(eventLogger, beanName, methodName, args); 
	}
	public static DataCollectionServiceUtil getDataCollectionServiceUtil()
	{
		return (DataCollectionServiceUtil) BundleUtil.getServiceByBeanName(DataCollectionServiceUtil.class.getSimpleName());
	}
}
