package kr.co.aim.messolution.port.event;

import org.jdom.Document;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class LoadRequest extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("LoadRequest", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true); 
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);
		String portType = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", false);
		String portUseType =SMessageUtil.getBodyItemValue(doc, "PORTUSETYPE", false);
		String portAccessMode = SMessageUtil.getBodyItemValue(doc, "PORTACCESSMODE", false);

		// change portInfo
		MESPortServiceProxy.getPortServiceUtil().loadRequest(eventInfo, machineName, portName);

		// send to TEMsvr
		try
		{
			String replySubject = GenericServiceProxy.getESBServive().makeCustomServerLocalSubject("TEMsvr");
			GenericServiceProxy.getESBServive().sendReplyBySender(replySubject, doc, "TEMSender");
		}
		catch (Exception e)
		{
		}
	}
}