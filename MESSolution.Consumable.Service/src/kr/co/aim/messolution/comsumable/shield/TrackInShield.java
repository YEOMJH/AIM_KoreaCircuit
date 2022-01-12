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
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;

import org.jdom.Document;
import org.jdom.Element;

public class TrackInShield extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> shieldList = SMessageUtil.getBodySequenceItemList(doc, "SHIELDLIST", true);
		String durableName = SMessageUtil.getBodyItemValue(doc, "DURABLENAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String chamberID = SMessageUtil.getBodyItemValue(doc, "CHAMBERID", false);
		String operationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);

		Durable durInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableName);
		
		CommonValidation.CheckDurableHoldState(durInfo);
		
		ProcessOperationSpec operationSpec = CommonUtil.getProcessOperationSpec(factoryName, operationName, "00001");
		String detailOper = operationSpec.getDetailProcessOperationType();
		
		if(detailOper.equals("BANK") || detailOper.equals("Stock"))
		{
			if(!durInfo.getDurableType().equals("Car"))
			{
				throw new CustomException("SHIELD-0014");
			}
		}
		else
		{
			if(!durInfo.getDurableType().equals("Basket"))
			{
				throw new CustomException("SHIELD-0014");
			}
		}
		
		//不解绑，去分拣界面操作
//		if(detailOper.equals("BANK") || detailOper.equals("PACK"))
//		{
//			EventInfo eventInfoDur = EventInfoUtil.makeEventInfo("Deassign" + durInfo.getDurableType() + "Shield", this.getEventUser(), this.getEventComment(), "", "", "Y");
//			eventInfoDur.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
//			deassignCarrier(eventInfoDur, durInfo);
//		}
		
		List<ShieldLot> shieldLotList = new ArrayList<ShieldLot>();
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TrackInShield", this.getEventUser(), this.getEventComment(), "", "", "Y");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		for (Element shieldData : shieldList)
		{
			String shieldID = SMessageUtil.getChildText(shieldData, "SHIELDID", true);
			String sampleFlag = SMessageUtil.getChildText(shieldData, "SAMPLEFLAG", true);
			ShieldLot shieldLotData = ExtendedObjectProxy.getShieldLotService().selectByKey(true, new Object[] { shieldID });
			
			CommonValidation.CheckShieldHoldState(shieldLotData);
			CommonValidation.CheckShieldProcessState_WAIT(shieldLotData);
			CommonValidation.CheckShieldState_Released(shieldLotData);

			shieldLotData = setShieldData( shieldLotData, detailOper, eventInfo, machineName, chamberID, sampleFlag);
			
			shieldLotList.add(shieldLotData);
		}

		ExtendedObjectProxy.getShieldLotService().modify(eventInfo, shieldLotList);
		
		return doc;
	}
	
	private ShieldLot setShieldData(ShieldLot shieldLot, String detailOper, EventInfo eventInfo, String machineName, String chamberName, String sampleFlag)
	{
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		shieldLot.setLotProcessState(constantMap.Lot_Run);
		shieldLot.setMachineName(machineName);
		shieldLot.setChamberName(chamberName);
		shieldLot.setLastLoggedInTime(eventInfo.getEventTime());
		shieldLot.setLastLoggedInUser(eventInfo.getEventUser());
		if((detailOper.equals("Inspection") || detailOper.equals("BANK")) && sampleFlag.equals("True"))
		{
			shieldLot.setSampleFlag("Y");
		}
		else
		{
			shieldLot.setSampleFlag("N");
		}
//		if(detailOper.equals("PACK") || detailOper.equals("BANK"))
//		{
//			shieldLot.setCarrierName("");
//		}
		shieldLot.setLastEventUser(eventInfo.getEventUser());
		shieldLot.setLastEventTime(eventInfo.getEventTime());
		shieldLot.setLastEventTimekey(eventInfo.getEventTimeKey());
		shieldLot.setLastEventName(eventInfo.getEventName());
		shieldLot.setLastEventComment(eventInfo.getEventComment());
		
		return shieldLot;
	}
	
	private void deassignCarrier(EventInfo eventInfo, Durable durableData)
	{
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		durableData.setLotQuantity(0);
		durableData.setDurableState(constantMap.Dur_Available);

		DurableServiceProxy.getDurableService().update(durableData);
		SetEventInfo setEventInfo = new SetEventInfo();
		DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEventInfo);
	}
}
