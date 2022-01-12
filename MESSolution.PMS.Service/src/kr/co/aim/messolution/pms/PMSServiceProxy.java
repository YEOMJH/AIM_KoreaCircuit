package kr.co.aim.messolution.pms;

import org.apache.commons.logging.Log;

import kr.co.aim.messolution.generic.MESStackTrace;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.pms.service.MaintenanceGroupServiceImpl;
import kr.co.aim.messolution.pms.service.MaintenanceOrderItemServiceImpl;
import kr.co.aim.messolution.pms.service.MaintenanceOrderServiceImpl;
import kr.co.aim.messolution.pms.service.MaintenanceSpecItemServiceImpl;
import kr.co.aim.messolution.pms.service.MaintenanceSpecServiceImpl;
import kr.co.aim.greenframe.util.bundle.BundleUtil;

public class PMSServiceProxy extends MESStackTrace {
	
	public static Object executeMethod(Log eventLogger, String beanName, String methodName, Object... args)
		throws CustomException
	{
		return executeMethodMonitor(eventLogger, beanName, methodName, args);
	}
	
	public static MaintenanceGroupServiceImpl getMaintGroupService()
		throws CustomException
	{
		return (MaintenanceGroupServiceImpl) BundleUtil.getServiceByBeanName("MaintenanceGroupService");
	}
	
	public static MaintenanceSpecServiceImpl getMaintSpecService()
		throws CustomException
	{
		return (MaintenanceSpecServiceImpl) BundleUtil.getServiceByBeanName("MaintenanceSpecService");
	}
	
	public static MaintenanceSpecItemServiceImpl getMaintSpecItemService()
		throws CustomException
	{
		return (MaintenanceSpecItemServiceImpl) BundleUtil.getServiceByBeanName("MaintenanceSpecItemService");
	}
	
	public static MaintenanceOrderServiceImpl getOrderService()
		throws CustomException
	{
		return (MaintenanceOrderServiceImpl) BundleUtil.getServiceByBeanName("MaintenanceOrderService");
	}
	
	public static MaintenanceOrderItemServiceImpl getOrderItemService()
		throws CustomException
	{
		return (MaintenanceOrderItemServiceImpl) BundleUtil.getServiceByBeanName("MaintenanceOrderItemService");
	}
}
