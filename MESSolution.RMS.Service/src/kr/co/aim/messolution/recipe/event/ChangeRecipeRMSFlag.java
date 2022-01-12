package kr.co.aim.messolution.recipe.event;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.POSMachine;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class ChangeRecipeRMSFlag extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> recipeList = SMessageUtil.getBodySequenceItemList(doc, "MACHINERECIPENAMELIST", true);

		for (Element recipe : recipeList)
		{
			String conditionID = SMessageUtil.getChildText(recipe, "CONDITIONID", true);
			String machineName = SMessageUtil.getChildText(recipe, "MACHINENAME", true);
			String machineRecipeName = SMessageUtil.getChildText(recipe, "MACHINERECIPENAME", true);
			String rmsFlag = SMessageUtil.getChildText(recipe, "RMSFLAG", true);
			String ecRecipeName = SMessageUtil.getChildText(recipe, "ECRECIPENAME", false);
			String ecRecipeFlag = SMessageUtil.getChildText(recipe, "ECRECIPEFLAG", false);
			//String checkType = SMessageUtil.getChildText(recipe, "CHECKTYPE", false);

			EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeRecipeRMSFlag", this.getEventUser(), this.getEventComment(), "", "");
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			eventInfo.setEventTime(null);

			POSMachine posMachine = ExtendedObjectProxy.getPOSMachineService().selectByKey(true, new Object[] { conditionID, machineName });
			posMachine.setRmsFlag(rmsFlag);
			posMachine.setEcRecipeFlag(ecRecipeFlag);
			ExtendedObjectProxy.getPOSMachineService().modify(posMachine, eventInfo);
		}

		return doc;
	}

}
