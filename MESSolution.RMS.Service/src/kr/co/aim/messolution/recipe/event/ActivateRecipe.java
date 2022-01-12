package kr.co.aim.messolution.recipe.event;

import org.jdom.Document;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.Recipe;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class ActivateRecipe extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {

		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME",false);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME",true);
		String recipeName = SMessageUtil.getBodyItemValue(doc, "RECIPENAME",true);
		MESRecipeServiceProxy.getRecipeServiceUtil().verifyUserPrivilege(getEventUser(), machineName);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Activate",getEventUser(), getEventComment(), null, null);
		Recipe recipeInfo = ExtendedObjectProxy.getRecipeService().selectByKey(false, new Object[] { machineName, recipeName });
		
		if (recipeInfo.getRecipeType().equalsIgnoreCase("MAIN"))
		{
			//RMS-007: RMSError:Only Unit or Sub-unit Recipe could become activated
			throw new CustomException("RMS-007");
		}
		// RecipeInfo 
		recipeInfo.setActiveState(GenericServiceProxy.getConstantMap().Spec_Active);
		recipeInfo.setLastActivatedTime(eventInfo.getEventTime());
		// history trace
		recipeInfo.setLastEventName(eventInfo.getEventName());
		System.out.println(eventInfo.getEventTimeKey());
		recipeInfo.setLastEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
		recipeInfo.setLastEventUser(eventInfo.getEventUser());
		recipeInfo.setLastEventComment(eventInfo.getEventComment());
		// recipeInfo.setLastModifiedTime(eventInfo.getEventTime()); 
		recipeInfo = ExtendedObjectProxy.getRecipeService().modify(eventInfo,recipeInfo);
		return doc;
	}
}
