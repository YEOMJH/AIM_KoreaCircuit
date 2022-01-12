package kr.co.aim.messolution.dispatch.event.CNX;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.TryRunControl;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class ModifyTryRunControl extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{

		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String processOperationVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONVERSION", true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", false);
		String processFLowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", false);
		String useCount = SMessageUtil.getBodyItemValue(doc, "NEWUSECOUNT", false);
		String useCountLimit = SMessageUtil.getBodyItemValue(doc, "NEWUSECOUNTLIMIT", false);
		String useFlag = SMessageUtil.getBodyItemValue(doc, "USEFLAG", false);

		TryRunControl dataInfo = ExtendedObjectProxy.getTryRunControlService().selectByKey(false, new Object[] { machineName, processOperationName, processOperationVersion });

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ModifyTryRunControl", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		if (StringUtil.isNotEmpty(productSpecName))
			dataInfo.setProductSpecName(productSpecName);

		if (StringUtil.isNotEmpty(processFLowName))
			dataInfo.setProcessFlowName(processFLowName);

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
		ExtendedObjectProxy.getTryRunControlService().modify(eventInfo, dataInfo);

		return doc;
	}

}
