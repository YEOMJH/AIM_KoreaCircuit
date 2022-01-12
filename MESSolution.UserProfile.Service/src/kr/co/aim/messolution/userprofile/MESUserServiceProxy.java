package kr.co.aim.messolution.userprofile;

import org.apache.commons.logging.Log;

import kr.co.aim.messolution.generic.MESStackTrace;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.userprofile.service.UserInvisibleButtonHistService;
import kr.co.aim.messolution.userprofile.service.UserInvisibleButtonService;
import kr.co.aim.messolution.userprofile.service.UserProfileHistoryService;
import kr.co.aim.messolution.userprofile.service.UserProfileMenuHistoryService;
import kr.co.aim.messolution.userprofile.service.UserProfileServiceImpl;
import kr.co.aim.messolution.userprofile.service.UserProfileServiceUtil;
import kr.co.aim.greenframe.util.bundle.BundleUtil;

public class MESUserServiceProxy extends MESStackTrace {
	
	
	public static UserProfileMenuHistoryService getUserProfileMenuHistoryService()
	{
		return (UserProfileMenuHistoryService) BundleUtil.getServiceByBeanName(UserProfileMenuHistoryService.class.getSimpleName());
	}
	
	public static UserInvisibleButtonHistService getUserInvisibleButtonHistService()
	{
		return (UserInvisibleButtonHistService) BundleUtil.getServiceByBeanName(UserInvisibleButtonHistService.class.getSimpleName());
	}
	public static UserInvisibleButtonService getUserInvisibleButtonService()
	{
		return (UserInvisibleButtonService) BundleUtil.getServiceByBeanName(UserInvisibleButtonService.class.getSimpleName());
	}
	public static UserProfileHistoryService getUserProfileHistoryService()
	{
		return (UserProfileHistoryService) BundleUtil.getServiceByBeanName(UserProfileHistoryService.class.getSimpleName());
	}

	public static UserProfileServiceImpl getUserProfileServiceImpl()
	{
		return (UserProfileServiceImpl) BundleUtil.getServiceByBeanName(UserProfileServiceImpl.class.getSimpleName());
	}
	
	public static UserProfileServiceUtil getUserProfileServiceUtil()
	{
		return (UserProfileServiceUtil) BundleUtil.getServiceByBeanName(UserProfileServiceUtil.class.getSimpleName());
	}
	
	public static Object executeMethod(Log eventLogger, String beanName, String methodName, Object... args)
		throws CustomException
	{
		return executeMethodMonitor(eventLogger, beanName, methodName, args);
	}
}
