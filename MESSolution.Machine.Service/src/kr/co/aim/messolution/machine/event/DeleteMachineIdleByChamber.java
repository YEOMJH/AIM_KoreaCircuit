package kr.co.aim.messolution.machine.event;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class DeleteMachineIdleByChamber extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> chamberList = SMessageUtil.getBodySequenceItemList(doc, "CHAMBERLIST", true);

		for (Element chamber : chamberList)
		{
			String machineName = SMessageUtil.getChildText(chamber, "MACHINENAME", true);
			String chamberName = SMessageUtil.getChildText(chamber, "CHAMBERNAME", true);
			String processOperationName = SMessageUtil.getChildText(chamber, "PROCESSOPERATIONNAME", true);
			String processOperationVersion = SMessageUtil.getChildText(chamber, "PROCESSOPERATIONVERSION", true);
			
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);

			ExtendedObjectProxy.getMachineIdleByChamberService().deleteMachineIdleByChamber(eventInfo, machineName, chamberName, processOperationName, processOperationVersion);
		}

		return doc;
	}

}