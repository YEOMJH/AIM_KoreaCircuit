package kr.co.aim.messolution.comsumable.shield;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.AlterOperationByJudge;
import kr.co.aim.messolution.extended.object.management.data.ShieldLot;
import kr.co.aim.messolution.extended.object.management.data.ShieldSpec;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.PolicyUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;

import org.jdom.Document;
import org.jdom.Element;

public class ScratchShield extends SyncHandler {

	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> shieldList = SMessageUtil.getBodySequenceItemList(doc, "SHIELDLIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ScratchShield", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		List<ShieldLot> shieldLotList = new ArrayList<ShieldLot>();
		
		for (Element eleShield : shieldList)
		{
			String sShieldID = SMessageUtil.getChildText(eleShield, "SHIELDID", true);
			
			ShieldLot shieldLot = ExtendedObjectProxy.getShieldLotService().selectByKey(false, new Object[] { sShieldID });
			
			shieldLot.setLastEventUser(eventInfo.getEventUser());
			shieldLot.setLastEventTime(eventInfo.getEventTime());
			shieldLot.setLastEventTimekey(eventInfo.getEventTimeKey());
			shieldLot.setLastEventName(eventInfo.getEventName());
			shieldLot.setLastEventComment(eventInfo.getEventComment());
			
			shieldLotList.add(shieldLot);
		}

		ExtendedObjectProxy.getShieldLotService().modify(eventInfo, shieldLotList);
		
		return doc;
	}
}
