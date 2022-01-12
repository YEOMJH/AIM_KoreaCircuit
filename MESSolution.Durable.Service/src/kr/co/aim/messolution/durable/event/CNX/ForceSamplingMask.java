package kr.co.aim.messolution.durable.event.CNX;

import java.util.ArrayList;
import java.util.List;

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
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;

import org.jdom.Document;
import org.jdom.Element;

public class ForceSamplingMask extends SyncHandler {
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String sampleFlowName = SMessageUtil.getBodyItemValue(doc, "SAMPLEFLOWNAME", true);
		String sampleFlowVer = SMessageUtil.getBodyItemValue(doc, "SAMPLEFLOWVERSION", true);
		String sampleOperationName = SMessageUtil.getBodyItemValue(doc, "SAMPLEOPERATIONNAME", true);
		String sampleOperationVer = SMessageUtil.getBodyItemValue(doc, "SAMPLEOPERATIONVERSION", false);
		if (StringUtil.isEmpty(sampleOperationVer))
			sampleOperationVer = "00001";
		String returnFlowName = SMessageUtil.getBodyItemValue(doc, "RETURNFLOWNAME", true);
		String returnFlowVer = SMessageUtil.getBodyItemValue(doc, "RETURNFLOWVERSION", true);
		String returnOperationName = SMessageUtil.getBodyItemValue(doc, "RETURNOPERATIONNAME", true);
		String returnOperationVer = SMessageUtil.getBodyItemValue(doc, "RETURNOPERATIONVER", true);
		List<Element> maskLotList = SMessageUtil.getBodySequenceItemList(doc, "MASKLOTLIST", true);

		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ForceSampleMask", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		List<SampleMask> sampleMaskList = new ArrayList<SampleMask>();
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
				throw new CustomException("MASK-0026", maskLotData.getMaskLotName());

			//if (StringUtil.isEmpty(maskLotData.getCarrierName()))
				//throw new CustomException("MASK-0037", maskLotName);

			CommonValidation.checkJobDownFlag(maskLotData);

			ProcessFlow processFlowData = ExtendedObjectProxy.getMaskLotService().getProcessFlowData(maskLotData);
			if (StringUtil.equalsIgnoreCase(processFlowData.getProcessFlowType(), "Sample"))
				throw new CustomException("MASK-0038", maskLotName, processFlowData.getKey().getProcessFlowName());

			String factoryName = maskLotData.getFactoryName();
			String maskSpecName = maskLotData.getMaskSpecName();

			SampleMask sampleMask = ExtendedObjectProxy.getSampleMaskService().getSampleMask(true, maskLotName, factoryName, maskSpecName, sampleFlowName, sampleFlowVer, sampleOperationName,
					sampleOperationVer);

			if (sampleMask != null)
				throw new CustomException("MASK-0039", maskLotName, sampleOperationName);

			sampleMask = new SampleMask(maskLotName, factoryName, maskSpecName, sampleFlowName, sampleFlowVer, sampleOperationName, sampleOperationVer, returnFlowName, returnFlowVer,
					returnOperationName, returnOperationVer, eventInfo.getEventName(), eventInfo.getLastEventTimekey(), eventInfo.getEventTime(), eventInfo.getEventUser(), eventInfo.getEventComment());

			sampleMaskList.add(sampleMask);

			String newNodeStack = CommonUtil.getNodeStack(maskLotData.getFactoryName(), sampleFlowName, sampleFlowVer, sampleOperationName, sampleOperationVer);
			maskLotData.setNodeStack(newNodeStack);
			maskLotData.setMaskProcessFlowName(sampleFlowName);
			maskLotData.setMaskProcessFlowVersion(sampleFlowVer);
			maskLotData.setMaskProcessOperationName(sampleOperationName);
			maskLotData.setMaskProcessOperationVersion(sampleOperationVer);
			maskLotListToBeModified.add(maskLotData);
		}

		ExtendedObjectProxy.getSampleMaskService().create(eventInfo, sampleMaskList);
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