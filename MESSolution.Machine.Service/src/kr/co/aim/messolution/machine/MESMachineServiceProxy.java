package kr.co.aim.messolution.machine;

import org.apache.commons.logging.Log;

import kr.co.aim.messolution.generic.MESStackTrace;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.machine.service.MachineInfoUtil;
import kr.co.aim.messolution.machine.service.MachineServiceImpl;
import kr.co.aim.greenframe.util.bundle.BundleUtil;

public class MESMachineServiceProxy extends MESStackTrace {

	
	public static Object executeMethod(Log eventLogger, String beanName, String methodName, Object... args)
		throws CustomException
	{
		return executeMethodMonitor(eventLogger, beanName, methodName, args);
	}
	
	public static MachineInfoUtil getMachineInfoUtil()
	{
		return (MachineInfoUtil) BundleUtil.getBundleServiceClass(MachineInfoUtil.class);
	}
	
	public static MachineServiceImpl getMachineServiceImpl() 
	{
		return (MachineServiceImpl) BundleUtil.getBundleServiceClass(MachineServiceImpl.class);
	}
}
