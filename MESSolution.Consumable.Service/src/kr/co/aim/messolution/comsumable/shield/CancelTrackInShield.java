package kr.co.aim.messolution.comsumable.shield;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ShieldLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class CancelTrackInShield extends SyncHandler {

	public Object doWorks(Document doc) throws CustomException
	{
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		List<Element> shieldList = SMessageUtil.getBodySequenceItemList(doc, "SHIELDLIST", true);
		String durableName = SMessageUtil.getBodyItemValue(doc, "NEWCARRIERNAME", false);
		
		boolean durFlag = false;
		
		Durable durableInfo = new Durable();

		List<ShieldLot> shieldLotList = new ArrayList<ShieldLot>();
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelTrackInShield", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		ShieldLot firstShieldLot = new ShieldLot();
		Element firstShiled = shieldList.get(0);
		
		String firstShieldID = SMessageUtil.getChildText(firstShiled, "SHIELDID", true);
		firstShieldLot = ExtendedObjectProxy.getShieldLotService().selectByKey(false, new Object[] { firstShieldID });
		
		if(!StringUtil.isEmpty(durableName))
		{
			durFlag = true;
			durableInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableName);
			
			CommonValidation.CheckDurableHoldState(durableInfo);
			CommonValidation.checkAvailableCst(durableInfo);
		}
		
		for (Element eleShield : shieldList)
		{
			String sShieldID = SMessageUtil.getChildText(eleShield, "SHIELDID", true);
			
			ShieldLot shieldLot = ExtendedObjectProxy.getShieldLotService().selectByKey(false, new Object[] { sShieldID });
			
			CommonValidation.CheckShieldState_Released(shieldLot);
			CommonValidation.CheckShieldProcessState_RUN(shieldLot);
			CommonValidation.CheckShieldItemCommon(firstShieldLot, shieldLot);

			if(durFlag)
			{
				CommonValidation.CheckShieldItemLineSetChamber(firstShieldLot, shieldLot);
			}
			
			shieldLot.setLotProcessState(constantMap.Lot_Wait);
			shieldLot.setChamberName("");
			shieldLot.setMachineName("");
			shieldLot.setSampleFlag("N");
			if(durFlag)
			{
				shieldLot.setCarrierName(durableName);
			}
			shieldLot.setLastEventUser(eventInfo.getEventUser());
			shieldLot.setLastEventTime(eventInfo.getEventTime());
			shieldLot.setLastEventTimekey(eventInfo.getEventTimeKey());
			shieldLot.setLastEventName(eventInfo.getEventName());
			shieldLot.setLastEventComment(eventInfo.getEventComment());
			
			shieldLotList.add(shieldLot);
			
			//20210218 houxk
			if(durFlag && StringUtil.isNotEmpty(shieldLot.getCarrierName()))
			{
				Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(shieldLot.getCarrierName());
				long lotQuantity = durableData.getLotQuantity() - 1;
				durableData.setLotQuantity(lotQuantity);
				if (lotQuantity < 1)
				{
					durableData.setDurableState("Available");
				}
				DurableServiceProxy.getDurableService().update(durableData);
				SetEventInfo setEventInfo = new SetEventInfo();
				DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEventInfo);
			}
		}

		ExtendedObjectProxy.getShieldLotService().modify(eventInfo, shieldLotList);
		
		if(durFlag)
		{
			eventInfo.setEventName("Assign" + durableInfo.getDurableType() + "Shield");
			
			//Start 20210218 houxk
			//durableInfo.setLotQuantity(shieldList.size());
			//durableInfo.setDurableState(constantMap.Dur_InUse);
			durableInfo.setLotQuantity(durableInfo.getLotQuantity() + shieldList.size());

			if (StringUtils.equals(durableInfo.getDurableState(), "Available"))
				durableInfo.setDurableState("InUse");
			//End
			
			DurableServiceProxy.getDurableService().update(durableInfo);
			SetEventInfo setEventInfo = new SetEventInfo();
			DurableServiceProxy.getDurableService().setEvent(durableInfo.getKey(), eventInfo, setEventInfo);
		}
		
		return doc;
	}
}
