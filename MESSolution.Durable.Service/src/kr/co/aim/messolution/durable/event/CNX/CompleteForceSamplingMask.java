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

public class CompleteForceSamplingMask extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> maskLotList = SMessageUtil.getBodySequenceItemList(doc, "MASKLOTLIST", true);

		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CompleteForceSampleMask", getEventUser(), getEventComment(), null, null);
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
			
			CommonValidation.checkJobDownFlag(maskLotData);//CAIXU 20200403
			if (StringUtil.isEmpty(maskLotData.getCarrierName()))
				throw new CustomException("MASK-0035", maskLotName); 

			String factoryName = maskLotData.getFactoryName();
			String maskSpecName = maskLotData.getMaskSpecName();
			String processFlowName = maskLotData.getMaskProcessFlowName();
			String processFlowVersion = maskLotData.getMaskProcessFlowVersion();
			String processOperationName = maskLotData.getMaskProcessOperationName();
			String processOperationVersion = maskLotData.getMaskProcessOperationVersion();

			SampleMask sampleMask = ExtendedObjectProxy.getSampleMaskService().getSampleMask(true, maskLotName, factoryName, maskSpecName, processFlowName, processFlowVersion, processOperationName,
					processOperationVersion);

			if (sampleMask == null)
				throw new CustomException("MASK-0036", maskLotName, processOperationName);

			ExtendedObjectProxy.getSampleMaskService().remove(eventInfo, sampleMask);

			String returnFlowName = sampleMask.getReturnProcessFlowName();
			String returnFlowVersion = sampleMask.getReturnProcessFlowVersion();
			String returnOperationName = sampleMask.getReturnOperationName();
			String returnOperationVersion = sampleMask.getReturnOperationVersion();

			String newNodeStack = CommonUtil.getNodeStack(maskLotData.getFactoryName(), returnFlowName, returnFlowVersion, returnOperationName, returnOperationVersion);
			maskLotData.setNodeStack(newNodeStack);
			maskLotData.setMaskProcessFlowName(returnFlowName);
			maskLotData.setMaskProcessFlowVersion(returnFlowVersion);
			maskLotData.setMaskProcessOperationName(returnOperationName);
			maskLotData.setMaskProcessOperationVersion(returnOperationVersion);
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
