package kr.co.aim.messolution.consumable.event;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.CrucibleLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.info.SetMaterialLocationInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;

public class ScrapOrganic extends SyncHandler {
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String consumableLotName = SMessageUtil.getBodyItemValue(doc, "CRUCIBLRLOTNAME", true);
		String sDurableName = SMessageUtil.getBodyItemValue(doc, "DURABLENAME", true);
		long weight = Long.parseLong(SMessageUtil.getBodyItemValue(doc, "Weight", true));
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();

		// Crucible Location Set Bank
		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sDurableName);
		
		durableData.setDurableState(constantMap.Dur_Available);
		durableData.setLotQuantity(0);
		DurableServiceProxy.getDurableService().update(durableData);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("RecycleOrganic", getEventUser(), getEventComment(), null, null);
		SetMaterialLocationInfo setMaterialLocationInfo = new SetMaterialLocationInfo();
		setMaterialLocationInfo.setMaterialLocationName("Bank");
		MESDurableServiceProxy.getDurableServiceImpl().setMaterialLocation(durableData, setMaterialLocationInfo, eventInfo);

		// CrucibleLot State Set Scrap
		eventInfo = EventInfoUtil.makeEventInfo("RecycleOrganic", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		CrucibleLot dataInfo = ExtendedObjectProxy.getCrucibleLotService().selectByKey(true, new Object[] { consumableLotName });
		dataInfo.setCrucibleLotState(constantMap.CrucibleLotState_Scrapped);
		dataInfo.setDurableName("");
		dataInfo.setWeight(weight);
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		ExtendedObjectProxy.getCrucibleLotService().modify(eventInfo, dataInfo);

		return doc;
	}

}
