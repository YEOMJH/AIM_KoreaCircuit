package kr.co.aim.messolution.durable.event.OledMask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class MaskLoadComplete extends AsyncHandler {

	private static Log log = LogFactory.getLog(MaskLoadComplete.class);

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String maskName = SMessageUtil.getBodyItemValue(doc, "MASKNAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String portType = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", false);
		String portUseType = SMessageUtil.getBodyItemValue(doc, "PORTUSETYPE", false);
		String portAccessMode = SMessageUtil.getBodyItemValue(doc, "PORTACCESSMODE", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Load", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().getMaskLotData(maskName);

		// Change mask TransferState & LocationInfo
		maskLotData.setMachineName(machineName);
		maskLotData.setPortName(portName);
		maskLotData.setZoneName("");
		ExtendedObjectProxy.getMaskLotService().setTransferState(eventInfo, maskLotData, GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_ONEQP);

		// change portInfo
		MESPortServiceProxy.getPortServiceUtil().loadComplete(eventInfo, machineName, portName);
		
		// append full state
		Element eleFullState = new Element("FULLSTATE");
		eleFullState.setText("FULL");
		SMessageUtil.getBodyElement(doc).addContent(eleFullState);

		// success then report to FMB
		GenericServiceProxy.getESBServive().sendBySenderToFMB(doc);
	}
}
