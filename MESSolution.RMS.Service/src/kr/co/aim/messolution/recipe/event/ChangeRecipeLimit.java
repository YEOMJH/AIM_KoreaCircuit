package kr.co.aim.messolution.recipe.event;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.map.ListOrderedMap;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.Recipe;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class ChangeRecipeLimit extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String jobCode = SMessageUtil.getBodyItemValue(doc, "JOBCODE", true);
		String limit = SMessageUtil.getBodyItemValue(doc, "LIMIT", true);
		List<Element> recipeList = SMessageUtil.getBodySequenceItemList(doc, "RECIPELIST", true);
		
		//MESRecipeServiceProxy.getRecipeServiceUtil().verifyUserPrivilege(getEventUser(), machineName);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Recipe" + jobCode + "LimitChange", getEventUser(), getEventComment(), null, null);
		
		for(Element recipeData : recipeList)
		{
			String machineName = recipeData.getChildText("MACHINENAME");
			String recipeName = recipeData.getChildText("RECIPENAME");
			
			Recipe recipeInfo = ExtendedObjectProxy.getRecipeService().selectByKey(false, new Object[] {machineName, recipeName});
		    
			if(jobCode.equals("Time"))
			{
				recipeInfo.setTimeUsedLimit(Long.parseLong(limit));;
			}
			else if(jobCode.equals("Duration"))
			{
				recipeInfo.setDurationUsedLimit(Double.parseDouble(limit));
			}
			else if(jobCode.equals("MaxDuration"))
			{
				java.sql.Timestamp maxDurationLimit = null;
				maxDurationLimit = TimeUtils.getTimestamp(limit);
				recipeInfo.setMaxDurationUsedLimit(maxDurationLimit);;
			}
			
			recipeInfo.setLastEventName(eventInfo.getEventName());
			recipeInfo.setLastEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
			recipeInfo.setLastEventUser(eventInfo.getEventUser());
			recipeInfo.setLastEventComment(eventInfo.getEventComment());
			recipeInfo = ExtendedObjectProxy.getRecipeService().modify(eventInfo, recipeInfo);
		}
		
		return doc;
	}
}
