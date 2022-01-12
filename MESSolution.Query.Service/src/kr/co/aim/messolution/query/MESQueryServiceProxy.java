package kr.co.aim.messolution.query;
import org.apache.commons.logging.Log;

import kr.co.aim.messolution.generic.MESStackTrace;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.query.service.QueryServiceImpl;
import kr.co.aim.greenframe.util.bundle.BundleUtil;


public class MESQueryServiceProxy extends MESStackTrace {

	public static Object executeMethod(Log eventLogger, String beanName, String methodName, Object... args)
		throws CustomException
	{
		return executeMethodMonitor(eventLogger, beanName, methodName, args);
	}
	
	public static QueryServiceImpl getQueryServiceImpl()
	{
		return (QueryServiceImpl) BundleUtil.getServiceByBeanName(QueryServiceImpl.class.getSimpleName());
	}
}
