package kr.co.aim.messolution.durable.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.CheckOffset;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.extended.object.management.data.MaskMultiHold;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;

public class CancelTrackInMaskLot extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		List<Element> maskLotList = SMessageUtil.getBodySequenceItemList(doc, "MASKLOTLIST", false);
		String maskSpecName = SMessageUtil.getBodyItemValue(doc, "MASKSPECNAME", true);
		String maskProcessFlowName = SMessageUtil.getBodyItemValue(doc, "MASKPROCESSFLOWNAME", true);
		String maskProcessOperationName = SMessageUtil.getBodyItemValue(doc, "MASKPROCESSOPERATIONNAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);
		String carrierState = SMessageUtil.getBodyItemValue(doc, "CARRIERSTATE", false);
		String position = SMessageUtil.getBodyItemValue(doc, "POSITION", false);
		String eventName = SMessageUtil.getBodyItemValue(doc, "EVENTNAME", false);

		if(StringUtil.isNotEmpty(carrierName))
		{
			EventInfo eventInfoAssignCarrierMask = EventInfoUtil.makeEventInfo("AssignCarrierMask", this.getEventUser(), this.getEventComment(), "", "", "Y");
			eventInfoAssignCarrierMask.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			MESDurableServiceProxy.getDurableServiceImpl().durableStateChangeAfterOLEDMaskLotProcess(carrierName, carrierState, String.valueOf(maskLotList.size()), eventInfoAssignCarrierMask);
		}

		for (int i = 0; i < maskLotList.size(); i++)
		{
			String maskLotName = maskLotList.get(i).getChildText("MASKLOTNAME");
			MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().selectByKey(true, new Object[] { maskLotName });
			
			CommonValidation.checkMaskLotHoldState(maskLotData);
			CommonValidation.checkMaskLotState(maskLotData);
			CommonValidation.checkMaskLotProcessStateNotRun(maskLotData);
			if((!StringUtil.equals(carrierState, "ASSIGN")) && (StringUtil.isNotEmpty(maskLotData.getCarrierName())))
				throw new CustomException("MASK-1000");
			
			ProcessOperationSpec processOperationData = CommonUtil.getProcessOperationSpec(maskLotData.getFactoryName(), maskLotData.getMaskProcessOperationName(),
					maskLotData.getMaskProcessOperationVersion());

			if (processOperationData.getProcessOperationType().equals(constantMap.Mac_InspectUnit))
			{
				CheckOffset offsetInfo = new CheckOffset();

				try
				{
					EventInfo offsetEventInfo = EventInfoUtil.makeEventInfo("RemoveOffset", this.getEventUser(), this.getEventComment(), "", "", "Y");
					offsetEventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
					
					offsetInfo = ExtendedObjectProxy.getCheckOffsetService().selectByKey(false, new Object[] { maskLotName, maskSpecName, maskProcessFlowName, maskProcessOperationName });
					offsetInfo.setLastEventUser(offsetEventInfo.getEventUser());
					ExtendedObjectProxy.getCheckOffsetService().remove(offsetEventInfo, offsetInfo);
				}
				catch (Exception e)
				{
					eventLog.info("Not found OffSet data");
				}
			}
			this.checkMaskMultiHold(maskLotName, "CancelTrackInMaskLot");
			// Cancel Track Mask Lot
			// CT_MASKLOT / CT_MASKLOTHISTORY Insert
			// -> CARRIERNAME = Key-in, MASKLOTPROCESSSTATE = WAIT
			EventInfo eventInfoCancelTrackInMask = EventInfoUtil.makeEventInfo(eventName, this.getEventUser(), this.getEventComment(), "", "", "Y");
			eventInfoCancelTrackInMask.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			cancelTrackInMaskLot(eventInfoCancelTrackInMask, maskLotName, carrierName, position);

			EventInfo eventInfoByInfo = EventInfoUtil.makeEventInfo("Hold", getEventUser(), getEventComment(), "", "");
			MaskLot holdMaskLot = ExtendedObjectProxy.getMaskLotService().selectByKey(true, new Object[] { maskLotName });
			holdMaskLot.setLastEventUser(eventInfoByInfo.getEventUser());
			holdMaskLot.setLastEventName(eventInfoByInfo.getEventName());
			holdMaskLot.setLastEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
			holdMaskLot.setLastEventComment(eventInfoByInfo.getEventComment());
			holdMaskLot.setLastEventTime(TimeStampUtil.getCurrentTimestamp());
			holdMaskLot.setMaskLotHoldState("Y");
			holdMaskLot.setReasonCodeType("System");
			holdMaskLot.setReasonCode("Hold");

			MaskLot holdMaskInfo = ExtendedObjectProxy.getMaskLotService().modify(eventInfoByInfo, holdMaskLot);
			
			this.MultiHoldMaskLot(maskLotName, maskLotData, eventInfoByInfo);

		}

		return doc;
	}

	public void cancelTrackInMaskLot(EventInfo eventInfo, String maskLotName, String carrierName, String position) throws CustomException
	{
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();

		// Get Mask Lot Data.
		MaskLot inputMaskLot = ExtendedObjectProxy.getMaskLotService().selectByKey(true, new Object[] { maskLotName });
		MaskLot outputMaskLot = ExtendedObjectProxy.getMaskLotService().selectByKey(true, new Object[] { maskLotName });

		outputMaskLot.setMaskLotProcessState(constantMap.MaskLotProcessState_Wait);
		outputMaskLot.setMaskLotState(constantMap.Lot_Released);
		//outputMaskLot.setMachineName("");
		//outputMaskLot.setPortName("");
		//outputMaskLot.setPortType("");
		//outputMaskLot.setAreaName("");

		if (!inputMaskLot.getPortType().equals("PB"))
		{
			// Assign CST
			outputMaskLot.setCarrierName(carrierName);
			outputMaskLot.setPosition(position);
		}

		// Set EventInfo
		outputMaskLot.setLastEventUser(eventInfo.getEventUser());
		outputMaskLot.setLastEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
		outputMaskLot.setLastEventName(eventInfo.getEventName()); // TODO: Check it is necessary to move to ConstantMap.
		outputMaskLot.setLastEventComment(eventInfo.getEventComment()); // TODO: Check it is necessary to move to ConstantMap.
		outputMaskLot.setLastEventTime(TimeStampUtil.getCurrentTimestamp());

		MaskLot result = ExtendedObjectProxy.getMaskLotService().modify(eventInfo, outputMaskLot);

	}
	
	private void checkMaskMultiHold(String masklotname,String reasonCode) throws CustomException
	{
		StringBuffer inquirysql = new StringBuffer();
		inquirysql.append(" SELECT MASKLOTNAME ");
		inquirysql.append(" FROM CT_MASKMULTIHOLD ");
		inquirysql.append(" WHERE MASKLOTNAME=:MASKLOTNAME ");
		inquirysql.append(" AND REASONCODE=:REASONCODE ");
		
		Map<String, String> inquirybindMap = new HashMap<String, String>();
		inquirybindMap.put("MASKLOTNAME", masklotname);
		inquirybindMap.put("REASONCODE", reasonCode);
		
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(inquirysql.toString(), inquirybindMap);
		if(sqlResult!=null&& sqlResult.size()>0)
		{
			throw new CustomException("MASK-0082",masklotname,reasonCode);
		}
	}
	
	private void MultiHoldMaskLot(String maskLotName,MaskLot maskLotData,EventInfo eventInfo)throws CustomException
	{
		MaskMultiHold maskMultiHold =new MaskMultiHold();
		maskMultiHold.setMaskLotName(maskLotName);
		maskMultiHold.setFactoryName(maskLotData.getFactoryName());
		maskMultiHold.setMaskProcessOperationName(maskLotData.getMaskProcessOperationName());
		maskMultiHold.setMaskProcessOperationVersion(maskLotData.getMaskProcessOperationVersion());
		maskMultiHold.setReasonCode("CancelTrackInMaskLot");
		maskMultiHold.setReasonCodeType("");
		maskMultiHold.setLastEventComment(eventInfo.getEventComment());
		maskMultiHold.setLastEventName(eventInfo.getEventName());
		maskMultiHold.setLastEventTime(eventInfo.getEventTime());
		maskMultiHold.setLastEventUser(eventInfo.getEventUser());
		
		ExtendedObjectProxy.getMaskMultiHoldService().create(eventInfo, maskMultiHold);
	}
}
