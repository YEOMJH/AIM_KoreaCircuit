package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.extended.object.management.data.MaskMQCPlan;
import kr.co.aim.messolution.extended.object.management.data.MaskMQCPlanDetail;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class CreateOLEDMaskMQCPlan extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String maskLotName = SMessageUtil.getBodyItemValue(doc, "MASKLOTNAME", true);
		String maskSpecName = SMessageUtil.getBodyItemValue(doc, "MASKSPECNAME", true);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String processFlowVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWVERSION", true);
		String returnProcessFlowName = SMessageUtil.getBodyItemValue(doc, "RETURNPROCESSFLOWNAME", true);
		String returnProcessFlowVersion = SMessageUtil.getBodyItemValue(doc, "RETURNPROCESSFLOWVERSION", true);	
		String returnProcessOperationName = SMessageUtil.getBodyItemValue(doc, "RETURNPROCESSOPERATIONNAME", true);	
		String returnProcessOperationVersion = SMessageUtil.getBodyItemValue(doc, "RETURNPROCESSOPERATIONVERSION", true);

		MaskLot maskLotData = null;

		try
		{
			maskLotData = ExtendedObjectProxy.getMaskLotService().selectByKey(false, new Object[] { maskLotName });
		}
		catch (greenFrameDBErrorSignal nfdes)
		{
			throw new CustomException("DURABLE-5050", maskLotName);
		}

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateMaskMQC", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		if (!maskLotData.getMaskLotState().equals(GenericServiceProxy.getConstantMap().Lot_Released))
			throw new CustomException("LOT-0016", maskLotData.getMaskLotName(), maskLotData.getMaskLotState());

		String jobName = "MQC-" + maskLotName + "-" + TimeStampUtil.getCurrentEventTimeKey();

		MaskMQCPlan planData = new MaskMQCPlan(jobName);
		{
			planData.setFactoryName(maskLotData.getFactoryName());
			planData.setMaskLotName(maskLotName);
			planData.setMQCState("Created");
			planData.setMaskSpecName(maskSpecName);
			planData.setMaskProcessFlowName(processFlowName);
			planData.setMaskProcessFlowVersion(processFlowVersion);
			planData.setLastEventComment(eventInfo.getEventComment());
			planData.setLastEventName(eventInfo.getEventName());
			planData.setLastEventTime(eventInfo.getEventTime());
			planData.setLastEventUser(eventInfo.getEventUser());
			planData.setCreateUser(eventInfo.getEventUser());
			planData.setReturnFlowName(returnProcessFlowName);
			planData.setReturnFlowVersion(returnProcessFlowVersion);
			planData.setReturnOperationName(returnProcessOperationName);
			planData.setReturnOperationVersion(returnProcessOperationVersion);
		}

		planData = ExtendedObjectProxy.getMaskMQCPlanService().create(eventInfo, planData);

		// create MaskMQCPlanDetail
		// all components are mandatory
		List<MaskMQCPlanDetail> planDetailList = new ArrayList<MaskMQCPlanDetail>();

		for (Element eleMQCPlanDetail : SMessageUtil.getBodySequenceItemList(doc, "MQCPLANDETAILLIST", true))
		{
			String processOperationName = SMessageUtil.getChildText(eleMQCPlanDetail, "PROCESSOPERATIONNAME", true);
			String processOperationVersion = SMessageUtil.getChildText(eleMQCPlanDetail, "PROCESSOPERATIONVERSION", true);
			String machineName = SMessageUtil.getChildText(eleMQCPlanDetail, "MACHINENAME", true);
			String machineRecipeName = SMessageUtil.getChildText(eleMQCPlanDetail, "MACHINERECIPENAME", true);
			String RMSflag = SMessageUtil.getChildText(eleMQCPlanDetail, "RMSFLAG", true);

			MaskMQCPlanDetail planDetail = new MaskMQCPlanDetail(jobName, processFlowName, processFlowVersion, processOperationName, processOperationVersion);
			planDetail.setMaskLotName(maskLotData.getMaskLotName());
			planDetail.setMachineName(machineName);
			planDetail.setRecipeName(machineRecipeName);
			planDetail.setLastEventComment(eventInfo.getEventComment());
			planDetail.setLastEventName(eventInfo.getEventName());
			planDetail.setLastEventTime(eventInfo.getEventTime());
			planDetail.setLastEventUser(eventInfo.getEventUser());
			planDetail.setRMSFlag(RMSflag);

			planDetailList.add(planDetail);
		}

		if (planDetailList.size() > 0)
		{
			ExtendedObjectProxy.getMaskMQCPlanDetailService().create(eventInfo, planDetailList);
		}

		try
		{
			// Set Event Info
			maskLotData.setLastEventName(eventInfo.getEventName());
			maskLotData.setLastEventTime(eventInfo.getEventTime());
			maskLotData.setLastEventTimeKey(eventInfo.getLastEventTimekey());
			maskLotData.setLastEventUser(eventInfo.getEventUser());
			maskLotData.setLastEventComment(eventInfo.getEventComment());

			ExtendedObjectProxy.getMaskLotService().modify(eventInfo, maskLotData);
		}
		catch (Exception e)
		{
		}
		return doc;

	}
}
