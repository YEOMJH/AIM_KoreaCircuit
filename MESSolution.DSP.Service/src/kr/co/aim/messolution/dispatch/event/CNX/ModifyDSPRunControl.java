package kr.co.aim.messolution.dispatch.event.CNX;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DSPRunControl;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class ModifyDSPRunControl extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String useCount = SMessageUtil.getBodyItemValue(doc, "NEWUSECOUNT", false);
		String useCountLimit = SMessageUtil.getBodyItemValue(doc, "NEWUSECOUNTLIMIT", false);
		String useFlag = SMessageUtil.getBodyItemValue(doc, "USEFLAG", false);

		List<Element> controlList = SMessageUtil.getBodySequenceItemList(doc, "CONTROLLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ModifyDSPRunControl", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		for (Element control : controlList)
		{
			String machineName = SMessageUtil.getChildText(control, "MACHINENAME", true);
			String processOperationName = SMessageUtil.getChildText(control, "PROCESSOPERATIONNAME", true);
			String processOperationVersion = SMessageUtil.getChildText(control, "PROCESSOPERATIONVERSION", true);

			DSPRunControl dataInfo = ExtendedObjectProxy.getDSPRunControlService().selectByKey(false, new Object[] { machineName, processOperationName, processOperationVersion });

			if (!StringUtils.isEmpty(useFlag))
				dataInfo.setUseFlag(useFlag);

			if (!StringUtils.isEmpty(useCount))
				dataInfo.setUseCount(Integer.parseInt(useCount));

			if (!StringUtils.isEmpty(useCountLimit))
				dataInfo.setUseCountLimit(Integer.parseInt(useCountLimit));

			dataInfo.setLastEventName(eventInfo.getEventName());
			dataInfo.setLastEventUser(eventInfo.getEventUser());
			dataInfo.setLastEventTime(eventInfo.getEventTime());
			dataInfo.setLastEventComment(eventInfo.getEventComment());
			dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());

			dataInfo = ExtendedObjectProxy.getDSPRunControlService().modify(eventInfo, dataInfo);
		}

		return doc;
	}

}
