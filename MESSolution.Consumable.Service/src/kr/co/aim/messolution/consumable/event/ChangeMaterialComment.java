package kr.co.aim.messolution.consumable.event;

import org.jdom.Document;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class ChangeMaterialComment extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		String consumableName = SMessageUtil.getBodyItemValue(doc, "CONSUMABLENAME", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeMaterialComment", this.getEventUser(), this.getEventComment(), "", "");
		
		//Consumable consumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(consumableName);
		
		SetEventInfo setEventInfo = new SetEventInfo();
		//setEventInfo.getUdfs().put("DEPARTMENT", department);
		
		MESConsumableServiceProxy.getConsumableServiceImpl().setEvent(consumableName, setEventInfo, eventInfo);
		
		return doc;
	}

}
