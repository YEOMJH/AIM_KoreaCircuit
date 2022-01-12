package kr.co.aim.messolution.recipe.event;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ChangeRecipe;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class DeletePPIDChangeInfo extends SyncHandler {
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String processOperName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String afterProcessOperName = SMessageUtil.getBodyItemValue(doc, "AFTERPROCESSOPERATIONNAME", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Deleted", getEventUser(), getEventComment(), null, null);
		
		ChangeRecipe changeRecipeInfo = 
				ExtendedObjectProxy.getChangeRecipeService().selectByKey(false, new Object[] {factoryName, productSpecName, processFlowName, processOperName, machineName, afterProcessOperName});
		
		ExtendedObjectProxy.getChangeRecipeService().remove(eventInfo, changeRecipeInfo);
		
		return doc;
	}
}
