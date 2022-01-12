package kr.co.aim.messolution.recipe.event;

import org.jdom.Document;
import org.jdom.Element;

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

public class CreateRecipe extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", false);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);		
		MESRecipeServiceProxy.getRecipeServiceUtil().verifyUserPrivilege(getEventUser(), machineName);		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", getEventUser(), getEventComment(), null, null);		
		//recipe register
		for (Element eleParameter : SMessageUtil.getBodySequenceItemList(doc, "RECIPELIST", true))
		{
			String recipeName = SMessageUtil.getChildText(eleParameter, "RECIPENAME", true);			
			Recipe recipeInfo = new Recipe(machineName, recipeName);			
			//default spec info
			recipeInfo.setRecipeState("Created");
			recipeInfo.setActiveState(GenericServiceProxy.getConstantMap().Spec_NotActive);
			recipeInfo.setRecipeType("MAIN");
			recipeInfo.setDurationUsedLimit(0);
			recipeInfo.setTimeUsedLimit(0);
			recipeInfo.setTotalTimeUsed(0);
			recipeInfo.setTimeUsed(0);			
			//history trace
			recipeInfo.setLastEventComment(eventInfo.getEventName());
			recipeInfo.setLastEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
			recipeInfo.setLastEventUser(eventInfo.getEventUser());
			recipeInfo.setLastEventComment(eventInfo.getEventComment());			
			recipeInfo = ExtendedObjectProxy.getRecipeService().create(eventInfo, recipeInfo);
		}
		
		return doc;
	}
}
