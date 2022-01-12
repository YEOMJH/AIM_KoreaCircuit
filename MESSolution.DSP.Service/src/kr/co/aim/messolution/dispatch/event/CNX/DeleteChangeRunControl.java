package kr.co.aim.messolution.dispatch.event.CNX;

import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class DeleteChangeRunControl extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> controlList = SMessageUtil.getBodySequenceItemList(doc, "CONTROLLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeleteChangeRunControl", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		for (Element control : controlList)
		{
			String machineName = SMessageUtil.getChildText(control, "MACHINENAME", true);
			String processOperationName = SMessageUtil.getChildText(control, "PROCESSOPERATIONNAME", true);
			String processOperationVersion = SMessageUtil.getChildText(control, "PROCESSOPERATIONVERSION", true);

			ExtendedObjectProxy.getChangeRunControlService().deleteChangeRunControlData(eventInfo, machineName, processOperationName, processOperationVersion);
		}

		return doc;
	}

}
