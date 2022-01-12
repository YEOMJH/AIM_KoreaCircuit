package kr.co.aim.messolution.durable.event.CNX;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.extended.object.management.data.SampleMask;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;

public class CompleteReworkMask extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> maskLotList = SMessageUtil.getBodySequenceItemList(doc, "MASKLOTLIST", true);
		String returnProcessFlowName = SMessageUtil.getBodyItemValue(doc, "RETURNPROCESFLOWNAME", true);
		String returnOperationName = SMessageUtil.getBodyItemValue(doc, "RETURNPROCESSOPERATIONNAME", true);
		
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CompleteReworkMask", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		List<MaskLot> maskLotListToBeModified = new ArrayList<MaskLot>();
		for (Element maskLotE : maskLotList)
		{
			String maskLotName = maskLotE.getChildText("MASKLOTNAME");
			MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().selectByKey(true, new Object[] { maskLotName });

			// MaskLotProcessState Check
			if (maskLotData.getMaskLotProcessState().equalsIgnoreCase(constantMap.MaskLotProcessState_Run))
				throw new CustomException("OLEDMASK-0002", new Object[] { constantMap.MaskLotProcessState_Run + "(" + maskLotName + ")" });

			// MaskLotHoldState Check
			if (StringUtil.equals(maskLotData.getMaskLotHoldState(), constantMap.MaskLotHoldState_OnHold))
				throw new CustomException("MASK-0013", maskLotData.getMaskLotName());

			// MaskLotState Check
			if (!StringUtil.equals(maskLotData.getMaskLotState(), constantMap.MaskLotState_Released))
				throw new CustomException("MASK-0026", maskLotData.getMaskLotName(), maskLotData.getMaskLotState());
			
			ProcessFlowKey processFlowKey = new ProcessFlowKey();
			processFlowKey.setFactoryName(maskLotData.getFactoryName());
			processFlowKey.setProcessFlowName(maskLotData.getMaskProcessFlowName());
			processFlowKey.setProcessFlowVersion("00001");
			ProcessFlow processFlow = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);
			
			if(!StringUtil.equals(processFlow.getProcessFlowType(), "Rework"))
				throw new CustomException("LOT-0101", maskLotData.getMaskLotName());
			
			if(!StringUtil.equals(processFlow.getUdfs().get("PROCESSFLOWUSEDTYPE"), "Mask"))
				throw new CustomException("LOT-0101", maskLotData.getMaskLotName());
			
			CommonValidation.checkJobDownFlag(maskLotData);//CAIXU 20200403

			String newNodeStack = maskLotData.getReturnSequenceId();
			
			maskLotData.setNodeStack(newNodeStack);
			maskLotData.setMaskProcessFlowName(returnProcessFlowName);
			maskLotData.setMaskProcessFlowVersion("00001");
			maskLotData.setMaskProcessOperationName(returnOperationName);
			maskLotData.setMaskProcessOperationVersion("00001");
			maskLotData.setReworkState("NotInRework");
			maskLotData.setReturnSequenceId("");
			maskLotListToBeModified.add(maskLotData);
		}
		ExtendedObjectProxy.getMaskLotService().modify(eventInfo, maskLotListToBeModified);

		for (Element maskLotE : maskLotList)
		{
			String maskLotName = maskLotE.getChildText("MASKLOTNAME");
			MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().selectByKey(true, new Object[] { maskLotName });
			ExtendedObjectProxy.getMaskLotService().executePostAction(eventInfo, maskLotData);
		}

		return doc;
	}

}
