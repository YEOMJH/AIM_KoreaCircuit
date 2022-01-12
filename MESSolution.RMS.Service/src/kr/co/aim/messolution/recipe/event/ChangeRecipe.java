package kr.co.aim.messolution.recipe.event;

import org.jdom.Document;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.Recipe;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class ChangeRecipe extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", false);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String recipeName = SMessageUtil.getBodyItemValue(doc, "RECIPENAME", true);
		
		MESRecipeServiceProxy.getRecipeServiceUtil().verifyUserPrivilege(getEventUser(), machineName);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Change", getEventUser(), getEventComment(), null, null);
		
		String description = SMessageUtil.getBodyItemValue(doc, "DESCRIPTION", false);
		String timeUsedLimit = SMessageUtil.getBodyItemValue(doc, "TIMEUSEDLIMIT", true);
		String durationUsedLimit = SMessageUtil.getBodyItemValue(doc, "DURATIONUSEDLIMIT", true);
		String autoChangeFlag = SMessageUtil.getBodyItemValue(doc, "AUTOCHANGEFLAG", false);
		
		Recipe recipeInfo = ExtendedObjectProxy.getRecipeService().selectByKey(false, new Object[] {machineName, recipeName});
		
		//default spec info
		recipeInfo.setDescription(description);
		recipeInfo.setTimeUsedLimit(Long.parseLong(timeUsedLimit));
		recipeInfo.setDurationUsedLimit(Double.parseDouble(durationUsedLimit));
		recipeInfo.setRMSFlag("N");
		recipeInfo.setAutoChangeFlag(autoChangeFlag);
		
		//history trace
		recipeInfo.setLastEventName(eventInfo.getEventName());
		recipeInfo.setLastEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
		recipeInfo.setLastEventUser(eventInfo.getEventUser());
		recipeInfo.setLastEventComment(eventInfo.getEventComment());
		
		recipeInfo = ExtendedObjectProxy.getRecipeService().modify(eventInfo, recipeInfo);
		
		return doc;
	}
}
