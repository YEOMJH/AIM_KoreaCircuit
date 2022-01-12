package kr.co.aim.messolution.transportjob.event;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.transportjob.MESTransportServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;

public class TransportJobCancelFailed extends AsyncHandler {

	/**
	 * MessageSpec [MCS -> TEX]
	 * 
	 * <Body>
	 *    <TRANSPORTJOBNAME />
	 *    <CARRIERNAME />
	 *    <CURRENTMACHINENAME />
	 *    <CURRENTPOSITIONTYPE />
	 *    <CURRENTPOSITIONNAME />
	 *    <CURRENTZONENAME />
	 *    <CARRIERSTATE />
	 *    <TRANSFERSTATE />
	 *    <ALTERNATEFLAG />
	 * </Body>
	 */
	
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelFail", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		// Validation : Exist Carrier
		String transportJobName = SMessageUtil.getBodyItemValue(doc, "TRANSPORTJOBNAME", true);
		
		// Update CT_TRANSPORTJOBCOMMAND
		MESTransportServiceProxy.getTransportJobServiceUtil().updateTransportJobCommand(transportJobName, doc, eventInfo);
	}
}
