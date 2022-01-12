package kr.co.aim.messolution.recipe.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.Recipe;
import kr.co.aim.messolution.extended.object.management.data.RecipeParameter;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;

public class RemoveRecipeParameter extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		List<Element> paramList = SMessageUtil.getBodySequenceItemList(doc, "PARAMLIST", true);
		ConstantMap constMap = GenericServiceProxy.getConstantMap();
		
		MESRecipeServiceProxy.getRecipeServiceUtil().verifyUserPrivilege(getEventUser(), paramList.get(0).getChildText("MACHINENAME"));
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Remove", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey()); 
		
		//2021-03-31 ghhan Change Batch
		List<RecipeParameter> removeList = new ArrayList<>();
		String machineName = paramList.get(0).getChildTextTrim("MACHINENAME").toString();
		String recipeName = paramList.get(0).getChildTextTrim("RECIPENAME").toString();
		
		String condition = "WHERE MACHINENAME = '" + machineName + "' AND RECIPENAME = '" + recipeName + "' AND RECIPEPARAMETERNAME IN( ";
		
		for (Element param : paramList)
		{
			String parameterName = param.getChildText("RECIPEPARAMETERNAME");
			condition += "'" + parameterName + "',";
		}
		condition = condition.substring(0, condition.length() - 1) + ") ORDER BY RECIPEPARAMETERNAME";
		
		List<RecipeParameter> recipeParameterList = ExtendedObjectProxy.getRecipeParamService().select(condition, new Object[] {});
		
		Collections.sort(paramList, new Comparator<Element>()
		{ 
			@Override 
			public int compare(Element b1, Element b2) 
			{ 
				return b1.getChildText("RECIPEPARAMETERNAME").compareTo(b2.getChildText("RECIPEPARAMETERNAME")); 
			} 
		});
		
		for(RecipeParameter paramInfoDB : recipeParameterList)
		{		
			for(int i = 0 ; i < paramList.size() ; i++) 
			{
				if(StringUtil.equals(paramList.get(i).getChildText("RECIPEPARAMETERNAME"), paramInfoDB.getRecipeParameterName()) && StringUtil.equals(paramList.get(i).getChildText("VALUE"), paramInfoDB.getValue()))
				{
					removeList.add(paramInfoDB);
					paramList.remove(i);
				}
			}
		}
		
		if(removeList.size() > 0)
		{
			ExtendedObjectProxy.getRecipeParamService().remove(eventInfo, removeList);
		}
		
		eventInfo.setEventName("Modify");
		//ExtendedObjectProxy.getRecipeService().makeNotAvailable1(eventInfo, paramList.get(0).getChildText("MACHINENAME"), paramList.get(0).getChildText("RECIPENAME"), "");
		Recipe recipeData = ExtendedObjectProxy.getRecipeService().selectByKey(true, new Object[] { machineName, recipeName });
		recipeData.setLastModifiedTime(eventInfo.getEventTime());
		// history trace
		recipeData.setLastEventName(eventInfo.getEventName());
		recipeData.setLastEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
		recipeData.setLastEventUser(eventInfo.getEventUser());
		recipeData.setLastEventComment(eventInfo.getEventComment());
		recipeData.setLastModifiedTime(eventInfo.getEventTime());
		recipeData.setINTFlag("N");//when modify parameter, set INT\ENG\RMS  = N
		recipeData.setENGFlag("N");
		recipeData.setRMSFlag("N");
		recipeData.setActiveState(constMap.Spec_NotActive);
		recipeData.setRecipeState(constMap.RECIPESTATE_UNAPPROVED);
		ExtendedObjectProxy.getRecipeService().modify(eventInfo, recipeData);
		
		return doc;
	}
}
