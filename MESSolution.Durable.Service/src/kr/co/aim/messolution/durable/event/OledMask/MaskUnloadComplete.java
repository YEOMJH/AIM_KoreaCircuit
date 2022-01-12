package kr.co.aim.messolution.durable.event.OledMask;

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

public class MaskUnloadComplete extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Unload", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String maskLotName = SMessageUtil.getBodyItemValue(doc, "MASKNAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String bfSlotPosition = SMessageUtil.getBodyItemValue(doc, "BUFFERSLOTPOSITION", false);
		String portType = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", false);
		String portUseType = SMessageUtil.getBodyItemValue(doc, "PORTUSETYPE", false);
		String portAccessMode = SMessageUtil.getBodyItemValue(doc, "PORTACCESSMODE", false);

		MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().getMaskLotData(maskLotName);

		// Change mask TransferState & LocationInfo
		maskLotData.setMachineName("");
		maskLotData.setPortName("");
		maskLotData.setZoneName("");
		ExtendedObjectProxy.getMaskLotService().setTransferState(eventInfo, maskLotData, GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_MOVING);
		
		// change portInfo
		MESPortServiceProxy.getPortServiceUtil().unLoadComplete(eventInfo, machineName, portName);
		
		// append full state
		Element eleFullState = new Element("FULLSTATE");
		eleFullState.setText("EMPTY");
		SMessageUtil.getBodyElement(doc).addContent(eleFullState);

		// success then report to FMB
		GenericServiceProxy.getESBServive().sendBySenderToFMB(doc);
	}
}