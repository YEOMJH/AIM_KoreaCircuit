package kr.co.aim.messolution.durable.event;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class ChangeCSTAttributes extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String messageName = SMessageUtil.getMessageName(doc);
		String sDurableName = SMessageUtil.getBodyItemValue(doc, "DURABLENAME", true);
		String sTimeUsedLimit = SMessageUtil.getBodyItemValue(doc, "TIMEUSEDLIMIT", true);
		String sDurationUsedLimit = SMessageUtil.getBodyItemValue(doc, "DURATIONUSEDLIMIT", true);

		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sDurableName);

		if (StringUtils.isNotEmpty(sDurationUsedLimit))
		{
			durableData.setDurationUsedLimit(Double.parseDouble(sDurationUsedLimit));
		}

		if (StringUtils.isNotEmpty(sTimeUsedLimit))
		{
			durableData.setTimeUsedLimit(Double.parseDouble(sTimeUsedLimit));

		}

		DurableServiceProxy.getDurableService().update(durableData);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo(messageName, this.getEventUser(), this.getEventComment(), "", "");

		SetEventInfo setEventInfo = new SetEventInfo();
		MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);

		return doc;
	}

}
