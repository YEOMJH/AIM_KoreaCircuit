package kr.co.aim.messolution.transportjob.event;

import java.util.List;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.transportjob.MESTransportServiceProxy;
import kr.co.aim.messolution.transportjob.service.TransportJobServiceUtil;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class RequestMaskBatchJobReply extends AsyncHandler {

	/**
	 * MessageSpec [MCS -> TEX -> OIC]
	 * 
	 * <Body>
	 *    <BATCHJOBNAME />
	 *    <CARRIERNAME />
	 *    <MACHINENAME />
	 *    <SOURCEPOSITIONTYPE />
	 *    <SOURCEPOSITIONNAME />
	 *    <DESTINATIONPOSITIONTYPE />
	 *    <DESTINATIONPOSITIONNAME />
	 *    <TRANSPORTJOBLIST>
	 *       <TRANSPORTJOB>
	 *          <TRANSPORTJOBNAME />
	 *          <MASKNAME />
	 *          <SOURCEZONENAME />
	 *          <SOURCECARRIERSLOTNO />
	 *          <DESTINATIONZONENAME />
	 *          <DESTINATIONCARRIERSLOTNO />
	 *          <PRIORITY />
	 *          <MASKTYPE />
	 *       <TRANSPORTJOB>
	 *    <TRANSPORTJOBLIST>
	 * </Body>
	 */
	
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String returnCode = SMessageUtil.getReturnItemValue(doc, "RETURNCODE", false);

		EventInfo eventInfo = new EventInfo();
		if (StringUtils.equals(returnCode, "0"))
		{
			eventInfo = EventInfoUtil.makeEventInfo("TransportAccept", getEventUser(), getEventComment(), "", "");
		}
		else
		{
			eventInfo = EventInfoUtil.makeEventInfo("TransportReject", getEventUser(), getEventComment(), "", "");
		}
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		// Validation : Exist Carrier
		String batchJobName = SMessageUtil.getBodyItemValue(doc, "BATCHJOBNAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);

		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);

		// Update CT_TRANSPORTJOBCOMMAND
		MESTransportServiceProxy.getTransportJobServiceUtil().updateTransportJobCommand(batchJobName, doc, eventInfo);

		String jobState = TransportJobServiceUtil.getJobState(SMessageUtil.getMessageName(doc), doc);

		if (jobState.equals(GenericServiceProxy.getConstantMap().MCS_JOBSTATE_Rejected))
		{
			// Update Carrier TransportLockFlag
			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.getUdfs().put("TRANSPORTLOCKFLAG", "N");

			DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEventInfo);
		}

		List<Element> transportJobList = SMessageUtil.getBodySequenceItemList(doc, "TRANSPORTJOBLIST", true);

		for (Element transportJob : transportJobList)
		{
			String transportJobName = SMessageUtil.getChildText(transportJob, "TRANSPORTJOBNAME", true);
			
			// Update CT_TRANSPORTJOBCOMMAND+
			MESTransportServiceProxy.getTransportJobServiceUtil().updateTransportJobCommand(transportJobName, doc, eventInfo);
		}

		try
		{
			String originalSourceSubjectName = getOriginalSourceSubjectName();

			if (StringUtils.isNotEmpty(originalSourceSubjectName))
			{
				// Send Reply to OIC
				GenericServiceProxy.getESBServive().sendBySenderWithoutChangeReturnElement(originalSourceSubjectName, doc, "OICSender");
			}
		}
		catch (Exception e)
		{
		}
	}
}
