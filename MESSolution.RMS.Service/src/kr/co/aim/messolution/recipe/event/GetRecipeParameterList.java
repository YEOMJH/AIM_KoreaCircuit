package kr.co.aim.messolution.recipe.event;

import org.jdom.Document;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;

public class GetRecipeParameterList extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		
		
		
		//from CNX to PEM
		String targetSubjectName = GenericServiceProxy.getESBServive().getSendSubject("PEMsvr");
		
		GenericServiceProxy.getESBServive().sendBySender(targetSubjectName, doc, "PEMSender");
	}
}
