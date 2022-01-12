package kr.co.aim.messolution.recipe;

import org.apache.commons.logging.Log;

import kr.co.aim.messolution.generic.MESStackTrace;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.recipe.service.RecipeServiceImpl;
import kr.co.aim.messolution.recipe.service.RecipeServiceUtil;
import kr.co.aim.greenframe.util.bundle.BundleUtil;

public class MESRecipeServiceProxy extends MESStackTrace {

	
	public static Object executeMethod(Log eventLogger, String beanName, String methodName, Object... args)
		throws CustomException
	{
		return executeMethodMonitor(eventLogger, beanName, methodName, args);
	}
	
	public static RecipeServiceImpl getRecipeServiceImpl()
	{
		return (RecipeServiceImpl) BundleUtil.getServiceByBeanName(RecipeServiceImpl.class.getSimpleName());
	}
	
	public static RecipeServiceUtil getRecipeServiceUtil()
	{
		return (RecipeServiceUtil) BundleUtil.getServiceByBeanName(RecipeServiceUtil.class.getSimpleName());
	}
}
