package kr.co.aim.messolution.alarm;

import kr.co.aim.messolution.alarm.service.AlarmInfoUtil;
import kr.co.aim.messolution.alarm.service.AlarmServiceUtil;
import kr.co.aim.messolution.generic.MESStackTrace;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.util.bundle.BundleUtil;

import org.apache.commons.logging.Log;

public class MESAlarmServiceProxy extends MESStackTrace {
	
	public static Object executeMethod(Log eventLogger, String beanName, String methodName, Object... args)
		throws CustomException
	{
		return executeMethodMonitor(eventLogger, beanName, methodName, args); 
	} 
	
	public static AlarmInfoUtil getAlarmInfoUtil()
	{
		return (AlarmInfoUtil) BundleUtil.getServiceByBeanName(AlarmInfoUtil.class.getSimpleName());
	}
	
	public static AlarmServiceUtil getAlarmServiceUtil()
	{
		return (AlarmServiceUtil) BundleUtil.getServiceByBeanName(AlarmServiceUtil.class.getSimpleName());
	}
}
