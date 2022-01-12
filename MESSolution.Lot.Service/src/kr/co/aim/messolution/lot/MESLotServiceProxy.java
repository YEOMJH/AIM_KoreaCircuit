package kr.co.aim.messolution.lot;

import org.apache.commons.logging.Log;

import kr.co.aim.messolution.generic.MESStackTrace;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.lot.service.LotInfoUtil;
import kr.co.aim.messolution.lot.service.LotServiceImpl;
import kr.co.aim.messolution.lot.service.LotServiceUtil;
import kr.co.aim.greenframe.util.bundle.BundleUtil;

public class MESLotServiceProxy extends MESStackTrace {

	public static Object executeMethod(Log eventLogger, String beanName, String methodName, Object... args) throws CustomException
	{
		return executeMethodMonitor(eventLogger, beanName, methodName, args);
	}

	public static LotInfoUtil getLotInfoUtil()
	{
		return (LotInfoUtil) BundleUtil.getServiceByBeanName(LotInfoUtil.class.getSimpleName());
	}

	public static LotServiceImpl getLotServiceImpl()
	{
		return (LotServiceImpl) BundleUtil.getServiceByBeanName(LotServiceImpl.class.getSimpleName());
	}

	public static LotServiceUtil getLotServiceUtil()
	{
		return (LotServiceUtil) BundleUtil.getServiceByBeanName(LotServiceUtil.class.getSimpleName());
	}
}
