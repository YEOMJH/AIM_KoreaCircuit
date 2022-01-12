package kr.co.aim.messolution.durable.event.CNX;

import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
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

public class ChangeMaskState extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String messageName = SMessageUtil.getMessageName(doc);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo(messageName, this.getEventUser(), this.getEventComment(), "", "");

		String durableName = SMessageUtil.getBodyItemValue(doc, "DURABLENAME", true);
		String durableState = SMessageUtil.getBodyItemValue(doc, "DURABLESTATE", true);

		// getDurableData
		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableName);

		if (StringUtils.equals(durableData.getDurableState(), GenericServiceProxy.getConstantMap().Dur_Scrapped))
			throw new CustomException("MASK-0012", durableName, durableData.getDurableState());
		
		Map<String, String> udfs = new HashMap<String, String>();

		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.setUdfs(udfs);

		durableData.setDurableState(durableState);

		DurableServiceProxy.getDurableService().update(durableData);
		
		MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);

		return doc;
	}

}
