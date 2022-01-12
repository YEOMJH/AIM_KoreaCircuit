package kr.co.aim.messolution.comsumable.shield;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ShieldLot;
import kr.co.aim.messolution.extended.object.management.data.ShieldSpec;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.NodeStack;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableHistory;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpecKey;

import org.jdom.Document;
import org.jdom.Element;

public class CreateShield extends SyncHandler {

	public Object doWorks(Document doc) throws CustomException
	{
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		List<Element> shieldList = SMessageUtil.getBodySequenceItemList(doc, "SHIELDLIST", true);
		String durableName = SMessageUtil.getBodyItemValue(doc, "DURABLENAME", false);
		//boolean carMix = Boolean.parseBoolean(SMessageUtil.getBodyItemValue(doc, "CARMIX", true));
		
		boolean durFlag = false;
		boolean inUseFlag = false;
		String durOperation = "";
		String durOperationVersion = "";
		Durable durableInfo = new Durable();

		List<ShieldLot> shieldLotList = new ArrayList<ShieldLot>();
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateShield", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		if(!StringUtil.isEmpty(durableName))
		{
			durFlag = true;
			durableInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableName);
			
			CommonValidation.CheckDurableHoldState(durableInfo);
			CommonValidation.CheckDurableState(durableInfo);//caixu
			ShieldLot durShield = ExtendedObjectProxy.getShieldLotService().checkShieldSpec(durableName, shieldList, durableInfo);
			
			if(durableInfo.getDurableState().equals(constantMap.Dur_InUse))
			{
				inUseFlag = true;				
				//ShieldLot durShield = ExtendedObjectProxy.getShieldLotService().checkShieldSpec(durableName, shieldList, durableInfo);
				durOperation = durShield.getProcessOperationName();
				durOperationVersion = durShield.getProcessOperationVersion();
			}
		}
		
		for (Element eledur : shieldList)
		{
			String sfactoryName = SMessageUtil.getChildText(eledur, "FACTORYNAME", true);
			String sShieldID = SMessageUtil.getChildText(eledur, "SHIELDID", true);
			String sShieldSpec = SMessageUtil.getChildText(eledur, "SHIELDSPEC", true);
			String sShieldType = SMessageUtil.getChildText(eledur, "SHIELDTYPE", true);
			String sSet = SMessageUtil.getChildText(eledur, "SET", true);
			String sLine = SMessageUtil.getChildText(eledur, "LINE", true);
			String sChamberNo = SMessageUtil.getChildText(eledur, "CHAMBERNO", true);
			String carGroupName = SMessageUtil.getChildText(eledur, "CARGROUPNAME", true);
			String basketGroupName = SMessageUtil.getChildText(eledur, "BASKETGROUPNAME", true);
			//String sChamberType = SMessageUtil.getChildText(eledur, "CAMBERTYPE", true);
			
			ShieldLot shieldLot = null;
			
			//Make ShieldID
			//sShieldID = sLine + sChamberType + sChamberNo + sShieldID + sSet;
			
			try
			{
				shieldLot = ExtendedObjectProxy.getShieldLotService().selectByKey(false, new Object[] { sShieldID });
			}
			catch (greenFrameDBErrorSignal nfdes)
			{
				// Not found same shield
			}

			if (shieldLot != null)
				throw new CustomException("SHIELD-0001", sShieldID);
			
			shieldLot = new ShieldLot();

			ShieldSpec shieldSpecInfo = ExtendedObjectProxy.getShieldSpecService().selectByKey(false, new Object[] { sfactoryName, sShieldSpec });
			String shieldProcessFlowName = shieldSpecInfo.getProcessFlowName();
			String shieldProcessFlowVersion = shieldSpecInfo.getProcessFlowVersion();
			String targetOperationName = "";
			String targerOperationVer = "";
			
			// Set Operation
			ProcessOperationSpec targetOperationData = CommonUtil.getFirstOperation(sfactoryName, shieldProcessFlowName);
			
			if(inUseFlag)
			{
				targetOperationName = durOperation;
				targerOperationVer = durOperationVersion;
			}
			else
			{
				targetOperationName = targetOperationData.getKey().getProcessOperationName();
				targerOperationVer = targetOperationData.getKey().getProcessOperationVersion();
			}	
			String targetNodeId = NodeStack.getNodeID(sfactoryName, shieldProcessFlowName, targetOperationName, targerOperationVer);
			
			shieldLot.setShieldLotName(sShieldID);
			shieldLot.setLine(Integer.parseInt(sLine));
			shieldLot.setChamberType(shieldSpecInfo.getChamberType());
			shieldLot.setChamberNo(sChamberNo);
			shieldLot.setSetValue(sSet);
			shieldLot.setJudge("OK");
			shieldLot.setFactoryName(sfactoryName);
			shieldLot.setLotState(constantMap.Lot_Released);
			shieldLot.setLotProcessState(constantMap.Lot_Wait);
			shieldLot.setLotHoldState("N");
			shieldLot.setNodeStack(targetNodeId);
			shieldLot.setCleanState(constantMap.Dur_Clean);
			if(durFlag)
			{
				shieldLot.setCarrierName(durableName);
			}
			shieldLot.setShieldSpecName(sShieldSpec);
			shieldLot.setProcessFlowName(shieldProcessFlowName);
			shieldLot.setProcessFlowVersion(shieldProcessFlowVersion);
			shieldLot.setProcessOperationName(targetOperationName);
			shieldLot.setProcessOperationVersion(targerOperationVer);
			shieldLot.setReworkCount(0);
			shieldLot.setSampleFlag("N");
			shieldLot.setCreateTime(eventInfo.getEventTime());
			shieldLot.setLastEventComment(eventInfo.getEventComment());
			shieldLot.setLastEventName(eventInfo.getEventName());
			shieldLot.setLastEventUser(eventInfo.getEventUser());
			shieldLot.setLastEventTime(eventInfo.getEventTime());
			shieldLot.setLastEventTimekey(eventInfo.getEventTimeKey());
			shieldLot.setCarGroupName(carGroupName);
			shieldLot.setBasketGroupName(basketGroupName);
			
			shieldLotList.add(shieldLot);
		}
		
		ExtendedObjectProxy.getShieldLotService().create(eventInfo, shieldLotList);
		
		if(durFlag)
		{
			durableInfo.setLotQuantity(durableInfo.getLotQuantity() + shieldList.size());
			durableInfo.setDurableState(constantMap.Dur_InUse);

			DurableServiceProxy.getDurableService().update(durableInfo);
			SetEventInfo setEventInfo = new SetEventInfo();
			DurableServiceProxy.getDurableService().setEvent(durableInfo.getKey(), eventInfo, setEventInfo);
		}

		return doc;
	}

}
