package kr.co.aim.messolution.transportjob.event;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.event.LotProcessEnd;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class MaskCarrierUnloadRequest extends AsyncHandler {

	private static Log log = LogFactory.getLog(MaskCarrierUnloadRequest.class);
	
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
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("UnloadRequest", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);

		// change carrier TransferState & LocationInfo
		MESDurableServiceProxy.getDurableServiceImpl().makeTransportStateOnEQP(eventInfo, carrierName, machineName, portName);

		// change portInfo
		MESPortServiceProxy.getPortServiceUtil().unLoadRequest(eventInfo, machineName, portName);

		// 2020-12-18	dhko	Add Validation
		// UnloadRequest from 3MCTK03 machine does not send a transport order.
		if (StringUtils.equals(machineName, "3MSTK03") &&
				CommonUtil.equalsIn(portName, "P01", "P02"))
		{
			log.info("UnloadRequest from 3MCTK03 machine does not send a transport order.");
			return ;
		}

		// send to RTD
		Document rtnDoc = (Document) doc.clone();
		SMessageUtil.setHeaderItemValue(rtnDoc, "NAME", "Unload_Request");

		String replySubject = GenericServiceProxy.getESBServive().makeCustomServerLocalSubject("RTD");
		GenericServiceProxy.getESBServive().sendReplyBySender(replySubject, rtnDoc, "DSPSender");
	}
}