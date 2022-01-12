package kr.co.aim.messolution.extended.webinterface;

import kr.co.aim.messolution.extended.webinterface.service.ExtendedWebInterfaceServiceImpl;
import kr.co.aim.messolution.extended.webinterface.service.ExtendedWebInterfaceServiceUtil;
import kr.co.aim.greenframe.util.bundle.BundleUtil;

public class ExtendedWebInterfaceProxy {

	public static ExtendedWebInterfaceServiceImpl getExtendedWebInterfaceServiceImpl()
	{
		return (ExtendedWebInterfaceServiceImpl) BundleUtil.getServiceByBeanName(ExtendedWebInterfaceServiceImpl.class.getSimpleName());
	}
	
	public static ExtendedWebInterfaceServiceUtil getExtendedWebInterfaceServiceUtil()
	{
		return (ExtendedWebInterfaceServiceUtil) BundleUtil.getServiceByBeanName(ExtendedWebInterfaceServiceUtil.class.getSimpleName());
	}
}
