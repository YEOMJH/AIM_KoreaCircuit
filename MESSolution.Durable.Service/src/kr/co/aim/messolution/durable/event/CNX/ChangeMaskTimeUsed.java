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
import org.jdom.Element;

public class ChangeMaskTimeUsed extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String messageName = SMessageUtil.getMessageName(doc);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo(messageName, this.getEventUser(), this.getEventComment(), "", "");

		Element eleBody = SMessageUtil.getBodyElement(doc);

		if (eleBody != null)
		{
			String sDurableName = SMessageUtil.getChildText(eleBody, "DURABLENAME", true);
			String sTimeUsedLimit = SMessageUtil.getChildText(eleBody, "TIMEUSEDLIMIT", true);

			// getDurableData
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sDurableName);

			// Validation - Check DurableType
			if (!(StringUtils.equals(durableData.getDurableType(), GenericServiceProxy.getConstantMap().PHMask)))
			{
				throw new CustomException("MASK-0005");
			}

			if (!StringUtils.isEmpty(sTimeUsedLimit))
			{
				durableData.setTimeUsedLimit(Double.valueOf(sTimeUsedLimit).doubleValue());
			}
			DurableServiceProxy.getDurableService().update(durableData);

			// Put data into UDF
			Map<String, String> udfs = new HashMap<String, String>();

			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.setUdfs(udfs);
			DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEventInfo);

		}
		return doc;
	}

}
