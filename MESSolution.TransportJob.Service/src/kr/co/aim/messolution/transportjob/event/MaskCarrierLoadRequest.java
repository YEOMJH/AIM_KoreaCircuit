package kr.co.aim.messolution.transportjob.event;

import org.jdom.Document;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class MaskCarrierLoadRequest extends AsyncHandler {

	/**
	 * MessageSpec [MCS -> TEX -> DSP]
	 * 
	 * <Body>
	 *    <CARRIERNAME />
	 *    <MACHINENAME />
	 *    <PORTNAME />
	 * </Body>
	 */
	
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("LoadRequest", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		
		// change portInfo
		CommonValidation.checkIsOrNotTransferJob(machineName, portName);
		MESPortServiceProxy.getPortServiceUtil().loadRequest(eventInfo, machineName, portName);
		
		// send to RTD
		Document rtnDoc = (Document) doc.clone();
		SMessageUtil.setHeaderItemValue(rtnDoc, "NAME", "Load_Request");
		String replySubject = GenericServiceProxy.getESBServive().makeCustomServerLocalSubject("RTD");
		GenericServiceProxy.getESBServive().sendReplyBySender(replySubject, rtnDoc, "DSPSender");
	}
}