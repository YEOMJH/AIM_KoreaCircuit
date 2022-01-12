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
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class JudgeShield extends SyncHandler {

	public Object doWorks(Document doc) throws CustomException
	{
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		List<Element> shieldList = SMessageUtil.getBodySequenceItemList(doc, "SHIELDLIST", true);
		String newCarrierName = SMessageUtil.getBodyItemValue(doc, "NEWCARRIERNAME", false);
		String judge = SMessageUtil.getBodyItemValue(doc, "JUDGE", true);
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		
		String processOperation = "";
		String nextNodeStack = ""; 
		Durable durableInfo = new Durable();

		List<ShieldLot> shieldLotList = new ArrayList<ShieldLot>();
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("JudgeShield", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		if(!StringUtil.isEmpty(newCarrierName))
		{
			durableInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(newCarrierName);
			
			CommonValidation.CheckDurableHoldState(durableInfo);
			CommonValidation.checkAvailableCst(durableInfo);
			
			ShieldLot durShield = ExtendedObjectProxy.getShieldLotService().checkShieldSpecForRun(newCarrierName, shieldList, durableInfo);
		}
		Element firstShiled = shieldList.get(0);
		String firstShieldID = SMessageUtil.getChildText(firstShiled, "SHIELDLOTNAME", true);
		
		ShieldLot firstShieldLot = ExtendedObjectProxy.getShieldLotService().selectByKey(false, new Object[] { firstShieldID });
		firstShieldLot.setJudge(judge);
		Node nextNode = ExtendedObjectProxy.getExtendedProcessFlowUtil().getNextNode(firstShieldLot);
		
		processOperation = nextNode.getNodeAttribute1();
		nextNodeStack = nextNode.getKey().getNodeId();
		
		ProcessOperationSpec operationSpec = CommonUtil.getProcessOperationSpec(factoryName, firstShieldLot.getProcessOperationName(), "00001");
		if(judge.equals("OK") && StringUtil.isNotEmpty(firstShieldLot.getCarrierName()))
		{
			Durable usingDurable = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(firstShieldLot.getCarrierName());
			
			if(usingDurable.getLotQuantity() != shieldList.size())
			{
				throw new CustomException("SHIELD-0020");
			}
		}
		
		String deassignCarrierName = "";
		
		for (Element eleShield : shieldList)
		{
			String sShieldID = SMessageUtil.getChildText(eleShield, "SHIELDLOTNAME", true);
			//String carrierName = SMessageUtil.getChildText(eleShield, "CARRIERNAME", false);
			
			ShieldLot shieldLot = ExtendedObjectProxy.getShieldLotService().selectByKey(false, new Object[] { sShieldID });
			
			CommonValidation.CheckShieldState_Released(shieldLot);
			CommonValidation.CheckShieldProcessState_RUN(shieldLot);
			CommonValidation.CheckShieldItemCommon(firstShieldLot, shieldLot);
			CommonValidation.CheckShieldItemLineSetChamber(firstShieldLot, shieldLot);
			
			shieldLot.setLotProcessState(constantMap.Lot_Wait);
			shieldLot.setChamberName("");
			shieldLot.setMachineName("");
			shieldLot.setNodeStack(nextNodeStack);
			shieldLot.setProcessOperationName(processOperation);
			shieldLot.setLastLoggedOutTime(eventInfo.getEventTime());
			shieldLot.setLastLoggedOutUser(eventInfo.getEventUser());
			shieldLot.setJudge(judge);
			shieldLot.setSampleFlag("");
			
			if(judge.equals("NG") || judge.equals("RP"))
			{
				if(operationSpec.getDetailProcessOperationType().equals("Inspection"))
				{
					if(StringUtil.isEmpty(deassignCarrierName))
					{
						deassignCarrierName = shieldLot.getCarrierName();
					}
					shieldLot.setCarrierName("");
				}
			}
			else if(judge.equals("OK"))
			{
				if(operationSpec.getDetailProcessOperationType().equals("BANK"))
				{
					if(StringUtil.isEmpty(deassignCarrierName))
					{
						deassignCarrierName = shieldLot.getCarrierName();
					}
					shieldLot.setCarrierName(newCarrierName);
				}
			}
			else
			{
				if(operationSpec.getDetailProcessOperationType().equals("Inspection"))
				{
					if(operationSpec.getDescription().equals("Drying"))
					{
						if(StringUtil.isEmpty(deassignCarrierName))
						{
							deassignCarrierName = shieldLot.getCarrierName();
						}
						shieldLot.setCarrierName(newCarrierName);
					}
				}
			}
			shieldLot.setLastEventUser(eventInfo.getEventUser());
			shieldLot.setLastEventTime(eventInfo.getEventTime());
			shieldLot.setLastEventTimekey(eventInfo.getEventTimeKey());
			shieldLot.setLastEventName(eventInfo.getEventName());
			shieldLot.setLastEventComment(eventInfo.getEventComment());
			
			shieldLotList.add(shieldLot);
		}

		ExtendedObjectProxy.getShieldLotService().modify(eventInfo, shieldLotList);
		
		EventInfo eventInfoDur = EventInfoUtil.makeEventInfo("DeassignCarrierShield", this.getEventUser(), this.getEventComment(), "", "", "Y");
		eventInfoDur.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		if(judge.equals("NG"))
		{
			if(operationSpec.getDetailProcessOperationType().equals("Inspection")&&StringUtil.isNotEmpty(deassignCarrierName))
			{
				deassignCarrier(eventInfoDur, deassignCarrierName, shieldLotList.size());
			}
			
			EventInfo eventInfoScrap = EventInfoUtil.makeEventInfo("ScrapShield", this.getEventUser(), this.getEventComment(), "", "", "Y");
			eventInfoScrap.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			
			List<ShieldLot> scrapList = new ArrayList<ShieldLot>();
			
			for (Element eleShield : shieldList)
			{
				String sShieldID = SMessageUtil.getChildText(eleShield, "SHIELDLOTNAME", true);
				ShieldLot shieldLot = ExtendedObjectProxy.getShieldLotService().selectByKey(false, new Object[] { sShieldID });
				
				shieldLot.setLotState(constantMap.Lot_Scrapped);
				shieldLot.setLastEventUser(eventInfo.getEventUser());
				shieldLot.setLastEventTime(eventInfo.getEventTime());
				shieldLot.setLastEventTimekey(eventInfo.getEventTimeKey());
				shieldLot.setLastEventName(eventInfo.getEventName());
				shieldLot.setLastEventComment(eventInfo.getEventComment());
				
				scrapList.add(shieldLot);
			}
			
			ExtendedObjectProxy.getShieldLotService().modify(eventInfoScrap, scrapList);
		}
		else if(judge.equals("RP"))
		{
			if(operationSpec.getDetailProcessOperationType().equals("Inspection")&&StringUtil.isNotEmpty(deassignCarrierName))
			{
				deassignCarrier(eventInfoDur, deassignCarrierName, shieldLotList.size());
			}
		}
		else if(judge.equals("OK"))
		{
			if(operationSpec.getDetailProcessOperationType().equals("BANK"))
			{
				if(StringUtil.isNotEmpty(deassignCarrierName))
				{
					deassignCarrier(eventInfoDur, deassignCarrierName, shieldLotList.size());
				}
				eventInfoDur.setEventName("AssignBasketShield");
				assignCarrier(eventInfoDur, newCarrierName, shieldLotList.size());
			}
		}
		else
		{
			if(operationSpec.getDetailProcessOperationType().equals("Inspection"))
			{
				if(operationSpec.getDescription().equals("Drying"))
				{
					if(StringUtil.isNotEmpty(deassignCarrierName))
					{
						deassignCarrier(eventInfoDur, deassignCarrierName, shieldLotList.size());
					}					
					eventInfoDur.setEventName("AssignBasketShield");
					eventInfoDur.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
					assignCarrier(eventInfoDur, newCarrierName, shieldLotList.size());
				}
			}
		}
		
		return doc;
	}
	
	private void deassignCarrier(EventInfo eventInfo, String durableName, int count) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableName);
		if(durableData.getLotQuantity() == count)
		{
			durableData.setDurableState(constantMap.Dur_Available);
		}
		durableData.setLotQuantity(durableData.getLotQuantity() - count);

		DurableServiceProxy.getDurableService().update(durableData);
		SetEventInfo setEventInfo = new SetEventInfo();
		DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEventInfo);
	}
	
	private void assignCarrier(EventInfo eventInfo, String durableName, int count) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableName);
		//Start 20210218 houxk
		//durableData.setDurableState(constantMap.Dur_InUse);
		//durableData.setLotQuantity(count);
		durableData.setLotQuantity(durableData.getLotQuantity() + count);

		if (StringUtils.equals(durableData.getDurableState(), "Available"))
			durableData.setDurableState("InUse");
		//End

		DurableServiceProxy.getDurableService().update(durableData);
		SetEventInfo setEventInfo = new SetEventInfo();
		DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEventInfo);
	}
}
