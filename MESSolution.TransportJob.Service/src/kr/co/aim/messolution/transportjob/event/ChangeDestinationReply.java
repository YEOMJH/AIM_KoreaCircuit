package kr.co.aim.messolution.transportjob.event;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.transportjob.MESTransportServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class ChangeDestinationReply extends AsyncHandler {

	/**
	 * MessageSpec [MCS -> TEX -> OIC]
	 * 
	 * <Body>
	 *    <TRANSPORTJOBNAME />
	 *    <CARRIERNAME />
	 *    <OLDDESTINATIONMACHINENAME />
	 *    <OLDDESTINATIONPOSITIONTYPE />
	 *    <OLDDESTINATIONPOSITIONNAME />
	 *    <OLDDESTINATIONZONENAME />
	 *    <NEWDESTINATIONMACHINENAME />
	 *    <NEWDESTINATIONPOSITIONTYPE />
	 *    <NEWDESTINATIONPOSITIONNAME />
	 *    <NEWDESTINATIONZONENAME />
	 *    <PRIORITY />
	 * </Body>
	 */
	
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String returnCode = SMessageUtil.getReturnItemValue(doc, "RETURNCODE", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeAccept", "MCS", getEventComment(), "", "");
		if (!StringUtil.equals(returnCode, "0"))
		{
			eventInfo.setEventName("ChangeReject");
		}
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		String transportJobName = SMessageUtil.getBodyItemValue(doc, "TRANSPORTJOBNAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);

		// Validation : Exist Carrier
		MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);

		// Update CT_TRANSPORTJOBCOMMAND
		MESTransportServiceProxy.getTransportJobServiceUtil().updateTransportJobCommand(transportJobName, doc, eventInfo);

		String originalSourceSubjectName = getOriginalSourceSubjectName();

		if (StringUtils.isNotEmpty(originalSourceSubjectName))
		{
			if (!StringUtil.equals(returnCode, "0"))
			{
				// Set ReturnMessage - add [MCS Exception]
				String returnMessage = SMessageUtil.getReturnItemValue(doc, "RETURNMESSAGE", false);
				SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, "[MCS Exception] \n" + returnMessage);
			}

			// Send Reply to OIC
			GenericServiceProxy.getESBServive().sendBySenderWithoutChangeReturnElement(originalSourceSubjectName, doc, "OICSender");
		}
	}
}
