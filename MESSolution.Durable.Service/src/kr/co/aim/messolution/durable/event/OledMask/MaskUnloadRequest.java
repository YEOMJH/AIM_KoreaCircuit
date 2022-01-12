package kr.co.aim.messolution.durable.event.OledMask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class MaskUnloadRequest extends AsyncHandler {

	private static Log log = LogFactory.getLog(MaskUnloadRequest.class);

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String maskLotName = SMessageUtil.getBodyItemValue(doc, "MASKNAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String bfSlotPosition = SMessageUtil.getBodyItemValue(doc, "BUFFERSLOTPOSITION", false);
		String portType = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", false);
		String portUseType = SMessageUtil.getBodyItemValue(doc, "PORTUSETYPE", false);
		String portAccessMode = SMessageUtil.getBodyItemValue(doc, "PORTACCESSMODE", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("UnloadRequest", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		// change portInfo
		MESPortServiceProxy.getPortServiceUtil().unLoadRequest(eventInfo, machineName, portName);

		// send to TEMsvr
		String replySubject = GenericServiceProxy.getESBServive().makeCustomServerLocalSubject("TEMsvr");
		GenericServiceProxy.getESBServive().sendReplyBySender(replySubject, doc, "TEMSender");
	}
}
