package kr.co.aim.messolution.machine.event;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;

public class CreateOLEDBufferCSTMachine extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "FROMMACHINENAME", true);
		String toMachineName = SMessageUtil.getBodyItemValue(doc, "TOMACHINENAME", true);
		String toPortName = SMessageUtil.getBodyItemValue(doc, "TOPORTNAME", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateOLEDBufferCSTMachine", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		ExtendedObjectProxy.getBufferCSTTransInfoService().createBufferCSTTransInfo(eventInfo, machineName, toMachineName, toPortName, factoryName);

		return doc;
	}

}
