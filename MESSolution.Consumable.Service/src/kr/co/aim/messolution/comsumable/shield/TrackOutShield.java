package kr.co.aim.messolution.comsumable.shield;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.ExtendedProcessFlowUtil;
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
import kr.co.aim.greentrack.processflow.management.data.NodeKey;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class TrackOutShield extends SyncHandler {

	public Object doWorks(Document doc) throws CustomException
	{
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		List<Element> shieldList = SMessageUtil.getBodySequenceItemList(doc, "SHIELDLIST", true);
		String durableName = SMessageUtil.getBodyItemValue(doc, "DURABLENAME", false);
		String judge = SMessageUtil.getBodyItemValue(doc, "JUDGE", false);
		
		boolean durFlag = false;
		String nextOperationName = "";
		String nextOperationVersion = "";
		String nextNodeStack = ""; 
		String nextProcessFlowName = "";
		String nextProcessFlowVersion = "";
		Durable durableInfo = new Durable();

		List<ShieldLot> shieldLotList = new ArrayList<ShieldLot>();
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TrackOutShield", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		if(!StringUtil.isEmpty(durableName))
		{
			durFlag = true;
			durableInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableName);
			
			CommonValidation.CheckDurableHoldState(durableInfo);
			CommonValidation.checkAvailableCst(durableInfo);
		}
		Element firstShiled = shieldList.get(0);
		String firstShieldID = SMessageUtil.getChildText(firstShiled, "SHIELDID", true);
		
		ShieldLot firstShieldLot = ExtendedObjectProxy.getShieldLotService().selectByKey(false, new Object[] { firstShieldID });
		firstShieldLot.setJudge(judge);
		
		Node nextNode = ExtendedObjectProxy.getExtendedProcessFlowUtil().getNextNode(firstShieldLot);
		NodeKey nodekey = nextNode.getKey();
		nextNodeStack = nodekey.getNodeId();
		nextProcessFlowName = nextNode.getProcessFlowName();
		nextProcessFlowVersion = nextNode.getProcessFlowVersion();
		nextOperationName = nextNode.getNodeAttribute1();
		nextOperationVersion = nextNode.getNodeAttribute2();
		
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
			shieldLot.setNodeStack(nextNodeStack);
			shieldLot.setProcessFlowName(nextProcessFlowName);
			shieldLot.setProcessFlowVersion(nextProcessFlowVersion);
			shieldLot.setProcessOperationName(nextOperationName);
			shieldLot.setProcessOperationVersion(nextOperationVersion);
			shieldLot.setLastLoggedOutTime(eventInfo.getEventTime());
			shieldLot.setLastLoggedOutUser(eventInfo.getEventUser());
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
		
		EventInfo eventInfoDur = EventInfoUtil.makeEventInfo("Assign" + durableInfo.getDurableType() + "Shield", this.getEventUser(), this.getEventComment(), "", "", "Y");
		eventInfoDur.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		if(durFlag)
		{
			//Start 20210218 houxk
			//durableInfo.setLotQuantity(shieldList.size());
			//durableInfo.setDurableState(constantMap.Dur_InUse);
			durableInfo.setLotQuantity(durableInfo.getLotQuantity() + shieldList.size());

			if (StringUtils.equals(durableInfo.getDurableState(), "Available"))
				durableInfo.setDurableState("InUse");
			//End

			DurableServiceProxy.getDurableService().update(durableInfo);
			SetEventInfo setEventInfo = new SetEventInfo();
			DurableServiceProxy.getDurableService().setEvent(durableInfo.getKey(), eventInfoDur, setEventInfo);
		}
		return doc;
	}
}
