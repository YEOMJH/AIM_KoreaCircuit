package kr.co.aim.messolution.transportjob.event;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.transportjob.MESTransportServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;

public class DestinationChangeFailed extends AsyncHandler {

	/**
	 * MessageSpec [MCS -> TEX]
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
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeFail", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		String transportJobName = SMessageUtil.getBodyItemValue(doc, "TRANSPORTJOBNAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);

		MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);

		// Update CT_TRANSPORTJOBCOMMAND
		MESTransportServiceProxy.getTransportJobServiceUtil().updateTransportJobCommand(transportJobName, doc, eventInfo);
	}
}
