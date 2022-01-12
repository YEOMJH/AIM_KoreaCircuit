package kr.co.aim.messolution.durable.event.CNX;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.EVAReserveSpec;
import kr.co.aim.messolution.extended.object.management.impl.EVAReserveSpecService;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

public class ManagementReserveSpec extends SyncHandler {
	private Log log = LogFactory.getLog(ManagementReserveSpec.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String msName = SMessageUtil.getBodyItemValue(doc, "MSNAME", true);
		String maskKind = SMessageUtil.getBodyItemValue(doc, "MASKKIND", true);
		String maskType = SMessageUtil.getBodyItemValue(doc, "MASKTYPE", true);
		String reserveLimit = SMessageUtil.getBodyItemValue(doc, "RESERVELIMIT", true);
		String autoFlag = SMessageUtil.getBodyItemValue(doc, "AUTOFLAG", true);
		String actionName = SMessageUtil.getBodyItemValue(doc, "ACTIONNAME", true);

		if (StringUtil.equals(actionName, "Create"))
		{
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", this.getEventUser(), this.getEventComment(), "", "", "");
			eventInfo.setLastEventTimekey(TimeStampUtil.getCurrentEventTimeKey());

			EVAReserveSpec dataInfo = new EVAReserveSpec();
			dataInfo.setMSName(msName);
			dataInfo.setMaskKind(maskKind);
			dataInfo.setMaskType(maskType);
			dataInfo.setReserveLimit(Long.parseLong(reserveLimit));
			dataInfo.setAutoFlag(autoFlag);
			dataInfo.setLastEventName(eventInfo.getEventName());
			dataInfo.setLastEventTime(eventInfo.getEventTime());
			dataInfo.setLastEventTimeKey(eventInfo.getLastEventTimekey());
			dataInfo.setLastEventUser(eventInfo.getEventUser());
			dataInfo.setLastEventComment(eventInfo.getEventComment());

			ExtendedObjectProxy.getEVAReserveSpecService().create(eventInfo, dataInfo);
		}
		else if (StringUtil.equals(actionName, "Modify"))
		{
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("Modify", this.getEventUser(), this.getEventComment(), "", "", "");
			eventInfo.setLastEventTimekey(TimeStampUtil.getCurrentEventTimeKey());

			String condition = "MSNAME = ?";

			EVAReserveSpec dataInfo = ExtendedObjectProxy.getEVAReserveSpecService().select(condition, new Object[] { msName }).get(0);
			dataInfo.setMaskKind(maskKind);
			dataInfo.setMaskType(maskType);
			dataInfo.setReserveLimit(Long.parseLong(reserveLimit));
			dataInfo.setAutoFlag(autoFlag);
			dataInfo.setLastEventName(eventInfo.getEventName());
			dataInfo.setLastEventTime(eventInfo.getEventTime());
			dataInfo.setLastEventTimeKey(eventInfo.getLastEventTimekey());
			dataInfo.setLastEventUser(eventInfo.getEventUser());
			dataInfo.setLastEventComment(eventInfo.getEventComment());

			ExtendedObjectProxy.getEVAReserveSpecService().modify(eventInfo, dataInfo);
		}
		else if (StringUtil.equals(actionName, "Delete"))
		{
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("Delete", this.getEventUser(), this.getEventComment(), "", "", "");
			eventInfo.setLastEventTimekey(TimeStampUtil.getCurrentEventTimeKey());

			String condition = "MSNAME = ?";

			EVAReserveSpec dataInfo = ExtendedObjectProxy.getEVAReserveSpecService().select(condition, new Object[] { msName }).get(0);

			ExtendedObjectProxy.getEVAReserveSpecService().delete(dataInfo);
			ExtendedObjectProxy.getEVAReserveSpecService().addHistory(eventInfo, "EVAReserveSpecHistory", dataInfo, LogFactory.getLog(EVAReserveSpecService.class));
		}

		return doc;
	}

}
