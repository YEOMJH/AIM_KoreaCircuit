package kr.co.aim.messolution.comsumable.shield;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

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
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

public class ChangeJudgeShield extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		
		String carrierName = SMessageUtil.getBodyItemValue(doc, "NEWCARRIERNAME", false);
		List<Element> shieldList = SMessageUtil.getBodySequenceItemList(doc, "SHIELDLIST", true);
		String shieldID = SMessageUtil.getChildText(shieldList.get(0), "SHIELDLOTNAME", true);
		String judge = SMessageUtil.getChildText(shieldList.get(0), "JUDGE", true);
		//boolean carMix = Boolean.parseBoolean(SMessageUtil.getBodyItemValue(doc, "CARMIX", true));

		ShieldLot shieldInfo = ExtendedObjectProxy.getShieldLotService().selectByKey(false, new Object[] { shieldID });
		
		CommonValidation.checkShieldLotHoldStateN(shieldInfo);
		CommonValidation.checkShieldLotProcessStateWait(shieldInfo);
		CommonValidation.CheckShieldState_Released(shieldInfo);
		
		if(!judge.equals("NG")&&!StringUtil.isEmpty(carrierName))
		{
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
			
//			if(shieldInfo.getCarrierName().equals(carrierName))
//			{
//				throw new CustomException("SHIELD-0023");
//			}
			
			ShieldLot durShieldLot = ExtendedObjectProxy.getShieldLotService().checkShieldSpecNotCreate(carrierName, shieldList, durableData);
			
			if(durShieldLot != null)
			{
				if(!durShieldLot.getJudge().equals(judge))
				{
					throw new CustomException("SHIELD-0022", durShieldLot.getJudge());
				}
			}
		}
		
		if(StringUtil.isNotEmpty(shieldInfo.getCarrierName()))
		{
			Durable durableInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(shieldInfo.getCarrierName());
			CommonValidation.CheckDurableHoldState(durableInfo);
			
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("Deassign" + durableInfo.getDurableType() + "Shield", this.getEventUser(), this.getEventComment(), "", "");
			eventInfo.setEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
			
			deassignCarrier(eventInfo,durableInfo, shieldInfo);
		}
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeShieldJudge", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
		
		ShieldLot shieldJudgeInfo = ExtendedObjectProxy.getShieldLotService().selectByKey(false, new Object[] { shieldID });
		
		shieldJudgeInfo.setCarrierName(carrierName);
		shieldJudgeInfo.setJudge(judge);
		if(judge.equals("NG"))
		{
			shieldJudgeInfo.setLotState(constantMap.Lot_Scrapped);
		}
		shieldJudgeInfo.setLastEventUser(eventInfo.getEventUser());
		shieldJudgeInfo.setLastEventTime(eventInfo.getEventTime());
		shieldJudgeInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
		shieldJudgeInfo.setLastEventName(eventInfo.getEventName());
		shieldJudgeInfo.setLastEventComment(eventInfo.getEventComment());
		
		ExtendedObjectProxy.getShieldLotService().modify(eventInfo, shieldJudgeInfo);
		
		if(!judge.equals("NG"))
		{
			Durable newCarrierInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
			assignCarrier(eventInfo, newCarrierInfo);
		}
		
		return doc;
	}
	
	private void deassignCarrier(EventInfo eventInfo, Durable durableInfo, ShieldLot shieldInfo) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();

		shieldInfo.setCarrierName("");
		shieldInfo.setLastEventUser(eventInfo.getEventUser());
		shieldInfo.setLastEventTime(eventInfo.getEventTime());
		shieldInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
		shieldInfo.setLastEventName(eventInfo.getEventName());
		shieldInfo.setLastEventComment(eventInfo.getEventComment());
		
		ExtendedObjectProxy.getShieldLotService().modify(eventInfo, shieldInfo);
		
		if(durableInfo.getLotQuantity() == 1)
		{
			durableInfo.setDurableState(constantMap.Dur_Available);
		}
		durableInfo.setLotQuantity(durableInfo.getLotQuantity() - 1);

		DurableServiceProxy.getDurableService().update(durableInfo);
		SetEventInfo setEventInfo = new SetEventInfo();
		DurableServiceProxy.getDurableService().setEvent(durableInfo.getKey(), eventInfo, setEventInfo);
	}
	
	private void assignCarrier(EventInfo eventInfo, Durable durableInfo) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();

		//Start 20210218 houxk
		//durableInfo.setDurableState(constantMap.Dur_InUse);
		//durableInfo.setLotQuantity(1);
		durableInfo.setLotQuantity(durableInfo.getLotQuantity() + 1);

		if (StringUtils.equals(durableInfo.getDurableState(), "Available"))
			durableInfo.setDurableState("InUse");
		//End

		DurableServiceProxy.getDurableService().update(durableInfo);
		SetEventInfo setEventInfo = new SetEventInfo();
		DurableServiceProxy.getDurableService().setEvent(durableInfo.getKey(), eventInfo, setEventInfo);
	}
}
