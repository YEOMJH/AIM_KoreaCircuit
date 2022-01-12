package kr.co.aim.messolution.transportjob.event;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;

import org.jdom.Document;

public class UnloadRequest extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		// send to RTD
		Document rtnDoc = (Document) doc.clone();
		SMessageUtil.setHeaderItemValue(rtnDoc, "NAME", "Unload_Request");

		String replySubject = GenericServiceProxy.getESBServive().makeCustomServerLocalSubject("RTD");
		GenericServiceProxy.getESBServive().sendReplyBySender(replySubject, rtnDoc, "DSPSender");
	}
}

