package kr.co.aim.messolution.transportjob.event;

import kr.co.aim.messolution.extended.object.management.data.TransportJobCommand;
import kr.co.aim.messolution.transportjob.MESTransportServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;

public class CancelMaskTransportJobRequest extends SyncHandler {

	/**
	 * MessageSpec [OIC -> TEX -> MSC]
	 * 
	 * <Body>
	 *    <TRANSPORTJOBNAME />
	 *    <CARRIERNAME />
	 * </Body>
	 */
	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		try
		{
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelRequest", getEventUser(), getEventComment(), "", "");
			eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
	
			String transportJobName = SMessageUtil.getBodyItemValue(doc, "TRANSPORTJOBNAME", true);
	
			String messageName = SMessageUtil.getHeaderItemValue(doc, "MESSAGENAME", true);
	
			TransportJobCommand transprotJobCommandInfo = MESTransportServiceProxy.getTransportJobServiceUtil().getTransportJobInfo(transportJobName);
	
			if (StringUtil.equals(transprotJobCommandInfo.getJobState(), "Completed") ||
				StringUtil.equals(transprotJobCommandInfo.getJobState(), "Terminated") ||
				StringUtil.equals(transprotJobCommandInfo.getJobState(), "Rejected"))
			{
				throw new CustomException("JOB-2011", transprotJobCommandInfo.getJobState());
			}
	
			// Update CT_TRANSPORTJOBCOMMAND
			MESTransportServiceProxy.getTransportJobServiceUtil().updateTransportJobCommand(transportJobName, doc, eventInfo);
	
			doc.getRootElement().getChild("Header").getChild("EVENTCOMMENT").setText(messageName);
			String replySubject = GenericServiceProxy.getESBServive().getSendSubject("MCS");
			GenericServiceProxy.getESBServive().sendReplyBySender(replySubject, doc, "HIFSender");
	
			return doc;
		}
		catch(Exception ex)
		{
			String originalSourceSubjectName = getOriginalSourceSubjectName();
			
			// Reply NG to OIC
			GenericServiceProxy.getESBServive().sendErrorBySender(originalSourceSubjectName, doc, getLanguage(), ex, "OICSender");
			
			return null;
		}
	}
}
