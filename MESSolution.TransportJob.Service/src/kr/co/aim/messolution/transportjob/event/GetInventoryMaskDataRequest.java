package kr.co.aim.messolution.transportjob.event;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;

public class GetInventoryMaskDataRequest extends SyncHandler {

	/**
	 * MessageSpec [OIC -> TEX -> MCS]
	 * 
	 * <Body>
	 *    <MACHINENAME />
	 * </Body>
	 */
	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("GetInventoryMaskDataRequest", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		String messageName = SMessageUtil.getHeaderItemValue(doc, "MESSAGENAME", true);

		try
		{
			doc.getRootElement().getChild("Header").getChild("EVENTCOMMENT").setText(messageName);
			String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
			eventLog.debug("EQP ID=" + machineName);

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
