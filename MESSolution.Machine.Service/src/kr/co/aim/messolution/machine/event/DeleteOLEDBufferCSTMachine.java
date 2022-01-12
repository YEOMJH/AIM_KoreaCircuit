package kr.co.aim.messolution.machine.event;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.BufferCSTTransInfo;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class DeleteOLEDBufferCSTMachine extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> machineList = SMessageUtil.getBodySequenceItemList(doc, "MACHINELIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeleteOLEDBufferCSTMachine", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		for (Element machine : machineList)
		{
			String machineName = SMessageUtil.getChildText(machine, "MACHINENAME", true);
			String toMachineName = SMessageUtil.getChildText(machine, "TOMACHINENAME", true);
			String toPortName = SMessageUtil.getChildText(machine, "TOPORTNAME", true);

			BufferCSTTransInfo dataInfo = ExtendedObjectProxy.getBufferCSTTransInfoService().getBufferCSTTransInfo(machineName, toMachineName, toPortName);

			ExtendedObjectProxy.getBufferCSTTransInfoService().remove(eventInfo, dataInfo);
		}

		return doc;
	}

}
