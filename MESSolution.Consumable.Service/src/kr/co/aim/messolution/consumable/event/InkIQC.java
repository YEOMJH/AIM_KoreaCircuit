package kr.co.aim.messolution.consumable.event;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.info.MakeNotAvailableInfo;
import kr.co.aim.greentrack.consumable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class InkIQC extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String consumableName = SMessageUtil.getBodyItemValue(doc, "CONSUMABLENAME", true);
		String result = SMessageUtil.getBodyItemValue(doc, "RESULT", true);

		Consumable consumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getMaterialData(consumableName);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("InkIQC", getEventUser(), getEventComment(), null, null);

		if (StringUtils.equals(result, "OK"))
		{
			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.getUdfs().put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MaterialLocation_InStock);
			MESConsumableServiceProxy.getConsumableServiceImpl().setEvent(consumableName, setEventInfo, eventInfo);
		}
		else
		{
			MakeNotAvailableInfo makeNotAvailableInfo = new MakeNotAvailableInfo();

			MESConsumableServiceProxy.getConsumableServiceImpl().makeNotAvailable(consumableData, makeNotAvailableInfo, eventInfo);
		}

		return doc;
	}
}
