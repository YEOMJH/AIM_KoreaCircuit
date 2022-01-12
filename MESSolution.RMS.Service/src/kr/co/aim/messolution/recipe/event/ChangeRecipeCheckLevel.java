package kr.co.aim.messolution.recipe.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.ListOrderedMap;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskStick;
import kr.co.aim.messolution.extended.object.management.data.POSMachine;
import kr.co.aim.messolution.extended.object.management.data.Recipe;
import kr.co.aim.messolution.extended.object.management.data.RecipeParameter;
import kr.co.aim.messolution.extended.object.management.data.TPFOPolicy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class ChangeRecipeCheckLevel extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{

		Element eleBody = SMessageUtil.getBodyElement(doc);

		if (eleBody != null)
		{
			for (Element eledur : SMessageUtil.getBodySequenceItemList(doc, "MACHINERECIPENAMELIST", false))
			{

				String conditionID = SMessageUtil.getChildText(eledur, "CONDITIONID", true);
				String machineName = SMessageUtil.getChildText(eledur, "MACHINENAME", true);
				String recipeName = SMessageUtil.getChildText(eledur, "MACHINERECIPENAME", true);
				String rollType = SMessageUtil.getChildText(eledur, "ROLLTYPE", false);
				String checkLevel = SMessageUtil.getChildText(eledur, "CHECKLEVEL", true);
				String machineRecipeName = SMessageUtil.getChildText(eledur, "MACHINERECIPENAME", false);
				String checkType = SMessageUtil.getChildText(eledur, "CHECKTYPE", false);

				EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeRecipeCheckLevel", this.getEventUser(), this.getEventComment(), "", "");
				eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
				eventInfo.setEventTime(null);

				POSMachine posMachine = ExtendedObjectProxy.getPOSMachineService().selectByKey(true, new Object[] { conditionID, machineName });
				posMachine.setCheckLevel(checkLevel);
				ExtendedObjectProxy.getPOSMachineService().modify(posMachine, eventInfo);

			}
		}

		return doc;
	}

}
