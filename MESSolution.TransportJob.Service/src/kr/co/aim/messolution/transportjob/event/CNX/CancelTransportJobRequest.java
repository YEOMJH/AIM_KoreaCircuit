package kr.co.aim.messolution.transportjob.event.CNX;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import org.jdom.Document;

public class CancelTransportJobRequest extends AsyncHandler {
	
	@Override
	public void doWorks(Document doc) throws CustomException {
		
		
		String replySubject = GenericServiceProxy.getESBServive().makeCustomServerLocalSubject("TEMsvr");
		GenericServiceProxy.getESBServive().sendReplyBySender(replySubject, doc, "TEMSender");
	}
	
}