package kr.co.aim.messolution.dispatch.event.CNX;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DSPControl;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class ModifyDSPControl extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String dspFlag = SMessageUtil.getBodyItemValue(doc, "DSPFLAG", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", false);
		
		List<Element> controlList = SMessageUtil.getBodySequenceItemList(doc, "CONTROLLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ModifyDSPControl", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		for (Element control : controlList)
		{
			String machineName = SMessageUtil.getChildText(control, "MACHINENAME", true);
			String processOperationName = SMessageUtil.getChildText(control, "PROCESSOPERATIONNAME", true);
			String processOperationVersion = SMessageUtil.getChildText(control, "PROCESSOPERATIONVERSION", true);

			DSPControl dataInfo = ExtendedObjectProxy.getDSPControlService().selectByKey(false, new Object[] { machineName, processOperationName, processOperationVersion });

			if (!StringUtils.isEmpty(dspFlag))
				dataInfo.setDspFlag(dspFlag);

			dataInfo.setLastEventName(eventInfo.getEventName());
			dataInfo.setLastEventUser(eventInfo.getEventUser());
			dataInfo.setLastEventTime(eventInfo.getEventTime());
			dataInfo.setLastEventComment(eventInfo.getEventComment());
			dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
			dataInfo.setPortName(portName);
			
			dataInfo = ExtendedObjectProxy.getDSPControlService().modify(eventInfo, dataInfo);
		}

		return doc;
	}

}
