package kr.co.aim.messolution.recipe.event;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ChangeRecipe;

import org.jdom.Document;

public class CreatePPIDChangeInfo extends SyncHandler {
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String processOperName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String afterProcessOperName = SMessageUtil.getBodyItemValue(doc, "AFTERPROCESSOPERATIONNAME", true);
		String afterRecipeName = SMessageUtil.getBodyItemValue(doc, "AFTERRECIPENAME", true);
		String changedTime = SMessageUtil.getBodyItemValue(doc, "CHANGEDTIME", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Created", getEventUser(), getEventComment(), null, null);
		
		ChangeRecipe changeRecipeInfo = new ChangeRecipe();
		
		changeRecipeInfo.setFactoryName(factoryName);
		changeRecipeInfo.setProductSpecName(productSpecName);
		changeRecipeInfo.setProcessFlowName(processFlowName);
		changeRecipeInfo.setProcessOperationName(processOperName);
		changeRecipeInfo.setMachineName(machineName);
		changeRecipeInfo.setAfterProcessOperName(afterProcessOperName);
		changeRecipeInfo.setAfterRecipeName(afterRecipeName);
		changeRecipeInfo.setChangedTime(TimeUtils.getTimestamp(changedTime));
		
		changeRecipeInfo.setLastEventComment(eventInfo.getEventComment());
		changeRecipeInfo.setLastEventName(eventInfo.getEventName());
		changeRecipeInfo.setLastEventTime(eventInfo.getEventTime());
		changeRecipeInfo.setLastEventUser(eventInfo.getEventUser());

		ExtendedObjectProxy.getChangeRecipeService().create(eventInfo, changeRecipeInfo);
		
		return doc;
	}
}
