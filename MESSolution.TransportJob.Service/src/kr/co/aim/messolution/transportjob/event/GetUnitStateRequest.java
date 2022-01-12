package kr.co.aim.messolution.transportjob.event;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;

import org.jdom.Document;

public class GetUnitStateRequest extends SyncHandler {

	/**
	 * MessageSpec [TEX -> MCS]
	 * 
	 * <Body>
	 *    <MACHINENAME />
	 * </Body>
	 */
	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		try
		{
			String messageName = SMessageUtil.getHeaderItemValue(doc, "MESSAGENAME", true);
			doc.getRootElement().getChild("Header").getChild("EVENTCOMMENT").setText(messageName);

			// Send Message to MCS
			String replySubject = GenericServiceProxy.getESBServive().getSendSubject("MCS");
			GenericServiceProxy.getESBServive().sendReplyBySender(replySubject, doc, "HIFSender");
		}
		catch (Exception e)
		{
			String originalSourceSubjectName = getOriginalSourceSubjectName();
			GenericServiceProxy.getESBServive().sendErrorBySender(originalSourceSubjectName, doc, getLanguage(), e, "OICSender");

			throw new CustomException(e);
		}
		return null;
	}
}
