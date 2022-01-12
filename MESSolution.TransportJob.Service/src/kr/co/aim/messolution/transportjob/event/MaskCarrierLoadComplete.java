package kr.co.aim.messolution.transportjob.event;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class MaskCarrierLoadComplete extends AsyncHandler {

	/**
	 * MessageSpec [MCS -> TEX -> FMC]
	 * 
	 * <Body>
	 *    <CARRIERNAME />
	 *    <SLOTMAP />
	 *    <MACHINENAME />
	 *    <PORTNAME />
	 * </Body>
	 */
	
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Load", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);

		// change carrier TransferState & LocationInfo
		MESDurableServiceProxy.getDurableServiceImpl().makeTransportStateOnEQP(eventInfo, carrierName, machineName, portName);

		// change portInfo
		MESPortServiceProxy.getPortServiceUtil().loadComplete(eventInfo, machineName, portName);

		Document copyDoc = (Document) doc.clone();

		// RequestMaskBatchJobRequest
		SMessageUtil.setHeaderItemValue(copyDoc, "MESSAGENAME", "RequestMaskBatchJobRequest");

		try
		{
			GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("TEMsvr"), copyDoc, "TEMSender");
		}
		catch (Exception ex)
		{
			eventLog.warn("TEM Report Failed!");
		}

		// append full state
		Element eleFullState = new Element("FULLSTATE");
		eleFullState.setText("FULL");
		SMessageUtil.getBodyElement(doc).addContent(eleFullState);

		// success then report to FMB
		GenericServiceProxy.getESBServive().sendBySenderToFMB(doc);
	}
}