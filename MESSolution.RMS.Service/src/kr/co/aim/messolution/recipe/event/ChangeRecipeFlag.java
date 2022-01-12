package kr.co.aim.messolution.recipe.event;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.Recipe;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class ChangeRecipeFlag extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String jobCode = SMessageUtil.getBodyItemValue(doc, "JOBCODE", true);
		String flag = SMessageUtil.getBodyItemValue(doc, "FLAG", true);
		String depart=SMessageUtil.getBodyItemValue(doc, "DEPART", false);
		List<Element> recipeList = SMessageUtil.getBodySequenceItemList(doc, "RECIPELIST", true);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Recipe" + jobCode + "FlagChange", getEventUser(), getEventComment(), null, null);
		
		ConstantMap constMap = GenericServiceProxy.getConstantMap();

		for (Element recipeData : recipeList)
		{
			String machineName = recipeData.getChildText("MACHINENAME");
			String recipeName = recipeData.getChildText("RECIPENAME");
			String autoChangeFlag="";

			Recipe recipeInfo = ExtendedObjectProxy.getRecipeService().selectByKey(false, new Object[] { machineName, recipeName });

			if (jobCode.equals("INT"))
			{
				recipeInfo.setINTFlag(flag);
			}
			else if (jobCode.equals("CHECKTYPE"))
			{
				if (StringUtils.equals(flag, "UNIT"))
				{
					if (StringUtils.equals(recipeInfo.getUnitCheckFlag(), "Y"))
					{
						throw new CustomException("RECIPE-0001");
					}
					
					recipeInfo.setUnitCheckFlag("Y");
					recipeInfo.setVersionCheckFlag("N");
				}
				else if (StringUtils.equals(flag, "VERSION"))
				{
					if (StringUtils.equals(recipeInfo.getVersionCheckFlag(), "Y"))
					{
						throw new CustomException("RECIPE-0001");
					}
					
					recipeInfo.setUnitCheckFlag("N");
					recipeInfo.setVersionCheckFlag("Y");
				}
			}
			else if (jobCode.equals("ENG"))
			{
				recipeInfo.setENGFlag(flag);
			}
			else if (jobCode.equals("AUTOCHANGE"))
			{
				autoChangeFlag=recipeInfo.getAutoChangeFlag();
                if(flag.equals("Y"))
                {
                	if(autoChangeFlag.equals("N")||StringUtil.isEmpty(autoChangeFlag))
                	{
                		autoChangeFlag=depart;
                	}
                	else if(autoChangeFlag.contains(depart))
                	{
                		throw new CustomException("RECIPE-0001");
					}
                	else
                	{
                		autoChangeFlag+=depart;
                	}
                }
                else if(flag.equals("N"))
                {
                	if(!autoChangeFlag.contains(depart))
                	{
                		throw new CustomException("RECIPE-0001");
                	}
                	else
                	{
                		autoChangeFlag=autoChangeFlag.replace(depart, "");
                	}
                }
                if(StringUtil.isEmpty(autoChangeFlag))
                {
                	autoChangeFlag="N";
                }
				recipeInfo.setAutoChangeFlag(autoChangeFlag);
			}

			if (!jobCode.equals("AUTOCHANGE") && !jobCode.equals("CHECKTYPE") && (!recipeInfo.getINTFlag().equals("Y") || !recipeInfo.getMFGFlag().equals("Y") || !recipeInfo.getENGFlag().equals("Y")))
			{
				recipeInfo.setRMSFlag("N");
				recipeInfo.setActiveState(constMap.Spec_NotActive);
				recipeInfo.setRecipeState(constMap.RECIPESTATE_UNAPPROVED);
			}

			if (!jobCode.equals("AUTOCHANGE") && !jobCode.equals("CHECKTYPE") && recipeInfo.getINTFlag().equals("Y") && recipeInfo.getMFGFlag().equals("Y") && recipeInfo.getENGFlag().equals("Y") && !flag.equals("N"))
			{
				recipeInfo.setRMSFlag("Y");
				recipeInfo.setActiveState(constMap.Spec_Active);
				recipeInfo.setRecipeState(constMap.RECIPESTATE_APPROVED);
				recipeInfo.setLastApporveTime(eventInfo.getEventTime());
				recipeInfo.setLastTrackOutTimeKey("");
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
